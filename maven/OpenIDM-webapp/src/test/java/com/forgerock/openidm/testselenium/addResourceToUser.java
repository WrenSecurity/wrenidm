/*
 * This test is not finished and will fail!
 * To be changed soon!!
 *
*/
package com.forgerock.openidm.testselenium;

import com.thoughtworks.selenium.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class addResourceToUser extends SeleneseTestCase {


    @Before
    @Override
    public void setUp() throws Exception {
        selenium = new DefaultSelenium(Constants.hostName, Constants.port, Constants.userAgent, Constants.baseURL);
        selenium.start();
        selenium.open("/idm/login.iface");
        selenium.open("/idm/login.iface");
        selenium.type("loginForm:userName", Constants.loginUser);
        selenium.type("loginForm:password", Constants.password);
        selenium.click("loginForm:loginButton");
        for (int time = 0;; time++) {
            if (time >= 240) {fail("timeout");}
            try {if (selenium.isTextPresent("Common Tasks")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
    }

    @Test
    public void testAddResourceToUser() throws Exception {
        selenium.click("menuForm:menuBar:users:listUsers:out");
        for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("admin-content:addButton")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
        selenium.click("admin-content:userTable:0:name");
        for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("admin-content:editButton")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
        selenium.click("//a[@id='admin-content:editButton']/span");
        for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isTextPresent("Localhost OpenDJ")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
        // check for the presence of the added resouce in the result page
        assertTrue(selenium.isTextPresent("Localhost OpenDJ"));
    }

}