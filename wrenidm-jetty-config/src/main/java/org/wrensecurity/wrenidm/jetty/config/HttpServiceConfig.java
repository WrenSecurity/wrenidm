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
package org.wrensecurity.wrenidm.jetty.config;

/**
 * Configures Felix HTTP Jetty by setting the required system properties.
 */
public final class HttpServiceConfig {

    private HttpServiceConfig() {
    }

    /**
     * Sets the Felix HTTP Jetty system properties for HTTP/HTTPS configuration.
     */
    public static void configureHttpServiceProperties() {
        setProperty("org.osgi.service.http.port", JettyConfigParams.getHttpPort());
    }

    private static void setProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        }
    }

}
