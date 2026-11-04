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
 * Portions Copyright 2020-2026 Wren Security
 */
package org.forgerock.openidm.felix.webconsole;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.felix.webconsole.spi.SecurityProvider;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.EnhancedConfig;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.crypto.CryptoService;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceVendor;

/**
 * Security provider the Felix web console delegates authentication to.
 * <p>
 * The web console is disabled unless the configuration sets {@code "enabled" : true}. Only an enabled provider is
 * registered with the identifier {@value #SECURITY_PROVIDER_ID} that the framework property
 * {@code felix.webconsole.security.providers} requires, so a disabled web console is not registered at all. Without
 * that framework property, a disabled provider still takes precedence over the built-in Felix provider and rejects
 * all requests with {@code 404 Not Found}.
 */
@Component(
        name = WebConsoleSecurityProviderService.PID,
        immediate = true,
        service = {},
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@ServiceVendor(ServerConstants.SERVER_VENDOR_NAME)
@ServiceDescription("OpenIDM Felix Web Console Security Provider")
public class WebConsoleSecurityProviderService implements SecurityProvider {

    public static final String PID = "org.forgerock.openidm.felix.webconsole";
    /** Service property identifying a web console security provider */
    public static final String SECURITY_PROVIDER_ID_PROPERTY = "webconsole.security.provider.id";
    /** Identifier of this security provider */
    public static final String SECURITY_PROVIDER_ID = "wrenidm";
    private static final String ENABLED = "enabled";
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String AUTHENTICATED = "authenticated";
    private static final String BASIC_AUTHORIZATION_HEADER_PREFIX = "Basic ";

    /** Enhanced configuration service. */
    @Reference(policy = ReferencePolicy.DYNAMIC)
    private volatile EnhancedConfig enhancedConfig;

    @Reference
    private CryptoService cryptoService;

    private String userId;
    private JsonValue password;
    private volatile boolean enabled;
    private ServiceRegistration<SecurityProvider> registration;

    void bindEnhancedConfig(EnhancedConfig enhancedConfig) {
        this.enhancedConfig = enhancedConfig;
    }

    void bindCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Activate
    public void activate(ComponentContext context) {
        final Dictionary<String, Object> dict = context.getProperties();
        final String servicePid = (String) dict.get(Constants.SERVICE_PID);

        final JsonValue config = enhancedConfig.getConfiguration(dict, servicePid, false);
        userId = config.get(USER_NAME).asString();
        password = config.get(PASSWORD);
        enabled = config.get(ENABLED).defaultTo(false).asBoolean();

        final Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_VENDOR, ServerConstants.SERVER_VENDOR_NAME);
        if (enabled) {
            properties.put(SECURITY_PROVIDER_ID_PROPERTY, SECURITY_PROVIDER_ID);
        }
        registration = context.getBundleContext().registerService(SecurityProvider.class, this, properties);
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        enabled = false;
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    // TODO Enhance this to use CAF?
    @Override
    public Object authenticate(final HttpServletRequest request, final HttpServletResponse response) {
        if (!enabled) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                // Ignore - response already committed or connection closed
            }
            return null;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BASIC_AUTHORIZATION_HEADER_PREFIX)) {
            String[] credentials = new String(Base64.getDecoder().decode(authHeader.substring(
                    BASIC_AUTHORIZATION_HEADER_PREFIX.length()))).split(":", 2);
            if (credentials.length < 2) {
                return null;
            }
            String username = credentials[0];
            String password = credentials[1];
            if (username != null && password != null && username.equals(userId) && password.equals(
                    cryptoService.decryptIfNecessary(this.password).asString())) {
              return json(object(field(AUTHENTICATED, true))).asMap();
            }
            return null;
        }
        try {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Wren:IDM Felix Web Console\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IOException e) {
            // Ignore - response already committed or connection closed
        }
        return null;
    }

    @Override
    public boolean authorize(final Object user, final String role) {
        // accept all roles while the web console is enabled
        return enabled;
    }

    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
    }
}
