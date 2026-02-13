/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2018-2026 Wren Security.
 */
package org.forgerock.openidm.servlet.internal;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility for rendering custom error pages from the {@code ui/errors/} directory.
 * Removes sensitive information from HTTP error responses.
 */
public final class ErrorPageHandler {

    private ErrorPageHandler() {
    }

    /**
     * Outputs an error page, corresponding to pages stored in the {@code ui/errors/} directory within OpenIDM's root
     * directory. Status codes map to HTML file-names, or to the default HTML page.
     * <p>For example,</p>
     * <ul>
     * <li>ui/errors/404.html</li>
     * <li>ui/errors/default.html</li>
     * </ul>
     *
     * @param request Servlet HTTP request
     * @param response Servlet HTTP response
     * @throws IOException I/O error
     */
    public static void outputErrorPageResponse(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status == null) {
            status = 500;
        }

        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        response.setStatus(status);

        Path path = Paths.get(String.format("ui/errors/%1$d.html", status));
        if (!Files.exists(path)) {
            path = Paths.get("ui/errors/default.html");
            if (Files.notExists(path)) {
                // no error page exists, so return a blank page (has HTTP status code)
                response.setContentLength(0);
                return;
            }
        }

        final ServletOutputStream output = response.getOutputStream();
        try (final FileInputStream input = new FileInputStream(path.toFile())) {
            final FileChannel channel = input.getChannel();
            final byte[] buffer = new byte[8 * 1024];
            final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            int length;
            while ((length = channel.read(byteBuffer)) != -1) {
                output.write(buffer, 0, length);
                byteBuffer.clear();
            }
        }
        response.flushBuffer();
    }
}
