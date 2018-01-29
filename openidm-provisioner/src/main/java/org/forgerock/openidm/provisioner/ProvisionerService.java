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

import org.forgerock.services.context.Context;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceException;

import java.util.Map;

/**
 * Minimum behavior for a provisioner used to provision resources to external sources.
 */
public interface ProvisionerService {
    
    String ROUTER_PREFIX = "/system";

    /**
     * Gets the unique {@link SystemIdentifier} of this instance.
     * <p>
     * The service which refers to this service instance can distinguish between multiple instances
     * by this value.
     *
     * @return The provisioner's system identifier.
     */
    SystemIdentifier getSystemIdentifier();

    /**
     * Gets a brief stats report about the current status of this service instance.
     * <p>
     * TODO Provide a sample object
     *
     * @param   context
     *          The request's Context in case the status report operation needs to perform a router
     *          request.
     *
     * @return  The provisioner's status.
     */
    Map<String, Object> getStatus(Context context);
    
    /**
     * Tests a configuration for a connector.
     *
     * @param   config
     *          The config to test.
     *
     * @return  The result of the test.
     */
    Map<String, Object> testConfig(JsonValue config);

    /**
     * Synchronise changes in the end system for the given {@code objectType}.
     * <p>
     * OpenIDM takes an active role in the synchronisation process by asking the end system to get
     * all of the objects that have changed. Not all systems are capable of fulfilling this kind of
     * request. If the end system is capable, then the implementation sends each change to a new
     * request on the router and when it is finished, it returns a new <b>stage</b> object.
     * <p>
     * The {@code previousStage} object is the previously returned value of this method.
     * Unhandled exception will result in no updates to the staged object in repository.
     * <p>
     * All exceptions must be handled to save the newly-staged object.
     *
     * @param   context
     *          The request context associated with the invocation
     * @param   objectType
     *          The type of object being synchronized.
     * @param   previousStage
     *          The previously returned object. If {@code null}, then it's the first execution.
     *
     * @return  The new updated stage object. This will be the {@code previousStage} at next call.
     *
     * @throws  ResourceException
     *          If a failure occurs during live sync
     */
    JsonValue liveSynchronize(Context context, String objectType, JsonValue previousStage) throws ResourceException;
}
