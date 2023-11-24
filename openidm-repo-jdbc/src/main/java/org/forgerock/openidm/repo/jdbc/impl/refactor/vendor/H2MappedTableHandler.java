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

import java.util.Map;
import java.util.stream.Collectors;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.refactor.handler.MappedTableHandler;

/**
 * H2 database {@link MappedTableHandler} implementation.
 */
public class H2MappedTableHandler extends MappedTableHandler {

    public H2MappedTableHandler(
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
                            .map(config -> config.isJson() ? "? FORMAT JSON" : "?")
                            .collect(Collectors.joining(", "))
                +")");
        result.put(ImplicitSqlType.UPDATE,
                "UPDATE ${_dbSchema}.${_table} "
                + "SET "
                    + columnMapping.values().stream()
                            .map(config -> config.columnName + (config.isJson() ? " = ? FORMAT JSON" : " = ?"))
                            .collect(Collectors.joining(", "))
                + " WHERE objectid = ?");
        return result;
    }

}
