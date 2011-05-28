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
 * "Portions Copyrighted 2011 [name of copyright owner]"
 *
 * $Id$
 */
package com.forgerock.openidm.model.test;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.service.ResourceAccessInterface;
import com.forgerock.openidm.provisioning.service.ResourceConnector;
import com.forgerock.openidm.util.Base64;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectReferenceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Vilo Repan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context-model.xml", "classpath:application-context-repository.xml", "classpath:application-context-repository-test.xml", "classpath:application-context-provisioning.xml", "classpath:application-context-model-test.xml"})
public class ModelServiceTest {

    private static final Trace trace = TraceManager.getTrace(ModelServiceTest.class);
    @Autowired(required = true)
    ModelPortType modelService;
    @Autowired(required = true)
    RepositoryPortType repositoryService;
    @Autowired(required = true)
    private ResourceAccessInterface rai;

    @Test
    public void createDefaultUserAccounts() throws Exception {
        String resourceOid = null;
        String userOid = null;
        String accountOid = null;
        try {
            ResourceType resource = ((JAXBElement<ResourceType>) JAXBUtil.unmarshal(
                    new File("src/test/resources/resource-simple.xml"))).getValue();
            UserType user = ((JAXBElement<UserType>) JAXBUtil.unmarshal(new File(
                    "src/test/resources/user-default-accounts.xml"))).getValue();

            resourceOid = resource.getOid();
            ObjectContainerType container = new ObjectContainerType();
            container.setObject(resource);

            //test objects
            skipTestsIfExists(resource.getOid());
            skipTestsIfExists(user.getOid());

            //mocking repository
            repositoryService.addObject(container);
            //mock provisioning
            ResourceConnector c = new ResourceConnector(resource) {

                @Override
                public Object getConfiguration() {
                    throw new UnsupportedOperationException("Get configuration method not implemented - mock.");
                }
            };
            when(rai.getConnector()).thenReturn(c);

            Answer answer = new Answer<Object>() {

                ResourceObject object;

                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    if ("get".equals(invocation.getMethod().getName())) {
                        return object;
                    }
                    object = (ResourceObject) invocation.getArguments()[1];

                    ResourceObjectShadowType shadow = (ResourceObjectShadowType) invocation.getArguments()[2];
                    List<Element> elements = shadow.getAttributes().getAny();
                    for (Element element : elements) {
                        ResourceAttribute attribute = object.getValue(new QName(element.getNamespaceURI(),
                                element.getLocalName()));
                        if (attribute != null) {
                            attribute.addJavaValue(element.getTextContent());
                        }
                    }
                    return object;
                }
            };
            when(rai.add(any(OperationalResultType.class), any(ResourceObject.class),
                    any(ResourceObjectShadowType.class))).thenAnswer(answer);
            when(rai.get(any(OperationalResultType.class), any(ResourceObject.class))).thenAnswer(answer);

            //test begins
            container = new ObjectContainerType();
            container.setObject(user);
            userOid = modelService.addObject(container);

            container = repositoryService.getObject(userOid, new PropertyReferenceListType());
            user = (UserType) container.getObject();

            //test user
            assertNotNull(user);
            assertEquals("chivas", user.getName());
            assertEquals("Chivas Regal", user.getFullName());
            assertEquals("Chivas", user.getGivenName());
            assertEquals("Regal", user.getFamilyName());
            assertEquals(1, user.getEMailAddress().size());
            assertEquals("chivas@regal.com", user.getEMailAddress().get(0));
            //test user account
            assertEquals(1, user.getAccountRef().size());
            ObjectReferenceType accountRef = user.getAccountRef().get(0);
            accountOid = accountRef.getOid();

            container = modelService.getObject(accountOid, new PropertyReferenceListType());
            AccountShadowType account = (AccountShadowType) container.getObject();
            //test account credentials
            assertEquals(resource.getOid(), account.getResourceRef().getOid());
            assertNotNull(account.getCredentials());
            assertNotNull(account.getCredentials().getPassword());
            assertNotNull(account.getCredentials().getPassword().getAny());

            Element element = (Element) account.getCredentials().getPassword().getAny();
            assertNotNull(element.getTextContent());
            assertEquals(4, new String(Base64.decode(element.getTextContent())).length());
            //test account attributes
            assertEquals("uid=chivas,ou=people,dc=example,dc=com", getAttributeValue("http://openidm.forgerock." +
                    "com/xml/ns/public/resource/idconnector/resource-schema-1.xsd", "__NAME__", account));
            assertEquals("Chivas Regal", getAttributeValue("cn", account));
            assertEquals("Chivas", getAttributeValue("givenName", account));
            assertEquals("Regal", getAttributeValue("sn", account));
            assertEquals("Created by IDM", getAttributeValue("description", account));
        } finally {
            deleteObject(accountOid);
            deleteObject(resourceOid);
            deleteObject(userOid);
        }
    }

    private String getAttributeValue(String namespace, String name, AccountShadowType account) {
        ResourceObjectShadowType.Attributes attributes = account.getAttributes();
        List<Element> elements = attributes.getAny();
        for (Element element : elements) {
            if (namespace.equals(element.getNamespaceURI()) && element.getLocalName().equals(name)) {
                return element.getTextContent();
            }
        }

        return null;
    }

    private String getAttributeValue(String name, AccountShadowType account) {
        return getAttributeValue("http://openidm.forgerock.com/xml/ns/public/resource/instances/" +
                "a1a1a1a1-76e0-48e2-86d6-3d4f02d3e1a2", name, account);
    }

    private void deleteObject(String oid) {
        if (oid == null) {
            return;
        }
        trace.info("Test cleanup: Removing object '{}'", oid);
        try {
            repositoryService.deleteObject(oid);
        } catch (Exception ex) {
            trace.error("Couldn't delete '{}', reason: {}", new Object[]{oid, ex.getMessage()});
        }
    }

    private void skipTestsIfExists(String oid) {
        try {
            repositoryService.getObject(oid, new PropertyReferenceListType());

            //delete
            repositoryService.deleteObject(oid);
            //skip
//            fail("Object with oid '" + oid + "'");
        } catch (Exception ex) {
        }
    }
}
