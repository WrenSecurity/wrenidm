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
package com.forgerock.openidm.web.model.impl;

import com.forgerock.openidm.web.model.ObjectDto;
import com.forgerock.openidm.web.model.ObjectManager;
import com.forgerock.openidm.web.model.ObjectTypeCatalog;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

/**
 * End user entity.
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class ObjectTypeCatalogImpl implements ObjectTypeCatalog {

    private Map<Class<? extends ObjectType>, ObjectManager> supportedObjectManagers = new HashMap<Class<? extends ObjectType>, ObjectManager>();

    @Override
    public Set<Class> listSupportedObjectTypes() {
        Set supportedObjectTypes = supportedObjectManagers.keySet();
        return supportedObjectTypes;
    }

    public <T extends ObjectType> void add(Class<T> type, ObjectManager objectManager) {
        supportedObjectManagers.put(type, objectManager);
    }

    public void setSupportedObjectManagers(Map<Class<? extends ObjectType>, ObjectManager> objectManagers) {
        Validate.notNull(objectManagers);
        supportedObjectManagers = objectManagers;
    }

    @Override
    public <T extends ObjectDto, C extends T> ObjectManager<T> getObjectManager(Class<T> managerType, Class<C> dtoType) {
                return supportedObjectManagers.get(dtoType);
    }

}
