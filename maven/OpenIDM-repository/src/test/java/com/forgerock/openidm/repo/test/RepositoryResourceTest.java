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

import com.forgerock.openidm.util.QNameUtil;
import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PagingType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
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
public class RepositoryResourceTest {

    @Autowired(required = true)
    private RepositoryPortType repositoryService;

    public RepositoryPortType getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryPortType repositoryService) {
        this.repositoryService = repositoryService;
    }

    public RepositoryResourceTest() {
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
    public void testResource() throws Exception {
        final String resourceOid = "aae7be60-df56-11df-8608-0002a5d5c51b";
        try {
            ObjectContainerType objectContainer = new ObjectContainerType();
            ResourceType resource = ((JAXBElement<ResourceType>) JAXBUtil.unmarshal(new File("target/test-data/repository/aae7be60-df56-11df-8608-0002a5d5c51b.xml"))).getValue();
            objectContainer.setObject(resource);
            repositoryService.addObject(objectContainer);
            ObjectContainerType retrievedObjectContainer = repositoryService.getObject(resourceOid, new PropertyReferenceListType());
            assertEquals(resource.getOid(), ((ResourceType) (retrievedObjectContainer.getObject())).getOid());
            ObjectListType objects = repositoryService.listObjects(QNameUtil.qNameToUri(SchemaConstants.I_RESOURCE_TYPE), new PagingType());
            assertEquals(1, objects.getObject().size());
            assertEquals(resourceOid, objects.getObject().get(0).getOid());
        } finally {
            repositoryService.deleteObject(resourceOid);
        }
    }

    @Test
    public void testResourceModification() throws Exception {
        final String resourceOid = "aae7be60-df56-11df-8608-0002a5d5c51b";
        try {
            ObjectContainerType objectContainer = new ObjectContainerType();
            ResourceType resource = ((JAXBElement<ResourceType>) JAXBUtil.unmarshal(new File("target/test-data/repository/aae7be60-df56-11df-8608-0002a5d5c51b.xml"))).getValue();
            objectContainer.setObject(resource);
            repositoryService.addObject(objectContainer);
            ObjectContainerType retrievedObjectContainer = repositoryService.getObject(resourceOid, new PropertyReferenceListType());
            assertEquals(resource.getOid(), ((ResourceType) (retrievedObjectContainer.getObject())).getOid());
            ResourceType modifiedResource = ((JAXBElement<ResourceType>) JAXBUtil.unmarshal(new File("src/test/resources/resource-modified-removed-tags.xml"))).getValue();
            ObjectModificationType objectModificationType = CalculateXmlDiff.calculateChanges(new File("target/test-data/repository/aae7be60-df56-11df-8608-0002a5d5c51b.xml"), new File("src/test/resources/resource-modified-removed-tags.xml"));
            repositoryService.modifyObject(objectModificationType);
            retrievedObjectContainer = repositoryService.getObject(resourceOid, new PropertyReferenceListType());
            assertEquals(modifiedResource.getOid(), ((ResourceType) (retrievedObjectContainer.getObject())).getOid());
            assertEquals(modifiedResource.getSchemaHandling(), ((ResourceType) (retrievedObjectContainer.getObject())).getSchemaHandling());
            assertEquals(modifiedResource.getConfiguration(), ((ResourceType) (retrievedObjectContainer.getObject())).getConfiguration());
            assertEquals(modifiedResource.getSchema(), ((ResourceType) (retrievedObjectContainer.getObject())).getSchema());
            assertEquals(modifiedResource.getScripts(), ((ResourceType) (retrievedObjectContainer.getObject())).getScripts());
        } finally {
            repositoryService.deleteObject(resourceOid);
        }
    }
}
