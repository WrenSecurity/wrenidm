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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class AbstractProperty<T> extends IdentifiableBase implements Property<T> {

    public static final String code_id = "$Id$";
    private String propertyName;
    protected T propertyValue;

    @Override
    @Column(name = "attrname", nullable = false, length = 128)
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public void setPropertyValue(T propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractProperty other = (AbstractProperty) obj;
        if ((this.propertyName == null) ? (other.propertyName != null) : !this.propertyName.equals(other.propertyName)) {
            return false;
        }
        if ((this.propertyValue == null) ? (other.propertyValue != null) : !this.propertyValue.equals(other.propertyValue)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.propertyName != null ? this.propertyName.hashCode() : 0);
        hash = 29 * hash + (this.propertyValue != null ? this.propertyValue.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Property [" + "oid " + getOid() + " " + "propertyName " + propertyName + " " + "propertyValue " + propertyValue + "]";
    }
}
