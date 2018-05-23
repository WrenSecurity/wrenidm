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
package org.forgerock.openidm.provisioner.openicf;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.*;

import java.net.URI;

/**
 * @version $Revision$ $Date$
 */
public interface OperationHelper {
    /**
     * Gets the {@link ObjectClass} value of this instance.
     *
     * @return
     */
    ObjectClass getObjectClass();

    /**
     * Checks the {@code operation} permission before execution.
     *
     * @param   operation
     *          The operation being checked for permission.
     *
     * @return  Several possible values:
     *          <dl>
     *            <dt>{@code true}</dt>
     *            <dd>If the operation is permitted.</dd>
     *
     *            <dt>{@code false}</dt>
     *            <dd>If the operation is not permitted, and the resource does
     *                not throw exceptions when operations are not permitted.
     *            </dd>
     *            </dl>
     *
     * @throws  ResourceException
     *          If the operation is not permitted and the resource throws
     *          exceptions when operations are not permitted.
     */
    boolean isOperationPermitted(Class<? extends APIOperation> operation) throws ResourceException;

    /**
     * Gets a new instance of {@link OperationOptionsBuilder} filled with {@link OperationOptions}.
     *
     * @param operation
     * @param connectorObject
     * @param source
     * @return
     * @throws Exception
     */
    OperationOptionsBuilder getOperationOptionsBuilder(Class<? extends APIOperation> operation, ConnectorObject connectorObject, JsonValue source) throws Exception;

    /**
     * Generate the fully qualified id from unqualified object {@link Uid}
     * <p>
     * The result id will be system/{@code [endSystemName]}/{@code [objectType]}/{@code [escapedObjectId]}
     *
     * @param uid original un escaped unique identifier of the object
     * @return
     */
    URI resolveQualifiedId(Uid uid);

    ConnectorObject build(Class<? extends APIOperation> operation, JsonValue source) throws Exception;

    /**
     * Build a new Map object from the {@code source} object.
     * <p>
     * This class uses the embedded schema to convert the {@code source}.
     *
     * @param source
     * @return
     * @throws Exception
     */
    JsonValue build(ConnectorObject source) throws Exception;
}
