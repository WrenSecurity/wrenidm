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
 * Copyright 2023 Wren Security. All rights reserved.
 */
package org.forgerock.openidm.repo.util;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * {@link TokenHandler} test cases.
 */
public class TokenHandlerTest {

    @DataProvider
    public Object[][] extractTokensData() {
        return new Object[][] {
            { "HELLO ${WORLD}", List.of("WORLD") }
        };
    }

    @Test(dataProvider = "extractTokensData")
    public void testExtractTokens(String statement, List<String> expected) {
        TokenHandler tokenHandler = new TokenHandler();
        assertEquals(tokenHandler.extractTokens("HELLO ${WORLD}"), expected);
    }

    @DataProvider
    public Object[][] replaceTokensData() {
        return new Object[][] {
            { "HELLO ${WORLD}", new String[0], "HELLO ?" },
            { "FOO ${list:FOO} BAR ${BAR}", new String[] { "list" }, "FOO ${list:FOO} BAR ?" },
            { "${START} ${MIDDLE} ${FINISH}", new String[0], "? ? ?" },
            { "${START} MIDDLE ${FINISH}", new String[0], "? MIDDLE ?" },
        };
    }

    @Test(dataProvider = "replaceTokensData")
    public void testReplaceTokens(String statement, String[] excludes, String expected) {
        TokenHandler tokenHandler = new TokenHandler();
        assertEquals(tokenHandler.replaceTokens(statement, "?", excludes), expected);
    }

    @DataProvider
    public Object[][] replaceSomeTokensData() {
        return new Object[][] {
            { "HELLO ${WORLD}", Map.of("WORLD", "UNIVERSE"), "HELLO UNIVERSE" },
            { "HELLO ${WORLD}", Map.of("FOO", "BAR"), "HELLO ${WORLD}" }
        };
    }

    @Test(dataProvider = "replaceSomeTokensData")
    public void testReplaceSomeTokens(String statement, Map<String, String> replacements, String expected) {
        TokenHandler tokenHandler = new TokenHandler();
        assertEquals(tokenHandler.replaceSomeTokens(statement, replacements), expected);
    }

    @DataProvider
    public Object[][] replaceListTokensData() {
        return new Object[][] {
            { "foo IN (${bar})", Map.of("bar", 1), "foo IN (?)" },
            { "foo IN (${bar})", Map.of("bar", 2), "foo IN (?, ?)" },
            { "foo IN (${bar})", Map.of(), "foo IN (${bar})" }
        };
    }

    @Test(dataProvider = "replaceListTokensData")
    public void testReplaceListTokens(String statement, Map<String, Integer> counts, String expected) {
        TokenHandler tokenHandler = new TokenHandler();
        assertEquals(tokenHandler.replaceListTokens(statement, counts, "?"), expected);
    }

}
