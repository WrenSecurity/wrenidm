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

import java.util.Hashtable;
import org.apache.felix.http.jetty.ConnectorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator that configures Felix HTTP Jetty properties and registers
 * the HTTPS and HTTPS+mTLS {@link ConnectorFactory} service.
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) {
        // Configure Felix HTTP Jetty system properties before the Jetty bundle starts
        HttpServiceConfig.configureHttpServiceProperties();

        // Register HTTPS conector factory
        String httpsPort = JettyConfigParams.getHttpsPort();
        if (httpsPort != null && !httpsPort.isBlank() && Integer.valueOf(httpsPort) > 0) {
            context.registerService(
                    ConnectorFactory.class.getName(),
                    new HttpsConnectorFactory("https", Integer.valueOf(httpsPort), false),
                    new Hashtable<String, String>());
        }

        // Register mTLS connector factory
        String mtlsPort = JettyConfigParams.getMutualAuthPort();
        if (mtlsPort != null && !mtlsPort.isBlank() && Integer.valueOf(mtlsPort) > 0) {
            context.registerService(
                    ConnectorFactory.class.getName(),
                    new HttpsConnectorFactory("mutualauth", Integer.valueOf(mtlsPort), true),
                    new Hashtable<String, String>());
        }
    }

    @Override
    public void stop(BundleContext context) {
        // Services are automatically unregistered when the bundle stops
    }

}
