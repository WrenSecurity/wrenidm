/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2016 ForgeRock AS.
 * Portions Copyright 2018-2024 Wren Security.
 */
package org.forgerock.openidm.repo.jdbc.impl.handler;

import static java.util.function.Function.identity;
import static org.forgerock.json.resource.Responses.newResourceResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.forgerock.audit.util.JsonValueUtils;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.PreconditionFailedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.openidm.repo.jdbc.impl.mapper.MappedResultMapper;
import org.forgerock.openidm.repo.jdbc.impl.mapper.ResultMapper;
import org.forgerock.openidm.repo.jdbc.impl.mapper.ResultMappers;
import org.forgerock.openidm.repo.jdbc.impl.query.MappedSQLQueryFilterVisitor;
import org.forgerock.openidm.repo.jdbc.impl.query.TableQueryHandler;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterSql;
import org.forgerock.openidm.repo.util.Clauses;
import org.forgerock.openidm.repo.util.TokenHandler;
import org.forgerock.util.query.QueryFilter;

/**
 * Mapped table handler that supports objects stored in a dedicated table that maps object properties
 * to column values.
 */
public class MappedTableHandler extends AbstractTableHandler {

    /**
     * Well-known implicit SQL statement types.
     */
    protected enum ImplicitSqlType {
        READ,
        READFORUPDATE,
        CREATE,
        UPDATE,
        DELETE
    }

    protected final String schemaName;

    protected final String tableName;

    protected final Map<String, MappedColumnConfig> columnMapping;

    private final Map<ImplicitSqlType, String> implicitSql;

    private final TableQueryHandler<Map<String, Object>> queryHandler;

    private final ResultMappers resultMappers;

    public MappedTableHandler(
            String schemaName,
            String tableName,
            JsonValue columnMapping,
            Map<String, String> queryConfig,
            Map<String, String> commandConfig,
            SQLExceptionHandler exceptionHandler) {
        super(exceptionHandler);

        this.schemaName = schemaName;
        this.tableName = tableName;

        this.columnMapping = columnMapping.keys().stream()
                .collect(Collectors.toMap(identity(), name -> {
                    return MappedColumnConfig.parse(name, columnMapping.get(name));
                }));

        var replacementTokens = resolveReplacementTokens();

        this.implicitSql = initializeImplicitSql().entrySet().stream()
                .map(entry -> {
                    String resolved = new TokenHandler().replaceSomeTokens(entry.getValue(), replacementTokens);
                    return Map.entry(entry.getKey(), resolved);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        logger.debug("Prepared implicit SQL strings {}", implicitSql);

        this.queryHandler = new TableQueryHandler<>(
                replacementTokens,
                queryConfig.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            return NamedParameterSql.parse(entry.getValue(), replacementTokens);
                        })),
                commandConfig.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            return NamedParameterSql.parse(entry.getValue(), replacementTokens);
                        })),
                this::resolveQueryFilter,
                this::createResultMapper);

        this.resultMappers = new ResultMappers(objectMapper);
    }

    @Override
    protected Map<String, String> resolveReplacementTokens() {
        return Map.ofEntries(
            Map.entry("_dbSchema", schemaName),
            Map.entry("_table", tableName)
        );
    }

    /**
     * Initialize implicit SQL statements for the table handler.
     *
     * <p>
     * This method serves as extension point for vendor specific subclasses.
     *
     * @return mutable map with implicit SQL statements
     * @see ImplicitSqlType
     */
    protected Map<ImplicitSqlType, String> initializeImplicitSql() {
        Map<ImplicitSqlType, String> result = new EnumMap<>(ImplicitSqlType.class);
        result.put(ImplicitSqlType.READ,
                "SELECT * FROM ${_dbSchema}.${_table} WHERE objectid = ?");
        result.put(ImplicitSqlType.READFORUPDATE,
                "SELECT * FROM ${_dbSchema}.${_table} WHERE objectid = ? FOR UPDATE");
        result.put(ImplicitSqlType.CREATE,
                "INSERT INTO ${_dbSchema}.${_table} ("
                    + columnMapping.values().stream().map(config -> config.columnName)
                            .collect(Collectors.joining(", "))
                + ") VALUES ("
                    + columnMapping.values().stream().map(config -> "?")
                            .collect(Collectors.joining(", "))
                +")");
        result.put(ImplicitSqlType.UPDATE,
                "UPDATE ${_dbSchema}.${_table} "
                + "SET "
                    + columnMapping.values().stream().map(config -> config.columnName + " = ?")
                            .collect(Collectors.joining(", "))
                + " WHERE objectid = ?");
        result.put(ImplicitSqlType.DELETE,
                "DELETE FROM ${_dbSchema}.${_table} WHERE objectid = ? AND rev = ?");
        return result;
    }

    @Override
    protected ResultMapper<Map<String, Object>> createResultMapper(ResultSetMetaData metaData) throws SQLException {
        Collection<String> columnNames = extractColumnNames(metaData);
        if (columnNames.contains(ResultMappers.TOTAL_COLUMN)) {
            return resultMappers.forTotalCount();
        }
        var columnConfigs = this.columnMapping.values().stream()
            .filter(mapping -> columnNames.contains(mapping.columnName.toLowerCase()))
            .collect(Collectors.toList());
        return new MappedResultMapper(objectMapper, columnConfigs);
    }

    @Override
    public ResourceResponse read(String fullId, String type, String localId, Connection connection)
            throws NotFoundException, IOException, SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        var readSql = implicitSql.get(ImplicitSqlType.READ);
        try (var readStatement = connection.prepareStatement(readSql)) {
            logger.debug("Populating prepared statement {} for {}", readStatement, fullId);
            readStatement.setString(1, localId);

            logger.debug("Executing: {}", readStatement);
            try (var resultSet = readStatement.executeQuery()) {
                var resultMapper = createResultMapper(resultSet.getMetaData());
                while (resultSet.next()) {
                    results.add(resultMapper.map(resultSet));
                }
            }
        }

        if (results.isEmpty()) {
            throw new NotFoundException("Object " + fullId + " not found in " + type);
        }
        var result = results.get(0);
        String revision = (String) result.get(Constants.OBJECT_REV);
        logger.debug(" full id: {}, rev: {}, obj {}", fullId, revision, result);

        return newResourceResponse(localId, revision, new JsonValue(result));
    }

    @Override
    public void create(String fullId, String type, String localId, Map<String, Object> obj, Connection connection)
            throws PreconditionFailedException, InternalServerErrorException, IOException, SQLException {
        logger.debug("Create with fullid {}", fullId);

        String revision = "0";

        // update object properties
        obj.put(Constants.OBJECT_ID, localId);
        obj.put(Constants.OBJECT_REV, revision);

        var createSql = implicitSql.get(ImplicitSqlType.CREATE);
        try (var createStatement = connection.prepareStatement(createSql)) {
            logger.trace("Populating statement {} with params {}, {}, {}", createStatement, type, localId, revision);
            populatePreparedStatement(createStatement, new JsonValue(obj));

            logger.debug("Executing: {}", createStatement);
            createStatement.executeUpdate();
            logger.debug("Created object for id {} with rev {}", fullId, revision);
        }
    }

    /**
     * Populate prepared statement for positional parameters in the same order as {@link #columnMapping}.
     *
     * @param statement the statement to populate
     * @param jsonObject the object to extract values from
     * @throws BadRequestException when there are unmapped properties
     * @throws InternalServerErrorException in case an illegal state is encountered
     * @throws JsonProcessingException in case of property mapping error
     * @throws SQLException in case of DB failure
     */
    private void populatePreparedStatement(PreparedStatement statement, JsonValue jsonObject)
            throws BadRequestException, InternalServerErrorException, JsonProcessingException, SQLException {
        var checkObject = jsonObject.copy();

        int index = 0;
        for (MappedColumnConfig config : columnMapping.values()) {
            checkObject.remove(config.propertyName);
            applyStatementParameter(statement, ++index, config, jsonObject.get(config.propertyName));
        }

        // some tables don't map _id and _rev (e.g., audit)
        checkObject.remove(Constants.OBJECT_ID);
        checkObject.remove(Constants.OBJECT_REV);

        var unmappedFields = JsonValueUtils.flatten(checkObject);
        if (!unmappedFields.isEmpty()) {
            throw new BadRequestException("Unmapped fields " + unmappedFields + " for table "
                    + schemaName + "." + tableName);
        }
    }

    /**
     * Set prepared statement parameter according to the provided column configuration.
     *
     * @param statement the statement for which the parameter should be set
     * @param index the parameter index
     * @param config the column configuration
     * @param value the parameter value to set
     * @throws InternalServerErrorException in case an illegal state is encountered
     * @throws JsonProcessingException in case of property mapping error
     * @throws SQLException in case of DB failure
     */
    protected void applyStatementParameter(PreparedStatement statement, int index, MappedColumnConfig config,
            JsonValue value) throws InternalServerErrorException, JsonProcessingException, SQLException {
        Object rawValue = value != null ? value.getObject() : null;
        switch (config.valueType) {
            case STRING:
                if (rawValue != null && !(rawValue instanceof String)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Value for {} is getting stringified from type {} to store in a STRING "
                                + "column as value: {}", config.propertyName, rawValue.getClass(), rawValue);
                    }
                    rawValue = objectMapper.writeValueAsString(rawValue);
                }
                statement.setString(index, (String) rawValue);
                break;
            case NUMBER:
                if (rawValue instanceof Integer) {
                    statement.setInt(index, (Integer) rawValue);
                } else if (rawValue instanceof Long) {
                    statement.setLong(index, (Long) rawValue);
                } else if (rawValue instanceof Float) {
                    statement.setFloat(index, (Float) rawValue);
                } else if (rawValue instanceof Double) {
                    statement.setDouble(index, (Double) rawValue);
                } else if (rawValue == null) {
                    statement.setNull(index, Types.INTEGER);
                } else {
                    throw new InternalServerErrorException("Invalid value type " + rawValue.getClass());
                }
                break;
            case BOOLEAN:
                if (rawValue instanceof Boolean) {
                    statement.setObject(index, ((Boolean) rawValue).booleanValue() ? 1 : 0, Types.BIT);
                } else if (rawValue == null) {
                    statement.setNull(index, Types.BIT);
                } else {
                    throw new InternalServerErrorException("Invalid value type " + rawValue.getClass());
                }
                break;
            case JSON_LIST:
                statement.setString(index, value != null
                        ? objectMapper.writeValueAsString(value.asList()) : null);
                break;
            case JSON_MAP:
                statement.setString(index, value != null
                        ? objectMapper.writeValueAsString(value.asMap()) : null);
                break;
            default:
                throw new InternalServerErrorException("Unsupported DB column type " + config.valueType);
        }
    }

    /**
     * Read an object with <i>FOR UPDATE</i> lock applied.
     *
     * @param fullId qualified id of component type and id
     * @param type the qualifier of the object to retrieve
     * @param localId the identifier without the qualifier of the object to retrieve
     * @param connection database connection to use
     * @return the row as a map of column name/value pairs for the requested object
     * @throws NotFoundException if the requested object was not found in the DB
     * @throws java.sql.SQLException for general DB issues
     */
    protected Map<String, Object> readForUpdate(String fullId, String type, String localId, Connection connection)
            throws NotFoundException, SQLException {
        var readSql = implicitSql.get(ImplicitSqlType.READFORUPDATE);
        try (var readStatement = connection.prepareStatement(readSql)) {
            logger.trace("Populating prepared statement {} for {}", readStatement, fullId);
            readStatement.setString(1, localId);

            logger.debug("Executing: {}", readStatement);
            try (var resultSet = readStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NotFoundException("Object " + fullId + " not found in " + type);
                }
                try {
                    return resultMappers.forObjectRef(true).map(resultSet);
                } catch (IOException e) {
                    throw new IllegalStateException("Unexpected error", e);
                }
            }
        }
    }

    @Override
    public void update(String fullId, String type, String localId, String rev, Map<String, Object> obj,
            Connection connection) throws NotFoundException, PreconditionFailedException, BadRequestException,
            InternalServerErrorException, IOException, SQLException {
        logger.debug("Update with fullid {}", fullId);

        // read existing object state
        JsonValue existingObj = new JsonValue(readForUpdate(fullId, type, localId, connection));
        String existingRev = existingObj.get(Constants.OBJECT_REV).asString();
        logger.debug("Update existing object {} rev: {}", fullId, existingRev);

        // perform optimistic version locking
        if (!existingRev.equals(rev)) {
            throw new PreconditionFailedException("Update rejected as current Object revision " + existingRev
                    + " is different than expected by caller (" + rev + "), the object has changed since retrieval.");
        }

        // support changing object identifier
        String updatedId = (String) obj.get(Constants.OBJECT_ID);
        if (updatedId != null && !updatedId.equals(localId)) {
            logger.debug("Object identifier is changing from " + localId + " to " + updatedId);
        } else {
            updatedId = localId; // if it hasn't changed, use the existing ID
            obj.put(Constants.OBJECT_ID, updatedId); // ensure the ID is saved in the object
        }

        // handle revision update
        String updatedRev = Integer.toString(Integer.parseInt(rev) + 1);
        obj.put(Constants.OBJECT_REV, updatedRev);

        var updateSql = implicitSql.get(ImplicitSqlType.UPDATE);
        try (var updateStatement = connection.prepareStatement(updateSql)) {
            logger.trace("Populating prepared statement {} with {} {} {}", updateStatement, fullId, updatedId,
                    updatedRev);
            populatePreparedStatement(updateStatement, new JsonValue(obj));
            updateStatement.setString(columnMapping.size() + 1, localId);
            logger.debug("Update statement: {}", updateStatement);

            int updateCount = updateStatement.executeUpdate();
            logger.trace("Updated rows: {} for {}", updateCount, fullId);
            if (updateCount != 1) {
                throw new InternalServerErrorException("Update execution did not result in updating 1 "
                        + "row as expected. Updated rows: " + updateCount);
            }
        }
    }

    @Override
    public void delete(String fullId, String type, String localId, String rev, Connection connection)
            throws SQLException, ResourceException {
        logger.debug("Delete with fullid {}", fullId);

        // read existing object state
        JsonValue existingObj = new JsonValue(readForUpdate(fullId, type, localId, connection));
        String existingRev = existingObj.get(Constants.OBJECT_REV).asString();

        // perform optimistic version locking
        if (!"*".equals(rev) && !existingRev.equals(rev)) {
            throw new PreconditionFailedException("Delete rejected as current Object revision " + existingRev
                    + " is different than the expected by caller " + rev + ", the object has changed since retrieval.");
        }

        var deleteSql = implicitSql.get(ImplicitSqlType.DELETE);
        try (var deleteStatement = connection.prepareStatement(deleteSql)) {
            logger.trace("Populating prepared statement {} for {} {} {} {}", deleteStatement, fullId, type, localId, rev);
            deleteStatement.setString(1, localId);
            deleteStatement.setString(2, rev);
            logger.debug("Delete statement: {}", deleteStatement);

            int deletedRows = deleteStatement.executeUpdate();
            logger.trace("Deleted {} rows for id : {} {}", deletedRows, localId);
            if (deletedRows < 1) {
                throw new InternalServerErrorException("Deleting object for " + fullId + " failed, DB reported " + deletedRows + " rows deleted");
            } else {
                logger.debug("Delete for id succeeded: {} revision: {}", localId, rev);
            }
        }
    }

    @Override
    public List<Map<String, Object>> query(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        return queryHandler.query(type, params, connection);
    }

    @Override
    protected SQLBuilder resolveQueryFilter(QueryFilter<JsonPointer> queryFilter, List<SortKey> sortKeys,
            Map<String, Object> sqlParams) {
        var builder = createSqlBuilder();

        var configResolver = createConfigResolver();

        var visitor = createFilterVisitor(configResolver);
        builder.addColumn("obj.*")
                .from("${_dbSchema}.${_table}", "obj")
                .where(Clauses.where(queryFilter.accept(visitor, new NamedParameterCollector(sqlParams)).toSQL()));

        if (sortKeys != null) {
            for (SortKey sortKey : sortKeys) {
                var config = configResolver.resolve(sortKey.getField());
                builder.orderBy(config.columnName, sortKey.isAscendingOrder());
            }
        }

        return builder;
    }

    /**
     * Create new column configuration resolver.
     *
     * @return new configuration resolver instance
     */
    protected MappedConfigResolver createConfigResolver() {
        Map<JsonPointer, MappedColumnConfig> columnConfig = columnMapping.values().stream()
                .collect(Collectors.toMap(value -> value.propertyName, value -> value));
        return field -> {
            var config = columnConfig.get(field);
            if (config == null) {
                throw new IllegalArgumentException("Unknown object field: " + field.toString());
            }
            return config;
        };
    }

    /**
     * Create new {@link SQLBuilder} to render query filter queries.
     *
     * @return new SQLBuilder instance
     */
    protected SQLBuilder createSqlBuilder() {
        return new SQLBuilder() {
            @Override
            public String toSQL() {
                return "SELECT " + getColumns().toSQL()
                        + getFromClause().toSQL()
                        + getJoinClause().toSQL()
                        + getWhereClause().toSQL()
                        + getOrderByClause().toSQL()
                        + " LIMIT ${int:_pageSize} "
                        + " OFFSET ${int:_pagedResultsOffset}";
            }
        };
    }

    /**
     * Create new {@link MappedSQLQueryFilterVisitor} to render query filter queries.
     *
     * @param configResolver column configuration resolver
     * @return new MappedSQLQueryFilterVisitor instance
     */
    protected MappedSQLQueryFilterVisitor createFilterVisitor(MappedConfigResolver configResolver) {
        return new MappedSQLQueryFilterVisitor(configResolver, objectMapper);
    }

    @Override
    public Integer queryCount(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        return queryHandler.queryCount(type, params, connection);
    }

    @Override
    public Integer command(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        return queryHandler.command(type, params, connection);
    }

}
