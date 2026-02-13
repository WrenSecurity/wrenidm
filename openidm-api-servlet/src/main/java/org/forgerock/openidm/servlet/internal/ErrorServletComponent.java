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
 * Portions Copyright 2020-2026 Wren Security.
 */

package org.forgerock.openidm.servlet.internal;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component to create and register an error-handler servlet via OSGi HTTP Whiteboard.
 */
@Component(
        name = ErrorServletComponent.PID,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        immediate = true)
public class ErrorServletComponent {

    static final String PID = "org.forgerock.openidm.error-servlet";

    private final static Logger logger = LoggerFactory.getLogger(ServletComponent.class);

    private ServiceRegistration<Servlet> errorServletRegistration;

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();

        HttpServlet errorServlet = new HttpServlet() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void service(final HttpServletRequest request, final HttpServletResponse response)
                    throws ServletException, IOException {
                ErrorPageHandler.outputErrorPageResponse(request, response);
            }
        };

        // Register error servlet via OSGi HTTP Whiteboard with error page properties
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("osgi.http.whiteboard.servlet.name", "ErrorServlet");
        props.put("osgi.http.whiteboard.servlet.errorPage", new String[] {
                "java.lang.Throwable",
                "400", "401", "403", "404", "405", "406", "408", "409", "410",
                "500", "501", "502", "503", "504"
        });
        errorServletRegistration = bundleContext.registerService(Servlet.class, errorServlet, props);
        logger.info("Registered error servlet via OSGi HTTP Whiteboard");
    }

    @Deactivate
    protected synchronized void deactivate(ComponentContext context) {
        if (errorServletRegistration != null) {
            errorServletRegistration.unregister();
            errorServletRegistration = null;
        }
    }

}
