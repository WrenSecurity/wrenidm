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
package com.forgerock.openidm.provisioning.integration.identityconnector;

import com.forgerock.openidm.provisioning.integration.identityconnector.schema.ResourceUtils;
import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.provisioning.service.AttributeChange;
import com.forgerock.openidm.provisioning.service.SynchronizationResult;
import com.forgerock.openidm.provisioning.util.ShadowUtil;
import com.forgerock.openidm.test.ldap.OpenDJUnitTestAdapter;
import com.forgerock.openidm.test.util.SampleObjects;
import com.forgerock.openidm.test.util.TestUtil;
import com.forgerock.openidm.util.DOMUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyModificationTypeType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceStateType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import org.junit.Test;
import org.opends.server.protocols.internal.InternalSearchOperation;
import org.opends.server.types.Attribute;
import org.opends.server.types.DereferencePolicy;
import org.opends.server.types.SearchResultEntry;
import org.opends.server.types.SearchScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author elek
 */
public class IdentityConnectorRAIIntegrationTest extends OpenDJUnitTestAdapter {

    ConnectorFactory f = new ConnectorFactory();

    @BeforeClass
    public static void startLdap() throws Exception {
        startACleanDJ();

        //TODO for ldap based synchronization test replication is required. Currently the
        // code below is not working
        
//        String[] args = new String[]{
//            "--configClass", "org.opends.server.extensions.ConfigFileHandler",
//            "--configFile", "target/test-data/opendj/config/config.ldif",
//            "enable",
//            "--host1", "localhost",
//            "--port1", "14444",
//            "--bindDN1", "cn=Directory Manager",
//            "--bindPassword1", "password",
//            "--trustAll",
//            "--onlyReplicationServer1",
//            "--replicationPort1", "8989",
//            "--baseDN", "dc=forgerock,dc=org",
//            "--no-prompt",
//            "--adminUID", "admin2",
//            "--adminPassword", "password2"
//        };
//        for (String arg : args) {
//            System.out.print(arg + " ");
//        }
//        System.getProperties().setProperty("org.opends.server.dsreplicationcallstatus", "true");
//        System.getProperties().setProperty("org.opends.server.scriptName", "dsreplication");
//        ReplicationCliMain.mainCLI(args);

    }

    @AfterClass
    public static void stopLdap() throws Exception {
        stopDJ();




    }

    @Before
    public void createDatabase() throws Exception {
        f.createDatabase();




    }

    @After
    public void dropDatabase() throws Exception {
        f.deleteDatabase();




    }

    @Test
    public void syncrhonizationWithDatabase() throws Exception {

        //given
        ResourceType ff = (ResourceType) TestUtil.getSampleObject(SampleObjects.RESOURCETYPE_LOCALHOST_DATABASETABLE);
        IdentityConnector connector = new IdentityConnector(ff);

        IdentityConnectorRAI rai = new IdentityConnectorRAI();
        rai.initialise(
                IdentityConnectorRAI.class, connector);

        OperationalResultType result = new OperationalResultType();


        String ns = ff.getNamespace();

        ResourceObjectDefinition def = connector.getSchema().getObjectDefinition(new QName(ns, "Account"));

        ResourceObject id = new ResourceObject(def);

        id.addValue(ResourceUtils.ATTRIBUTE_UID, true).addJavaValue("a812b7c2-24f3-4b93-8451-08c816a6e92a");

        ResourceStateType.SynchronizationState token = new ResourceStateType.SynchronizationState();
        Document doc = ShadowUtil.getXmlDocument();
        QName syncTokenName = IdentityConnectorRAI.SYNC_TOKEN_ATTRIBUTE;
        Element e = doc.createElementNS(syncTokenName.getNamespaceURI(),syncTokenName.getLocalPart());
        e.appendChild(doc.createTextNode("1"));
        e.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:type", "xsd:integer");
        e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + "xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        System.out.println(DOMUtil.printDom(e));
        token.getAny().add(e);

        //when

        SynchronizationResult ret = rai.synchronize(token, result, def);

        //then
        Assert.assertTrue(ret.getChanges().size() > 0);
    }



    @Test
    @Ignore()
    public void modify() throws Exception {

        //given
        ResourceType ff = (ResourceType) TestUtil.getSampleObject(SampleObjects.RESOURCETYPE_LOCALHOST_OPENDJ);

        IdentityConnector connector = new IdentityConnector(ff);

        IdentityConnectorRAI rai = new IdentityConnectorRAI();
        rai.initialise(
                IdentityConnectorRAI.class, connector);

        OperationalResultType result = new OperationalResultType();


        String ns = ff.getNamespace();

        ResourceObjectDefinition def = connector.getSchema().getObjectDefinition(new QName(ns, "Account"));
//        BasicSchemaElements.addElementsToResourceSchema(def);

        ResourceObject id = new ResourceObject(def);

        id.addValue(ResourceUtils.ATTRIBUTE_UID, true).addJavaValue("a812b7c2-24f3-4b93-8451-08c816a6e92a");

//      comment if you implement activation well
//        ActivationType disable = new ActivationType();
//        disable.setEnabled(Boolean.FALSE);
//        attrsToModify.addValue(new QName(SchemaConstants.NS_C, "activation"), true).addJavaValue(disable);



        // TODO: This will fail, as these are not initialized

        QName attrQName = new QName(ns, "givenName");

        ResourceAttributeDefinition attributeDefinition = def.getAttributeDefinition(attrQName);

        ResourceAttribute resourceAttribute = new ResourceAttribute(attributeDefinition);

        resourceAttribute.addJavaValue("newGivenName");
        AttributeChange attributeChange = new AttributeChange();

        attributeChange.setChangeType(PropertyModificationTypeType.replace);

        attributeChange.setAttribute(resourceAttribute);
        Set<AttributeChange> changes = new HashSet<AttributeChange>();

        changes.add(attributeChange);
        //when
        ResourceObject ro = rai.modify(result, id, def, changes);

        //then
        Assert.assertNotNull(ro);
        ResourceAttribute attr = ro.getValue(ResourceUtils.ATTRIBUTE_NAME);

        Assert.assertEquals("uid=jbond,ou=People,dc=forgerock,dc=org",
                attr.getSingleJavaValue(String.class));

        //check if the attribute is changed

        LinkedHashSet<String> attributes = new LinkedHashSet();
        attributes.add(
                "ds-pwp-account-disabled");
        attributes.add(
                "givenName");
        InternalSearchOperation op = controller.getInternalConnection().processSearch(
                "dc=forgerock,dc=org",
                SearchScope.WHOLE_SUBTREE,
                DereferencePolicy.NEVER_DEREF_ALIASES,
                100,
                100,
                false,
                "(uid=jbond)",
                attributes);

        Assert.assertEquals(1, op.getEntriesSent());
        SearchResultEntry response = op.getSearchEntries().get(0);

        assertAttribute(response, "givenName",
                "newGivenName");


        //should be uncommented after fixation
        //assertAttribute(response,"ds-pwp-account-disabled","true");





    }

    protected void assertAttribute(SearchResultEntry response, String name, String value) {
        Assert.assertNotNull(response.getAttribute(name.toLowerCase()));
        Assert.assertEquals(1, response.getAttribute(name.toLowerCase()).size());
        Attribute givenName = response.getAttribute(name.toLowerCase()).get(0);
        Assert.assertEquals("1", givenName.iterator().next().getValue().toString());
        Assert.assertEquals(value, givenName.iterator().next().getValue().toString());
    }
}
