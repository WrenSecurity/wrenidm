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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.felix.webconsole.spi.SecurityProvider;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.crypto.CryptoService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * Security provider the Felix web console delegates authentication to.
 * <p>
 * This is the only class of the bundle that refers to the web console SPI. The SPI package is exported by the web
 * console bundle, which {@link WebConsoleSetupService} installs at runtime, so the package is imported dynamically and
 * this class must not be loaded before the web console bundle is installed and resolved.
 */
final class WebConsoleSecurityProvider implements SecurityProvider {

    private static final String AUTHENTICATED = "authenticated";
    private static final String BASIC_AUTHORIZATION_HEADER_PREFIX = "Basic ";

    private final String userId;
    private final JsonValue password;
    private final CryptoService cryptoService;

    WebConsoleSecurityProvider(String userId, JsonValue password, CryptoService cryptoService) {
        this.userId = userId;
        this.password = password;
        this.cryptoService = cryptoService;
    }

    /**
     * Registers a new security provider service.
     * <p>
     * The provider is registered with the highest service ranking: the web console uses the highest ranked provider,
     * and falls back to its built-in provider (registered with the lowest ranking) only when there is no other.
     *
     * @param context bundle context to register the service with
     * @param userId the user name accepted by the provider
     * @param password the (possibly encrypted) password accepted by the provider
     * @param cryptoService crypto service decrypting the password
     * @return the service registration
     */
    static ServiceRegistration<?> register(BundleContext context, String userId, JsonValue password,
            CryptoService cryptoService) {
        final Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_VENDOR, ServerConstants.SERVER_VENDOR_NAME);
        properties.put(Constants.SERVICE_DESCRIPTION, "Wren:IDM Felix Web Console Security Provider");
        properties.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        properties.put(SecurityProvider.PROPERTY_ID, WebConsoleSetupService.SECURITY_PROVIDER_ID);
        return context.registerService(SecurityProvider.class,
                new WebConsoleSecurityProvider(userId, password, cryptoService), properties);
    }

    // TODO Enhance this to use CAF?
    @Override
    public Object authenticate(final HttpServletRequest request, final HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BASIC_AUTHORIZATION_HEADER_PREFIX)) {
            final String[] credentials;
            try {
                credentials = new String(Base64.getDecoder().decode(authHeader.substring(
                        BASIC_AUTHORIZATION_HEADER_PREFIX.length())), StandardCharsets.UTF_8).split(":", 2);
            } catch (IllegalArgumentException e) {
                return null;
            }
            if (credentials.length < 2) {
                return null;
            }
            final String expectedPassword = cryptoService.decryptIfNecessary(password).asString();
            if (expectedPassword != null && credentials[0].equals(userId) && MessageDigest.isEqual(
                    credentials[1].getBytes(StandardCharsets.UTF_8), expectedPassword.getBytes(StandardCharsets.UTF_8))) {
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
