package com.forgerock.openidm.testselenium;

import com.thoughtworks.selenium.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class cleanAll extends SeleneseTestCase {
    
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

//    @Test
//    public void testCleanAllUsers() throws Exception {
//
//        if (Constants.removeUsers | Constants.removeAll) {
//            //navigate to list of users
//            selenium.click("menuForm:menuBar:users:deleteUsers:out");
//            for (int time = 0;; time++) {
//            if (time >= 120) {fail("timeout");}
//            try {if (selenium.isElementPresent("admin-content:userTable:userName")) break;} catch (Exception e) {}
//            Thread.sleep(250);
//            }
//            // list resources
//            selenium.click("admin-content:deleteAllUsers");
//            Thread.sleep(1000);
//            selenium.click("admin-content:deleteUser");
//            Thread.sleep(5000);
//            assertFalse(selenium.isTextPresent("First1 Last1"));
//        }
//    }
    
    @Test
    public void testCleanAllResources() throws Exception {
        if (Constants.removeResource | Constants.removeAll) {
            //navigate to the dbug page
            selenium.click("menuForm:menuBar:configuration:debugPages:out");
            for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("j_idt26:j_idt27")) break;} catch (Exception e) {}
            Thread.sleep(250);
            }
            selenium.select("j_idt26:selectOneMenuList", "label=ResourceType");
            Thread.sleep(1000);
            selenium.click("j_idt26:j_idt27");
            for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("j_idt27:backButton")) break;} catch (Exception e) {}
            Thread.sleep(250);
            }
            //delete the resource
            if (selenium.isElementPresent("j_idt27:j_idt31:0:j_idt36")) {
                selenium.click("j_idt27:j_idt31:0:j_idt36");
            }
            
            //check that resource is gone
            //navigate to the dbug page
            selenium.click("menuForm:menuBar:configuration:debugPages:out");
            for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("j_idt26:j_idt27")) break;} catch (Exception e) {}
            Thread.sleep(250);
            }
            selenium.select("j_idt26:selectOneMenuList", "label=ResourceType");
            Thread.sleep(1000);
            selenium.click("j_idt26:j_idt27");
            for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("j_idt27:backButton")) break;} catch (Exception e) {}
            Thread.sleep(250);
            }
            assertFalse(selenium.isTextPresent("Localhost OpenDJ"));

        }

    }

    @After
    @Override
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
