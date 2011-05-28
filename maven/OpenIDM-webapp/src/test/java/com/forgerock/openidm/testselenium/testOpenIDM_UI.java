/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.forgerock.openidm.testselenium;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author matthiastristl
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({com.forgerock.openidm.testselenium.cleanAll.class,com.forgerock.openidm.testselenium.addResource.class,com.forgerock.openidm.testselenium.addUsers.class,com.forgerock.openidm.testselenium.editUser.class})
public class testOpenIDM_UI {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}