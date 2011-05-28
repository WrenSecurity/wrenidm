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
import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.diff.DiffException;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ActivationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ScriptsType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import com.forgerock.openidm.xml.ns._public.provisioning.provisioning_1.FaultMessage;
import javax.xml.ws.Holder;

/**
 *
 * @author Vilo Repan
 */
public class DisableAccountAction extends AbstractAction {

    private static Trace trace = TraceManager.getTrace(DisableAccountAction.class);

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        if (!(change.getShadow() instanceof AccountShadowType)) {
            throw new SynchronizationException("Resource object is not account (class '" +
                    AccountShadowType.class + "'), but it's '" + change.getShadow().getClass() + "'.");
        }

        AccountShadowType account = (AccountShadowType) change.getShadow();
        ActivationType activation = account.getActivation();
        if (activation == null) {
            ObjectFactory of = new ObjectFactory();
            activation = of.createActivationType();
            account.setActivation(activation);
        }

        activation.setEnabled(false);

        try {
            ObjectContainerType container = getProvisioning().getObject(account.getOid(),
                    new PropertyReferenceListType(), new Holder<OperationalResultType>());
            AccountShadowType oldAccount = (AccountShadowType) container.getObject();

            ObjectModificationType changes = CalculateXmlDiff.calculateChanges(oldAccount, account);
            ScriptsType scripts = getScripts(change.getResource());
            getProvisioning().modifyObject(changes, scripts, new Holder<OperationalResultType>());
        } catch (DiffException ex) {
            trace.error("Couldn't disable account {}, error while creating diff: {}.", new Object[]{
                        account.getOid(), ex.getMessage()});
            throw new SynchronizationException("Couldn't disable account " + account.getOid() +
                    ", error while creating diff: " + ex.getMessage() + ".", ex);
        } catch (FaultMessage ex) {
            trace.error("Couldn't update (disable) account '{}' in provisioning, reason: {}.",
                    new Object[]{account.getOid(), getMessage(ex)});
            throw new SynchronizationException("Couldn't update (disable) account '" + account.getOid() +
                    "' in provisioning, reason: " + getMessage(ex) + ".", ex, ex.getFaultInfo());
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
