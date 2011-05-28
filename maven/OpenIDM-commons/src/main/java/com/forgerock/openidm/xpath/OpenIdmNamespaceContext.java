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
package com.forgerock.openidm.xpath;

import com.forgerock.openidm.xml.schema.SchemaConstants;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.*;
import javax.xml.namespace.NamespaceContext;

/**
 * Used to register namespaces for prefixes for JAXP
 *
 * @see NamespaceContext
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */

public class OpenIdmNamespaceContext implements NamespaceContext {

    Map map = new HashMap();

    public OpenIdmNamespaceContext(Map map) {
        this.map = map;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) throw new NullPointerException("Null prefix");

        String namespace = (String) map.get(prefix);
        if (null != namespace) {
            return namespace;
        }

        return XMLConstants.NULL_NS_URI;
    }

    // This method isn't necessary for XPath processing.
    @Override
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    @Override
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}