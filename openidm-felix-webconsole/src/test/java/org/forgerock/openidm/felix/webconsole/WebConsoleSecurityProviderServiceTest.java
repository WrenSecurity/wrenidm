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
 * Portions Copyright 2026 Wren Security
 */
package org.forgerock.openidm.felix.webconsole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.json;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.webconsole.spi.SecurityProvider;

import org.forgerock.http.util.Json;
import org.forgerock.json.JsonValue;
import org.forgerock.json.JsonValueException;
import org.forgerock.openidm.config.enhanced.JSONEnhancedConfig;
import org.forgerock.openidm.crypto.CryptoService;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WebConsoleSecurityProviderServiceTest {

    private static final String FELIX_WEBCONSOLE_JSON_CONFIG = "/felix.webconsole.json";
    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD = "password";
    private static final String INCORRECT_USERNAME = "incorrectUsername";
    private static final String INCORRECT_PASSWORD = "incorrectPassword";

    private BundleContext bundleContext;
    private ServiceRegistration<SecurityProvider> registration;

    /**
     * Creates a credentials provider to test all the possible credential cases. The format of the data provider is:
     * <pre>
     *     {username , password, [true|false]}
     *
     *     true if the username and password combination is valid, false otherwise.
     * </pre>
     * @return
     */
    @DataProvider(name = "credentialsData")
    public Object[][] credentials() {
        return new Object[][] {
                { CORRECT_USERNAME, INCORRECT_PASSWORD, false },
                { INCORRECT_USERNAME, CORRECT_PASSWORD, false },
                { CORRECT_USERNAME, CORRECT_PASSWORD, true}
        };
    }

    @Test(dataProvider = "credentialsData")
    public void testAuthenticateWithCredentials(String username, String password, boolean valid)
            throws IOException {
        // given
        final WebConsoleSecurityProviderService webConsoleSecurityProviderService =
                createWebConsoleSecurityProviderService(getConfiguration(FELIX_WEBCONSOLE_JSON_CONFIG));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        when(request.getHeader(eq("Authorization"))).thenReturn(authHeader);

        // when
        final Object user = webConsoleSecurityProviderService.authenticate(request, response);

        // then
        if (valid) {
            assertThat(user).isNotNull();
        } else {
            assertThat(user).isNull();
        }
        assertThat(getRegisteredProviderId(webConsoleSecurityProviderService))
                .isEqualTo(WebConsoleSecurityProviderService.SECURITY_PROVIDER_ID);
    }

    @DataProvider(name = "enabledData")
    public Object[][] enabledData() {
        return new Object[][] {
                { true, true },
                { false, false },
                { null, false }
        };
    }

    @Test(dataProvider = "enabledData")
    public void testEnabledFlag(Boolean enabled, boolean expectEnabled) throws IOException {
        // given
        final JsonValue config = getConfiguration(FELIX_WEBCONSOLE_JSON_CONFIG);
        config.remove("enabled");
        if (enabled != null) {
            config.put("enabled", enabled);
        }
        final WebConsoleSecurityProviderService webConsoleSecurityProviderService =
                createWebConsoleSecurityProviderService(config);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (CORRECT_USERNAME + ":" + CORRECT_PASSWORD).getBytes());
        when(request.getHeader(eq("Authorization"))).thenReturn(authHeader);

        // when
        final Object user = webConsoleSecurityProviderService.authenticate(request, response);

        // then
        final Object providerId = getRegisteredProviderId(webConsoleSecurityProviderService);
        if (expectEnabled) {
            assertThat(user).isNotNull();
            assertThat(webConsoleSecurityProviderService.authorize(user, "any")).isTrue();
            assertThat(providerId).isEqualTo(WebConsoleSecurityProviderService.SECURITY_PROVIDER_ID);
        } else {
            assertThat(user).isNull();
            assertThat(webConsoleSecurityProviderService.authorize(new Object(), "any")).isFalse();
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
            assertThat(providerId).isNull();
        }
    }

    @Test(expectedExceptions = JsonValueException.class)
    public void testEnabledFlagRejectsNonBooleanValue() throws IOException {
        // given
        final JsonValue config = getConfiguration(FELIX_WEBCONSOLE_JSON_CONFIG);
        config.put("enabled", "true");

        // when
        createWebConsoleSecurityProviderService(config);
    }

    @Test
    public void testDeactivateUnregistersProvider() throws IOException {
        // given
        final WebConsoleSecurityProviderService webConsoleSecurityProviderService =
                createWebConsoleSecurityProviderService(getConfiguration(FELIX_WEBCONSOLE_JSON_CONFIG));

        // when
        webConsoleSecurityProviderService.deactivate(null);

        // then
        verify(registration).unregister();
        assertThat(webConsoleSecurityProviderService.authorize(new Object(), "any")).isFalse();
    }

    @SuppressWarnings("unchecked")
    private Object getRegisteredProviderId(final WebConsoleSecurityProviderService service) {
        final ArgumentCaptor<Dictionary<String, Object>> properties = ArgumentCaptor.forClass(Dictionary.class);
        verify(bundleContext).registerService(eq(SecurityProvider.class), eq(service), properties.capture());
        return properties.getValue().get(WebConsoleSecurityProviderService.SECURITY_PROVIDER_ID_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    private WebConsoleSecurityProviderService createWebConsoleSecurityProviderService(final JsonValue config) {
        bundleContext = mock(BundleContext.class);
        registration = mock(ServiceRegistration.class);
        when(bundleContext.registerService(eq(SecurityProvider.class), any(SecurityProvider.class),
                ArgumentMatchers.<Dictionary<String, Object>>any())).thenReturn(registration);
        final WebConsoleSecurityProviderService webConsoleSecurityProviderService =
                new WebConsoleSecurityProviderService();
        final JSONEnhancedConfig jsonEnhancedConfig = mock(JSONEnhancedConfig.class);
        final CryptoService cryptoService = mock(CryptoService.class);
        ComponentContext context = mock(ComponentContext.class);
        when(cryptoService.decryptIfNecessary(any(JsonValue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(context.getProperties()).thenReturn(new Hashtable<String, Object>());
        when(context.getBundleContext()).thenReturn(bundleContext);
        when(jsonEnhancedConfig.getConfiguration(ArgumentMatchers.<Hashtable<String, Object>>any(), ArgumentMatchers.<String>any(), anyBoolean()))
                .thenReturn(config);
        webConsoleSecurityProviderService.bindCryptoService(cryptoService);
        webConsoleSecurityProviderService.bindEnhancedConfig(jsonEnhancedConfig);
        webConsoleSecurityProviderService.activate(context);
        return webConsoleSecurityProviderService;
    }

    private JsonValue getConfiguration(final String configFile) throws IOException {
        try(final InputStream config = getClass().getResourceAsStream(configFile)) {
            return json(Json.readJsonLenient(config));
        }
    }
}
