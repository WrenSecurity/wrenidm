/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2015 ForgeRock AS. All rights reserved.
 * Portions Copyright 2018 Wren Security.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.provisioner;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceException;

import java.util.Map;

/**
 * Helper methods which assist with generating connector configuration. These methods can be called
 * to validate connector configurations as well as check which connectors are available for configuration.
 * After each phase of generated configuration the user can customize the properties generated and have
 * them validated.
 */
public interface ConnectorConfigurationHelper {

    /** configuration property that describes the connector's meta-data */
    String CONNECTOR_REF = "connectorRef";
    /** configuration property that holds the actual connector configuration (passed to the connector) */
    String CONFIGURATION_PROPERTIES = "configurationProperties";
    /** the connector name (within connectorRef) */
    String CONNECTOR_NAME = "connectorName";
    /** configuration property that holds the connector object type detail */
    String OBJECT_TYPES = "objectTypes";

    /**
     * Return the provisioner type that is used to manage configuration/connectors created by this configuration helper.
     *
     * @return the configuration system type.
     */
    String getProvisionerType();

    /**
     * Test the given configuration.
     *
     * @param   params
     *          The configuration to test.
     *
     * @return  A map that contains the results of the test. The map includes the following keys:
     *          <dl>
     *            <dt>{@code "ok"}</dt><dd>A boolean indicating if the test was successful.</dd>
     *            <dt>{@code "name"}</dt><dd>The name of the connector.</dd>
     *            <dt>{@code "error"}</dt><dd>The error from the test, for non-fatal failures.</dd>
     *            <dt>{@code "reason"}</dt><dd>The reason the test succeeded despite errors.</dd>
     *          </dl>
     *
     * @throws  ResourceException
     *          When the test fails the {@link ResourceException#getDetail()}
     *          contains the detailed information.
     */
    Map<String, Object> test(JsonValue params) throws ResourceException;

    /**
     * Get available connectors from an installation
     *
     * @return  {@code JsonValue} containing all available connectors
     *
     * @throws  ResourceException
     *          If the list of connectors cannot be loaded.
     */
    JsonValue getAvailableConnectors() throws ResourceException;

    /**
     * Generate the core configuration
     *
     * The implementer of this method must ensure proper encryption/decryption
     * of private properties according to the schema definition.
     *
     * @param   params
     *          Loaded connector configuration.
     *
     * @return  Core connector configuration, with private values decrypted.
     *
     * @throws  ResourceException
     *          If a connector cannot be loaded / is not found.
     */
    JsonValue generateConnectorCoreConfig(JsonValue params) throws ResourceException;

    /**
     * Generate the full configuration
     *
     * The implementer of this method must ensure proper encryption/decryption
     * of private properties according to the schema definition.
     *
     * @param   params
     *          Connector configuration.
     *
     * @return  Full connector configuration.
     *
     * @throws  ResourceException
     *          If a connector cannot be loaded / is not found.
     */
    JsonValue generateConnectorFullConfig(JsonValue params) throws ResourceException;


}
