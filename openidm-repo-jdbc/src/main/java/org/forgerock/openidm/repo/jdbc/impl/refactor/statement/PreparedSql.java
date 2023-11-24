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
package org.forgerock.openidm.repo.jdbc.impl.refactor.statement;

import java.util.List;

/**
 * Simple convenient value class for prepared SQL string with it's positional parameters. This is intermediate
 * product when processing SQL strings before preparing statements through the active JDBC connection.
 */
// TODO Introduce parameter value class if we need SQL types alongside the parameter values.
public class PreparedSql {

    private final String sqlString;

    private final List<Object> parameters;

    public PreparedSql(String sqlString, List<Object> parameters) {
        this.sqlString = sqlString;
        this.parameters = parameters;
    }

    public String getSqlString() {
        return sqlString;
    }

    public List<Object> getParameters() {
        return parameters;
    }

}
