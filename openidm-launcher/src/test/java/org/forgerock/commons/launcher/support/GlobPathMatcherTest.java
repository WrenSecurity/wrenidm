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
package org.forgerock.commons.launcher.support;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import org.testng.annotations.Test;

/**
 * {@link GlobPathMatcher} test case.
 *
 * <p>
 * This class serves mainly as a template of what one can expect to work when matching path patterns.
 */
public class GlobPathMatcherTest {

    @Test
    public void testSimpleMatch() {
        var matcher = new GlobPathMatcher("hello.txt");
        assertTrue(matcher.matches(Path.of("hello.txt")));
        assertTrue(matcher.matches(Path.of("HELLO.TXT")));
        assertFalse(matcher.matches(Path.of("greetings/hello.txt")));
        assertFalse(matcher.matches(Path.of("goodbye.txt")));
    }

    @Test
    public void testDirectoryMatch() {
        var matcher = new GlobPathMatcher("**/hello.txt");
        assertTrue(matcher.matches(Path.of("hello.txt")));
        assertTrue(matcher.matches(Path.of("foo/hello.txt")));
        assertTrue(matcher.matches(Path.of("foo/bar/hello.txt")));
        assertFalse(matcher.matches(Path.of("foo/bar/goodbye.txt")));
    }

    @Test
    public void testWildcardMatch() {
        var matcher = new GlobPathMatcher("greetings/**/*.txt");
        assertTrue(matcher.matches(Path.of("greetings/hello.txt")));
        assertTrue(matcher.matches(Path.of("greetings/foo/hello.txt")));
        assertFalse(matcher.matches(Path.of("greetings/bar/hello.jar")));
        assertFalse(matcher.matches(Path.of("foo/bar/hello.txt")));
    }

    @Test
    public void testCaseInsensitivity() {
        var matcher = new GlobPathMatcher("foo/*.txt");
        assertTrue(matcher.matches(Path.of("foo/hello.txt")));
        assertTrue(matcher.matches(Path.of("FOO/hello.txt")));
        assertTrue(matcher.matches(Path.of("foo/hello.TXT")));
    }

}
