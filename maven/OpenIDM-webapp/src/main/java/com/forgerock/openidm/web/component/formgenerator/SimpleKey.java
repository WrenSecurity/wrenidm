package com.forgerock.openidm.web.component.formgenerator;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 *
 * @author Vilo Repan
 */
public class SimpleKey implements Serializable {

    private QName qname;

    public SimpleKey(QName qname) {
        this.qname = qname;
    }

    public SimpleKey(String namespace, String name) {
        this(new QName(namespace, name));
    }

    public String getName() {
        return qname.getLocalPart();
    }

    public String getNamespace() {
        return qname.getNamespaceURI();
    }

    @Override
    public String toString() {
        return qname.toString();
    }

    @Override
    public int hashCode() {
//        int hashCode = super.hashCode();

        return qname.hashCode();// + hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof SimpleKey)) {
            return false;
        }

        SimpleKey key = (SimpleKey) obj;
        return qname != null ? qname.equals(key.qname) : key.qname == null;
    }
}
