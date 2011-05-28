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

import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author semancik
 */
public final class PropertyChange {

    public enum ChangeType { ADD, DELETE, REPLACE };

    private QName propertyName;
    private ChangeType changeType;
    private Set<Object> values;

    public PropertyChange(QName name, ChangeType type, Set<Object> values) {
        propertyName = name;
        changeType = type;
        this.values = values;
    }

    public QName getPropertyName() {
        return propertyName;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Set<Object> getValues() {
        return values;
    }

}
