package com.forgerock.openidm.testselenium;

import com.thoughtworks.selenium.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class addUsers extends SeleneseTestCase {

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
    public void testAddUsers() throws Exception {
        // create the users
        for (Integer userNr = 1; userNr <= Constants.nrUsers; userNr++) {
            selenium.click("menuForm:menuBar:users:createUser:out");
            for (int time = 0;; time++) {if (time >= 60) {fail("timeout");}
                try {if (selenium.isElementPresent("admin-content:givenName")) {break;}} catch (Exception e) {}
                Thread.sleep(250);
            }
            selenium.type("admin-content:givenName", "First" + userNr.toString());
            selenium.type("admin-content:familyName", "Last" + userNr.toString());
            selenium.type("admin-content:fullName", "First" + userNr.toString() + " " + "Last" + userNr.toString());
            selenium.type("admin-content:name", "user" + userNr.toString());
            selenium.type("admin-content:email", "user" + userNr.toString() + "@example.com");
            selenium.type("admin-content:password1", "password");
            selenium.type("admin-content:password2", "password");
            //click the create button
            selenium.click("admin-content:createUser");
            for (int time = 0;; time++) {
                if (time >= 60) {
                    fail("timeout");
                }
                try {
                    if (selenium.isTextPresent("user" + userNr.toString())) {
                        break;
                    }
                } catch (Exception e) {
                }
                Thread.sleep(250);
            }
        }
        assertTrue(selenium.isTextPresent("First" + Constants.nrUsers.toString() + " " + "Last" + Constants.nrUsers.toString()));
     }

    @Test
    public void testRemoveUsers() throws Exception {
        /* Delet the added resource again */
        if (Constants.removeUsersAtEnd) {
            //navigate to list of users
            selenium.click("menuForm:menuBar:users:deleteUsers:out");
            for (int time = 0;; time++) {
                if (time >= 60) {
                    fail("timeout");
                }
                try {
                    if (selenium.isElementPresent("admin-content:userTable:userName")) {
                        break;
                    }
                } catch (Exception e) {
                }
                Thread.sleep(250);
            }
            // list resources
            selenium.click("admin-content:deleteAllUsers");
            Thread.sleep(1000);
            selenium.click("admin-content:deleteUser");
            Thread.sleep(5000);
            assertFalse(selenium.isTextPresent("First" + Constants.nrUsers.toString() + " " + "Last" + Constants.nrUsers.toString()));
        }
       
    }

    @After
    @Override
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
