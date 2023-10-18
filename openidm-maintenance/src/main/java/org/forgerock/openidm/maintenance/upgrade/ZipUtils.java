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
package org.forgerock.openidm.maintenance.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Zip file manipulation methods.
 */
public class ZipUtils {

    /**
     * Extract the contents of the specified zip file to the specified target directory.
     * @param zipFilePath Zip file to unzip. Never null.
     * @param targetDir Target directory. Never null.
     */
    public static void unzipFile(Path zipFilePath, Path targetDir) {
        unzipFile(zipFilePath, null, targetDir);
    }

    /**
     * Extract the contents of the specified zip file to the specified target directory.
     * @param zipFilePath Zip file to unzip. Never null.
     * @param fileMatcher Matcher to restrict extracted files. Can be null.
     * @param targetDir Target directory. Never null.
     */
    public static void unzipFile(Path zipFilePath, PathMatcher fileMatcher, Path targetDir) {
        if (!Files.isDirectory(targetDir)) {
            throw new IllegalArgumentException("Invalid target directory to unzip file.");
        }
        try (ZipFile zipFile = new ZipFile(zipFilePath.toString())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (fileMatcher != null && !fileMatcher.matches(Path.of(entry.getName()))) {
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(targetDir.resolve(entry.getName()));
                } else {
                    Files.createDirectories(targetDir.resolve(entry.getName()).getParent());
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Files.copy(inputStream, targetDir.resolve(entry.getName()));
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to unzip '" + zipFilePath + "' file.", e);
        }
    }

}
