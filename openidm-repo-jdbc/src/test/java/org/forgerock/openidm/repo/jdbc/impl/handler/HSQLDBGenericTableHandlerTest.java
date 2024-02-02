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
package org.forgerock.openidm.repo.jdbc.impl.handler;

import java.sql.Connection;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.testng.annotations.Test;

@Test(singleThreaded = true, suiteName = "hsqldb")
public class HSQLDBGenericTableHandlerTest extends AbstractGenericTableHandlerTest {

    @Override
    protected Connection getConnection() throws Exception {
        return new HSQLDBTestConnectionProvider().getConnection();
    }

    @Override
    protected TableHandler createTableHandler() {
        return new GenericTableHandler(
            getSchemaName(),
            getTableConfig(),
            getQueryConfig(),
            getCommandConfig(),
            getBatchSize(),
            getExceptionHandler()
        );
    }
}