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

import javax.xml.namespace.QName;

/**
 *
 * QName <-> URI conversion.
 * 
 * Very simplistic but better than nothing.
 *
 * @author semancik
 */
public class QNameUtil {

    public static String qNameToUri(QName qname) {
        String qUri = qname.getNamespaceURI();
        StringBuilder sb = new StringBuilder(qUri);

        // TODO: Check if there's already a fragment
        // e.g. http://foo/bar#baz


        if (!qUri.endsWith("#") && !qUri.endsWith("/")) {
            sb.append("#");
        }
        sb.append(qname.getLocalPart());

        return sb.toString();
    }

    public static QName uriToQName(String uri) {

        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        int index = uri.lastIndexOf("#");
        if (index != -1) {
            String ns = uri.substring(0, index);
            String name = uri.substring(index+1);
            return new QName(ns,name);
        }
        index = uri.lastIndexOf("/");
        // TODO check if this is still in the path section, e.g.
        // if the matched slash is not a beginning of authority
        // section
        if (index != -1) {
            String ns = uri.substring(0, index+1);
            String name = uri.substring(index+1);
            return new QName(ns,name);
        }
        throw new IllegalArgumentException("The URI ("+uri+") does not contain slash character");
    }
}
