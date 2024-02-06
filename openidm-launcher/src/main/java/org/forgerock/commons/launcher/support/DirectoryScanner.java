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

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simple file tree walker based on previously used Plexus Utils' {@code DirectoryScanner}.
 *
 * <p>
 * The implementation does not try to do anything clever to optimize path traversal (e.g. by prefix
 * matching to prevent walking into directories that can not contain matching files).
 */
public class DirectoryScanner {

    private PathMatcher includes;

    private PathMatcher excludes;

    public DirectoryScanner(List<String> includes, List<String> excludes) {
        this(includes != null ? new GlobPathMatcher(includes) : null,
                excludes != null ? new GlobPathMatcher(excludes) : null);
    }

    public DirectoryScanner(PathMatcher includes, PathMatcher excludes) {
        this.includes = includes != null ? includes : new GlobPathMatcher("**");
        this.excludes = excludes != null ? excludes : new GlobPathMatcher();
    }

    public List<Path> scan(Path start) throws IOException {
        return Files.walk(start, FileVisitOption.FOLLOW_LINKS)
                .map(start::relativize)
                .filter(includes::matches)
                .filter(Predicate.not(excludes::matches))
                .filter(Predicate.not(Files::isDirectory))
                .map(start::resolve)
                .collect(Collectors.toList());
    }

}
