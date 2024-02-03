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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Glob based path matcher that emulates behavior of previously used {@code MatchPattern} from Plexus Utils.
 *
 * <p>
 * Matcher uses native JRE's path matcher provided by default {@code FileSystem} implementation. This means that
 * the behavior might be platform dependent depending on the used patterns.
 *
 * <p>
 * <b>CAUTION:</b> Plexus Utils' {@code MatchPattern} was matching simple filename patterns even in subdirectories.
 * This implementation is not matching subdirectories without path traversal wildcard <code>**&#47;</code>.
 */
public class GlobPathMatcher implements PathMatcher {

    private final List<PathMatcher> matchers;

    public GlobPathMatcher(String... pattern) {
        this(List.of(pattern));
    }

    public GlobPathMatcher(List<String> patterns) {
        var fileSystem = FileSystems.getDefault();
        this.matchers = patterns.stream()
            // make directory wildcard pattern optional and the pattern case insensitive
            .map(pattern -> pattern.replace("**/", "{**/,}").toLowerCase())
            .map(pattern -> fileSystem.getPathMatcher("glob:" + pattern))
            .collect(Collectors.toList());
    }

    @Override
    public boolean matches(Path path) {
        var normalized = Path.of(path.toString().toLowerCase());
        return matchers.stream().anyMatch(matcher -> matcher.matches(normalized));
    }

}
