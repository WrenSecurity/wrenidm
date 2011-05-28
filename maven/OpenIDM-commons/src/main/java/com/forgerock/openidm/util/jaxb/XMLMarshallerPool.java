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
package com.forgerock.openidm.util.jaxb;

import com.forgerock.openidm.xml.util.XMLMarshaller;
import com.forgerock.openidm.util.jaxb.impl.XMLMarshallerImpl;
import com.forgerock.openidm.xml.common.ObjectPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class XMLMarshallerPool extends ObjectPool<XMLMarshaller> {

    public static final String code_id = "$Id$";
    private Logger logger = Logger.getLogger(XMLMarshallerPool.class.getName());

    public XMLMarshallerPool() {
        super();
    }

    public XMLMarshallerPool(int initialSize) {
        super(initialSize);
    }

    @Override
    protected XMLMarshaller create() {
        XMLMarshaller result = null;

        try {
            result = new XMLMarshallerImpl();
        } catch (JAXBException e) {
            logger.log(Level.SEVERE, "Unable to create XML marshaller", e);
        }

        return result;
    }
}
