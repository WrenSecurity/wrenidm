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

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.object;

import java.sql.Connection;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.forgerock.openidm.repo.jdbc.impl.handler.AbstractMappedTableHandlerTest;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig.ValueType;
import org.testng.annotations.Test;

@Test(singleThreaded = true, suiteName = "oracle")
public class OracleMappedTableHandlerIT extends AbstractMappedTableHandlerTest {

    @Override
    protected Connection getConnection() throws Exception {
        return new OracleTestConnectionProvider().getConnection();
    }

    @Override
    protected TableHandler createTableHandler() throws Exception {
        return new OracleMappedTableHandler(
            getSchemaName(),
            getTableName(),
            getColumnMapping(),
            getQueryConfig(),
            getCommandConfig(),
            getExceptionHandler()
        );
    }

    @Override
    protected JsonValue getColumnMapping() {
        var mapping = super.getColumnMapping();
        // we need to specify java type hint because for Oracle every numeric data type is mapped to BigDecimal
        mapping.put("priority", object(
            field("column", "priority"),
            field("type", ValueType.NUMBER.name()),
            field("javaType", Integer.class.getName()
        )));
        return mapping;
    }

}
