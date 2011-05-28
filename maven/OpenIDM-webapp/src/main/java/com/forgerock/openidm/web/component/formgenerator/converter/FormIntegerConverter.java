package com.forgerock.openidm.web.component.formgenerator.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.convert.IntegerConverter;

/**
 *
 * @author Vilo Repan
 */
@FacesConverter(value = "formIntegerConverter")
public class FormIntegerConverter extends IntegerConverter implements StateHolder {

    public static final String CONVERTER_ID = "formIntegerConverter";
    private boolean isTransient;
    private Integer minValue;
    private Integer maxValue;
//    private boolean canBeMin;
//    private boolean canBeMax;

    public void init(Integer minValue, Integer maxValue) {//, boolean canBeMin, boolean canBeMax) {
        this.minValue = minValue;
        this.maxValue = maxValue;
//        this.canBeMin = canBeMin;
//        this.canBeMax = canBeMax;
    }

//    public void setCanBeMax(boolean canBeMax) {
//        this.canBeMax = canBeMax;
//    }
//
//    public void setCanBeMin(boolean canBeMin) {
//        this.canBeMin = canBeMin;
//    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Integer number = (Integer) super.getAsObject(context, component, value);
        if (number == null) {
            return null;
        }

        if (minValue != null) {
//            if (canBeMin) {
                if (minValue > number) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "To low value.", "Value must be more than or equal '" + minValue + "'."));
//                    throw new ConverterException("Value must be more than '" + minValue + "'.");
                }
//            } else {
//                if (minValue >= number) {
////                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
////                            "To low value.", "Value must be equal or more than '" + minValue + "'."));
//                    throw new ConverterException("Value must be equal or more than '" + minValue + "'.");
//                }
//            }
        }

        if (maxValue != null) {
//            if (canBeMax) {
                if (maxValue < number) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "To high value.", "Value must be less than or equal '" + maxValue + "'."));
                }
//            } else {
//                if (maxValue >= number) {
//                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            "To high value.", "Value must be equal or less than '" + maxValue + "'."));
//                }
//            }
        }

        return number;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return super.getAsString(context, component, value);
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    public Object saveState(FacesContext context) {
        Object[] values = new Object[5];
        values[0] = isTransient;
        values[1] = minValue;
        values[2] = maxValue;
//        values[3] = canBeMin;
//        values[4] = canBeMax;

        return values;
    }

    public void restoreState(FacesContext context, Object object) {
        Object[] state = (Object[]) object;

        isTransient = (Boolean) state[0];
        minValue = (Integer) state[1];
        maxValue = (Integer) state[2];
//        canBeMin = (Boolean) state[3];
//        canBeMax = (Boolean) state[4];
    }
}
