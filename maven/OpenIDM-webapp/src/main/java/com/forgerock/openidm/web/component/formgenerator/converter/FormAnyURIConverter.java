package com.forgerock.openidm.web.component.formgenerator.converter;

import java.net.URI;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Vilo Repan
 */
@FacesConverter(value = "formAnyURIConverter")
public class FormAnyURIConverter implements Converter {

    public static final String CONVERTER_ID = "formAnyURIConverter";

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return null;
        }

        try {
            return URI.create(value);
        } catch (Exception ex) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Uri '" +
                    value + "' violates RFC 2396.", "Uri violates RFC 2396."));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }
}
