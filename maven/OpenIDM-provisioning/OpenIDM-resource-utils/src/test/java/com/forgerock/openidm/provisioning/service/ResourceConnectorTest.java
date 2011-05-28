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

import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author laszlohordos
 */
public class ResourceConnectorTest {

    private ResourceType resource = null;

    public ResourceConnectorTest() {
    }

    @Before
    public void setUp() throws JAXBException {
        InputStream in = getClass().getResourceAsStream("/ResourceConfiguration.xml");
        JAXBContext ctx = JAXBContext.newInstance(ResourceType.class);
        JAXBElement<ObjectType> o =  (JAXBElement<ObjectType>) ctx.createUnmarshaller().unmarshal(in);
        if (o.getValue() instanceof ResourceType) {
            resource = (ResourceType) o.getValue();
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getSchema method, of class ResourceConnector.
     */
    @Test
    public void testGetSchema() {
        System.out.println("getSchema");
        assertNotNull(resource);
        ResourceConnector instance = new ResourceConnectorImpl(resource);
        ResourceSchema result = instance.getSchema();
        assertNotNull(result);
    }


    public class ResourceConnectorImpl extends ResourceConnector<String> {

        public ResourceConnectorImpl(ResourceType resourceType) {
            super(resourceType);
        }

        @Override
        public String getConfiguration() {
            return null;
        }
    }

}