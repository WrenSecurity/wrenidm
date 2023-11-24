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
package org.forgerock.openidm.repo.jdbc.impl.refactor.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Column-based result mapper that maps raw column values to object properties.
 */
public class ColumnResultMapper implements ResultMapper<Map<String, Object>> {

    private final int columnCount;

    private final List<String> columnNames = new ArrayList<>();

    public ColumnResultMapper(ResultSetMetaData metaData) throws SQLException {
        columnCount = metaData.getColumnCount();
        for (int idx = 1; idx <= columnCount; idx++) {
            columnNames.add(metaData.getColumnName(idx));
        }
    }

    @Override
    public Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int idx = 1; idx <= columnCount; idx++) {
            result.put(columnNames.get(idx - 1).toLowerCase(), rs.getObject(idx));
        }
        return result;
    }

}
