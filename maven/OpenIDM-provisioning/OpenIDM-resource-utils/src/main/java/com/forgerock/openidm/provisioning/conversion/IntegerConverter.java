package com.forgerock.openidm.provisioning.conversion;

import com.forgerock.openidm.provisioning.util.ShadowUtil;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author elek
 */
public class IntegerConverter implements Converter {

    private Collection<Class> supportedClasses;

    public IntegerConverter() {
        supportedClasses = Arrays.asList(new Class[]{Integer.class, int.class});
    }

    @Override
    public QName getXmlType() {
        return new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int");
    }

    @Override
    public Collection<Class> getJavaTypes() {
        return supportedClasses;
    }

    @Override
    public Object convertToJava(Node node) {
        return Integer.valueOf(node.getTextContent());
    }

    @Override
    public Node convertToXML(QName qname, Object o) {
        Document d = ShadowUtil.getXmlDocument();
        Element e = d.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
        e.appendChild(d.createTextNode("" + o));
        return e;
    }
}
