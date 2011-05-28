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
package com.forgerock.openidm.provisioning.conversion;

import com.forgerock.openidm.provisioning.util.ShadowUtil;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author elek
 */
public class BooleanConverter implements Converter {

    private Collection<Class> supportedClasses;

    public BooleanConverter() {
        supportedClasses = Arrays.asList(new Class[]{Boolean.class, boolean.class});
    }

    @Override
    public QName getXmlType() {
        return new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "boolean");
    }

    @Override
    public Collection<Class> getJavaTypes() {
        return supportedClasses;
    }

    @Override
    public Object convertToJava(Node node) {
        Boolean b = Boolean.FALSE;
        if ("true".equalsIgnoreCase(node.getTextContent())) {
            b = Boolean.TRUE;
        }
        return b;
    }

    @Override
    public Node convertToXML(QName qname, Object o) {
        String value = "false";
        if ((o instanceof Boolean) && (Boolean)o) {
            value = "true";
        } else {
            if (Boolean.parseBoolean(o.toString())){
                value = "true";
            }
        }
        Document d = ShadowUtil.getXmlDocument();
        Element e = d.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
        e.appendChild(d.createTextNode(value));
        return e;
    }
}
