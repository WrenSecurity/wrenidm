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

import com.forgerock.openidm.api.exceptions.OpenIDMException;
import com.forgerock.openidm.provisioning.integration.identityconnector.schema.ResourceUtils;
import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.test.util.SampleObjects;
import com.forgerock.openidm.test.util.TestUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import java.sql.Connection;
import java.sql.ResultSet;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;

/**
 *
 * @author elek
 */
public class IdentityConnectorRAITest {


    @Test
    public void get() throws OpenIDMException {
        //given
        ResourceType ff = (ResourceType) TestUtil.getSampleObject(SampleObjects.RESOURCETYPE_FLATFILE);

        IdentityConnector connector = new IdentityConnector(ff);

        IdentityConnectorRAI rai = new IdentityConnectorRAI();
        rai.initialise(IdentityConnectorRAI.class, connector);

        OperationalResultType result = new OperationalResultType();
        ResourceObjectDefinition def = connector.getSchema().getObjectDefinition(new QName("http://openidm.forgerock.com/xml/ns/public/resource/instances/eced6f20-df52-11df-9e9a-0002a5d5c51b", "Account"));
        ResourceObject id = new ResourceObject(def);
        id.addValue(ResourceUtils.ATTRIBUTE_UID).addJavaValue("1");

        //when
        ResourceObject ro = rai.get(result, id);

        //then
        Assert.assertNotNull(ro);
        ResourceAttribute attr = ro.getValue(ResourceUtils.ATTRIBUTE_NAME);

        Assert.assertEquals("1", attr.getSingleJavaValue(String.class));

    }

    @Test
    public void delete() throws Exception {
        //given
        ConnectorFactory f = new ConnectorFactory();
        f.createDatabase();

        ResourceType ff = (ResourceType) TestUtil.getSampleObject(SampleObjects.RESOURCETYPE_LOCALHOST_DATABASETABLE);

        IdentityConnector connector = new IdentityConnector(ff);

        IdentityConnectorRAI rai = new IdentityConnectorRAI();
        rai.initialise(IdentityConnectorRAI.class, connector);

        OperationalResultType result = new OperationalResultType();
        ResourceObjectDefinition def = connector.getSchema().getObjectDefinition(new QName("http://openidm.forgerock.com/xml/ns/public/resource/instances/aae7be60-df56-11df-8608-0002a5d5c51b", "Account"));


        ResourceObject id = new ResourceObject(def);
        id.addValue(ResourceUtils.ATTRIBUTE_UID).addJavaValue("1");

        //when
        rai.delete(result, id);

        //then
        Connection con = null;
        try {
            con = f.createConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT * from account where id = '1'");
            Assert.assertFalse(rs.next());
        } finally {
            if (con != null) {
                con.close();
            }
        }

        f.deleteDatabase();

    }
}
