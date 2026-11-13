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
 * Copyright 2026 Wren Security. All rights reserved.
 */
package org.forgerock.openidm.felix.webconsole;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.felix.webconsole.spi.SecurityProvider;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.EnhancedConfig;
import org.forgerock.openidm.crypto.CryptoService;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WebConsoleSetupServiceTest {

    private static final String WEB_CONSOLE_JAR = "org.apache.felix.webconsole-5.0.18.jar";
    private static final String PLUGIN_JAR = "org.apache.felix.webconsole.plugins.ds-2.3.0.jar";

    private Path consoleDirectory;
    private BundleContext bundleContext;
    private FrameworkWiring frameworkWiring;
    private ServiceRegistration<?> registration;
    private Bundle webConsole;
    private Bundle plugin;

    @BeforeMethod
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setUp() throws Exception {
        consoleDirectory = Files.createTempDirectory("console");
        Files.createFile(consoleDirectory.resolve(WEB_CONSOLE_JAR));
        Files.createFile(consoleDirectory.resolve(PLUGIN_JAR));
        Files.createFile(consoleDirectory.resolve("README.txt"));

        bundleContext = mock(BundleContext.class);
        when(bundleContext.getProperty(WebConsoleSetupService.FRAMEWORK_PROP_SECURITY_PROVIDERS))
                .thenReturn(WebConsoleSetupService.SECURITY_PROVIDER_ID);

        final Bundle self = mockBundle("self");
        when(bundleContext.getBundle()).thenReturn(self);

        final Bundle systemBundle = mock(Bundle.class);
        frameworkWiring = mock(FrameworkWiring.class);
        when(systemBundle.adapt(FrameworkWiring.class)).thenReturn(frameworkWiring);
        when(bundleContext.getBundle(Constants.SYSTEM_BUNDLE_LOCATION)).thenReturn(systemBundle);

        webConsole = mockBundle(WebConsoleSetupService.WEB_CONSOLE_SYMBOLIC_NAME);
        plugin = mockBundle("org.apache.felix.webconsole.plugins.ds");
        when(bundleContext.installBundle(location(WEB_CONSOLE_JAR))).thenReturn(webConsole);
        when(bundleContext.installBundle(location(PLUGIN_JAR))).thenReturn(plugin);

        registration = mock(ServiceRegistration.class);
        when(bundleContext.registerService(eq(SecurityProvider.class), any(SecurityProvider.class),
                ArgumentMatchers.<Dictionary<String, Object>>any())).thenReturn((ServiceRegistration) registration);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        for (File file : consoleDirectory.toFile().listFiles()) {
            Files.delete(file.toPath());
        }
        Files.delete(consoleDirectory);
    }

    @Test
    public void testDisabledDoesNotInstallConsole() throws Exception {
        // when
        activate(json(object(field("enabled", false), field("username", "admin"), field("password", "secret"))));
        activate(json(object(field("username", "admin"), field("password", "secret"))));

        // then
        verify(bundleContext, never()).installBundle(any(String.class));
        verify(bundleContext, never()).registerService(eq(SecurityProvider.class), any(SecurityProvider.class),
                ArgumentMatchers.<Dictionary<String, Object>>any());
    }

    @Test
    public void testEnabledRegistersProviderBeforeStartingConsole() throws Exception {
        // when
        activate(enabledConfig());

        // then
        final InOrder inOrder = inOrder(bundleContext, frameworkWiring, webConsole, plugin);
        inOrder.verify(bundleContext).installBundle(location(WEB_CONSOLE_JAR));
        inOrder.verify(frameworkWiring).resolveBundles(anyCollection());
        inOrder.verify(bundleContext).registerService(eq(SecurityProvider.class), any(SecurityProvider.class),
                ArgumentMatchers.<Dictionary<String, Object>>any());
        inOrder.verify(webConsole).start(Bundle.START_TRANSIENT);
        verify(plugin).start(Bundle.START_TRANSIENT);
        verify(bundleContext, never()).installBundle(location("README.txt"));
    }

    @Test
    public void testDeactivateStopsConsoleBeforeUnregisteringProvider() throws Exception {
        // given
        final WebConsoleSetupService service = activate(enabledConfig());

        // when
        service.deactivate(null);

        // then
        final InOrder inOrder = inOrder(webConsole, plugin, registration);
        inOrder.verify(plugin).stop();
        inOrder.verify(webConsole).stop();
        inOrder.verify(registration).unregister();
    }

    @Test
    public void testMissingWebConsoleBundleFails() throws Exception {
        // given
        Files.delete(consoleDirectory.resolve(WEB_CONSOLE_JAR));

        // then
        assertThatThrownBy(() -> activate(enabledConfig())).isInstanceOf(IllegalStateException.class);
        verifyConsoleNotStarted();
    }

    @Test
    public void testUnresolvedWebConsoleBundleFails() throws Exception {
        // given
        when(webConsole.getState()).thenReturn(Bundle.INSTALLED);

        // then
        assertThatThrownBy(() -> activate(enabledConfig())).isInstanceOf(IllegalStateException.class);
        verifyConsoleNotStarted();
    }

    @Test
    public void testWebConsoleStartFailureStopsConsoleAndUnregistersProvider() throws Exception {
        // given
        doThrow(new BundleException("failure")).when(webConsole).start(anyInt());

        // then
        assertThatThrownBy(() -> activate(enabledConfig())).isInstanceOf(IllegalStateException.class);
        final InOrder inOrder = inOrder(plugin, webConsole, registration);
        inOrder.verify(plugin).stop();
        inOrder.verify(webConsole).stop();
        inOrder.verify(registration).unregister();
    }

    @Test
    public void testPluginStartFailureDoesNotFail() throws Exception {
        // given
        doThrow(new BundleException("failure")).when(plugin).start(anyInt());

        // when
        activate(enabledConfig());

        // then
        verify(webConsole).start(Bundle.START_TRANSIENT);
        verify(registration, never()).unregister();
    }

    @Test
    public void testEnabledWithoutPasswordFails() throws Exception {
        assertThatThrownBy(() -> activate(json(object(field("enabled", true), field("username", "admin"),
                field("password", ""))))).isInstanceOf(IllegalStateException.class);
        verify(bundleContext, never()).installBundle(any(String.class));
    }

    @Test
    public void testMissingConsoleDirectoryFails() throws Exception {
        // given
        tearDown();

        // then
        assertThatThrownBy(() -> activate(enabledConfig())).isInstanceOf(IllegalStateException.class);
        Files.createDirectory(consoleDirectory);
    }

    private void verifyConsoleNotStarted() throws BundleException {
        verify(bundleContext, never()).registerService(eq(SecurityProvider.class), any(SecurityProvider.class),
                ArgumentMatchers.<Dictionary<String, Object>>any());
        verify(webConsole, never()).start(anyInt());
        verify(plugin, never()).start(anyInt());
    }

    private WebConsoleSetupService activate(JsonValue config) {
        final EnhancedConfig enhancedConfig = mock(EnhancedConfig.class);
        when(enhancedConfig.getConfiguration(ArgumentMatchers.<Dictionary<String, Object>>any(),
                ArgumentMatchers.<String>any(), anyBoolean())).thenReturn(config);
        final ComponentContext context = mock(ComponentContext.class);
        when(context.getProperties()).thenReturn(new Hashtable<>());
        when(context.getBundleContext()).thenReturn(bundleContext);

        final WebConsoleSetupService service = new WebConsoleSetupService() {
            @Override
            File getConsoleDirectory() {
                return consoleDirectory.toFile();
            }
        };
        service.bindEnhancedConfig(enhancedConfig);
        service.bindCryptoService(mock(CryptoService.class));
        service.activate(context);
        return service;
    }

    private static JsonValue enabledConfig() {
        return json(object(field("enabled", true), field("username", "admin"), field("password", "secret")));
    }

    private String location(String fileName) {
        return consoleDirectory.resolve(fileName).toFile().toURI().toString();
    }

    private static Bundle mockBundle(String symbolicName) {
        final Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn(symbolicName);
        when(bundle.getState()).thenReturn(Bundle.RESOLVED);
        when(bundle.adapt(BundleStartLevel.class)).thenReturn(mock(BundleStartLevel.class));
        return bundle;
    }
}
