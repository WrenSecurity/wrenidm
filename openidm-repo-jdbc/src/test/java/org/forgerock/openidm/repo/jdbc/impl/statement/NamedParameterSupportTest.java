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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import org.forgerock.json.resource.ResourcePath;
import org.testng.annotations.Test;

/**
 * {@link NamedParameterSupport} test case.
 */
public class NamedParameterSupportTest {

    @Test
    public void testPrepareSqlString() throws Exception {
        var sql = NamedParameterSql.parse("SELECT * FROM hello WHERE id = ${int:foo} AND val IN (${list:bar})");

        var preparedSql = NamedParameterSupport.prepareSqlString(sql, Map.of(
                "foo", 13,
                "bar", List.of("world", "universe")));

        assertEquals(preparedSql.getSqlString(), "SELECT * FROM hello WHERE id = ? AND val IN (?, ?)");
        assertEquals(preparedSql.getParameters(), List.of(13, "world", "universe"));
    }

    @Test
    public void testApplyStatementParams() throws Exception {
        var resourcePath = new ResourcePath("parent", "child");
        PreparedStatement statement = mock(PreparedStatement.class);
        List<Object> parameters = List.of(1, 1L, 1.0f, 1.0, true, "foo", resourcePath);
        NamedParameterSupport.applyStatementParams(statement, parameters);
        verify(statement, times(1)).setInt(1, 1);
        verify(statement, times(1)).setLong(2, 1L);
        verify(statement, times(1)).setFloat(3, 1.0f);
        verify(statement, times(1)).setDouble(4, 1.0);
        verify(statement, times(1)).setBoolean(5, true);
        verify(statement, times(1)).setString(6, "foo");
        verify(statement, times(1)).setString(7, resourcePath.toString());
    }

}
