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
import java.sql.DriverManager;
import org.forgerock.openidm.repo.jdbc.impl.refactor.handler.AbstractTestConnectionProvider;

public class MySQLTestConnectionProvider extends AbstractTestConnectionProvider {

    @Override
    protected Connection openConnection(boolean first) throws Exception {
        return DriverManager.getConnection(
                // https://github.com/testcontainers/testcontainers-java/issues/4121
                "jdbc:tc:mysql:8.0.36://hostname/wrenidm?TC_INITSCRIPT=vendor/mysql.sql",
                "wrenidm",
                "wrenidm");
    }

}
