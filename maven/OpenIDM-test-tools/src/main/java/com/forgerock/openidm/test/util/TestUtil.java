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
package com.forgerock.openidm.test.util;

import com.forgerock.openidm.xml.ns._public.common.common_1.ExtensibleObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class TestUtil {

    public static final String code_id = "$Id$";
    public static final JAXBContext ctx;

    static {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        ctx = context;
    }

    /**
     * Gets an object from the sample object store and unmarshalizise it.
     *
     * The sample object list is available at /repository/%s.xml and all object
     * is referenceable by {@link SampleObjects}
     * 
     * @param object
     * @return the sample object instance or assert null error if the object is not available
     */
    public static ExtensibleObjectType getSampleObject(SampleObjects object) {
        assert null != object;
        String resourceName = String.format("/test-data/repository/%s.xml", object.getOID());

        InputStream in = TestUtil.class.getResourceAsStream(resourceName);
        ExtensibleObjectType out = null;
        if (null != in) {
            try {
                JAXBElement<ExtensibleObjectType> o = (JAXBElement<ExtensibleObjectType>) ctx.createUnmarshaller().unmarshal(in);
                out = o.getValue();
            } catch (JAXBException ex) {
                Logger.getLogger(TestUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        assert null != out;
        return out;
    }
}
