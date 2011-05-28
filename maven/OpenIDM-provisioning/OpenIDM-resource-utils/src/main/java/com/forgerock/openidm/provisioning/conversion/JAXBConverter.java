package com.forgerock.openidm.provisioning.conversion;

import com.forgerock.openidm.api.exceptions.OpenIDMException;
import com.forgerock.openidm.provisioning.util.ShadowUtil;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Convert from/to Jaxb POJO-s.
 *
 * @author elek
 */
public class JAXBConverter implements Converter {

    private Document d;

    private JAXBContext context;

    private Class type;

    private String namespace;

    public JAXBConverter(String namespace, Class type) {
        this.namespace = namespace;
        this.type = type;
        try {
            d = ShadowUtil.getXmlDocument();
            context = JAXBContext.newInstance(type, Container.class);
        } catch (JAXBException ex) {
            throw new OpenIDMException("Error on creating JAXBContext", ex);
        }
    }

    @Override
    public QName getXmlType() {
        return new QName(namespace, type.getSimpleName());
    }

    @Override
    public Collection<Class> getJavaTypes() {
        return Arrays.asList(new Class[]{type});
    }

    @Override
    public Object convertToJava(Node node) {
        try {
            Object o = context.createUnmarshaller().unmarshal(node);
            if (o instanceof JAXBElement) {
                return ((JAXBElement) o).getValue();
            } else {
                return o;
            }
        } catch (JAXBException ex) {
            throw new OpenIDMException("Error on convertion object " + node, ex);
        }

    }

    @Override
    public Node convertToXML(QName name, Object o) {
        try {
            Node n = d.createElement("container");
            context.createMarshaller().marshal(new Container(o), n);
            //first child is from <container> Node, the sedond is from the Container object
            return n.getFirstChild().getFirstChild();
        } catch (JAXBException ex) {
            throw new OpenIDMException("Error on convertion object " + o, ex);
        }
    }

    @XmlRootElement
    public static class Container {

        private Object child;

        public Container() {
        }

        public Container(Object child) {
            this.child = child;
        }

        public Object getChild() {
            return child;
        }

        public void setChild(Object child) {
            this.child = child;
        }
    }
}
