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
import com.forgerock.openidm.model.xpath.SchemaHandling;
import com.forgerock.openidm.model.xpath.SchemaHandlingException;
import com.forgerock.openidm.util.DebugUtil;
import com.forgerock.openidm.util.patch.PatchException;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserTemplateType;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

/**
 *
 * @author Vilo Repan
 */
public class AddUserAction extends AbstractAction {

    private static Trace trace = TraceManager.getTrace(AddUserAction.class);

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        UserType userType = getUser(userOid);

        ObjectFactory of = new ObjectFactory();
        if (userType == null) {
            //user was not found, so create user
            userType = of.createUserType();
            UserTemplateType userTemplate = getUserTemplate();

            try {

                if (trace.isDebugEnabled()) {
                    trace.debug("Action:addUser: Resource Object Shadow before action: {}", DebugUtil.toReadableString(shadowAfterChange));
                }
                userType = getSchemaHandling().applyInboundSchemaHandlingOnUser(userType, shadowAfterChange);

                if (trace.isDebugEnabled()) {
                    trace.debug("Action:addUser: User after processing of inbound expressions: {}",DebugUtil.toReadableString(userType));
                }

                //apply user template
                userType = getSchemaHandling().applyUserTemplate(userType, userTemplate);

                if (trace.isDebugEnabled()) {
                    trace.debug("Action:addUser: User after processing of user template: {}",DebugUtil.toReadableString(userType));
                }

                //save user
                ObjectContainerType userContainer = of.createObjectContainerType();
                userContainer.setObject(userType);
                userOid = getModel().addObject(userContainer);
            } catch (com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage ex) {
                throw new SynchronizationException("Can't save user", ex, ex.getFaultInfo());
            } catch (SchemaHandlingException ex) {
                throw new SynchronizationException("Couldn't apply user template '" +
                        userTemplate.getOid() + "' on user '" + userOid + "'.", ex, ex.getFaultType());
            } catch (PatchException ex) {
                throw new SynchronizationException("Couldn't apply user template '" +
                        userTemplate.getOid() + "' on user '" + userOid + "'.", ex, null);
            }
        } else {
            trace.debug("User already exists ({}), skipping create.", userType.getOid());
        }

        return userOid;
    }

    private String getUserTemplateOid() {
        List<Object> parameters = getParameters();
        Element userTemplateRef = null;
        for (Object object : parameters) {
            if (!(object instanceof Element)) {
                continue;
            }
            Element element = (Element) object;
            if ("userTemplateRef".equals(element.getLocalName()) &&
                    SchemaConstants.NS_C.equals(element.getNamespaceURI())) {
                userTemplateRef = element;
                break;
            }
        }

        if (userTemplateRef != null) {
            return userTemplateRef.getAttribute("oid");
        }

        return null;
    }

    private UserTemplateType getUserTemplate() throws SynchronizationException {
        String userTemplateOid = getUserTemplateOid();
        if (userTemplateOid == null) {
            throw new SynchronizationException("User Template Oid not defined in parameters for this action.");
        }

        UserTemplateType userTemplate = null;
        try {
            ObjectContainerType container = getModel().getObject(userTemplateOid, new PropertyReferenceListType());
            userTemplate = (UserTemplateType) container.getObject();
        } catch (FaultMessage ex) {
            throw new SynchronizationException("Couldn't get user template with oid '" +
                    userTemplateOid + "'.", ex, ex.getFaultInfo());
        }

        return userTemplate;
    }
}
