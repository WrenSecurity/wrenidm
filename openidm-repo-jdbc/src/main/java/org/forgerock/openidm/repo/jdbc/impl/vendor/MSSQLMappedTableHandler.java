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
package org.forgerock.openidm.repo.jdbc.impl.vendor;

import java.util.Map;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedTableHandler;

/**
 * MSSQL database {@link MappedTableHandler} implementation.
 */
public class MSSQLMappedTableHandler extends MappedTableHandler {

    public MSSQLMappedTableHandler(
            String schemaName,
            String tableName,
            JsonValue columnMapping,
            Map<String, String> queryConfig,
            Map<String, String> commandConfig,
            SQLExceptionHandler exceptionHandler) {
        super(schemaName, tableName, columnMapping, queryConfig, commandConfig, exceptionHandler);
    }

    @Override
    protected Map<ImplicitSqlType, String> initializeImplicitSql() {
        var result = super.initializeImplicitSql();
        result.put(ImplicitSqlType.READFORUPDATE,
                "SELECT * FROM ${_dbSchema}.${_table} WITH (UPDLOCK, ROWLOCK) WHERE objectid = ?");
        return result;
    }

    @Override
    protected SQLBuilder createSqlBuilder() {
        return new SQLBuilder() {
            @Override
            public String toSQL() {
                var innerSql =
                        "SELECT "
                            + getColumns().toSQL() + ", "
                            + "ROW_NUMBER() OVER (" + getOrderByClause().toSQL() + " ) __rn "
                        + getFromClause().toSQL()
                        + getJoinClause().toSQL()
                        + getWhereClause().toSQL();
                return "WITH results AS (" + innerSql +") "
                        + "SELECT * FROM results WHERE "
                            + "__rn BETWEEN "
                                + "(${int:_pagedResultsOffset} + 1) AND "
                                + "(${int:_pagedResultsOffset} + ${int:_pageSize}) "
                        + "ORDER BY __rn";
            }
        };
    }

}
