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
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;

/**
 *
 * @author Vilo Repan
 */
public class AddAccountAction extends AbstractAction {

    private static Trace trace = TraceManager.getTrace(AddAccountAction.class);

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        if (!(change.getShadow() instanceof AccountShadowType)) {
            throw new SynchronizationException("Resource object is not account (class '" +
                    AccountShadowType.class + "'), but it's '" + change.getShadow().getClass() + "'.");
        }

        ObjectFactory of = new ObjectFactory();
        AccountShadowType account = (AccountShadowType) change.getShadow();

//account password generator
//        int randomPasswordLength = getRandomPasswordLength(account);
//        if (randomPasswordLength != -1) {
//            generatePassword(account, randomPasswordLength);
//        }
//account password generator end

//        UserType userType = getUser(userOid);
        Utils.unresolveResource(account);
        try {
//            trace.debug("Applying outbound schema handling on account '{}'.", account.getOid());
//            SchemaHandling util = new SchemaHandling();
//            util.setModel(getModel());
//            account = (AccountShadowType) util.applyOutboundSchemaHandlingOnAccount(userType, account);
//            ScriptsType scripts = getScripts(change.getResource());
            ObjectContainerType container = of.createObjectContainerType();
            container.setObject(account);
//
//            trace.debug("Adding account '{}' to provisioning.", account.getOid());
//            provisioning.addObject(container, scripts, new Holder<OperationalResultType>());
            getModel().addObject(container);
//        } catch (SchemaHandlingException ex) {
//            trace.error("Couldn't add account to provisioning: Couldn't apply resource outbound schema handling " +
//                    "(resource '{}') on account '{}', reason: {}", new Object[]{change.getResource().getOid(),
//                        account.getOid(), ex.getMessage()});
//            throw new SynchronizationException("Couldn't add account to provisioning: Couldn't apply resource " +
//                    "outbound schema handling (resource '" + change.getResource().getOid() + "') on account '" +
//                    account.getOid() + "', reason: " + ex.getMessage() + ".", ex.getFaultType());
        } catch (FaultMessage ex) {
            trace.error("Couldn't add account to provisioning, reason: " + getMessage(ex));
            throw new SynchronizationException("Can't add account to provisioning.", ex, ex.getFaultInfo());
        }

        return userOid;
    }

    private String getMessage(FaultMessage ex) {
        String message = null;
        if (ex.getFaultInfo() != null) {
            message = ex.getFaultInfo().getMessage();
        } else {
            message = ex.getMessage();
        }

        return message;
    }
}
