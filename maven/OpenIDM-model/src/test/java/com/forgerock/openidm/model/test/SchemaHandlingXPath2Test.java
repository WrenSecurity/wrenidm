package com.forgerock.openidm.model.test;

import com.forgerock.openidm.model.xpath.SchemaHandling;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import java.io.File;
import javax.xml.bind.JAXBElement;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context-model.xml", "classpath:application-context-repository.xml", "classpath:application-context-repository-test.xml", "classpath:application-context-provisioning.xml", "classpath:application-context-model-test.xml"})
public class SchemaHandlingXPath2Test {

    @Autowired
    SchemaHandling schemaHandling;

    public SchemaHandlingXPath2Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIfThenElseSupportInOutboundSection() throws Exception {
        JAXBElement<AccountShadowType> accountJaxb = (JAXBElement<AccountShadowType>) JAXBUtil.unmarshal(new File("src/test/resources/account-xpath2.xml"));
        JAXBElement<UserType> userJaxb = (JAXBElement<UserType>) JAXBUtil.unmarshal(new File("src/test/resources/user-xpath2.xml"));
        ResourceObjectShadowType appliedAccountShadow = schemaHandling.applyOutboundSchemaHandlingOnAccount(userJaxb.getValue(), accountJaxb.getValue(), accountJaxb.getValue().getResource()) ;
        assertEquals("__NAME__", appliedAccountShadow.getAttributes().getAny().get(0).getLocalName());
        assertEquals("James Bond 007", appliedAccountShadow.getAttributes().getAny().get(0).getTextContent());
    }
}