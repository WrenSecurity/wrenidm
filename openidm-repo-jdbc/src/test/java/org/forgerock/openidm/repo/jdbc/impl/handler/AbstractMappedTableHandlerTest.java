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

import static org.forgerock.json.JsonValue.array;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.resource.ResourcePath.resourcePath;
import static org.forgerock.openidm.repo.jdbc.Constants.OBJECT_ID;
import static org.testng.Assert.assertEquals;

import java.util.Map;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig.ValueType;
import org.testng.annotations.Test;

/**
 * Common superclass for {@link MappedTableHandler} test cases.
 */
public abstract class AbstractMappedTableHandlerTest extends AbstractTableHandlerTest {

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected String getTableName() {
        return "managedgreeting";
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected JsonValue getColumnMapping() {
        return json(object(
            field("_id", "objectid"),
            field("_rev", Constants.RAW_OBJECT_REV),
            // string column property
            field("name", "name"),
            // number (integer) column property
            field("priority", array("priority", ValueType.NUMBER.name())),
            // number (decimal) column property
            field("score", object(
                field("column", "ranking"), // intentional name discrepancy
                field("type", ValueType.NUMBER.name())
            )),
            // boolean column property
            field("visible", array("visible", ValueType.BOOLEAN.name())),
            // json list column property
            field("tags", array("tags", ValueType.JSON_LIST.name())),
            // json map column property
            field("meta", array("meta", ValueType.JSON_MAP.name()))
        ));
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected Map<String, String> getQueryConfig() {
        return json(object(
            field("sample-query", "SELECT * FROM ${_dbSchema}.${_table} obj"),
            field("sample-query-count", "SELECT COUNT(*) AS total FROM ${_dbSchema}.${_table}")
        )).asMap(String.class);
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected Map<String, String> getCommandConfig() {
        return json(object(
            field("sample-command", "DELETE FROM ${_dbSchema}.${_table} WHERE objectid = ${id}")
        )).asMap(String.class);
    }

    /**
     * Table handler constructor value provider.
     * @see #createTableHandler()
     */
    protected SQLExceptionHandler getExceptionHandler() {
        return null;
    }

    @Override
    protected void assertResourceValues(Map<String, Object> resource, Map<String, Object> template) {
        super.assertResourceValues(resource, template);
    }

    @Override
    protected String getTestQueryExpression() {
        return "SELECT * FROM wrenidm.managedgreeting";
    }

    @Override
    protected String getParamQueryExpression() {
        return "SELECT * FROM wrenidm.managedgreeting WHERE objectid IN (${list:ids})";
    }

    @Test
    public void testQueryFilterLegacy() throws Exception {
        createResource("stringified-boolean", Map.of("name", "true"));
        createResource("stringified-integer", Map.of("name", "7"));
        createResource("stringified-double", Map.of("name", "7.0"));

        var booleanResult = queryResource("name eq true");
        assertEquals(booleanResult.size(), 1);
        assertEquals(booleanResult.get(0).get(OBJECT_ID), "stringified-boolean");

        var integerResult = queryResource("name eq 7");
        assertEquals(integerResult.size(), 1);
        assertEquals(integerResult.get(0).get(OBJECT_ID), "stringified-integer");

        var doubleResult = queryResource("name eq 7.0");
        assertEquals(doubleResult.size(), 1);
        assertEquals(doubleResult.get(0).get(OBJECT_ID), "stringified-double");
    }

    @Test(
        expectedExceptions = BadRequestException.class,
        expectedExceptionsMessageRegExp = "Unmapped.*foobar.*"
    )
    public void testStateCheckCreate() throws Exception {
        createResource(RESOURCE_ID, Map.of("foobar", "unmapped"));
    }

    @Test(
        expectedExceptions = BadRequestException.class,
        expectedExceptionsMessageRegExp = "Unmapped.*foobar.*"
    )
    public void testStateCheckUpdate() throws Exception {
        var resource = createResource(RESOURCE_ID, Map.of("name", "hello"));
        resource.put("foobar", "unmapped");
        tableHandler.update(
            resourcePath(OBJECT_TYPE).child(RESOURCE_ID).toString(),
            OBJECT_TYPE,
            RESOURCE_ID,
            "0",
            resource,
            connection
        );
    }

}
