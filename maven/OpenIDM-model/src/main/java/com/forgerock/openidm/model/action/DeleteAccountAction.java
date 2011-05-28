/*
 *
 * Copyright (c) 2010 ForgeRock Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1.php or
 * OpenIDM/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at OpenIDM/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2010 [name of copyright owner]"
 *
 * $Id$
 */
package com.forgerock.openidm.model.action;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.model.AbstractAction;
import com.forgerock.openidm.model.SynchronizationException;
import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ScriptsType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import com.forgerock.openidm.xml.ns._public.provisioning.provisioning_1.FaultMessage;
import javax.xml.ws.Holder;

/**
 *
 * @author Vilo Repan
 */
public class DeleteAccountAction extends AbstractAction {

    private static Trace trace = TraceManager.getTrace(DeleteAccountAction.class);

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        try {
            ScriptsType scripts = getScripts(change.getResource());
            getProvisioning().deleteObject(change.getShadow().getOid(), scripts, new Holder<OperationalResultType>());
        } catch (FaultMessage ex) {
            ResourceType resource = change.getResource();
            String resourceName = resource == null ? "Undefined" : resource.getName();
            trace.error("Couldn't delete resource object with oid '{}' on resource '{}'.",
                    new Object[]{change.getShadow().getOid(), resourceName});
            throw new SynchronizationException("Couldn't delete resource object with oid '" +
                    change.getShadow().getOid() + "' on resource '" + resourceName + "'.", ex, ex.getFaultInfo());
        }

        return userOid;
    }
}
