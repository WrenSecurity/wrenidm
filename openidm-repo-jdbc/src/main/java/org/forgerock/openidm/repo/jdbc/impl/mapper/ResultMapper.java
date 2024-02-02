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
package org.forgerock.openidm.repo.jdbc.impl.mapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database query result set mapper.
 *
 * <p>
 * The responsibility of this component is to map a single object from the provided
 * {@link ResultSet}. Mapper implementations should not move the cursor of the result
 * set, i.e. iterating through the result set is responsibility of the caller.
 *
 * @param <T> mapped object type
 */
@FunctionalInterface
public interface ResultMapper<T> {

    /**
     * Map single object from the result set.
     *
     * @param resultSet the result set containing single row
     * @return mapped object
     * @throws IOException in case of JSON mapping error
     * @throws SQLException in case of SQL error
     */
    T map(ResultSet resultSet) throws SQLException, IOException;

}
