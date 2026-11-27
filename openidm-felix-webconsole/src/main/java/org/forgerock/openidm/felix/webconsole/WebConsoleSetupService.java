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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.EnhancedConfig;
import org.forgerock.openidm.core.IdentityServer;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.crypto.CryptoService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the Felix web console from the bundles in the {@value #CONSOLE_DIRECTORY} directory of the installation.
 * <p>
 * The web console bundles are not started by the launcher. When the configuration sets {@code "enabled" : true}, this
 * component installs them, registers the {@link WebConsoleSecurityProvider} and only then starts them (transiently),
 * so the web console never runs without the provider. When the component is deactivated - the web console is
 * disabled, the configuration changes or is removed - the web console bundles are stopped before the provider is
 * unregistered.
 * <p>
 * The web console bundles stay installed when they are stopped: the security provider class is wired to the web
 * console SPI package of the installed bundle, so replacing the bundles requires a restart.
 */
@Component(
        name = WebConsoleSetupService.PID,
        immediate = true,
        service = {},
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@ServiceVendor(ServerConstants.SERVER_VENDOR_NAME)
@ServiceDescription("Wren:IDM Felix Web Console Setup")
public class WebConsoleSetupService {

    public static final String PID = "org.forgerock.openidm.felix.webconsole";

    /** Directory of the web console bundles, relative to the installation directory. */
    static final String CONSOLE_DIRECTORY = "console";

    /** Symbolic name of the web console bundle, which must be present and started. */
    static final String WEB_CONSOLE_SYMBOLIC_NAME = "org.apache.felix.webconsole";

    /** Identifier of the security provider, as required by {@value #FRAMEWORK_PROP_SECURITY_PROVIDERS}. */
    static final String SECURITY_PROVIDER_ID = "wrenidm";

    /** Framework property listing the security providers the web console requires. */
    static final String FRAMEWORK_PROP_SECURITY_PROVIDERS = "felix.webconsole.security.providers";

    private static final String ENABLED = "enabled";
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";

    private static final Logger logger = LoggerFactory.getLogger(WebConsoleSetupService.class);

    /** Enhanced configuration service. */
    @Reference(policy = ReferencePolicy.DYNAMIC)
    private volatile EnhancedConfig enhancedConfig;

    @Reference
    private CryptoService cryptoService;

    /** Web console bundles, in the order they were installed. */
    private final List<Bundle> consoleBundles = new ArrayList<>();

    private ServiceRegistration<?> providerRegistration;

    void bindEnhancedConfig(EnhancedConfig enhancedConfig) {
        this.enhancedConfig = enhancedConfig;
    }

    void bindCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Activate
    public synchronized void activate(ComponentContext context) {
        final Dictionary<String, Object> dict = context.getProperties();
        final String servicePid = (String) dict.get(Constants.SERVICE_PID);

        final JsonValue config = enhancedConfig.getConfiguration(dict, servicePid, false);
        if (!config.get(ENABLED).defaultTo(false).asBoolean()) {
            logger.info("Felix web console is disabled");
            return;
        }
        final String userId = config.get(USER_NAME).asString();
        final JsonValue password = config.get(PASSWORD);
        if (userId == null || userId.isEmpty() || password.isNull()
                || (password.isString() && password.asString().isEmpty())) {
            throw new IllegalStateException("Felix web console is enabled without a user name and password");
        }

        final BundleContext bundleContext = context.getBundleContext();
        try {
            setUp(bundleContext, userId, password);
        } catch (RuntimeException e) {
            tearDown();
            throw e;
        }
    }

    @Deactivate
    public synchronized void deactivate(ComponentContext context) {
        tearDown();
    }

    /**
     * Returns the directory with the web console bundles.
     *
     * @return the web console bundle directory
     */
    File getConsoleDirectory() {
        return IdentityServer.getFileForInstallPath(CONSOLE_DIRECTORY);
    }

    private void setUp(BundleContext bundleContext, String userId, JsonValue password) {
        warnIfProviderNotRequired(bundleContext);

        installBundles(bundleContext);
        final Bundle webConsole = consoleBundles.stream()
                .filter(bundle -> WEB_CONSOLE_SYMBOLIC_NAME.equals(bundle.getSymbolicName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Felix web console bundle "
                        + WEB_CONSOLE_SYMBOLIC_NAME + " not found in " + getConsoleDirectory()));

        resolveBundles(bundleContext);
        if (webConsole.getState() == Bundle.INSTALLED) {
            throw new IllegalStateException("Felix web console bundle " + webConsole.getLocation()
                    + " cannot be resolved");
        }

        // The SPI package is imported dynamically from the web console bundle resolved above
        try {
            providerRegistration = WebConsoleSecurityProvider.register(bundleContext, userId, password, cryptoService);
        } catch (LinkageError e) {
            throw new IllegalStateException("Felix web console security provider cannot be registered", e);
        }

        for (Bundle bundle : consoleBundles) {
            if (isFragment(bundle)) {
                continue;
            }
            try {
                bundle.start(Bundle.START_TRANSIENT);
            } catch (BundleException | RuntimeException e) {
                if (bundle == webConsole) {
                    throw new IllegalStateException("Felix web console bundle " + bundle.getLocation()
                            + " cannot be started", e);
                }
                logger.warn("Felix web console bundle {} cannot be started", bundle.getLocation(), e);
            }
        }
        logger.info("Felix web console is enabled");
    }

    private void installBundles(BundleContext bundleContext) {
        final File directory = getConsoleDirectory();
        final File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            throw new IllegalStateException("Felix web console bundle directory " + directory + " not found");
        }
        Arrays.sort(files);

        final Integer startLevel = getStartLevel(bundleContext.getBundle());
        for (File file : files) {
            final String location = file.toURI().toString();
            try {
                // returns the bundle already installed from the location, if any
                final Bundle bundle = bundleContext.installBundle(location);
                if (startLevel != null) {
                    final BundleStartLevel bundleStartLevel = bundle.adapt(BundleStartLevel.class);
                    if (bundleStartLevel != null) {
                        bundleStartLevel.setStartLevel(startLevel);
                    }
                }
                consoleBundles.add(bundle);
            } catch (BundleException | RuntimeException e) {
                logger.warn("Felix web console bundle {} cannot be installed", location, e);
            }
        }
    }

    private void resolveBundles(BundleContext bundleContext) {
        final FrameworkWiring frameworkWiring =
                bundleContext.getBundle(Constants.SYSTEM_BUNDLE_LOCATION).adapt(FrameworkWiring.class);
        if (frameworkWiring != null) {
            frameworkWiring.resolveBundles(consoleBundles);
        }
    }

    /**
     * Stops all web console bundles, then unregisters the security provider.
     * <p>
     * The bundles are stopped persistently, so that they are never started automatically when the framework restarts
     * from its bundle cache, even if they were started persistently by other means.
     */
    private void tearDown() {
        final List<Bundle> bundles = new ArrayList<>(consoleBundles);
        Collections.reverse(bundles);
        for (Bundle bundle : bundles) {
            if (isFragment(bundle) || bundle.getState() == Bundle.UNINSTALLED) {
                continue;
            }
            try {
                bundle.stop();
            } catch (BundleException | RuntimeException e) {
                logger.error("Felix web console bundle {} cannot be stopped", bundle.getLocation(), e);
            }
        }
        consoleBundles.clear();

        if (providerRegistration != null) {
            try {
                providerRegistration.unregister();
            } catch (IllegalStateException e) {
                // already unregistered
            }
            providerRegistration = null;
        }
    }

    private static void warnIfProviderNotRequired(BundleContext bundleContext) {
        final String required = bundleContext.getProperty(FRAMEWORK_PROP_SECURITY_PROVIDERS);
        if (required == null || Arrays.stream(required.split(","))
                .map(String::trim)
                .noneMatch(SECURITY_PROVIDER_ID::equals)) {
            logger.warn("Framework property {} does not require the security provider '{}'; "
                    + "add it to conf/config.properties", FRAMEWORK_PROP_SECURITY_PROVIDERS,
                    SECURITY_PROVIDER_ID);
        }
    }

    private static Integer getStartLevel(Bundle bundle) {
        final BundleStartLevel startLevel = bundle != null ? bundle.adapt(BundleStartLevel.class) : null;
        return startLevel != null ? startLevel.getStartLevel() : null;
    }

    private static boolean isFragment(Bundle bundle) {
        final BundleRevision revision = bundle.adapt(BundleRevision.class);
        return revision != null && (revision.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
    }
}
