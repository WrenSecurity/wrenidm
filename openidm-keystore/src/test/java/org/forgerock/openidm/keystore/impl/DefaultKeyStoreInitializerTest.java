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
 * Portions Copyright 2018-2021 Wren Security.
 */

package org.forgerock.openidm.keystore.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.openidm.core.IdentityServer.CONFIG_CRYPTO_ALIAS;
import static org.forgerock.openidm.core.IdentityServer.CONFIG_CRYPTO_ALIAS_SELF_SERVICE;
import static org.forgerock.openidm.core.IdentityServer.KEYSTORE_LOCATION;
import static org.forgerock.openidm.core.IdentityServer.KEYSTORE_PASSWORD;
import static org.forgerock.openidm.core.IdentityServer.KEYSTORE_PROVIDER;
import static org.forgerock.openidm.core.IdentityServer.KEYSTORE_TYPE;
import static org.forgerock.openidm.core.IdentityServer.TRUSTSTORE_LOCATION;
import static org.forgerock.openidm.core.IdentityServer.TRUSTSTORE_PASSWORD;
import static org.forgerock.openidm.core.IdentityServer.TRUSTSTORE_TYPE;
import static org.forgerock.openidm.core.ServerConstants.JWTSESSION_SIGNING_KEY_ALIAS_PROPERTY;
import static org.forgerock.openidm.core.ServerConstants.SELF_SERVICE_CERT_ALIAS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.Security;
import java.util.Collections;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.forgerock.openidm.core.IdentityServer;
import org.forgerock.openidm.core.IdentityServerTestUtils;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.keystore.KeyStoreDetails;
import org.forgerock.openidm.keystore.KeyStoreService;
import org.forgerock.security.keystore.KeyStoreType;
import org.forgerock.util.Utils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultKeyStoreInitializerTest {

    @BeforeClass
    public void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        IdentityServerTestUtils.initInstanceForTest(this.getClass());
    }

    @Test
    public void testInitializeKeyStore() throws Exception {
        // given
        createKeyStores();
        final DefaultKeyStoreInitializer defaultKeyStoreInitializer = new DefaultKeyStoreInitializer();
        final KeyStoreDetails keyStoreDetails = createKeyStoreDetails();

        // when
        final KeyStore keyStore = defaultKeyStoreInitializer.initializeKeyStore(keyStoreDetails);

        // then
        assertThat(keyStore).isNotNull();
        assertThat(Collections.list(keyStore.aliases()))
                .asList()
                .hasSize(4)
                .contains(
                        IdentityServer.getInstance().getProperty(CONFIG_CRYPTO_ALIAS),
                        IdentityServer.getInstance().getProperty(CONFIG_CRYPTO_ALIAS_SELF_SERVICE),
                        IdentityServer.getInstance().getProperty(
                                JWTSESSION_SIGNING_KEY_ALIAS_PROPERTY,
                                ServerConstants.DEFAULT_JWTSESSION_SIGNING_KEY_ALIAS),
                        SELF_SERVICE_CERT_ALIAS
                );

    }

    @Test
    public void testInitializeTrustStore() throws Exception {
        // given
        createKeyStores();
        final DefaultKeyStoreInitializer defaultKeyStoreInitializer = new DefaultKeyStoreInitializer();
        final KeyStoreDetails keyStoreDetails = createKeyStoreDetails();
        final KeyStore keyStore = defaultKeyStoreInitializer.initializeKeyStore(keyStoreDetails);

        final KeyStoreService keyStoreService = mock(KeyStoreService.class);
        when(keyStoreService.getKeyStore()).thenReturn(keyStore);

        // when
        final KeyStore trustStore = defaultKeyStoreInitializer.initializeTrustStore(keyStoreService, keyStoreDetails);

        // then
        final String alias = IdentityServer.getInstance().getProperty(
                IdentityServer.HTTPS_KEYSTORE_CERT_ALIAS, "openidm-localhost");
        assertThat(keyStore).isNotNull();
        assertThat(trustStore).isNotNull();
        assertThat(Collections.list(trustStore.aliases()))
                .asList()
                .hasSize(5)
                .contains(alias);
        assertThat(Collections.list(keyStore.aliases())).asList().contains(alias);

    }

    private void createKeyStores() throws Exception {
        createKeyStore(IdentityServer.getFileForPath(IdentityServer.getInstance().getProperty(KEYSTORE_LOCATION)));
        createTrustStore(IdentityServer.getFileForPath(IdentityServer.getInstance().getProperty(TRUSTSTORE_LOCATION)));
    }

    private void createKeyStore(final File keystoreFile) throws Exception {
        keystoreFile.deleteOnExit();
        if (keystoreFile.exists()) {
            keystoreFile.delete();
        }

        keystoreFile.getParentFile().mkdirs();
        assertThat(keystoreFile.createNewFile()).isTrue().as("Unable to create keystore file");
        try (final OutputStream outputStream = new FileOutputStream(keystoreFile)) {
            final KeyStore keyStore =
                    KeyStore.getInstance(IdentityServer.getInstance().getProperty(KEYSTORE_TYPE));
            keyStore.load(null, IdentityServer.getInstance().getProperty(KEYSTORE_PASSWORD).toCharArray());
            keyStore.store(
                    outputStream,
                    IdentityServer.getInstance().getProperty(KEYSTORE_PASSWORD).toCharArray()
            );
        }
    }

    private void createTrustStore(final File keystoreFile) throws Exception {
        keystoreFile.deleteOnExit();
        if (keystoreFile.exists()) {
            keystoreFile.delete();
        }

        keystoreFile.getParentFile().mkdirs();
        assertThat(keystoreFile.createNewFile()).isTrue().as("Unable to create keystore file");
        try (final OutputStream outputStream = new FileOutputStream(keystoreFile)) {
            final KeyStore keyStore =
                    KeyStore.getInstance(IdentityServer.getInstance().getProperty(TRUSTSTORE_TYPE));
            keyStore.load(null, IdentityServer.getInstance().getProperty(TRUSTSTORE_PASSWORD).toCharArray());
            keyStore.store(
                    outputStream,
                    IdentityServer.getInstance().getProperty(TRUSTSTORE_PASSWORD).toCharArray()
            );
        }
    }

    private KeyStoreDetails createKeyStoreDetails() {
        return new KeyStoreDetails(
                Utils.asEnum(IdentityServer.getInstance().getProperty(KEYSTORE_TYPE), KeyStoreType.class),
                IdentityServer.getInstance().getProperty(KEYSTORE_PROVIDER),
                IdentityServer.getFileForPath(IdentityServer.getInstance().getProperty(KEYSTORE_LOCATION)).getAbsolutePath(),
                IdentityServer.getInstance().getProperty(KEYSTORE_PASSWORD));
    }
}
