package com.forgerock.openidm.provisioning.conversion;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;

/**
 * Convert between XML fragement and Java values.
 * 
 * @author elek
 */
public interface Converter {

    public QName getXmlType();

    public Collection<Class> getJavaTypes();

    public Object convertToJava(Node node);

    public Node convertToXML(QName name, Object o);
}
