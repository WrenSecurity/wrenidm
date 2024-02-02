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
import org.forgerock.openidm.repo.jdbc.impl.handler.GenericTableHandler;

/**
 * MSSQL database {@link GenericTableHandler} implementation.
 */
public class MSSQLGenericTableHandler extends GenericTableHandler {

    /**
     * Max length of searchable properties for MSSQL.
     * Anything larger than 195 will overflow the max index size and error.
     */
    private static final int MSSQL_SEARCHABLE_LENGTH = 195;

    public MSSQLGenericTableHandler(
            String schemaName,
            JsonValue tableConfig,
            Map<String, String> queryConfig,
            Map<String, String> commandConfig,
            int batchSize,
            SQLExceptionHandler exceptionHandler) {
        super(schemaName, tableConfig, queryConfig, commandConfig, batchSize, exceptionHandler);
    }

    @Override
    protected Map<ImplicitSqlType, String> initializeImplicitSql() {
        var result = super.initializeImplicitSql();
        result.put(ImplicitSqlType.READFORUPDATE,
                "SELECT obj.* "
                + "FROM ${_dbSchema}.${_mainTable} obj "
                + "WITH (UPDLOCK, ROWLOCK) "
                + "WHERE "
                    + "obj.objecttypes_id = ("
                        + "SELECT id FROM ${_dbSchema}.objecttypes objtype "
                        + "WHERE objtype.objecttype = ?"
                    + ") AND "
                    + "obj.objectid  = ?");
        return result;
    }

    @Override
    protected int getSearchableLength() {
        return MSSQL_SEARCHABLE_LENGTH;
    }

    @Override
    protected SQLBuilder createSqlBuilder() {
        return new SQLBuilder() {
            @Override
            public String toSQL() {
                var innerSql =
                        "SELECT "
                            + "obj.fullobject, "
                            + "ROW_NUMBER() OVER (" + getOrderByClause().toSQL() + " ) __rn "
                        + getFromClause().toSQL()
                        + getJoinClause().toSQL()
                        + getWhereClause().toSQL();
                return "WITH results AS (" + innerSql +") "
                        + "SELECT fullobject FROM results WHERE "
                            + "__rn BETWEEN "
                                + "(${int:_pagedResultsOffset} + 1) AND "
                                + "(${int:_pagedResultsOffset} + ${int:_pageSize}) "
                        + "ORDER BY __rn";
            }
        };
    }

}
