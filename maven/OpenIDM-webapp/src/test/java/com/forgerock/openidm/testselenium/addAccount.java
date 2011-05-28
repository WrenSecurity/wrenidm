
package com.forgerock.openidm.testselenium;

import com.thoughtworks.selenium.*;
//import java.util.regex.Pattern;

public class addAccount extends SeleneseTestCase {
    @Override
        public void setUp() throws Exception {
                setUp("http://localhost:8080/idm", "*chrome");
        }
        public void testAdd() throws Exception {
                selenium.open("/idm/login.iface");
                selenium.type("loginForm:userName", "administrator");
                selenium.type("loginForm:password", "secret");
                selenium.click("loginForm:loginButton");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("menuForm:menuBar")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                selenium.click("menuForm:menuBar:users:listUsers:out");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("listUserForm:userTable")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                selenium.click("listUserForm:userTable:1:fullName");
                selenium.click("listUserForm:listButton");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("detailUserPanelGroup")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                selenium.click("addButtonForm:addButton");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("selectAccountForm:selectAccount")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                selenium.select("selectAccountForm:selectAccount", "Localhost OpenDS3");
                selenium.click("selectAccountForm:addAccountButton");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("generatedForm:collapsiblePanel")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                selenium.click("generatedForm:collapsiblePanel");
                selenium.click("generatedForm:linkHeader");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("generatedForm:submitFormButton")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                selenium.click("generatedForm:submitFormButton");
                for (int second = 0;; second++) {
                        if (second >= 60) fail("timeout");
                        try { if (selenium.isVisible("detailUserPanelGroup")) break; } catch (Exception e) {}
                        Thread.sleep(1000);
                }

                verifyTrue(selenium.isTextPresent(""));
        }
}
