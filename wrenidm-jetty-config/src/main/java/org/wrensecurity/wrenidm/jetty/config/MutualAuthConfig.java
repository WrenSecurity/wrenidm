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

import org.apache.felix.http.jetty.ConnectorFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.forgerock.openidm.util.ConfigPropertyUtil;

/**
 * Required mTLS connector factory.
 */
public class MutualAuthConfig implements ConnectorFactory {

    @Override
    public ServerConnector createConnector(Server server) {
        String mtlsPort = ConfigPropertyUtil.getProperty("openidm.port.mutualauth", false);

        // 1. SSL context (keystore, passwords, etc.)
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(ConfigPropertyUtil.getPathProperty("openidm.keystore.location"));
        sslContextFactory.setKeyStoreType(ConfigPropertyUtil.getProperty("openidm.keystore.type", false));
        sslContextFactory.setKeyStorePassword(ConfigPropertyUtil.getProperty("openidm.keystore.password", false));
        sslContextFactory.setTrustStorePath(ConfigPropertyUtil.getPathProperty("openidm.truststore.location"));
        sslContextFactory.setTrustStoreType(ConfigPropertyUtil.getProperty("openidm.truststore.type", false));
        sslContextFactory.setTrustStorePassword(ConfigPropertyUtil.getProperty("openidm.truststore.password", false));
        sslContextFactory.setNeedClientAuth(true);

        // 2. HTTP configuration for TLS
        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // 3. Create the actual Connector
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(httpsConfig));
        connector.setPort(Integer.valueOf(mtlsPort));
        connector.setName("mutual-tls");

        return connector;
    }

}
