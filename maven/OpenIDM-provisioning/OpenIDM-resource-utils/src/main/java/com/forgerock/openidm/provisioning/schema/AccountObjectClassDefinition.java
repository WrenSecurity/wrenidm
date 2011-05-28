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

package com.forgerock.openidm.provisioning.schema;

import javax.xml.namespace.QName;

/**
 * http://openidm.forgerock.com/xml/ns/public/resource/resource-schema-1.xsd#accountType
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class AccountObjectClassDefinition extends ResourceObjectDefinition {
    
    public static final String code_id = "$Id$";

    /**
     * Flag which says that this is default account object class
     * (read from schema handling, or by default from schema)
     */
    private boolean isDefault = false;

    public AccountObjectClassDefinition(QName qname) {
        super(qname);
    }

    public AccountObjectClassDefinition(QName qname, String nativeObjectClass) {
        super(qname, nativeObjectClass);
    }
    
    public ResourceAttributeDefinition getPasswordAttribute(){
        for (ResourceAttributeDefinition ra : getAttributes()){
            if (ra.isPasswordAttribute()) {
                return ra;
            }
        }
        return null;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
