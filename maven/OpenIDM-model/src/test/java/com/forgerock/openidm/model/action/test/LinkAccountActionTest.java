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
 * "Portions Copyrighted 2011 [name of copyright owner]"
 * 
 * $Id$
 */
package com.forgerock.openidm.model.action.test;

import com.forgerock.openidm.provisioning.service.ResourceAccessInterface;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectReferenceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowChangeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.ns._public.provisioning.provisioning_1.ProvisioningPortType;
import com.forgerock.openidm.xml.ns._public.provisioning.resource_object_change_listener_1.ResourceObjectChangeListenerPortType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
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
 * @author Katuska
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context-model.xml", "classpath:application-context-repository.xml", "classpath:application-context-repository-test.xml", "classpath:application-context-provisioning.xml", "classpath:application-context-model-test.xml"})
public class LinkAccountActionTest {

    @Autowired(required = true)
    private ResourceObjectChangeListenerPortType resourceObjectChangeService;
    @Autowired(required = true)
    private RepositoryPortType repositoryService;
    @Autowired(required = true)
    private ResourceAccessInterface rai;
    @Autowired(required = true)
    private ProvisioningPortType provisioningService;

    public LinkAccountActionTest() {
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

    private ObjectType addObjectToRepo(ObjectType object) throws Exception {
        ObjectContainerType objectContainer = new ObjectContainerType();
        objectContainer.setObject(object);
        repositoryService.addObject(objectContainer);
        return object;
    }

    private ObjectType addObjectToRepo(String fileString) throws Exception {
        ObjectContainerType objectContainer = new ObjectContainerType();
        ObjectType object = ((JAXBElement<ObjectType>) JAXBUtil.unmarshal(new File(fileString))).getValue();
        objectContainer.setObject(object);
        repositoryService.addObject(objectContainer);
        return object;
    }

    private ResourceObjectShadowChangeDescriptionType createChangeDescription(String file) throws JAXBException {
        ResourceObjectShadowChangeDescriptionType change = ((JAXBElement<ResourceObjectShadowChangeDescriptionType>) JAXBUtil.unmarshal(new File(file))).getValue();
        return change;
    }

    @Test
    public void testAddUserAction() throws Exception {

        final String resourceOid = "ef2bc95b-76e0-48e2-97e7-3d4f02d3e1a2";
        final String userOid = "12345678-d34d-b33f-f00d-987987987987";
        final String accountOid = "c0c010c0-d34d-b33f-f00d-222333444555";

        try {
            //create additional change
            ResourceObjectShadowChangeDescriptionType change = createChangeDescription("src/test/resources/account-change-add.xml");
            //adding objects to repo
            UserType userType = (UserType) addObjectToRepo("src/test/resources/user.xml");   
            ResourceType resourceType = (ResourceType) addObjectToRepo(change.getResource());
            AccountShadowType accountType = (AccountShadowType) addObjectToRepo(change.getShadow());

            resourceObjectChangeService.notifyChange(change);


            ObjectContainerType container = repositoryService.getObject(userOid, new PropertyReferenceListType());
            UserType changedUser = (UserType) container.getObject();
            List<ObjectReferenceType> accountRefs = changedUser.getAccountRef();

            assertNotNull(changedUser);
            assertEquals(accountOid, accountRefs.get(0).getOid());

            container = repositoryService.getObject(accountOid, new PropertyReferenceListType());
            AccountShadowType linkedAccount = (AccountShadowType) container.getObject();

            assertNotNull(linkedAccount);
            assertEquals(changedUser.getName(), linkedAccount.getName());

        } finally {
            //cleanup repo
            try {
                repositoryService.deleteObject(accountOid);
            } catch(com.forgerock.openidm.xml.ns._public.repository.repository_1.FaultMessage e) {
            }
            try {
                repositoryService.deleteObject(resourceOid);
            } catch(com.forgerock.openidm.xml.ns._public.repository.repository_1.FaultMessage e) {
            }
            try {
                repositoryService.deleteObject(userOid);
            } catch(com.forgerock.openidm.xml.ns._public.repository.repository_1.FaultMessage e) {
            }
        }

    }
}
