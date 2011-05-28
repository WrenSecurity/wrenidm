package com.forgerock.openidm.web.component.formgenerator.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Vilo Repan
 */
@FacesConverter(value = "formShortConverter")
public class FormShortConverter extends FormIntegerConverter {

    public static final String CONVERTER_ID = "formShortConverter";

    public FormShortConverter() {
        super();
        init(Integer.valueOf(Short.MIN_VALUE), Integer.valueOf(Short.MAX_VALUE));
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Integer number = (Integer) super.getAsObject(context, component, value);
        if (number == null) {
            return null;
        }

        return new Short(number.shortValue());
    }
}
