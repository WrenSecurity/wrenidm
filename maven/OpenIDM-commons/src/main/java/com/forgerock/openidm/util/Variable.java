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
package com.forgerock.openidm.util;

import org.w3c.dom.Node;

/**
 *
 * @author Vilo Repan
 */
public class Variable {

    private boolean userDefined;
    private Object object;

    public Variable(Object object) {
        this(object, true);
    }

    public Variable(Object object, boolean userDefined) {
        if (object == null) {
            throw new IllegalArgumentException("Object variable can't be null.");
        }
        if (!(object instanceof String) && !(object instanceof Node)) {
            throw new IllegalArgumentException("Object '" + object.getClass().getName() +
                    "'have to be instance of " + String.class.getName() + " or " + Node.class.getName());
        }
        this.userDefined = userDefined;
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    @Override
    public String toString() {
        if (userDefined) {
            return "Variable(("+object.getClass().getSimpleName()+")"+object+" (user defined))";
        } else {
            return "Variable(("+object.getClass().getSimpleName()+")"+object+")";
        }
    }
}
