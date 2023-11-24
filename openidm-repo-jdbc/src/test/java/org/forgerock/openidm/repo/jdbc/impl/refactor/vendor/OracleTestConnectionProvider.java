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

import static org.testng.Assert.assertFalse;

import java.sql.Connection;
import java.sql.DriverManager;
import org.forgerock.openidm.repo.jdbc.impl.refactor.handler.AbstractTestConnectionProvider;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;

@SuppressWarnings("rawtypes")
public class OracleTestConnectionProvider extends AbstractTestConnectionProvider {

    private static final String IMAGE_NAME = "gvenzl/oracle-xe:21-slim-faststart";

    private static JdbcDatabaseContainer container = new OracleContainer(IMAGE_NAME)
            .withDatabaseName("wrenidm")
            .withUsername("wrenidm")
            .withPassword("wrenidm")
            .withInitScript("vendor/oracle.sql");

    @Override
    protected Connection openConnection(boolean first) throws Exception {
        if (first) {
            assertFalse(container.isRunning());
            container.start();
        }
        return DriverManager.getConnection(container.getJdbcUrl(), "wrenidm", "wrenidm");
    }

    @Override
    protected void closeConnection(Connection connection, boolean last) throws Exception {
        super.closeConnection(connection, last);
        if (last) {
            container.stop();
        }
    }

}
