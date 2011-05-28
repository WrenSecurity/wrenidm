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

import com.forgerock.openidm.web.dto.GuiUserDto;
import com.forgerock.openidm.web.model.ObjectManager;
import com.forgerock.openidm.web.model.ObjectTypeCatalog;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.web.model.UserManager;
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
 * Test of spring application context initialization
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:applicationContext-test.xml", "classpath:application-context-repository-test.xml"})
public class SpringApplicationContextTest {

    @Autowired(required=true)
    private ObjectTypeCatalog objectTypeCatalog;


    public SpringApplicationContextTest() {
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
    public void initApplicationContext() {
        assertNotNull(objectTypeCatalog.listSupportedObjectTypes());
        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
        UserManager userManager = (UserManager) (objectManager);
        assertNotNull(userManager);
    }

}