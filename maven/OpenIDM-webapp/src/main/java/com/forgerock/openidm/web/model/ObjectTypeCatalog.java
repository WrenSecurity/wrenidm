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
package com.forgerock.openidm.web.model;

import java.io.Serializable;
import java.util.Set;

/**
 * GUI Model Interface.
 * 
 * Provides unified access to the business logic, model, repository or anything
 * else that may be "down there". The Goal is to isolate GUI from deployment
 * changes and changes in business logic.
 *
 * This interface is supposed to support many object types. ObjectTypeCatalog
 * provides instances of ObjectManagers. ObjectManagers can manipulate
 * individual object types.
 *
 * DRAFT: this is the very first and quite simple version.
 *
 * @author semancik
 */
public interface ObjectTypeCatalog extends Serializable{

    /**
     * Retuns list of supported object types (in form of Java classes).
     *
     * @return list of supported object types (in form of Java classes)
     */
    Set<Class> listSupportedObjectTypes();

    /**
     *  Returns instance of ObjectManager appropriate for specified class.
     * @param <T> stadard object type (in form of Java classes)
     * @param <C> custom DTO class
     * @param type custom DTO class
     * @return
     */
    <T extends ObjectDto, C extends T> ObjectManager<T> getObjectManager(Class<T> managerType, Class<C> dtoType);

}
