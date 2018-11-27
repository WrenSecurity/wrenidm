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
 */
package org.forgerock.openidm.felix.webconsole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.json;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.forgerock.http.util.Json;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.JSONEnhancedConfig;
import org.forgerock.openidm.crypto.CryptoService;
import org.mockito.ArgumentMatchers;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WebConsoleSecurityProviderServiceTest {

    private static final String FELIX_WEBCONSOLE_JSON_CONFIG = "/felix.webconsole.json";
    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD = "password";
    private static final String INCORRECT_USERNAME = "incorrectUsername";
    private static final String INCORRECT_PASSWORD = "incorrectPassword";

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
                createWebConsoleSecurityProviderService(CORRECT_PASSWORD);

        // when
        final Object user = webConsoleSecurityProviderService.authenticate(username, password);

        // then
        if (valid) {
            assertThat(user).isNotNull();
        } else {
            assertThat(user).isNull();
        }
    }

    private WebConsoleSecurityProviderService createWebConsoleSecurityProviderService(final String password)
            throws IOException {
        final WebConsoleSecurityProviderService webConsoleSecurityProviderService =
                new WebConsoleSecurityProviderService();
        final JSONEnhancedConfig jsonEnhancedConfig = mock(JSONEnhancedConfig.class);
        final CryptoService cryptoService = mock(CryptoService.class);
        ComponentContext context = mock(ComponentContext.class);
        when(cryptoService.decryptIfNecessary(any(JsonValue.class))).thenReturn(json(password));
        when(context.getProperties()).thenReturn(new Hashtable<String, Object>());
        when(jsonEnhancedConfig.getConfiguration(ArgumentMatchers.<Hashtable<String, Object>>any(), ArgumentMatchers.<String>any(), anyBoolean()))
                .thenReturn(getConfiguration(FELIX_WEBCONSOLE_JSON_CONFIG));
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
