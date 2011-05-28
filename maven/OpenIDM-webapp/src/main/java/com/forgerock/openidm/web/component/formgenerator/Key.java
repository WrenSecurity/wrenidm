package com.forgerock.openidm.web.component.formgenerator;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 *
 * @author Vilo Repan
 */
public class Key extends SimpleKey implements Serializable {

    private int index;

    public Key(QName qname, int index) {
        super(qname);
        this.index = index;
    }

    public Key(String namespace, String name, int index) {
        this(new QName(namespace, name), index);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return super.toString() + "_" + Integer.toString(index);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Key)) {
            return false;
        }

        Key key = (Key) obj;
        if (!super.equals(key)) {
            return false;
        }

        if (key.getIndex() != index) {
            return false;
        }

        return true;
    }
}
