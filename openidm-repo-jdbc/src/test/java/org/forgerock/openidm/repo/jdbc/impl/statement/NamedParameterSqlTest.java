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
package org.forgerock.openidm.repo.jdbc.impl.statement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.Test;

/**
 * {@link NamedParameterSql} test case.
 */
public class NamedParameterSqlTest {

    @Test
    public void testParseNoParams() {
        var parsed = NamedParameterSql.parse("SELECT * FROM hello");
        assertEquals(parsed.getSqlString(), "SELECT * FROM hello");
        assertNotNull(parsed.getParamTokens());
        assertTrue(parsed.getParamTokens().isEmpty());
    }

    @Test
    public void testParseWithParams() {
        var parsed = NamedParameterSql.parse("SELECT * FROM hello WHERE "
                + "value = ${foo} OR value = ${int:bar} OR value IN (${list:baz})");

        assertEquals(parsed.getSqlString(), "SELECT * FROM hello "
                + "WHERE value = ? OR value = ? OR value IN (${list:baz})");

        var paramTypes = parsed.getParamTokens();
        assertNotNull(paramTypes);

        var paramTokens = paramTypes.stream()
                .map(NamedParameterToken::getToken).collect(Collectors.toList());
        assertEquals(paramTokens, List.of("foo", "int:bar", "list:baz"));
    }

    @Test
    public void testParseParamTokens() {
        var simpleParam = NamedParameterToken.parse("foo");
        assertEquals(simpleParam.getToken(), "foo");
        assertEquals(simpleParam.getName(), "foo");
        assertEquals(simpleParam.getJavaType(), null);
        assertFalse(simpleParam.isList());

        var integerParam = NamedParameterToken.parse("int:foo");
        assertEquals(integerParam.getToken(), "int:foo");
        assertEquals(integerParam.getName(), "foo");
        assertEquals(integerParam.getJavaType(), Integer.class);
        assertFalse(integerParam.isList());

        var listParam = NamedParameterToken.parse("list:foo");
        assertEquals(listParam.getToken(), "list:foo");
        assertEquals(listParam.getName(), "foo");
        assertEquals(listParam.getJavaType(), null);
        assertTrue(listParam.isList());

        var listOfIntsParam = NamedParameterToken.parse("list:int:foo");
        assertEquals(listOfIntsParam.getToken(), "list:int:foo");
        assertEquals(listOfIntsParam.getName(), "foo");
        assertEquals(listOfIntsParam.getJavaType(), Integer.class);
        assertTrue(listOfIntsParam.isList());

    }

}
