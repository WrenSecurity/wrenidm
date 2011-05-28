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
package com.forgerock.openidm.staging.test;

import com.forgerock.openidm.staging.*;
import com.forgerock.openidm.staging.test.mock.IdmModelServiceMock;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import javax.xml.ws.Holder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author katuska
 * @author Igor Farinic
 */
public class StagingServiceTest {

    public StagingServiceTest() {
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
     * Test of createObjectStage method, of class StagingService.
     */
    @Test
    @Ignore
    public void testCreateObjectStage() {
        System.out.println("createObjectStage");
        String objectType = Utils.getObjectType("UserType");
        StagingService instance = new StagingService();

        ObjectStageType result = instance.createObjectStage(objectType);
        assertNotNull(result);
        assertEquals("user", result.getObject().getName().getLocalPart());
    }

    /**
     * Test of getObjectStage method, of class StagingService.
     */
    @Test
    @Ignore
    public void testGetObjectStage() {
        System.out.println("getObjectStage");
        String oid = "32769";
        PropertyReferenceListType resolve = new PropertyReferenceListType();
//        PropertyReferenceListType resolve = new PropertyReferenceListType();
//        PropertyReferenceType prop = new PropertyReferenceType();
//        prop.setProperty(Utils.getXsdNameForType(new ObjectFactory(), "Account"));
//        resolve.getProperty().add(prop);

        StagingService instance = new StagingService();
        IdmModelServiceMock modelServiceMock = new IdmModelServiceMock();
        instance.setService_1(modelServiceMock);

        ObjectStageType result = instance.getObjectStage(oid, resolve);
        assertNotNull(result);
        assertNotNull(result.getObject());
        UserType userType = (UserType) result.getObject().getValue();
        assertNotNull(userType);
        assertEquals(oid, userType.getOid());

    }

    /**
     * Test of submitObjectStage method, of class StagingService.
     */
    @Test
    @Ignore
    public void testSubmitObjectStage() {
        System.out.println("submitObjectStage");
        String oid = "12345";

        //original object
        UserType oldUserType = new UserType();
        oldUserType.setFullName("Janko Mrkvicka");
        //change: not null -> not null
        oldUserType.setFamilyName("Hrasko");
        //change: not null --> null
        oldUserType.setGivenName("Janko");
        oldUserType.setName("name");
        oldUserType.setOid(oid);
        oldUserType.setVersion("1.0");
        //change: null --> not null
        oldUserType.setHonorificPrefix(null);
        //no change: null --> null
        oldUserType.setHonorificSuffix(null);

        //new object
        UserType userType = new UserType();
        userType.setFullName("Janko Mrkvicka");
        userType.setFamilyName("Mrkvicka");
        userType.setGivenName(null);
        userType.setName("name");
        userType.setOid(oid);
        userType.setVersion("1.0");
        userType.setHonorificPrefix("prefix");
        userType.setHonorificSuffix(null);


        StagingService instance = new StagingService();
        IdmModelServiceMock modelServiceMock = new IdmModelServiceMock();
        instance.setService_1(modelServiceMock);

        ObjectStageType stage = instance.createObjectStage(Utils.getObjectType("UserType"));
        stage.setObject(new ObjectFactory().createUser((UserType) userType));
        
        //TODO: Check this code again!!!
        //stage.getAny().add(oldUserType);

        String result = instance.submitObjectStage(stage);
        assertNotNull(result);
        assertEquals(oid, result);

        ObjectStageType stageAfterModify = instance.getObjectStage(oid, null);
        UserType modifiedUser = (UserType) stageAfterModify.getObject().getValue();

        assertEquals("Janko Mrkvicka", modifiedUser.getFullName());
        assertEquals("Mrkvicka", modifiedUser.getFamilyName());
         
        assertNull(modifiedUser.getGivenName());
        assertEquals("name", modifiedUser.getName());
        assertEquals(oid, modifiedUser.getOid());
        assertEquals("1.0", modifiedUser.getVersion());
        assertEquals("prefix", modifiedUser.getHonorificPrefix());

    }

    /**
     * Test of addUserStageAccount method, of class StagingService.
     */
    @Test
    @Ignore
    public void testAddUserStageAccount() {
        System.out.println("addUserStageAccount");
        StagingService instance = new StagingService();
        IdmModelServiceMock modelServiceMock = new IdmModelServiceMock();
        instance.setService_1(modelServiceMock);

        ObjectStageType objectStageType = instance.createObjectStage(Utils.getObjectType("UserType"));

        Holder<ObjectStageType> stage = new Holder<ObjectStageType>();
        stage.value = objectStageType;
        String resourceOid = "333";

        instance.addUserStageAccount(stage, resourceOid);

        UserType user = ((UserType) stage.value.getObject().getValue());

        assertEquals(1, user.getAccount().size());
        AccountShadowType account = user.getAccount().get(0);
        assertEquals(resourceOid, account.getResource().getOid());

    }
}
