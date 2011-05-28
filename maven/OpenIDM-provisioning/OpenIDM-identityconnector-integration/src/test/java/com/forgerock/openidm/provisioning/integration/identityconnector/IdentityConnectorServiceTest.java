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

import java.util.Collection;
import java.util.List;
import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

/**
 *
 * @author laszlohordos
 */
public class IdentityConnectorServiceTest {

    ConnectorFactory f = new ConnectorFactory();

    @Before
    public void createDatabase() throws Exception {
        f.createDatabase();
    }

    @After
    public void dropDatabase() throws Exception {
        f.deleteDatabase();
    }

    @Test
    public void doSynchronization() throws Exception{
          //given

        IdentityConnectorService connectorService = new IdentityConnectorService();
        ConnectorFacade connector = f.createTestDbConnector();


        //when        
        Collection<SyncDelta> deltas = connectorService.doSyncronization(connector, ObjectClass.ACCOUNT, 1);

        //then
        assertTrue(deltas.size()>0);
    }


    @Test
    public void doGetConnectorObject() throws Exception {
        //given

        IdentityConnectorService connectorService = new IdentityConnectorService();
        ConnectorFacade connector = f.createTestDbConnector();
        Uid uid = new Uid("1");

        //when
        Set<Attribute> attributes = new HashSet();
        attributes.add(AttributeBuilder.build("attr1"));
        ConnectorObject object = connectorService.doGetConnectorObject(connector, ObjectClass.ACCOUNT, uid, attributes, null);

        //then
        Assert.assertNotNull(object);
        Assert.assertEquals(new Uid("1"), object.getUid());

        //todo it shoud be a name string
        Assert.assertEquals("1", object.getName().getNameValue());
        Assert.assertEquals("value1", object.getAttributeByName("attr1").getValue().get(0));


    }

    @Test
    public void doMofidyConnectorObject() throws Exception {
        //given
        IdentityConnectorService connectorService = new IdentityConnectorService();
        ConnectorFacade connector = f.createTestDbConnector();

        Set<Attribute> attributes = new HashSet();
        Attribute attr = AttributeBuilder.build("attr1", "value2");
        attributes.add(attr);
        Uid uid = new Uid("1");
        //when
        connectorService.doModifyReplaceConnectorObject(connector, ObjectClass.ACCOUNT, uid, attributes, null);

        // TODO: test more than just a replace

        //then

        Connection con = f.createConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT * FROM account where id = '" + uid.getUidValue() + "'");

        assertTrue(rs.next());

        assertEquals("value2", rs.getString("attr1"));
        con.close();
    }

    @Test
    public void doCreateConnectorObject() throws Exception {
        //given
        IdentityConnectorService connectorService = new IdentityConnectorService();
        ConnectorFacade connector = f.createTestDbConnector();

        Set<Attribute> attributes = new HashSet();
        attributes.add(AttributeBuilder.build(Name.NAME, "name"));

        //when
        Uid uid = connectorService.doCreateConnectorObject(connector, ObjectClass.ACCOUNT, attributes, null);

        //then
        assertNotNull(uid);
        assertNotNull(uid.getUidValue());

        Connection con = f.createConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT * FROM account where id = '" + uid.getUidValue() + "'");

        assertTrue(rs.next());

        assertEquals("name", rs.getString("id"));
        con.close();
    }

    @Test
    public void doDeleteConnectorObject() throws Exception {
        //given
        IdentityConnectorService connectorService = new IdentityConnectorService();
        ConnectorFacade connector = f.createTestDbConnector();

        Set<Attribute> attributes = new HashSet();
        attributes.add(AttributeBuilder.build(Name.NAME, "1"));

        //when
        connectorService.doDeleteConnectorObject(connector, ObjectClass.ACCOUNT, new Uid("1"), null);

        //then

        Connection con = f.createConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT * FROM account ");

        assertFalse(rs.next());
        con.close();

    }

//    @Test
    @Ignore
    public void createDefinitionFromSchema() throws Exception {
        ConnectorFacade facade = f.createTestICFConnector();
        IdentityConnectorService s = new IdentityConnectorService();
        String nameSpace = "http://testns";

        //This method is not safe because not all bundle implements the Schema interface. The ResourceObjectDefinition must come from top
//        ResourceObjectDefinition rod = s.createDefinitionFromSchema(facade.schema(), nameSpace, "__ACCOUNT__");
//        Assert.assertEquals(3, rod.getAttributes().size());
//        for (ResourceAttributeDefinition rad : rod.getAttributes()) {
//            System.out.println(rad.getQName());
//        }
//        Assert.assertEquals(new QName(SchemaConstants.NS_XSD, "string"), rod.getAttributeDefinition(new QName(nameSpace, "name")).getType());


    }

    @Test
    @Ignore
    public void buildResourceObject() throws Exception {
        //given
        String namespace = "http://ns";
        ConnectorFacade facade = f.createTestICFConnector();
        IdentityConnectorService s = new IdentityConnectorService();

        ConnectorObject co = facade.getObject(ObjectClass.ACCOUNT, new Uid("1"), null);
        Assert.assertNotNull(co);

        //when
        ResourceObject ro = null;//s.buildResourceObject(facade, co, namespace);

        //then
        for (ResourceAttribute attr : ro.getValues()) {
            System.out.println(attr.getDefinition().getQName());
            for (Node n : attr.getValues()) {

                System.out.println(n);
            }
        }
        Assert.assertEquals(3, ro.getValues().size());


    }
//   
}
