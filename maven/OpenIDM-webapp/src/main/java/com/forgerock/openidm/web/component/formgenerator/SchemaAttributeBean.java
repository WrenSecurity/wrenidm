package com.forgerock.openidm.web.component.formgenerator;

import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.event.ActionEvent;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vilo Repan
 */
public class SchemaAttributeBean implements Serializable {

    private static transient Logger logger = LoggerFactory.getLogger(SchemaAttributeBean.class);
    private ResourceAttributeDefinition attribute;
    private List<Object> values = new ArrayList<Object>();

    public SchemaAttributeBean(ResourceAttributeDefinition attribute) {
        this(attribute, null);
    }

    public SchemaAttributeBean(ResourceAttributeDefinition attribute, List<Object> values) {
        if (attribute == null) {
            throw new IllegalArgumentException("Schema attribute can't be null.");
        }

        this.attribute = attribute;
        if (values != null) {
            this.values = values;
        }
    }

    public List<Object> getValues() {
        return values;
    }

    public String getIdentifier() {
        QName qname = attribute.getQName();
        return qname.getNamespaceURI() + qname.getLocalPart();
    }

    public int getValueListSize() {
        return values.size();
    }

    public Object getValue(Key key) {
        if (!isKeyNameValid(key) || !isKeyIndexValid(key)) {
            return null;
        }

        return values.get(key.getIndex());
    }

    public void setValue(Key key, Object value) {
        if (value == null) {
            value = new NullAttributeValue();
        }

        if (!isKeyNameValid(key)) {
            return;
        }

        if (key.getIndex() == -1) {
            values.add(value);
        } else {
            values.set(key.getIndex(), value);
        }
    }

    private boolean isKeyIndexValid(Key key) {
        if (key == null) {
            return false;
        }

        if (key.getIndex() < 0 || key.getIndex() >= values.size()) {
            return false;
        }

        return true;
    }

    private boolean isKeyNameValid(Key key) {
        if (key == null) {
            return false;
        }

        QName qname = attribute.getQName();
        if (!qname.getLocalPart().equals(key.getName())) {
            System.out.println("bad key for attribute...");
            return false;
        }

        if (!qname.getNamespaceURI().equals(key.getNamespace())) {
            System.out.println("bad key for attribute...");
            return false;
        }

        return true;
    }

    public ResourceAttributeDefinition getAttribute() {
        return attribute;
    }

    public boolean canAddValue() {
        if (attribute.getMaxOccurs() == -1) {
            return true;
        }

        if (attribute.getMaxOccurs() > values.size()) {
            return true;
        }

        return false;
    }

    public boolean canRemoveValue() {
        if (attribute.getMinOccurs() < values.size()) {
            return true;
        }

        return false;
    }

    public void addValue(ActionEvent evt) {
        logger.trace("addValue: " + attribute.getQName());
        values.add(new NullAttributeValue());
    }

    public void removeValue(ActionEvent evt) {
        int index = (Integer) evt.getComponent().getAttributes().get(SchemaFormGrid.BEAN_VALUE_INDEX);
        logger.trace("removeValue: " + attribute.getQName() + values.get(index));
        values.remove(index);
    }

    @Override
    public String toString() {
        return attribute.getQName() + ":" + values.size();
    }
}
