package com.forgerock.openidm.web.component.formgenerator;

import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;

/**
 *
 * @author Vilo Repan
 */
public class SchemaFormGridTag extends ComponentHandler {

    private final TagAttribute bean;
    private final TagAttribute editable;

    public SchemaFormGridTag(ComponentConfig config) {
        super(config);

        editable = getAttribute("editable");
        bean = getRequiredAttribute("bean");
        if (bean == null) {
            throw new TagAttributeException(bean, "Schema form bean has to be specified.");
        }
    }

    @Override
    public void setAttributes(FaceletContext ctx, Object instance) {
        super.setAttributes(ctx, instance);

        SchemaFormGrid grid = (SchemaFormGrid) instance;
        grid.setBean(bean.getValueExpression(ctx, SchemaFormBean.class));
        if (editable != null) {
            grid.setEditable(editable.getValueExpression(ctx, Boolean.class));
        }
    }
}
