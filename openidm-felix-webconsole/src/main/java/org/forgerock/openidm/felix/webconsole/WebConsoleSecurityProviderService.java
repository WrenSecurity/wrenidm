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
import org.apache.felix.webconsole.spi.SecurityProvider;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.EnhancedConfig;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.crypto.CryptoService;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceVendor;

/**
 * Creates a SecurityProvider service that the felix web console will use to delegate authentication attempts.
 */
@Component(
        name = WebConsoleSecurityProviderService.PID,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@ServiceVendor(ServerConstants.SERVER_VENDOR_NAME)
@ServiceDescription("OpenIDM Felix Web Console Security Provider")
public class WebConsoleSecurityProviderService implements SecurityProvider {

    public static final String PID = "org.forgerock.openidm.felix.webconsole";
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
    }

    // TODO Enhance this to use CAF?
    @Override
    public Object authenticate(final HttpServletRequest request, final HttpServletResponse response) {
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
        // accept all roles
        return true;
    }

    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
    }
}
