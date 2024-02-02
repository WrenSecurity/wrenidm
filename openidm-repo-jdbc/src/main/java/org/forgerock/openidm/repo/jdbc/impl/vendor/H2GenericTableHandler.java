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
import org.forgerock.openidm.repo.jdbc.impl.handler.GenericTableHandler;

/**
 * H2 database {@link GenericTableHandler} implementation.
 */
public class H2GenericTableHandler extends GenericTableHandler {

    public H2GenericTableHandler(
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
                    + "?, ?, ?, ? FORMAT JSON"
                + ")");
        result.put(ImplicitSqlType.UPDATE,
                "UPDATE ${_dbSchema}.${_mainTable} obj "
                + "SET "
                    + "obj.objectid = ?, "
                    + "obj.rev = ?, "
                    + "obj.fullobject = ? FORMAT JSON "
                + "WHERE obj.id = ?");
        return result;
    }

}
