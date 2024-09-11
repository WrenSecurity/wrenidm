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
 * Copyright 2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.identity;

import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.interceptor.EngineConfigurationConstants;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.forgerock.json.resource.ConnectionFactory;

public class IdmEngineConfigurator extends org.flowable.idm.engine.configurator.IdmEngineConfigurator {

    private ConnectionFactory connectionFactory;

    public IdmEngineConfigurator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void beforeInit(AbstractEngineConfiguration engineConfiguration) {
        // Nothing to do
    }

    @Override
    public void configure(AbstractEngineConfiguration engineConfiguration) {
        super.configure(engineConfiguration);

        getIdmEngineConfiguration(engineConfiguration)
            .setIdmIdentityService(new IdmIdentityService(connectionFactory, idmEngineConfiguration));
    }

    protected static IdmEngineConfiguration getIdmEngineConfiguration(AbstractEngineConfiguration engineConfiguration) {
        return (IdmEngineConfiguration) engineConfiguration.getEngineConfigurations()
                .get(EngineConfigurationConstants.KEY_IDM_ENGINE_CONFIG);
    }

}
