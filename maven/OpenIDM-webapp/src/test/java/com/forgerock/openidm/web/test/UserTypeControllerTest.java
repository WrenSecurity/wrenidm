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
package com.forgerock.openidm.web.test;

import com.forgerock.openidm.web.controller.UserTypeController;
import com.forgerock.openidm.web.dto.GuiUserDto;
import com.forgerock.openidm.web.model.ObjectStage;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author sleepwalker
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:applicationContext-test.xml", "classpath:application-context-repository-test.xml"})
public class UserTypeControllerTest {

    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserTypeControllerTest.class);
    @Autowired
    UserTypeController utc;
//    List<guiUserDto> GuiUserDtoList;

    public UserTypeControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
//        createResourceForTest();
//        createAccountForTest();
        createUsersForTest();

    }

    @After
    public void tearDown() {
    }

    private void createUsersForTest() {
        List<GuiUserDto> GuiUserDtoList = new ArrayList<GuiUserDto>();

        GuiUserDto guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Janko Mrkvicka");
        guiUserDto.setFamilyName("Mrkvicka");
        guiUserDto.setGivenName("Janko");
        guiUserDto.setName("aaa");
        guiUserDto.setOid("a");
        guiUserDto.setSelected(true);
        utc.setUser(guiUserDto);
        utc.createUser();
        GuiUserDtoList.add(guiUserDto);

        guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Janko Hrasko");
        guiUserDto.setFamilyName("Hrasko");
        guiUserDto.setGivenName("Janko");
        guiUserDto.setName("bbb");
        guiUserDto.setOid("b");
        guiUserDto.setSelected(true);
        utc.setUser(guiUserDto);
        utc.createUser();
        GuiUserDtoList.add(guiUserDto);

        guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Filip Mrkvicka");
        guiUserDto.setFamilyName("Mrkvicka");
        guiUserDto.setGivenName("Filip");
        guiUserDto.setName("ccc");
        guiUserDto.setOid("c");
        guiUserDto.setSelected(true);
        utc.setUser(guiUserDto);
        utc.createUser();
        GuiUserDtoList.add(guiUserDto);

        guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Anicka Dusicka");
        guiUserDto.setFamilyName("Dusicka");
        guiUserDto.setGivenName("Anicka");
        guiUserDto.setName("ddd");
        guiUserDto.setOid("d");
        guiUserDto.setSelected(false);
        utc.setUser(guiUserDto);
        utc.createUser();
        GuiUserDtoList.add(guiUserDto);

        utc.setUserData(GuiUserDtoList);

    }

//    public void createAccountForTest() throws Exception {
//        logger.info("createAccountForTest start");
//        try {
//            Unmarshaller unmarshaller = JAXBUtil.createUnmarshaller("com.forgerock.openidm.xml.ns._public.common.common_1");
//            JAXBElement ob = (JAXBElement) unmarshaller.unmarshal(new File("target/test-data/repository/dbb0c37d-9ee6-44a4-8d39-016dbce18b4c.xml"));
//            AccountShadowType accountType = (AccountShadowType) ob.getValue();
//            utc.setAccount(new AccountShadowDto(accountType));
//            utc.createAccount();
//
//        } catch (JAXBException ex) {
//            logger.info("Unmarshaler failed");
//            logger.error("Exception was {}", ex);
//            throw new Exception("Unmarshalling failed");
//        }
//        logger.info("createAccountForTest end");
//    }

//    public void createResourceForTest() throws Exception {
//        logger.info("createResourceForTest start");
//        try {
//            Unmarshaller unmarshaller = JAXBUtil.createUnmarshaller("com.forgerock.openidm.xml.ns._public.common.common_1");
//            JAXBElement ob = (JAXBElement) unmarshaller.unmarshal(new File("target/test-data/repository/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2.xml"));
//            ResourceType resourceType = (ResourceType) ob.getValue();
//            utc.setResource(new ResourceDto(resourceType));
//            utc.createResource();
//
//        } catch (JAXBException ex) {
//            logger.info("Unmarshaler faild");
//            logger.error("Exception was {}", ex);
//            throw new Exception("Unmarshalling failed");
//        }
//        logger.info("createResourceForTest end");
//    }

    @Test
    public void createUser() {

        GuiUserDto guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Janko Mrkvicka");
        guiUserDto.setFamilyName("Mrkvicka");
        guiUserDto.setGivenName("Janko");
        guiUserDto.setName("aaa");
        guiUserDto.setSelected(false);


        utc.setUser(guiUserDto);
        String oid = utc.createUser();

        assertNotNull(oid);

    }

    @Test(expected = java.lang.NullPointerException.class)
    public void notCreateUser() {

        GuiUserDto GuiUserDto = null;
        utc.setUser(GuiUserDto);
        String oid = utc.createUser();
        assertNull("Oid null", oid);

    }

    @Test
    public void deleteUsers() {


        List<GuiUserDto> GuiUserDtoList = new ArrayList<GuiUserDto>();

        GuiUserDto guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Janko Mrkvicka");
        guiUserDto.setFamilyName("Mrkvicka");
        guiUserDto.setGivenName("Janko");
        guiUserDto.setName("aaa");
        guiUserDto.setOid("a");
        guiUserDto.setSelected(true);
        GuiUserDtoList.add(guiUserDto);

        guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Janko Hrasko");
        guiUserDto.setFamilyName("Hrasko");
        guiUserDto.setGivenName("Janko");
        guiUserDto.setName("bbb");
        guiUserDto.setOid("b");
        guiUserDto.setSelected(true);
        GuiUserDtoList.add(guiUserDto);

        guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Filip Mrkvicka");
        guiUserDto.setFamilyName("Mrkvicka");
        guiUserDto.setGivenName("Filip");
        guiUserDto.setName("ccc");
        guiUserDto.setOid("c");
        guiUserDto.setSelected(true);
        GuiUserDtoList.add(guiUserDto);

        guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Anicka Dusicka");
        guiUserDto.setFamilyName("Dusicka");
        guiUserDto.setGivenName("Anicka");
        guiUserDto.setName("ddd");
        guiUserDto.setOid("d");
        guiUserDto.setSelected(false);
        GuiUserDtoList.add(guiUserDto);

        utc.setUserData(GuiUserDtoList);

        assertNotNull("not null", utc.getUserData());
        assertEquals(utc.getUserData().size(), GuiUserDtoList.size());
        assertArrayEquals(utc.getUserData().toArray(), GuiUserDtoList.toArray());
        utc.deleteUsers();
        assertEquals("Not equals", 1, utc.getUserData().size());
    }

//    @Test(expected = java.lang.UnsupportedOperationException.class)
//    public void updateUser() {
//
//        GuiUserDto guiUserDto = new GuiUserDto();
//        guiUserDto.setStage(new ObjectStage());
//        guiUserDto.getStage().setObject(new UserType());
//        guiUserDto.setFullName("Janko Mrkvicka");
//        guiUserDto.setFamilyName("Mrkvicka");
//        guiUserDto.setGivenName("Janko");
//        guiUserDto.setName("aaa");
//        guiUserDto.setOid("a");
//        guiUserDto.setSelected(true);
//
//        utc.setUser(guiUserDto);
//        utc.updateUser();
//
//    }

    @Test
    public void listUsers() {
        utc.listUsers();
        assertNotNull(utc.getUserData());
    }

    private void createUserWithAccountOnResource() {
        GuiUserDto guiUserDto = new GuiUserDto();
        guiUserDto.setStage(new ObjectStage());
        guiUserDto.getStage().setObject(new UserType());
        guiUserDto.setFullName("Janko Mrkvicka");
        guiUserDto.setFamilyName("Mrkvicka");
        guiUserDto.setGivenName("Janko");
        guiUserDto.setName("aaa");
        guiUserDto.setOid("a");


//        List<AccountShadowDto> accounts = new ArrayList<AccountShadowDto>(utc.listAccounts());
//        assertNotNull(accounts);
//        for (AccountShadowDto acc : accounts){
//            System.out.println("account "+acc.getName());
//        }
//        assertEquals(1, accounts.size());
//        List<ResourceDto> resources = new ArrayList<ResourceDto>(utc.listResources());
//
//        ObjectReferenceType objectReferenceType = new ObjectReferenceType();
//        objectReferenceType.setOid(accounts.get(0).getOid());
//
//        ((UserType) (guiUserDto.getXmlObject())).getAccountRef().add(objectReferenceType);
//        System.out.println("acc ref " + guiUserDto.getAccountRef().size());
//        System.out.println("guiUser acc " + guiUserDto.getAccountRef().get(0).getOid());
//
//        utc.setUser(guiUserDto);
//        utc.createUser();

    }

//    @Test
//    public void listResources() {
//        Collection<ResourceDto> allResources = utc.listResources();
//        assertNotNull(allResources);
//        List<ResourceDto> resources = new ArrayList<ResourceDto>(allResources);
//        assertEquals("Localhost OpenDJ v3", resources.get(0).getName());
//        assertNotNull(resources.get(0).getSchema());
//
//    }

//    @Test
//    public void listUserAccounts() {
//
//        createUserWithAccountOnResource();
//
//        utc.listUserAccounts(utc.getUser().getOid());
//        assertNotNull(utc.getUser().getAccount());
//        assertEquals("jbond", utc.getUser().getAccount().get(0).getName());
//        assertEquals("Localhost OpenDJ v3", utc.getUser().getAccount().get(0).getResource().getName());
//    }

    /**
     * TODO fix this tests. newAccount array is empty so currently it don't do anything.
     * TODO assertions should be added
     * @throws SchemaParserException
     */
//    @Test
//    public void addUserAccount() throws SchemaParserException {
//        //not complete
//        List<ResourceDto> resources = new ArrayList<ResourceDto>(utc.listResources());
//        utc.setResource(resources.get(0));
//        utc.listUsers();
//        List<GuiUserDto> guiUsers = utc.getUserData();
//        utc.setUser(guiUsers.get(0));
//        System.out.println("resource oid" + resources.get(0).getName());
//        utc.generateFormForNewAccount();
//    }
}
