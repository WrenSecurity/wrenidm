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
 * Portions Copyright 2024 Wren Security.
 */
package org.forgerock.openidm.repo.jdbc.impl.vendor;

import java.util.Map;
import java.util.stream.Collectors;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedConfigResolver;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedTableHandler;
import org.forgerock.openidm.repo.jdbc.impl.query.MappedSQLQueryFilterVisitor;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
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
                +")");
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
    protected MappedSQLQueryFilterVisitor createFilterVisitor(MappedConfigResolver configResolver) {
        return new MappedSQLQueryFilterVisitor(configResolver, objectMapper) {
            @Override
            protected StringSQLRenderer visitBooleanAssertion(NamedParameterCollector collector,
                    MappedColumnConfig config, String operand, JsonPointer field, Object valueAssertion) {
                String paramName = collector.register("v", valueAssertion);
                return new StringSQLRenderer(config.columnName + " " + operand + " " + "${" + paramName + "}");
            }
        };
    }

}
