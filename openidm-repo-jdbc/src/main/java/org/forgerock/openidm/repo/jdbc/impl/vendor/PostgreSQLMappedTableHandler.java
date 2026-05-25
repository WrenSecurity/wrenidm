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
 * Copyright 2015 ForgeRock AS.
 * Portions Copyright 2024-2026 Wren Security.
 */
package org.forgerock.openidm.repo.jdbc.impl.vendor;

import java.util.Map;
import java.util.stream.Collectors;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedTableHandler;
import org.forgerock.openidm.repo.jdbc.impl.query.JsonFieldFilterVisitor;
import org.forgerock.openidm.repo.jdbc.impl.query.SQLJSONUtils;
import org.forgerock.openidm.repo.jdbc.impl.query.SQLRendererFieldFilterVisitor;
import org.forgerock.openidm.repo.jdbc.impl.query.SimpleFieldFilterVisitor;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.SQLRenderer;
import org.forgerock.openidm.repo.util.StringSQLRenderer;

/**
 * PostgreSQL database {@link MappedTableHandler} implementation.
 */
public class PostgreSQLMappedTableHandler extends MappedTableHandler {

    public PostgreSQLMappedTableHandler(
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

        result.put(ImplicitSqlType.CREATE,
                "INSERT INTO ${_dbSchema}.${_table} ("
                    + columnMapping.values().stream().map(config -> config.columnName)
                            .collect(Collectors.joining(", "))
                + ") VALUES ("
                    + columnMapping.values().stream()
                            .map(config -> config.isJson() ? "?::json" : "?")
                            .collect(Collectors.joining(", "))
                + ")");
        result.put(ImplicitSqlType.UPDATE,
                "UPDATE ${_dbSchema}.${_table} "
                + "SET "
                    + columnMapping.values().stream()
                            .map(config -> config.columnName + " = ?" + (config.isJson() ? "::json" : ""))
                            .collect(Collectors.joining(", "))
                + " WHERE objectid = ?");

        return result;
    }


    @Override
    protected SimpleFieldFilterVisitor createSimpleFieldVisitor(MappedColumnConfig columnConfig) {
        return new SimpleFieldFilterVisitor(columnConfig, objectMapper) {
            @Override
            protected SQLRenderer<String> visitBooleanAssertion(NamedParameterCollector collector, String operand,
                    Object valueAssertion) {
                String paramName = collector.register("v", valueAssertion);
                return new StringSQLRenderer(columnConfig.columnName + " " + operand + " " + "${" + paramName + "}");
            }
        };
    }

    @Override
    protected SQLRendererFieldFilterVisitor createJsonFieldVisitor(MappedColumnConfig columnConfig) {
        return new JsonFieldFilterVisitor(columnConfig, objectMapper) {
            @Override
            protected SQLRenderer<String> visitJsonAssertion(NamedParameterCollector collector, String operand,
                    JsonPointer nestedPath, Object valueAssertion) {
                String pathExpression = SQLJSONUtils.toSqlJsonPath(nestedPath) + " " + operand + " "
                        + toJsonValue(valueAssertion);
                String paramName = collector.register("v", pathExpression);
                return new StringSQLRenderer(columnConfig.columnName + "::JSONB @@ ${" + paramName + "}::JSONPATH");
            }

            @Override
            public SQLRenderer<String> visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
                String paramName = collector.register("v", SQLJSONUtils.toSqlJsonPath(toRelativePointer(field)));
                return new StringSQLRenderer(columnConfig.columnName + "::JSONB @? ${" + paramName + "}::JSONPATH");
            }
      };
    }

}
