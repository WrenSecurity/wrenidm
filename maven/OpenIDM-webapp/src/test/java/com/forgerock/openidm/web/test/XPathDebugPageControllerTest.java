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

import com.forgerock.openidm.web.XPathVariables;
import com.forgerock.openidm.web.controller.XPathDebugPageController;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Katuska
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:applicationContext-test.xml", "classpath:application-context-repository-test.xml"})
public class XPathDebugPageControllerTest {

    @Autowired
    XPathDebugPageController xpathController;

    public XPathDebugPageControllerTest() {
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

    private void setAttributes() {
        xpathController.prepareXpathDebugPage();
        String expression = "declare namespace x='http://xxx.com/'; concat($x:foo,' ',$x:bar)";
        xpathController.setExpresion(expression);

        XPathVariables variable1 = new XPathVariables();
        variable1.setType("String");
        variable1.setValue("salala");
        variable1.setVariableName("x:foo");
        xpathController.setVariable1(variable1);
        XPathVariables variable2 = new XPathVariables();
        variable2.setType("String");
        variable2.setValue("tralala");
        variable2.setVariableName("x:bar");
        xpathController.setVariable2(variable2);

    }

    @Test
    public void testEvaluate() throws Exception {
        setAttributes();
        String result = xpathController.evaluate();
        System.out.println("result: " + result);
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
