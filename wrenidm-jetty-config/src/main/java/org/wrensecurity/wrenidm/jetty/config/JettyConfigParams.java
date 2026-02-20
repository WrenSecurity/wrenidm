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
 * System property wrapper for Http Service and Jetty specific parameters.
 */
public class JettyConfigParams {

    private JettyConfigParams() {
    }

    public static String getHttpPort() {
        return ConfigPropertyUtil.getProperty("openidm.port.http", false);
    }

    public static String getHttpsPort() {
        return ConfigPropertyUtil.getProperty("openidm.port.https", false);
    }

    public static String getMutualAuthPort() {
        return ConfigPropertyUtil.getProperty("openidm.port.mutualauth", false);
    }

    public static String getKeyStorePath() {
        return ConfigPropertyUtil.getPathProperty("openidm.keystore.location");
    }

    public static String getKeyStoreType() {
        return ConfigPropertyUtil.getProperty("openidm.keystore.type", false);
    }

    public static String getKeyStorePassword() {
        return ConfigPropertyUtil.getProperty("openidm.keystore.password", false);
    }

    public static String getTrustStorePath() {
        return ConfigPropertyUtil.getPathProperty("openidm.truststore.location");
    }

    public static String getTrustStoreType() {
        return ConfigPropertyUtil.getProperty("openidm.truststore.type", false);
    }

    public static String getTrustStorePassword() {
        return ConfigPropertyUtil.getProperty("openidm.truststore.password", false);
    }

    public static String getDefaultCertAlias() {
        return ConfigPropertyUtil.getProperty("openidm.https.keystore.cert.alias", false);
    }

}
