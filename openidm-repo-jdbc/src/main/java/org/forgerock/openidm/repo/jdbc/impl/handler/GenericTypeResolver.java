/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2024 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.handler;

import java.sql.Connection;
import java.sql.SQLException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic object type resolver that maps type names to numeric identifiers stored in <code>objecttypes</code> table.
 *
 * <p>
 * This class simply serves as a place to offload type handling logic from {@link GenericTableHandler} to keep it
 * more simple.
 */
public class GenericTypeResolver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String readSql;

    private final String createSql;

    /**
     * Create new type resolver using the given CREATE and READ SQL strings.
     * @param readSql read SQL string for type ID with one type name parameter
     * @param createSql create SQL string with one type name parameter
     */
    public GenericTypeResolver(String readSql, String createSql) {
        this.readSql = readSql;
        this.createSql = createSql;
    }

    /**
     * Resolve type name to the stored type identifier.
     *
     * <p>
     * Method will automatically register missing types when requested. Callers should note that this
     * may commit a transaction and start a new one if a new type gets added.
     *
     * @param type object type name
     * @param connection current database connection
     * @return resolved type identifier or {@code -1} if unable to resolve
     * @throws InternalServerErrorException in case the automatic registration fails
     * @throws SQLException in case of SQL error
     */
    public long resolveTypeId(String type, Connection connection)
            throws InternalServerErrorException, SQLException {
        long typeId = readTypeId(type, connection);
        if (typeId >= 0) {
            return typeId;
        }

        Exception detectedEx = null;
        try {
            createTypeId(type, connection);
        } catch (SQLException e) {
            // ignore exception as it may have been caused by duplicate key violation
            detectedEx = e;
        }

        typeId = readTypeId(type, connection);
        if (typeId < 0) {
            throw new InternalServerErrorException(
                    "Failed to populate and look up objecttypes table, no id could be retrieved for " + type,
                    detectedEx);
        }
        return typeId;
    }

    /**
     * Resolve type name to the stored type identifier.
     *
     * @param type object type name
     * @param connection current database connection
     * @return resolved type identifier or {@code -1} if unable to resolve
     * @throws SQLException in case of SQL error
     */
    public long readTypeId(String type, Connection connection) throws SQLException {
        try (var readStatement = connection.prepareStatement(readSql)) {
            logger.trace("Populating prepared statement {} for {}", readSql, type);
            readStatement.setString(1,  type);

            logger.debug("Executing: {}", readStatement);
            try (var resultSet = readStatement.executeQuery()) {
                if (resultSet.next()) {
                    var typeId = resultSet.getLong(Constants.RAW_ID);
                    logger.debug("Type: {}, id: {}", type, typeId);
                    return typeId;
                }
            }
        }
        return -1;
    }

    private void createTypeId(String type, Connection connection) throws SQLException {
        // commit the new type right away, and have no transaction isolation for read
        connection.setAutoCommit(true);
        try (var createStatement = connection.prepareStatement(createSql)) {
            logger.debug("Create objecttype {}", type);
            createStatement.setString(1, type);
            logger.debug("Executing: {}", createStatement);
            createStatement.executeUpdate();
        } finally {
            connection.setAutoCommit(false);
        }
    }

}
