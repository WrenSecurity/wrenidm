/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License
 * for the specific language governing permission and limitations under the
 * License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each
 * file and include the License file at legal/CDDLv1.0.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2016 ForgeRock AS.
 * Portions Copyright 2018 Wren Security.
 */

package org.forgerock.openidm.provisioner.openicf;

import java.util.List;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.util.promise.Promise;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorInfo;

public interface ConnectorInfoProvider {
    /**
     * Resolves a reference to a connector to the information about the
     * connector itself.
     *
     * @param   connectorReference
     *          The reference to resolve.
     *
     * @return  Either info about the requested connector; or, {@code null} if
     *          there is no {@link ConnectorInfo} available.
     */
    ConnectorInfo findConnectorInfo(ConnectorReference connectorReference);

    /**
     * Asynchronously resolves a reference to a connector to the information
     * about the connector itself.
     *
     * @param   connectorReference
     *          The reference to resolve.
     *
     * @return  The promise that will resolve the request.
     */
    Promise<ConnectorInfo, RuntimeException> findConnectorInfoAsync(
                                                             ConnectorReference connectorReference);

    /**
     * Creates a {@link APIConfiguration ConnectorFacade} from a
     * {@link APIConfiguration Configuration} config.
     *
     * @param   configuration
     *          The configuration to use for the new facade.
     *
     * @return  ConnectorFacade created with the configuration
     */
    ConnectorFacade createConnectorFacade(APIConfiguration configuration);

    /**
     * Get all available {@link ConnectorInfo} from the local and the remote
     * {@link org.identityconnectors.framework.api.ConnectorInfoManager}s
     *
     * @return list of all available {@link ConnectorInfo}s
     */
    List<ConnectorInfo> getAllConnectorInfo();

    /**
     * Tests the {@link APIConfiguration Configuration} with the connector.
     *
     * @param   configuration
     *          The configuration to test.
     * @throws  ResourceException
     *          If an unexpected error occurs attempting to process the request.
     * @throws  RuntimeException
     *          If the configuration is not valid or the test failed.
     */
    void testConnector(APIConfiguration configuration)
    throws ResourceException, RuntimeException;

    /**
     * Initializes a new system object configuration for the specified
     * connector from the specified configuration.
     *
     * @param   connectorReference
     *          The connector for which a system object configuration is
     *          desired.
     * @param   configuration
     *          The configuration from which to initialize the system object
     *          configuration.
     *
     * @return  The system object configuration.
     *
     * @throws  ResourceException
     *          If an unexpected error occurs attempting to process the request.
     */
    JsonValue createSystemConfiguration(ConnectorReference connectorReference, APIConfiguration configuration) throws ResourceException;
}
