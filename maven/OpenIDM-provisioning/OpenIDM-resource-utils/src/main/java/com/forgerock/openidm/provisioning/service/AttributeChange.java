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

package com.forgerock.openidm.provisioning.service;

import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyModificationTypeType;

/**
 *
 * TODO: Documentation
 *
 * @author semancik
 */
public class AttributeChange {

    protected PropertyModificationTypeType changeType;

    protected ResourceAttribute attribute;


    /**
     * Get the value of changeType
     *
     * TODO: this is not really OK to use JAXB type
     * PropertyChangeTypeType here. But it is lesser
     * evil for now.
     *
     * @return the value of changeType
     */
    public PropertyModificationTypeType getChangeType() {
        return changeType;
    }

    /**
     * Set the value of changeType
     *
     * TODO: this is not really OK to use JAXB type
     * PropertyChangeTypeType here. But it is lesser
     * evil for now.
     *
     * @param changeType new value of changeType
     */
    public void setChangeType(PropertyModificationTypeType changeType) {
        this.changeType = changeType;
    }

    /**
     * Get the value of attribute
     *
     * @return the value of attribute
     */
    public ResourceAttribute getAttribute() {
        return attribute;
    }

    /**
     * Set the value of attribute
     *
     * @param attribute new value of attribute
     */
    public void setAttribute(ResourceAttribute attribute) {
        this.attribute = attribute;
    }


}
