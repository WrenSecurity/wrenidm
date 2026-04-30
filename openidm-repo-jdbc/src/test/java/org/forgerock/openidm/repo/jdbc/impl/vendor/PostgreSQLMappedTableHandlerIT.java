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
 * Copyright 2024-2026 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.vendor;

import static org.forgerock.openidm.repo.jdbc.Constants.OBJECT_ID;
import static org.testng.Assert.assertEquals;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.forgerock.openidm.repo.jdbc.impl.handler.AbstractMappedTableHandlerTest;
import org.testng.annotations.Test;

@Test(singleThreaded = true, suiteName = "postgresql")
public class PostgreSQLMappedTableHandlerIT extends AbstractMappedTableHandlerTest {

    @Override
    protected Connection getConnection() throws Exception {
        return new PostgreSQLTestConnectionProvider().getConnection();
    }

    @Override
    protected TableHandler createTableHandler() throws Exception {
        return new PostgreSQLMappedTableHandler(
            getSchemaName(),
            getTableName(),
            getColumnMapping(),
            getQueryConfig(),
            getCommandConfig(),
            getExceptionHandler()
        );
    }

    @Test
    public void testQueryFilterJsonListEquals() throws Exception {
        createResource("resource-1", Map.of("tags", List.of("foo", "bar")));
        createResource("resource-2", Map.of("tags", List.of("foo", "baz")));
        createResource("resource-3", Map.of("tags", List.of(1, 2)));
        createResource("resource-4", Map.of("tags", List.of(1, 3)));
        createResource("resource-5", Map.of("tags", List.of(true)));
        createResource("resource-6", Map.of("tags", List.of(false)));

        var fooResult = queryResource("tags eq \"foo\"");
        assertEquals(fooResult.size(), 2);
        assertEquals(fooResult.stream().map(r -> r.get(OBJECT_ID)).sorted().toList(),
                List.of("resource-1", "resource-2"));

        var bazResult = queryResource("tags eq \"baz\"");
        assertEquals(bazResult.size(), 1);
        assertEquals(bazResult.stream().map(r -> r.get(OBJECT_ID)).toList(),
                List.of("resource-2"));

        var numberResult = queryResource("tags eq 1");
        assertEquals(numberResult.size(), 2);
        assertEquals(numberResult.stream().map(r -> r.get(OBJECT_ID)).toList(),
                List.of("resource-3", "resource-4"));

        var booleanResult = queryResource("tags eq true");
        assertEquals(booleanResult.size(), 1);
        assertEquals(booleanResult.stream().map(r -> r.get(OBJECT_ID)).toList(),
                List.of("resource-5"));

        var noResult = queryResource("tags eq \"hello\"");
        assertEquals(noResult.size(), 0);
    }

}
