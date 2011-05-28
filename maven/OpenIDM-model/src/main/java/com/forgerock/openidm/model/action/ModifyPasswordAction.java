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
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectChangeModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.SynchronizationSituationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import com.forgerock.openidm.xml.schema.XPathSegment;
import com.forgerock.openidm.xml.schema.XPathType;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 *
 * @author Vilo Repan
 */
public class ModifyPasswordAction extends AbstractAction {

    private static Trace trace = TraceManager.getTrace(ModifyPasswordAction.class);

    @Override
    public String executeChanges(String userOid, ResourceObjectShadowChangeDescriptionType change,
            SynchronizationSituationType situation, ResourceObjectShadowType shadowAfterChange) throws SynchronizationException {
        UserType userType = getUser(userOid);
        if (userType == null) {
            throw new SynchronizationException("Can't find user with oid '" + userOid + "'.");
        }

        if (!(change.getObjectChange() instanceof ObjectChangeModificationType)) {
            throw new SynchronizationException("Object change is not instacne of " + ObjectChangeModificationType.class.getName());
        }

        PropertyModificationType pwd = getPasswordFromModification((ObjectChangeModificationType) change.getObjectChange());
        if (pwd == null) {
            trace.error("Couldn't find property modification with password change, returning.");
            return userOid;
        }

        try {
            ObjectModificationType changes = createPasswordModification(userType, pwd);
            getModel().modifyObjectWithExclusion(changes, change.getShadow().getOid());
        } catch (com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage ex) {
            throw new SynchronizationException("Can't save user", ex, ex.getFaultInfo());
        }

        return userOid;
    }

    private ObjectModificationType createPasswordModification(UserType user, PropertyModificationType password) {
        ObjectModificationType changes = new ObjectModificationType();
        changes.setOid(user.getOid());
        changes.getPropertyModification().add(password);

        return changes;
    }

    private PropertyModificationType getPasswordFromModification(ObjectChangeModificationType objectChange) {
        List<PropertyModificationType> list = objectChange.getObjectModification().getPropertyModification();
        for (PropertyModificationType propModification : list) {
            XPathType path = new XPathType(propModification.getPath());
            List<XPathSegment> segments = path.toSegments();
            if (segments.size() == 0 || !segments.get(0).getQName().equals(SchemaConstants.I_CREDENTIALS)) {
                continue;
            }

            PropertyModificationType.Value value = propModification.getValue();
            if (value == null) {
                continue;
            }
            List<Element> elements = value.getAny();
            for (Element element : elements) {
                if (SchemaConstants.I_PASSWORD.equals(new QName(element.getNamespaceURI(), element.getLocalName()))) {
                    return propModification;
                }
            }
        }

        return null;
    }
}
