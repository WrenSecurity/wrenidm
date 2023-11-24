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
package org.forgerock.openidm.repo.jdbc.impl.refactor.vendor;

import static org.forgerock.openidm.repo.util.Clauses.where;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.openidm.repo.jdbc.impl.refactor.handler.GenericTableHandler;
import org.forgerock.openidm.repo.jdbc.impl.refactor.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.Clauses;
import org.forgerock.openidm.repo.util.StringSQLQueryFilterVisitor;
import org.forgerock.openidm.repo.util.StringSQLRenderer;
import org.forgerock.openidm.util.ResourceUtil;
import org.forgerock.util.query.QueryFilter;

/**
 * PostgreSQL database {@link GenericTableHandler} implementation.
 */
public class PostgreSQLGenericTableHandler extends GenericTableHandler {

    public PostgreSQLGenericTableHandler(
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

        result.put(ImplicitSqlType.CREATE,
                "INSERT INTO ${_dbSchema}.${_mainTable} ("
                    + "objecttypes_id, objectid, rev, fullobject"
                + ") VALUES ("
                    + "?, ?, ?, ?::json"
                + ")");
        result.put(ImplicitSqlType.UPDATE,
                "UPDATE ${_dbSchema}.${_mainTable} "
                + "SET "
                    + "objectid = ?, "
                    + "rev = ?, "
                    + "fullobject = ?::json "
                + "WHERE id = ?");

        return result;
    }

    @Override
    protected SQLBuilder resolveQueryFilter(QueryFilter<JsonPointer> queryFilter, List<SortKey> sortKeys, Map<String, Object> sqlParams) {
        var builder = createSqlBuilder();

        var collector = new NamedParameterCollector(sqlParams);

        var visitor = createFilterVisitor();
        builder.addColumn("fullobject::text")
                .from("${_dbSchema}.${_mainTable}", "obj")
                .join("${_dbSchema}.objecttypes", "objecttypes")
                .on(where("obj.objecttypes_id = objecttypes.id")
                        .and("objecttypes.objecttype = ${_resource}"))
                .where(Clauses.where(queryFilter.accept(visitor, collector).toSQL()));

        if (sortKeys != null) {
            for (var sortKey : sortKeys) {
                if (ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(sortKey.getField())) {
                    builder.orderBy("objectid", sortKey.isAscendingOrder());
                } else {
                    // TODO support numeric ordering
                    var orderBy = resolveJsonExtractPath(sortKey.getField(), collector);
                    builder.orderBy(orderBy.toString(), sortKey.isAscendingOrder());
                }
            }
        }
        return builder;
    }

    private StringSQLQueryFilterVisitor<NamedParameterCollector> createFilterVisitor() {
        return new StringSQLQueryFilterVisitor<NamedParameterCollector>() {

            @Override
            public StringSQLRenderer visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
                if (ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(field)) {
                    // NOT NULL enforced by the schema
                    return new StringSQLRenderer("obj.objectid IS NOT NULL");
                } else {
                    return new StringSQLRenderer(resolveJsonExtractPath(field, collector) + " IS NOT NULL");
                }
            }

            @Override
            public StringSQLRenderer visitValueAssertion(NamedParameterCollector collector, String operand,
                    JsonPointer field, Object valueAssertion) {
                String parameterKey = collector.register("v", valueAssertion);
                if (ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(field)) {
                    return new StringSQLRenderer("(obj.objectid " + operand + " ${" + parameterKey + "})");
                }
                String cast = "";
                if (isNumeric(valueAssertion)) {
                    cast = "::numeric";
                } else if (isBoolean(valueAssertion)) {
                    cast = "::boolean";
                }
                return new StringSQLRenderer(resolveJsonExtractPath(field, collector).append(cast)
                        .append(" ").append(operand).append(" ")
                        .append("${").append(parameterKey).append("}").append(cast).toString());
            }

            private boolean isNumeric(Object value) {
                return value instanceof Integer
                        || value instanceof Long
                        || value instanceof Float
                        || value instanceof Double;
            }

            private boolean isBoolean(Object value) {
                return value instanceof Boolean;
            }

        };
    }

    private StringBuilder resolveJsonExtractPath(JsonPointer field, NamedParameterCollector collector) {
        StringBuilder result = new StringBuilder("json_extract_path_text(fullobject");
        for (String pathPart : field.toArray()) {
            String tokenName = collector.register("p", pathPart);
            result.append(", ${").append(tokenName).append("}");
        }
        result.append(")");
        return result;
    }

    @Override
    protected void writeValueProperties(String fullId, long databaseId, JsonValue value, Connection connection)
            throws SQLException {
        // properties table is not necessary
    }

    @Override
    protected void clearValueProperties(String fullId, long databaseId, Connection connection) throws SQLException {
        // properties table is not necessary
    }

}
