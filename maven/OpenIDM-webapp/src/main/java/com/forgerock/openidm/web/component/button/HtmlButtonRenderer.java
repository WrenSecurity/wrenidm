package com.forgerock.openidm.web.component.button;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.CommandLinkRenderer;
import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

/**
 *
 * @author Vilo Repan
 */
@FacesRenderer(componentFamily = HtmlCommandLink.COMPONENT_FAMILY, rendererType = "HtmlButtonRenderer")
public class HtmlButtonRenderer extends CommandLinkRenderer {

    private String styleClasses;
    private Object value;
    private ValueExpression valueExpression;

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        HtmlButton link = (HtmlButton) component;
        value = link.getValue();
        styleClasses = link.getStyleClass();
        valueExpression = link.getValueExpression("value");

        link.setValue(null);
        link.setValueExpression("value", null);
        link.setStyleClass(styleClasses + " " + link.getButtonType());

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", component);
        writer.writeAttribute("class", "buttons", null);

        super.encodeBegin(context, component);
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        HtmlButton link = (HtmlButton) component;

        ResponseWriter writer = context.getResponseWriter();
        String src = RenderKitUtils.getImageSource(context, component, "img");
        if (src != null && !src.isEmpty()) {
            writer.startElement("img", component);
            writer.writeAttribute("style", "border: 0px none;", null);
            writer.writeAttribute("src", src, null);
            writer.endElement("img");
        }

        super.encodeChildren(context, component);

        writer.startElement("span", component);
        writer.writeAttribute("class", link.getButtonType(), null);
        writer.writeText(value, null);
        writer.endElement("span");
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        super.encodeEnd(context, component);

        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("div");

        HtmlButton link = (HtmlButton) component;
        link.setValue(value);
        link.setStyleClass(styleClasses);
        link.setValueExpression("value", valueExpression);
    }
}
