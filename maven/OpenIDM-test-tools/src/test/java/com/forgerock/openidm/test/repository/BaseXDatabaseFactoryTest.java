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
package com.forgerock.openidm.test.repository;

import java.io.File;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author laszlohordos
 */
public class BaseXDatabaseFactoryTest {

    private ObjectFactory objectFactory = new ObjectFactory();

    private File repoLocation;

    public BaseXDatabaseFactoryTest() {
        try {
            repoLocation = new File(getClass().getResource("/").toURI());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new AssertionError("Error on openin repository" + repoLocation);
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of XMLDBQuery method, of class BaseXDatabaseFactory.
     */
    @Test
    @Ignore
    public void testXMLDBQuery() throws Exception {
        BaseXDatabaseFactory instance = new BaseXDatabaseFactory();
        instance.XMLServerStart(BaseXDatabaseFactoryTest.class.getResource("/").getPath(), new String[]{"-d"});
        instance.XMLDBQuery();
        instance.XMLServerStop();
        assertTrue(true);
    }

    @Test
    //@Ignore
    public void testGetDeleteObject() throws Exception {
        RepositoryPortType port = BaseXDatabaseFactory.getRepositoryPort(repoLocation);
        assertNotNull   (port);

        String oid = "ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2";
        try {
            ObjectContainerType out = port.getObject(oid, new PropertyReferenceListType());
            assertNotNull(out.getObject());
            port.deleteObject(oid);
            out = port.getObject(oid, new PropertyReferenceListType());
            assertNull(out);
        } finally {
            BaseXDatabaseFactory.XMLServerStop();
        }

    }

    @Test
    //@Ignore
    public void testAddObject() throws Exception {
        RepositoryPortType port = BaseXDatabaseFactory.getRepositoryPort(repoLocation);
        assertNotNull(port);

        String oid = null;
        try {
            ResourceType res = objectFactory.createResourceType();

            res.setName("OpenDJ Local");
            res.setType("Type");

            AccountShadowType account = objectFactory.createAccountShadowType();
            account.setName("TAstUser");
            account.setObjectClass(new QName("http://openidm.forgerock.com/xml/ns/public/resource/instances/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2", "__ACCOUNT__"));



            ObjectContainerType in = objectFactory.createObjectContainerType();
            in.setObject(res);
            oid = port.addObject(in);

            assertNotNull(oid);

            in.setObject(account);
            oid = port.addObject(in);

            ObjectContainerType out = port.getObject(oid, null);

            assertNotNull(out.getObject());

        } finally {
            BaseXDatabaseFactory.XMLServerStop();
        }

    }
}
