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
package com.forgerock.openidm.model.test;

import com.forgerock.openidm.model.*;
import com.forgerock.openidm.xml.ns._public.model.password_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.model.password_1.PasswordChangeRequestType;
import com.forgerock.openidm.xml.ns._public.model.password_1.PasswordChangeResponseType;
import com.forgerock.openidm.xml.ns._public.model.password_1.PasswordSynchronizeRequestType;
import com.forgerock.openidm.xml.ns._public.model.password_1.SelfPasswordChangeRequestType;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author laszlohordos
 */
public class PasswordServiceTest {

    private JAXBContext ctx;
    private Unmarshaller unmarshaller;

    public PasswordServiceTest() throws JAXBException {
        ctx = JAXBContext.newInstance(ObjectFactory.class);
        unmarshaller = ctx.createUnmarshaller();

    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    @Ignore
    public void testSelfChangePassword() {
        System.out.println("selfChangePassword");
        SelfPasswordChangeRequestType spcrt = null;
        PasswordService instance = new PasswordService();
        PasswordChangeResponseType expResult = null;
        PasswordChangeResponseType result = instance.selfChangePassword(spcrt);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testChangePassword() {
        System.out.println("changePassword");
        PasswordChangeRequestType pcrt = null;
        PasswordService instance = new PasswordService();
        PasswordChangeResponseType expResult = null;
        PasswordChangeResponseType result = instance.changePassword(pcrt);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSynchronizePassword() throws JAXBException {
        System.out.println("synchronizePassword");
        InputStream in = PasswordServiceTest.class.getResourceAsStream("/password-SynchronizeRequest.xml");
        JAXBElement<PasswordSynchronizeRequestType> input = (JAXBElement<PasswordSynchronizeRequestType>) unmarshaller.unmarshal(in);
        PasswordService instance = new PasswordService();
        instance.synchronizePassword(input.getValue());        
    }

}
