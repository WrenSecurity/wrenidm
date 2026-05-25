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
 * Copyright 2026 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.query;

import static org.forgerock.json.JsonPointer.ptr;
import static org.testng.Assert.assertEquals;

import org.forgerock.json.JsonPointer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * {@link SQLJSONUtils} test case.
 */
public class SQLJSONUtilsTest {

    @DataProvider
    public Object[][] toSqlJsonPathData(){
        return new Object[][] {
                { ptr(""), "$", "empty pointer" },
                { ptr("/"), "$", "root pointer" },
                { ptr("/foo/bar/baz"), "$.\"foo\".\"bar\".\"baz\"", "property chain" },
                { ptr("/foo/0"), "$.\"foo\"[0]", "valid zero index" },
                { ptr("/foo/012"), "$.\"foo\".\"012\"", "invalid zero-leading index" },
                { ptr("/foo/123"), "$.\"foo\"[123]", "multi-digit array index" },
                { ptr("/foo/123/456"), "$.\"foo\"[123][456]", "array index chain" },
                { ptr("/foo/123/bar"), "$.\"foo\"[123].\"bar\"", "property of array item" },
        };
    }

    @Test(dataProvider = "toSqlJsonPathData")
    public void testToSqlJsonPath(JsonPointer pointer, String output, String message) {
        assertEquals(SQLJSONUtils.toSqlJsonPath(pointer), output, message);
    }

}
