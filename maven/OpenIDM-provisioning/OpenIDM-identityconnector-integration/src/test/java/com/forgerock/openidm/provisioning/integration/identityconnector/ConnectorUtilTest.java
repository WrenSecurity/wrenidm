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
package com.forgerock.openidm.provisioning.integration.identityconnector;

import com.forgerock.openidm.api.exceptions.OpenIDMException;
import com.forgerock.openidm.test.util.SampleObjects;
import com.forgerock.openidm.test.util.TestUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.resource.idconnector.configuration_1.ConnectorConfiguration;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author laszlohordos
 */
public class ConnectorUtilTest {

    private JAXBContext context = null;
    private com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory commonObjectFactory = new com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory();
    private com.forgerock.openidm.xml.ns._public.resource.idconnector.configuration_1.ObjectFactory icfObjectFactory = new com.forgerock.openidm.xml.ns._public.resource.idconnector.configuration_1.ObjectFactory();
    private Marshaller marshaller;
    private ConnectorConfiguration connectorConfiguration;
    private ResourceType resource;

    public ConnectorUtilTest() throws JAXBException {
        context = JAXBContext.newInstance("com.forgerock.openidm.xml.ns._public.common.common_1:com.forgerock.openidm.xml.ns._public.resource.idconnector.configuration_1");
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
   

    /**
     * Test of getBundleURLs method, of class ConnectorUtil.
     */
    @Test
    public void testGetBundleURLs() {
        System.out.println("getBundleURLs");

        URL[] result = ConnectorUtil.getBundleURLs();
        assertNotNull(result);
        assertTrue(result.length > 1);
    }

    /**
     * Test of clearManagerCaches method, of class ConnectorUtil.
     */
    @Test
    @Ignore
    public void testClearManagerCaches() {
        System.out.println("clearManagerCaches");
        ConnectorUtil.clearManagerCaches();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testCreateConnectorFacade() throws JAXBException {
        System.out.println("createConnectorFacade");
        ResourceType type = (ResourceType) TestUtil.getSampleObject(SampleObjects.RESOURCETYPE_LOCALHOST_OPENDJ);
        IdentityConnector c = new IdentityConnector(type);
        assertNotNull(c.getConfiguration());
        ConnectorFacade result = null;
        try {
            result = ConnectorUtil.createConnectorFacade(c);
        } catch (OpenIDMException ex) {
            Logger.getLogger(ConnectorUtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertNotNull(result);        
    }
}
