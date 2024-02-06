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

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;

import java.util.Map;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;

/**
 * Common superclass for {@link GenericTableHandler} test cases.
 */
public abstract class AbstractGenericTableHandlerTest extends AbstractTableHandlerTest {

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected JsonValue getTableConfig() {
        return json(object(
            field("mainTable", "genericobjects"),
            field("propertiesTable", "genericobjectproperties"),
            field("properties", object())
        ));
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected Map<String, String> getQueryConfig() {
        return json(object(
            field("sample-query", "SELECT * FROM ${_dbSchema}.${_mainTable} obj WHERE objectid LIKE ${id}"),
            field("sample-query-count", "SELECT COUNT(*) AS total FROM ${_dbSchema}.${_mainTable} WHERE objectid LIKE ${id}")
        )).asMap(String.class);
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected Map<String, String> getCommandConfig() {
        return json(object(
            field("sample-command", "DELETE FROM ${_dbSchema}.${_mainTable} WHERE objectid = ${id}")
        )).asMap(String.class);
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected int getBatchSize() {
        return -1;
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected SQLExceptionHandler getExceptionHandler() {
        return null;
    }

    @Override
    protected String getTestQueryExpression() {
        return "SELECT * FROM wrenidm.genericobjects";
    }

    @Override
    protected String getParamQueryExpression() {
        return "SELECT * FROM wrenidm.genericobjects WHERE objectid IN (${list:ids})";
    }

}
