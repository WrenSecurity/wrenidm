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

import com.forgerock.openidm.model.*;
import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.util.QNameUtil;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.diff.DiffException;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectReferenceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;

/**
 *
 * @author Vilo Repan
 */
public class LinkAccountAction extends AbstractAction {

    private static Trace trace = TraceManager.getTrace(LinkAccountAction.class);

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        UserType userType = getUser(userOid);
        UserType oldUserType = getUser(userOid);
        ResourceObjectShadowType resourceShadow = change.getShadow();

        if (userType != null) {
            if (!(resourceShadow instanceof AccountShadowType)) {
                throw new SynchronizationException("Can't link resource object of type '" +
                        resourceShadow.getClass() + "', only '" + AccountShadowType.class +
                        "' can be linked.");
            }

            ObjectReferenceType accountRef = new ObjectReferenceType();
            accountRef.setOid(resourceShadow.getOid());
            accountRef.setType(QNameUtil.uriToQName(Utils.getObjectType("AccountType")));
            userType.getAccountRef().add(accountRef);

            try {
                ObjectModificationType changes = CalculateXmlDiff.calculateChanges(oldUserType, userType);
                getModel().modifyObject(changes);
            } catch (com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage ex) {
                trace.error("Error while saving user {} (modifyObject on model).", new Object[]{userOid});
                throw new SynchronizationException("Can't link account. Can't save user",
                        ex, ex.getFaultInfo());
            } catch (DiffException ex) {
                trace.error("Couldn't create user diff for '{}', reason: {}.",
                        new Object[]{userOid, ex.getMessage()});
                throw new SynchronizationException("Couldn't create user diff for '" + userOid +
                        "', reason: " + ex.getMessage(), ex);
            }
        } else {
            throw new SynchronizationException("User with oid '" + userOid +
                    "' doesn't exits. Try insert create action before this action.");
        }

        return userOid;
    }
}
