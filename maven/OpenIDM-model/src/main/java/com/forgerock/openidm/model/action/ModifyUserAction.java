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
import com.forgerock.openidm.model.*;
import com.forgerock.openidm.model.xpath.SchemaHandlingException;
import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.diff.DiffException;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectChangeDeletionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Vilo Repan
 */
public class ModifyUserAction extends AbstractAction {

    private static transient Trace logger = TraceManager.getTrace(ModifyUserAction.class);

//    @Autowired
//    private SchemaHandling schemaHandling;

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        UserType userType = getUser(userOid);
        if (userType == null) {
            throw new SynchronizationException("Can't find user with oid '" + userOid + "'.");
        }

        // As this implementation is in fact diffing user before change and after change,
        // it can easily be applied to modification and addintion.
        // However, this is wrong. This approach may be appropriate for addition.
        // But for modification we should be a bit smarter and process only the list of
        // attributes that were really changed.

        if (change.getObjectChange() instanceof ObjectChangeDeletionType) {
            throw new SynchronizationException("The modifyUser action cannot be applied to deletion.");
        }

        try {
            UserType oldUserType = (UserType) JAXBUtil.clone(userType);

            userType = getSchemaHandling().applyInboundSchemaHandlingOnUser(userType, shadowAfterChange);
            ObjectFactory of = new ObjectFactory();
            ObjectContainerType userContainer = of.createObjectContainerType();
            userContainer.setObject(userType);

            ObjectModificationType modification = CalculateXmlDiff.calculateChanges(oldUserType, userType);
            if (modification != null && modification.getOid() != null) {
                getModel().modifyObject(modification);
            } else {
                logger.warn("Diff returned null for changes of user {}, caused by shadow {}", userType.getOid(),
                        shadowAfterChange.getOid());
            }
        } catch (SchemaHandlingException ex) {
            throw new SynchronizationException("Can't handle inbound section in schema handling", ex);
        } catch (com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage ex) {
            throw new SynchronizationException("Can't save user", ex, ex.getFaultInfo());
        } catch (DiffException ex) {
            throw new SynchronizationException("Can't save user. Unexpected error: " +
                    "Couldn't create create diff.", ex, null);
        } catch (JAXBException ex) {
            throw new SynchronizationException("Couldn't clone user object '" + userOid + "', reason: " +
                    ex.getMessage(), ex);
        }

        return userOid;
    }
}
