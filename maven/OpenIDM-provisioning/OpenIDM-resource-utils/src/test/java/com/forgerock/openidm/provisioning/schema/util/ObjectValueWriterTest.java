package com.forgerock.openidm.provisioning.schema.util;

import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.provisioning.util.ShadowUtil;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;




import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * @author elek
 */
public class ObjectValueWriterTest {


   

    @Test
    public void buildResourceObject() throws JAXBException {
        //given
        ObjectValueWriter ovw = new ObjectValueWriter();
        ResourceSchema schema = TestUtil.createSampleSchema();
        ResourceObjectShadowType st = load(new File("src/test/resources/resourceshadow-example.xml"));

        //when
        ResourceObject ro = ovw.buildResourceObject(st, schema);

        //then
        ResourceAttribute value = ro.getValue(new QName(TestUtil.ICC, "__UID__"));
        assertNotNull(value);
        String uid = value.getSingleJavaValue(String.class);
        assertEquals("oidoidoid-heyheyhey", uid);     
    }

    @Test
    public void testWrite() throws Exception {
        //given
        Document doc = ShadowUtil.getXmlDocument();

        ResourceSchema schema = TestUtil.createSampleSchema();
        ResourceObjectDefinition def = schema.getObjectClassesCopy().get(0);
        ResourceObject o = new ResourceObject(def);

        assertNotNull(def.getAttributeDefinition(new QName(TestUtil.ICC, "__UID__", "icc")));
        ResourceAttribute attr1 = new ResourceAttribute(def.getAttributeDefinition(new QName(TestUtil.ICC, "__UID__", "icc")));

        Element e = doc.createElementNS(TestUtil.ICC, "__UID__");
        e.appendChild(doc.createTextNode("TEST"));
        attr1.addValue(e);
        o.addValue(attr1);

        Element rootElement = (Element) doc.appendChild(doc.createElement("root"));

        //when
        new ObjectValueWriter().write(o, rootElement);

        //then
        File of = new File("target/test.xml");
        FileWriter fw = new FileWriter(of);
        TestUtil.writeXml(doc.getDocumentElement(), fw);
        //FIXME implement real assertions
        System.out.println("see " + of.getAbsolutePath());

        fw.close();

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix.equals("icc")) {
                    return TestUtil.ICC;
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });
        PrefixResolver resolver = new PrefixResolverDefault(doc);


        XPathExpression expr = xpath.compile("//root/icc:__UID__/text()");
        Text result = (Text) expr.evaluate(doc, XPathConstants.NODE);
        assertEquals("TEST", result.getNodeValue());




    }

    private ResourceObjectShadowType load(File file) throws JAXBException {
        JAXBContext c = JAXBContext.newInstance(ResourceObjectShadowType.class);
        javax.xml.bind.Unmarshaller um = c.createUnmarshaller();
        return ((JAXBElement<ResourceObjectShadowType>) um.unmarshal(file)).getValue();
    }
}
