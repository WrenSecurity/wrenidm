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
package com.forgerock.openidm.repo.test;

import com.forgerock.openidm.util.DOMUtil;
import com.forgerock.openidm.util.QNameUtil;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.GenericObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PagingType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import java.io.File;
import javax.xml.bind.JAXBElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 *
 * @author sleepwalker
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../../../../application-context-repository.xml", "../../../../../application-context-repository-test.xml"})
public class RepositorGenericObjectTest {

    @Autowired(required = true)
    private RepositoryPortType repositoryService;

    public RepositoryPortType getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryPortType repositoryService) {
        this.repositoryService = repositoryService;
    }

    public RepositorGenericObjectTest() {
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

    @Test
    public void testGenericObject() throws Exception {
        final String genericObjectOid = "c0c010c0-d34d-b33f-f00d-999111111111";
        try {
            ObjectContainerType objectContainer = new ObjectContainerType();
            GenericObjectType genericObject = ((JAXBElement<GenericObjectType>) JAXBUtil.unmarshal(new File("src/test/resources/generic-object.xml"))).getValue();
            objectContainer.setObject(genericObject);
            repositoryService.addObject(objectContainer);
            ObjectContainerType retrievedObjectContainer = repositoryService.getObject(genericObjectOid, new PropertyReferenceListType());
            assertEquals(genericObject.getName(), ((GenericObjectType) (retrievedObjectContainer.getObject())).getName());
            assertEquals(genericObject.getObjectType(), ((GenericObjectType) (retrievedObjectContainer.getObject())).getObjectType());
            assertEquals(genericObject.getOid(), ((GenericObjectType) (retrievedObjectContainer.getObject())).getOid());
            assertEquals(genericObject.getExtension().getAny().size(), ((GenericObjectType) (retrievedObjectContainer.getObject())).getExtension().getAny().size());
            assertEquals(genericObject.getExtension().getAny().get(1).getLocalName(), ((GenericObjectType) (retrievedObjectContainer.getObject())).getExtension().getAny().get(1).getLocalName());
            //assertEquals(genericObject.getExtension().getAny().get(1).getChildNodes().getLength(), ((GenericObjectType) (retrievedObjectContainer.getObject())).getExtension().getAny().get(1).getChildNodes().getLength());
            assertEquals(DOMUtil.serializeDOMToString(genericObject.getExtension().getAny().get(1)), DOMUtil.serializeDOMToString(((GenericObjectType) (retrievedObjectContainer.getObject())).getExtension().getAny().get(1)));
            ObjectListType objects = repositoryService.listObjects(QNameUtil.qNameToUri(SchemaConstants.I_GENERIC_OBJECT_TYPE), new PagingType());
            assertEquals(1, objects.getObject().size());
            assertEquals(genericObjectOid, objects.getObject().get(0).getOid());
        } finally {
            repositoryService.deleteObject(genericObjectOid);
        }
    }
}
