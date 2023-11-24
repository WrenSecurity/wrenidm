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

import java.sql.Connection;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.crypto.impl.CryptoServiceImpl;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.forgerock.openidm.repo.jdbc.impl.refactor.handler.AbstractMappedTableHandlerTest;
import org.testng.annotations.Test;

@Test(singleThreaded = true, suiteName = "h2")
public class H2MappedTableHandlerIT extends AbstractMappedTableHandlerTest {

    @Override
    protected Connection getConnection() throws Exception {
        return new H2TestConnectionProvider().getConnection();
    }

    @Override
    protected TableHandler createTableHandler() throws Exception {
        if (LEGACY_MODE) {
            return new org.forgerock.openidm.repo.jdbc.impl.H2MappedTableHandler(
                    getTableName(),
                    getColumnMapping().asMap(),
                    getSchemaName(),
                    JsonValue.json(getQueryConfig()),
                    JsonValue.json(getCommandConfig()),
                    getExceptionHandler(),
                    () -> new CryptoServiceImpl());

        }
        return new H2MappedTableHandler(
            getSchemaName(),
            getTableName(),
            getColumnMapping(),
            getQueryConfig(),
            getCommandConfig(),
            getExceptionHandler()
        );
    }

}
