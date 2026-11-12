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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Dictionary;
import org.apache.felix.webconsole.spi.SecurityProvider;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.crypto.CryptoService;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WebConsoleSecurityProviderTest {

    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD = "password";
    private static final String INCORRECT_USERNAME = "incorrectUsername";
    private static final String INCORRECT_PASSWORD = "incorrectPassword";

    /**
     * Credentials in the format {@code {username, password, valid}}.
     *
     * @return the credentials
     */
    @DataProvider(name = "credentialsData")
    public Object[][] credentials() {
        return new Object[][] {
                { CORRECT_USERNAME, INCORRECT_PASSWORD, false },
                { INCORRECT_USERNAME, CORRECT_PASSWORD, false },
                { CORRECT_USERNAME, "", false },
                { CORRECT_USERNAME, CORRECT_PASSWORD, true }
        };
    }

    @Test(dataProvider = "credentialsData")
    public void testAuthenticateWithCredentials(String username, String password, boolean valid) {
        // given
        final SecurityProvider provider = createProvider();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader(eq("Authorization"))).thenReturn(basic(username + ":" + password));

        // when
        final Object user = provider.authenticate(request, response);

        // then
        if (valid) {
            assertThat(user).isNotNull();
            assertThat(provider.authorize(user, "any")).isTrue();
        } else {
            assertThat(user).isNull();
        }
    }

    @Test
    public void testAuthenticateWithMalformedHeader() {
        // given
        final SecurityProvider provider = createProvider();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        when(request.getHeader(eq("Authorization"))).thenReturn("Basic not base64!");
        final Object invalidEncoding = provider.authenticate(request, response);
        when(request.getHeader(eq("Authorization"))).thenReturn(basic(CORRECT_USERNAME));
        final Object missingPassword = provider.authenticate(request, response);

        // then
        assertThat(invalidEncoding).isNull();
        assertThat(missingPassword).isNull();
    }

    @Test
    public void testAuthenticateWithoutCredentialsRequestsThem() throws IOException {
        // given
        final SecurityProvider provider = createProvider();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        final Object user = provider.authenticate(request, response);

        // then
        assertThat(user).isNull();
        verify(response).setHeader(eq("WWW-Authenticate"), any(String.class));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRegisterWithProviderIdAndHighestRanking() {
        // given
        final BundleContext bundleContext = mock(BundleContext.class);

        // when
        WebConsoleSecurityProvider.register(bundleContext, CORRECT_USERNAME, json(CORRECT_PASSWORD),
                mock(CryptoService.class));

        // then
        final ArgumentCaptor<Dictionary<String, Object>> properties = ArgumentCaptor.forClass(Dictionary.class);
        verify(bundleContext).registerService(eq(SecurityProvider.class), any(WebConsoleSecurityProvider.class),
                properties.capture());
        assertThat(properties.getValue().get(SecurityProvider.PROPERTY_ID))
                .isEqualTo(WebConsoleSetupService.SECURITY_PROVIDER_ID);
        assertThat(properties.getValue().get(Constants.SERVICE_RANKING)).isEqualTo(Integer.MAX_VALUE);
    }

    private static SecurityProvider createProvider() {
        final CryptoService cryptoService = mock(CryptoService.class);
        when(cryptoService.decryptIfNecessary(any(JsonValue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return new WebConsoleSecurityProvider(CORRECT_USERNAME, json(CORRECT_PASSWORD), cryptoService);
    }

    private static String basic(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
