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
package com.forgerock.openidm.model;

import com.forgerock.openidm.xml.ns._public.common.common_1.FaultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectNotFoundFaultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;

/**
 *
 * @author Vilo Repan
 */
class SynchronizationUtils {

    static UserType getUser(String oid, ModelPortType model) throws SynchronizationException {
        try {
            ObjectContainerType container = model.getObject(oid, new PropertyReferenceListType());
            if (container == null) {
                return null;
            }
            if (container.getObject() == null || !(container.getObject() instanceof UserType)) {
                throw new SynchronizationException("Returned object is null or not type of " +
                        UserType.class.getName() + ".");
            }
            return (UserType) container.getObject();
        } catch (com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage ex) {
            FaultType info = ex.getFaultInfo();
            if (info == null || !(info instanceof ObjectNotFoundFaultType)) {
                throw new SynchronizationException("Can't get user. Unknown error occured.", ex, ex.getFaultInfo());
            }
        }
        return null;
    }
}
