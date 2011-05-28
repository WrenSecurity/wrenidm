package com.forgerock.openidm.testselenium;

import com.thoughtworks.selenium.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class editUser extends SeleneseTestCase {


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
    public void testEditUser() throws Exception {
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
        Thread.sleep(500);
        String currentEmail = (selenium.getValue("admin-content:emailText"));
        selenium.type("admin-content:emailText", (currentEmail + "1"));
        selenium.click("admin-content:saveButton");
        for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("admin-content:addButton")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
        // check for the presence of the added resouce in the result page
        assertTrue(selenium.isTextPresent(currentEmail + "1"));
    }
    
    @After
    @Override
    public void tearDown() throws Exception {
        selenium.stop();
    }
}