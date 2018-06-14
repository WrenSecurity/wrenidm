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
 * Copyright 2018 Wren Security.
 */

package org.forgerock.openidm.core;

import static org.forgerock.openidm.core.ServerConstants.LAUNCHER_INSTALL_LOCATION;
import static org.forgerock.openidm.core.ServerConstants.LAUNCHER_PROJECT_LOCATION;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A utility class for tests that depend on the state of
 * {@code org.forgerock.openidm.coreIdentityServer} to function.
 */
public final class IdentityServerTestUtils {
    /**
     * The name of the field in {@code IdentityServer} that contains the current server instance.
     */
    public static final String IDENTITY_SERVER_FIELD = "IDENTITY_SERVER";

    /**
     * Private constructor for utility class.
     */
    private IdentityServerTestUtils() {
    }

    /**
     * Attempts to initialize the global {@code IdentityServer} instance for the current test.
     *
     * <p>If the server is already initialized, no exception is thrown.
     *
     * <p>This should be called only once per test, typically in a method annotated with
     * {@link org.testng.annotations.BeforeClass} or {@link org.testng.annotations.BeforeTest}.
     */
    @SuppressWarnings("RedundantCast")
    public static void initInstanceForTest() {
        try {
            IdentityServer.initInstance((IdentityServer)null);
        } catch (final IllegalStateException e) {
            // tried to reinitialize; ignore
        }
    }

    /**
     * Attempts to initialize the global {@code IdentityServer} instance to use the root of the
     * test classpath as the project location ({@code launcher.project.location} and install
     * location ({@code launcher.install.location} properties.
     *
     * <p>This should be called only once per test, typically in a method annotated with
     * {@link org.testng.annotations.BeforeClass} or {@link org.testng.annotations.BeforeTest}.
     *
     * @param testClass
     *   The class containing the tests being run.
     *
     * @throws IllegalStateException
     *   If the server was already initialized with different paths for the project and/or
     *   IDM install folder than the root of the test classpath.
     */
    public static void initInstanceForTest(Class<?> testClass) {
        final Path classpathRoot;
        final String classpathRootAbsolute;

        try {
            classpathRoot = Paths.get(testClass.getClass().getResource("/").toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Failed to parse classpath", ex);
        }

        classpathRootAbsolute = classpathRoot.toFile().getAbsolutePath();

        initInstanceForTest(classpathRootAbsolute, classpathRootAbsolute);
    }

    /**
     * Attempts to initialize the global {@code IdentityServer} instance to use the specified
     * absolute paths for the project location ({@code launcher.project.location} and install
     * location ({@code launcher.install.location} properties.
     *
     * <p>This should be called only once per test, typically in a method annotated with
     * {@link org.testng.annotations.BeforeClass} or {@link org.testng.annotations.BeforeTest}.
     *
     * @param projectLocation
     *   The absolute path to the folder that should be used as the project location.
     * @param installLocation
     *   The absolute path to the folder that should be used as the IDM install location.
     *
     * @throws IllegalStateException
     *   If the server was already initialized with a different project or install location than
     *   what has been provided.
     */
    public static void initInstanceForTest(final String projectLocation, final String installLocation)
    throws IllegalStateException {
        System.setProperty(LAUNCHER_PROJECT_LOCATION, projectLocation);
        System.setProperty(LAUNCHER_INSTALL_LOCATION, installLocation);

        initInstanceForTest();

        verifyServerInitPaths(projectLocation, installLocation);
    }

    /**
     * Ensures that the {@code IdentityServer} has been initialized with a readable boot properties
     * file.
     */
    public static void verifyBootPropertiesLoaded() {
        final File bootPropertyFile = IdentityServer.getInstance().getBootPropertyFile();

        if ((bootPropertyFile == null) || !bootPropertyFile.canRead()) {
            throw new IllegalStateException(
                "No boot properties file was loaded, but one is required for this test.");
        }
    }

    /**
     * Creates a new instance of the {@code IdentityServer} that is initialized without any
     * properties.
     *
     * <p>This should only be used by tests that are trying to verify two instances are different.
     *
     * @return
     *   A new identity server instance.
     */
    public static IdentityServer createServerInstance() {
        return new IdentityServer(null);
    }

    /**
     * Resets the {@code IdentityServer} to its initial state -- without any server instance.
     *
     * <p>This should only be used to temporarily manipulate server instances during a single test.
     */
    public static void clearServerInitialization() {
        IdentityServer.clearInstance();
    }

    /**
     * Verifies that the {@code IdentityServer} was initialized with the specified project and
     * install locations.
     *
     * <p>This is used as a precaution to detect consistency issues in tests. Unfortunately, the
     * identity server cannot be reinitialized again once it's been initialized, so it
     * is conceivable that one set of tests could bash the state for subsequent tests.
     *
     * @param projectLocation
     *   The absolute path to the folder that the server should be using for the project location.
     * @param installLocation
     *   The absolute path to the folder that the server should be using for the install location.
     *
     * @throws IllegalStateException
     *   If the server was already initialized with a different project or install location than
     *   what has been provided.
     */
    public static void verifyServerInitPaths(final String projectLocation, final String installLocation)
    throws IllegalStateException {
        final IdentityServer server = IdentityServer.getInstance();
        final String initializedProjectLocation = server.getProjectLocation().getAbsolutePath();

        if (!initializedProjectLocation.equals(projectLocation)) {
            throw new IllegalStateException(
                String.format(
                    "Server already initialized with a different project path (initialized with "
                    + "'%s' but wanted '%s')", initializedProjectLocation, projectLocation));
        }

        final String initializedInstallLocation = server.getInstallLocation().getAbsolutePath();

        if (!initializedInstallLocation.equals(installLocation)) {
            throw new IllegalStateException(
                String.format(
                    "Server already initialized with a different install path (initialized with "
                    + "'%s' but wanted '%s')", initializedInstallLocation, installLocation));
        }
    }
}
