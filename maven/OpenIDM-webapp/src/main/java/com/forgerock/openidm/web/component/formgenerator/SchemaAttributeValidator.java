package com.forgerock.openidm.web.component.formgenerator;

import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import java.util.List;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.validator.FacesValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vilo Repan
 */
@FacesValidator(value = "schemaAttributeValidator")
public class SchemaAttributeValidator extends AbstractStateHolder implements Converter {

    private static transient Logger logger = LoggerFactory.getLogger(SchemaAttributeValidator.class);
    private ValueExpression bean;
    private String attributeId;
    private transient Converter customConverter;

    public SchemaAttributeValidator() {
        this(null, null, null);
    }

    public SchemaAttributeValidator(ValueExpression bean, String attributeId, Converter customConverter) {
        this.bean = bean;
        this.attributeId = attributeId;
        this.customConverter = customConverter;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object object = null;
        if (customConverter != null) {
            object = customConverter.getAsObject(context, component, value);
        } else {
            object = value;
        }

        validateValues(context, object);

        return object;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (customConverter != null) {
            return customConverter.getAsString(context, component, value);
        }

        if (value == null) {
            return "";
        }

        return value.toString();
    }

    public void validateValues(FacesContext context, Object value) {
        SchemaFormBean formBean = (SchemaFormBean) bean.getValue(context.getELContext());
        List<SchemaAttributeBean> list = formBean.getAttributeBeanList();
        SchemaAttributeBean attrBean = null;
        for (SchemaAttributeBean sab : list) {
            if (sab.getIdentifier().equals(attributeId)) {
                attrBean = sab;
                break;
            }
        }
        if (attrBean == null) {
            logger.warn("Can't validate attribute '" + attributeId + "' in form.");
            return;
        }

        List<Object> values = attrBean.getValues();
        int count = value == null ? 0 : 1;
        for (Object object : values) {
            if (object == null || object instanceof NullAttributeValue) {
                continue;
            }

            count++;
        }

        ResourceAttributeDefinition attribute = attrBean.getAttribute();
        if (attribute.getMinOccurs() > count) {
//            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Values count is too small.",
//                    "Attribute must have min. " + attribute.getMinOccurs() + " value(s).");
            throw new ConverterException("Attribute must have min. " + attribute.getMinOccurs() + " value(s).");
        }
    }

    public void setBean(ValueExpression bean) {
        this.bean = bean;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        bean = (ValueExpression) values[1];
        attributeId = (String) values[2];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] state = new Object[3];
        state[0] = super.saveState(context);
        state[1] = bean;
        state[2] = attributeId;

        return state;
    }
}
