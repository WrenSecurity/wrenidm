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

import org.forgerock.openidm.util.ConfigPropertyUtil;

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
        setProperty("org.osgi.service.http.port",
                ConfigPropertyUtil.getProperty("openidm.port.http", false));
        setProperty("org.osgi.service.http.port.secure",
                ConfigPropertyUtil.getProperty("openidm.port.https", false));
        setProperty("org.apache.felix.https.keystore",
                ConfigPropertyUtil.getPathProperty("openidm.keystore.location"));
        setProperty("org.apache.felix.https.keystore.type",
                ConfigPropertyUtil.getProperty("openidm.keystore.type", false));
        setProperty("org.apache.felix.https.keystore.password",
                ConfigPropertyUtil.getProperty("openidm.keystore.password", false));
        setProperty("org.apache.felix.https.keystore.key.password",
                ConfigPropertyUtil.getProperty("openidm.keystore.password", false));
        setProperty("org.apache.felix.https.truststore",
                ConfigPropertyUtil.getPathProperty("openidm.truststore.location"));
        setProperty("org.apache.felix.https.truststore.password",
                ConfigPropertyUtil.getProperty("openidm.truststore.password", false));
        setProperty("org.apache.felix.https.clientcertificate", "wants");
    }

    private static void setProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        }
    }
}
