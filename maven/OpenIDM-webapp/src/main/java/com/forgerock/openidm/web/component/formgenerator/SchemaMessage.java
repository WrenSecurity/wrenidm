package com.forgerock.openidm.web.component.formgenerator;

import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlMessage;

/**
 *
 * @author lazyman
 */
@FacesComponent(value="schemaMessage")
public class SchemaMessage extends HtmlMessage {

    @Override
    public String getRendererType() {
        return "schemaMessageRenderer";
    }
}
