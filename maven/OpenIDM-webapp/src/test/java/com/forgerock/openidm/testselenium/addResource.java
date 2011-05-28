package com.forgerock.openidm.testselenium;

//import com.forgerock.openidmtestselenium.Constants.*;
import com.thoughtworks.selenium.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class addResource extends SeleneseTestCase {


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
    public void testAddResource() throws Exception {
        /* Import a Resouce given in this script */
        selenium.click("menuForm:menuBar:home:out");
        selenium.click("menuForm:menuBar:configuration:importPage:out");
        Boolean break1 = true;
        for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isElementPresent("j_idt40:_t42")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
        // insert the resource XML
        selenium.type("j_idt40:j_idt43", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<c:objects xmlns:c=\"http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd\"\n           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n           xmlns:ri=\"http://openidm.forgerock.com/xml/ns/public/resource/instances/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2\">\n    <c:resource oid=\"ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2\">\n        <c:name>Localhost OpenDJ</c:name>\n        <c:type>http://openidm.forgerock.com/xml/ns/public/resource/idconnector/resourceaccessconfiguration-1.xsd</c:type>\n        <c:namespace>http://openidm.forgerock.com/xml/ns/public/resource/instances/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2</c:namespace>\n        <c:schema>\n            <xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n                        targetNamespace=\"http://openidm.forgerock.com/xml/ns/public/resource/instances/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2\"\n                        elementFormDefault=\"qualified\"\n                        xmlns:c=\"http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd\"\n                        xmlns:r=\"http://openidm.forgerock.com/xml/ns/public/resource/resource-schema-1.xsd\"\n                        xmlns:ri=\"http://openidm.forgerock.com/xml/ns/public/resource/instances/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2\"\n                        xmlns:ids=\"http://openidm.forgerock.com/xml/ns/public/resource/idconnector/resource-schema-1.xsd\">\n                <xsd:import namespace=\"http://openidm.forgerock.com/xml/ns/public/resource/idconnector/resource-schema-1.xsd\"/>\n                <xsd:import namespace=\"http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd\"/>\n                <xsd:import namespace=\"http://openidm.forgerock.com/xml/ns/public/resource/resource-schema-1.xsd\"/>\n                <xsd:complexType name=\"Account\">\n                    <xsd:annotation>\n                        <xsd:appinfo>\n                            <r:identifier ref=\"ids:__UID__\"/>\n                            <r:secondaryIdentifier ref=\"ids:__NAME__\"/>\n                            <r:displayName ref=\"ri:description\"/>\n                            <r:descriptionAttribute ref=\"ri:description\"/>\n                            <r:nativeObjectClass>__ACCOUNT__</r:nativeObjectClass>\n                            <r:accountType default=\"true\"/>\n                        </xsd:appinfo>\n                    </xsd:annotation>\n                    <xsd:complexContent>\n                        <xsd:extension base=\"r:ResourceObjectClass\">\n                            <xsd:sequence>\n                                <xsd:element ref=\"ids:__NAME__\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:nativeAttributeName>__NAME__</r:nativeAttributeName>\n                                            <r:attributeDisplayName>DN</r:attributeDisplayName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element minOccurs=\"0\" name=\"description\" type=\"xsd:string\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:help>DESCRIPTION_NAME_HELP_KEY</r:help>\n                                            <r:nativeAttributeName>description</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element ref=\"ids:__PASSWORD__\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:attributeFlag>PASSWORD</r:attributeFlag>\n                                            <r:classifiedAttribute>\n                                                <r:encryption>HASH</r:encryption>\n                                                <r:classificationLevel>secret</r:classificationLevel>\n                                            </r:classifiedAttribute>\n                                            <r:nativeAttributeName>__PASSWORD__</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element name=\"sn\" type=\"xsd:string\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:nativeAttributeName>sn</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element name=\"cn\" type=\"xsd:string\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:nativeAttributeName>cn</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element ref=\"ids:__UID__\" minOccurs=\"0\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:attributeFlag>NOT_UPDATEABLE</r:attributeFlag>\n                                            <r:nativeAttributeName>__UID__</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element minOccurs=\"0\" name=\"givenName\" type=\"xsd:string\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:nativeAttributeName>givenName</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                            </xsd:sequence>\n                        </xsd:extension>\n                    </xsd:complexContent>\n                </xsd:complexType>\n                <xsd:complexType name=\"Group\">\n                    <xsd:annotation>\n                        <xsd:appinfo>\n                            <r:identifier ref=\"ids:__UID__\"/>\n                            <r:secondaryIdentifier ref=\"ids:__NAME__\"/>\n                            <r:displayName ref=\"ri:description\"/>\n                            <r:descriptionAttribute ref=\"ri:description\"/>\n                            <r:nativeObjectClass>__GROUP__</r:nativeObjectClass>\n                            <r:container/>\n                        </xsd:appinfo>\n                    </xsd:annotation>\n                    <xsd:complexContent>\n                        <xsd:extension base=\"r:ResourceObjectClass\">\n                            <xsd:sequence>\n                                <xsd:element ref=\"ids:__NAME__\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:nativeAttributeName>__NAME__</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element minOccurs=\"0\" name=\"description\" type=\"xsd:string\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:help>DESCRIPTION_NAME_HELP_KEY</r:help>\n                                            <r:nativeAttributeName>description</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                                <xsd:element ref=\"ids:__UID__\">\n                                    <xsd:annotation>\n                                        <xsd:appinfo>\n                                            <r:attributeFlag>NOT_UPDATEABLE</r:attributeFlag>\n                                            <r:nativeAttributeName>__UID__</r:nativeAttributeName>\n                                        </xsd:appinfo>\n                                    </xsd:annotation>\n                                </xsd:element>\n                            </xsd:sequence>\n                        </xsd:extension>\n                    </xsd:complexContent>\n                </xsd:complexType>\n            </xsd:schema>\n        </c:schema>\n        <c:schemaHandling>\n            <c:accountType objectClass=\"ri:Account\" default=\"true\">\n                <c:name>Default Account</c:name>\n            </c:accountType>\n        </c:schemaHandling>\n        <c:configuration>\n            <idc:ConnectorConfiguration xmlns:idc=\"http://openidm.forgerock.com/xml/ns/public/resource/idconnector/configuration-1.xsd\"\n                                        xmlns:iccldap=\"http://openidm.forgerock.com/xml/ns/resource/idconnector/bundle/org.identityconnectors.ldap/org.identityconnectors.ldap.LdapConnector/1.0.5531\">\n                <idc:ConnectorRef bundleName=\"org.identityconnectors.ldap\" bundleVersion=\"1.0.5531\" connectorName=\"org.identityconnectors.ldap.LdapConnector\">\n                    <idc:ConnectorHostRef>/configuration/connectorHost[@oid='1234']/configuration</idc:ConnectorHostRef>\n                </idc:ConnectorRef>\n                <idc:BundleProperties>\n                    <iccldap:port>1389</iccldap:port>\n                    <iccldap:host>localhost</iccldap:host>\n                    <iccldap:baseContexts>dc=example,dc=com</iccldap:baseContexts>\n                    <iccldap:principal>cn=directory manager</iccldap:principal>\n                    <iccldap:credentials>password</iccldap:credentials>\n                </idc:BundleProperties>\n                <idc:PoolConfigOption minEvictTimeMillis=\"5000\" minIdle=\"5\" maxIdle=\"30\" maxObjects=\"120\" maxWait=\"5000\"/>\n                <idc:OperationTimeouts>\n                    <idc:OperationTimeout name=\"create\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"update\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"delete\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"test\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"scriptOnConnector\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"scriptOnResource\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"get\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"authenticate\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"search\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"validate\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"sync\" timeout=\"50000\"/>\n                    <idc:OperationTimeout name=\"schema\" timeout=\"50000\"/>\n                </idc:OperationTimeouts>\n            </idc:ConnectorConfiguration>\n        </c:configuration>\n    </c:resource>\n</c:objects>");
        //click the import button
        selenium.click("//a[@id='j_idt40:_t42']/span");
        for (int time = 0;; time++) {
            if (time >= 120) {fail("timeout");}
            try {if (selenium.isTextPresent("Localhost OpenDJ")) break;} catch (Exception e) {}
            Thread.sleep(250);
        }
        // check for the presence of the added resouce in the result page
        assertTrue(selenium.isTextPresent("Localhost OpenDJ"));
    }

    @Test public void testRemoveResource() throws Exception {

        /* Delet the added resource again */

        if (Constants.removeResourceAtEnd) {
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
            try {if (selenium.isElementPresent("j_idt30:0:j_idt31:j_idt38")) break;} catch (Exception e) {}
            Thread.sleep(250);
            }
            //delete the resource
            selenium.click("j_idt30:0:j_idt31:j_idt38");

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
