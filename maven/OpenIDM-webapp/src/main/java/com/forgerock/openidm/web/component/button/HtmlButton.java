package com.forgerock.openidm.web.component.button;

import com.icesoft.faces.component.ext.HtmlCommandLink;
import java.util.ArrayList;
import java.util.List;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

/**
 *
 * @author Vilo Repan
 */
@FacesComponent("com.forgerock.openidm.web.component.button.HtmlButton")
public class HtmlButton extends HtmlCommandLink {

    @Override
    public String getType() {
        return HtmlButton.class.getName();
    }

    @Override
    public String getRendererType() {
        return "HtmlButtonRenderer";
    }

    public String getImg() {
        return (java.lang.String) getStateHelper().eval("img");
    }

    public void setImg(String image) {
        getStateHelper().put("img", image);
        handleAttribute("img", image);
    }

    public String getButtonType() {
        String type = (java.lang.String) getStateHelper().eval("buttonType");
        if (type == null) {
            type = "regular";
        }

        return type;
    }

    public void setButtonType(String type) {
        getStateHelper().put("buttonType", type);
        handleAttribute("buttonType", type);
    }
    private static final String OPTIMIZED_PACKAGE = "javax.faces.component.";

    private void handleAttribute(String name, Object value) {
        List<String> setAttributes = (List<String>) this.getAttributes().get("javax.faces.component.UIComponentBase.attributesThatAreSet");
        if (setAttributes == null) {
            String cname = this.getClass().getName();
            if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
                setAttributes = new ArrayList<String>(6);
                this.getAttributes().put("javax.faces.component.UIComponentBase.attributesThatAreSet", setAttributes);
            }
        }
        if (setAttributes != null) {
            if (value == null) {
                ValueExpression ve = getValueExpression(name);
                if (ve == null) {
                    setAttributes.remove(name);
                }
            } else if (!setAttributes.contains(name)) {
                setAttributes.add(name);
            }
        }
    }
}
