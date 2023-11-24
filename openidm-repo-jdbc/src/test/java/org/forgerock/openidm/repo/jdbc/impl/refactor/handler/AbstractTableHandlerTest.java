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
package org.forgerock.openidm.repo.jdbc.impl.refactor.handler;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.resource.ResourcePath.resourcePath;
import static org.forgerock.openidm.repo.QueryConstants.PAGED_RESULTS_OFFSET;
import static org.forgerock.openidm.repo.QueryConstants.PAGE_SIZE;
import static org.forgerock.openidm.repo.QueryConstants.QUERY_EXPRESSION;
import static org.forgerock.openidm.repo.QueryConstants.QUERY_FILTER;
import static org.forgerock.openidm.repo.QueryConstants.QUERY_ID;
import static org.forgerock.openidm.repo.QueryConstants.SORT_KEYS;
import static org.forgerock.openidm.repo.jdbc.Constants.OBJECT_ID;
import static org.forgerock.openidm.repo.jdbc.Constants.OBJECT_REV;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.PreconditionFailedException;
import org.forgerock.json.resource.QueryFilters;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Base test cases for {@link TableHandler} implementations.
 *
 * <p>
 * All tests are written for a single object type only and are run in sequence where
 * the first test creates the actual object that is being used by the subsequent tests.
 */
public abstract class AbstractTableHandlerTest {

    /**
     * Whether to use legacy behavior (legacy handlers and no support for number and boolean data types).
     */
    protected static final boolean LEGACY_MODE = true;

    /**
     * Whether to use support for number and boolean data types (this will be possible after dropping legacy mode).
     */
    protected static final boolean FUTURE_MODE = false;

    protected static final String OBJECT_TYPE = "greeting";

    protected static final String RESOURCE_ID = "hello";

    protected final TableHandler tableHandler;

    protected Connection connection;

    public AbstractTableHandlerTest() {
        try {
            tableHandler = createTableHandler();
        } catch (Exception ex) {
            throw new IllegalStateException("Error creating table handler", ex);
        }
    }

    /**
     * Get active database
     *
     * @return the database connection to a fully initialized database
     */
    protected abstract Connection getConnection() throws Exception;

    @BeforeMethod
    public void beginTransaction(Method method) throws Exception {
        connection = getConnection();
        connection.setAutoCommit(false);
    }

    @AfterMethod
    public void rollbackTransaction(ITestResult result, Method method) throws Exception {
        connection.rollback();
        connection = null;
    }

    /**
     * Get database schema name.
     */
    protected String getSchemaName() {
        return "wrenidm";
    }

    /**
     * Create table handler that will be tested.
     */
    protected abstract TableHandler createTableHandler() throws Exception;

    /**
     * Create new resource with the given ID and a set of properties.
     */
    protected Map<String, Object> createResource(String id, Map<String, Object> properties) throws Exception {
        Map<String, Object> resource = new LinkedHashMap<>(properties);
        tableHandler.create(
            resourcePath(OBJECT_TYPE).child(id).toString(),
            OBJECT_TYPE,
            id,
            resource,
            connection
        );
        return resource;
    }

    /**
     * Read resource state with the given ID from the database.
     */
    protected ResourceResponse readResource(String id) throws Exception {
        return tableHandler.read(
            resourcePath(OBJECT_TYPE).child(id).toString(),
            OBJECT_TYPE,
            id,
            connection
        );
    }

    /**
     * Run query filter against using the current table handler with no additional parameters.
     */
    protected List<Map<String, Object>> queryResource(String queryFilter) throws Exception {
        Map<String, Object> params = Map.of(
            QUERY_FILTER, QueryFilters.parse(queryFilter),
            PAGED_RESULTS_OFFSET, 0,
            PAGE_SIZE, 0
        );
        return queryResource(params);
    }

    /**
     * Run query filter against using the current table handler.
     */
    protected List<Map<String, Object>> queryResource(Map<String, Object> params) throws Exception {
        return tableHandler.query(
            OBJECT_TYPE,
            LEGACY_MODE ? new HashMap<>(params) : params,
            connection
        );
    }

    /**
     * Assert that resource properties match the given template (including value types).
     */
    protected void assertResourceValues(Map<String, Object> resource, Map<String, Object> template) {
        for (String propertyName : template.keySet()) {
            var resourceValue = resource.get(propertyName);
            var templateValue = template.get(propertyName);
            assertEquals(resourceValue, templateValue, "property " + propertyName);
        }
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, Object> resource = createResource(RESOURCE_ID, Map.of(
                "name", "HELLO",
                "score", 7,
                "visible", true));

        assertEquals(resource.get(OBJECT_ID), RESOURCE_ID);
        assertEquals(resource.get(OBJECT_REV), "0");
    }

    @Test
    public void testRead() throws Exception {
        var template = Map.of(
                "name", "HELLO",
                "score", FUTURE_MODE ? 7 : "7",
                "visible", FUTURE_MODE ? true : "true",
                "tags", List.of("foo", "bar"),
                "meta", Map.of("owner", "john"));
        createResource(RESOURCE_ID, template);

        var resource = readResource(RESOURCE_ID);
        assertNotNull(resource);
        assertEquals(resource.getId(), RESOURCE_ID);
        assertEquals(resource.getRevision(), "0");

        assertResourceValues(resource.getContent().asMap(), template);
    }

    @Test
    public void testReadNullable() throws Exception {
        createResource(RESOURCE_ID, Map.of());

        var resource = readResource(RESOURCE_ID);
        assertNotNull(resource);
        assertEquals(resource.getId(), RESOURCE_ID);
        for (String property : Arrays.asList("name", "score", "visible")) {
            var value = resource.getContent().get(property);
            assertNotNull(value);
            assertNull(value.getObject());
        }
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testReadNonExistent() throws Exception {
        readResource("non-existent");
    }

    @Test
    public void testQueryFilterSimple() throws Exception {
        var template = Map.of(
                "name", "HELLO",
                "score", FUTURE_MODE ? 7.0 : "7",
                "visible", FUTURE_MODE ? true : "true",
                "tags", List.of("foo", "bar"),
                "meta", Map.of("owner", "john"));
        createResource(RESOURCE_ID, template);

        var result = queryResource("_id eq '" + RESOURCE_ID + "'");
        assertNotNull(result);
        assertEquals(result.size(), 1);

        assertResourceValues(result.get(0), template);
    }

    @Test
    public void testQueryFilterComplex() throws Exception {
        createResource(RESOURCE_ID, Map.of("name", "HELLO TO", "score", 70, "visible", true));
        createResource("with-different-name", Map.of("name", "GOOD BYE", "score", 70, "visible", true));
        createResource("with-lower-score", Map.of("name", "HELLO TO", "score", 8, "visible", true));
        createResource("with-not-visible", Map.of("name", "HELLO TO", "score", 80, "visible", false));

        var resultIds = queryResource("name sw 'HELLO' and score eq 70 and visible eq true").stream()
                .map(resource -> resource.get(OBJECT_ID))
                .collect(Collectors.toSet());
        assertEquals(resultIds, Set.of(RESOURCE_ID));
    }

    @Test(enabled = FUTURE_MODE)
    public void testQueryFilterNumber() throws Exception {
        createResource("lower-value", Map.of("score", 9));
        createResource("lower-decimal", Map.of("score", 9.1));
        createResource(RESOURCE_ID, Map.of("score", 70));
        createResource("higher-value", Map.of("score", 90));
        createResource("higher-decimal", Map.of("score", 90.1));
        createResource("null-value", object(field("score", null)));

        var resultIds = queryResource("score ge 70").stream()
                .map(resource -> resource.get(OBJECT_ID))
                .collect(Collectors.toSet());
        assertEquals(resultIds, Set.of(RESOURCE_ID, "higher-value", "higher-decimal"));
    }

    @Test
    public void testQueryFilterBoolean() throws Exception {
        createResource(RESOURCE_ID, Map.of("visible", true));
        createResource("not-visible", Map.of("visible", false));
        createResource("null-visible", object(field("visible", null)));

        var resultIds = queryResource("visible eq true").stream()
                .map(resource -> resource.get(OBJECT_ID))
                .collect(Collectors.toSet());
        assertEquals(resultIds, Set.of(RESOURCE_ID));
    }

    @Test
    public void testQueryFilterEmpty() throws Exception {
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));

        var result = queryResource("name eq 'non-existent'");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testQueryPaging() throws Exception {
        for (int i = 0; i < 10; i++) {
            createResource("paging-" + i, Map.of("name", "HELLO " + i));
        }

        Map<String, Object> params = Map.of(
            QUERY_FILTER, QueryFilters.parse("_id sw 'paging-'"),
            SORT_KEYS, List.of(SortKey.ascendingOrder(OBJECT_ID)),
            PAGED_RESULTS_OFFSET, 3,
            PAGE_SIZE, 3
        );
        var matchedIds = queryResource(params).stream()
                .map(resource -> resource.get(OBJECT_ID))
                .collect(Collectors.toSet());
        assertEquals(matchedIds, Set.of("paging-3", "paging-4", "paging-5"));
    }

    @Test
    public void testQuerySorting() throws Exception {
        for (int i = 0; i < 10; i++) {
            createResource("sorting-" + i, Map.of("name", "HELLO " + i));
        }

        Map<String, Object> ascendingParams = Map.of(
            QUERY_FILTER, QueryFilters.parse("_id sw 'sorting-'"),
            PAGE_SIZE, 0,
            SORT_KEYS, List.of(SortKey.ascendingOrder(OBJECT_ID), SortKey.ascendingOrder("name"))
        );
        var ascendingIds = queryResource(ascendingParams).stream()
                .map(resource -> resource.get(OBJECT_ID))
                .collect(Collectors.toList());

        Map<String, Object> descendingParams = Map.of(
            QUERY_FILTER, QueryFilters.parse("_id sw 'sorting-'"),
            PAGE_SIZE, 0,
            SORT_KEYS, List.of(SortKey.descendingOrder(OBJECT_ID))
        );
        var descendingIds = queryResource(descendingParams).stream()
                .map(resource -> resource.get(OBJECT_ID))
                .collect(Collectors.toList());

        assertNotEquals(ascendingIds, descendingIds);
        assertEquals(ascendingIds, Lists.reverse(descendingIds));
    }

    @Test
    public void testQueryId() throws Exception {
        // single matching resource is enough (we are not testing DB engines)
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));

        var result = queryResource(Map.of(
            QUERY_ID, "sample-query",
            "id", RESOURCE_ID,
            PAGE_SIZE, 0
        ));
        assertFalse(result.isEmpty());
        for (var resource : result) {
            assertEquals(resource.get("name"), "HELLO");
        }
    }

    @Test(dependsOnMethods = "testCreate")
    public void testQueryExpression() throws Exception {
        // single matching resource is enough (we are not testing DB engines)
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));

        var result = queryResource(Map.of(
            QUERY_EXPRESSION, getTestQueryExpression(),
            PAGE_SIZE, 0
        ));
        assertFalse(result.isEmpty());
    }

    /**
     * Get test query expression that results in a non-empty result set.
     *
     * @return the test query expression
     */
    protected abstract String getTestQueryExpression();

    @Test
    public void testParamExpansion() throws Exception {
        // single matching resource is enough (we are not testing DB engines)
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));

        var result = queryResource(Map.of(
            QUERY_EXPRESSION, getParamQueryExpression(),
            "ids", "ahoy,hello,bonjour",
            PAGE_SIZE, 0
        ));
        assertFalse(result.isEmpty());
    }

    /**
     * Get test query expression that contains <code>${list:ids}</code> named parameter.
     *
     * @return the test query expression
     */
    protected abstract String getParamQueryExpression();

    @Test
    public void testUpdate() throws Exception {
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));

        Map<String, Object> resource = new LinkedHashMap<>(Map.of(
            "name", "BONJOUR"
        ));
        tableHandler.update(
            resourcePath(OBJECT_TYPE).child(RESOURCE_ID).toString(),
            OBJECT_TYPE,
            RESOURCE_ID,
            "0",
            resource,
            connection
        );
        assertEquals(resource.get(Constants.OBJECT_REV), "1");

        var updated = readResource(RESOURCE_ID);
        assertEquals(updated.getContent().get("name").asString(), "BONJOUR");
    }

    @Test(expectedExceptions = PreconditionFailedException.class)
    public void testUpdateLock() throws Exception {
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));

        Map<String, Object> resource = new LinkedHashMap<>(Map.of(
            "name", "AHOY"
        ));
        tableHandler.update(
            resourcePath(OBJECT_TYPE).child(RESOURCE_ID).toString(),
            OBJECT_TYPE,
            RESOURCE_ID,
            "-1",
            resource,
            connection
        );
    }

    @Test(enabled = !LEGACY_MODE)
    public void testQueryIdCount() throws Exception {
        assertNull(tableHandler.queryCount(OBJECT_TYPE, Map.of("_queryId", "non-existing"), connection));

        createResource(RESOURCE_ID, Map.of("name", "HELLO"));
        createResource("alternative", Map.of("name", "GUTEN TAG"));

        Map<String, Object> params = Map.of(
            QUERY_ID, "sample-query",
            "id", RESOURCE_ID,
            PAGE_SIZE, 0
        );
        var result = queryResource(params);
        var count = tableHandler.queryCount(OBJECT_TYPE, params, connection);
        assertFalse(result.isEmpty());
        assertNotNull(count);
        assertEquals(count, result.size());
    }

    @Test(enabled = !LEGACY_MODE)
    public void testQueryFilterCount() throws Exception {
        createResource(RESOURCE_ID, Map.of("name", "HELLO"));
        createResource("alternative", Map.of("name", "GUTEN TAG"));

        assertEquals(tableHandler.queryCount(OBJECT_TYPE, Map.of(
            QUERY_FILTER, QueryFilters.parse("name eq 'non-existent'"),
            PAGE_SIZE, 0
        ), connection), 0);

        assertEquals(tableHandler.queryCount(OBJECT_TYPE, Map.of(
            QUERY_FILTER, QueryFilters.parse("name eq 'HELLO'"),
            PAGE_SIZE, 0
        ), connection), 1);
    }

    @Test
    public void testDelete() throws Exception {
        createResource("for-deletion", Map.of("name", "ARRIVEDERCI"));

        assertNotNull(readResource("for-deletion"));

        tableHandler.delete(
            resourcePath(OBJECT_TYPE).child("for-deletion").toString(),
            OBJECT_TYPE,
            "for-deletion",
            "0",
            connection
        );

        try {
            readResource("for-deletion");
            fail("NotFoundException expected");
        } catch (NotFoundException e) { }
    }

    @Test
    public void testCommand() throws Exception {
        createResource("42", Map.of("name", "OREVUAR"));

        Map<String, Object> params = Map.of(
            "commandId", "sample-command",
            "id", "42"
        );
        var result = tableHandler.command(
            OBJECT_TYPE,
            LEGACY_MODE ? new HashMap<>(params) : params,
            connection
        );
        assertNotNull(result);
        assertEquals(result, 1);
    }

}
