package com.forgerock.openidm.web.component.formgenerator;

import com.icesoft.faces.component.ext.HtmlMessage;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.MessageRenderer;
import com.sun.faces.renderkit.html_basic.OutputMessageRenderer;
import com.sun.faces.util.MessageUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessage;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

/**
 *
 * @author Vilo Repan
 */
@FacesRenderer(componentFamily = HtmlMessage.COMPONENT_FAMILY, rendererType = "schemaMessageRenderer")
public class SchemaMessageRenderer extends MessageRenderer {

    private OutputMessageRenderer omRenderer = null;

    // ------------------------------------------------------------ Constructors


    public SchemaMessageRenderer() {

        omRenderer = new OutputMessageRenderer();

    }

    // ---------------------------------------------------------- Public Methods


    @Override
    public void encodeBegin(FacesContext context, UIComponent component)
          throws IOException {

        rendererParamsNotNull(context, component);

        if (component instanceof UIOutput) {
            omRenderer.encodeBegin(context, component);
        }

    }


    @Override
    public void encodeChildren(FacesContext context, UIComponent component)
          throws IOException {

        rendererParamsNotNull(context, component);

        if (component instanceof UIOutput) {
            omRenderer.encodeChildren(context, component);
        }

    }


    public void encodeEnd(FacesContext context, UIComponent component)
          throws IOException {

        rendererParamsNotNull(context, component);

        if (component instanceof UIOutput) {
            omRenderer.encodeEnd(context, component);
            return;
        }

        if (!shouldEncode(component)) {
            return;
        }

        //  If id is user specified, we must render
        boolean mustRender = shouldWriteIdAttribute(component);

        ResponseWriter writer = context.getResponseWriter();
        assert(writer != null);

        UIMessage message = (UIMessage) component;

        String clientId = message.getFor();
        //"for" attribute required for Message. Should be taken care of
        //by TLD in JSP case, but need to cover non-JSP case.
        if (clientId == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("'for' attribute cannot be null");
            }
            return;
        }

        if (clientId.equals("dn_0_0") || clientId.equals("asdf")) {
            System.out.println("");
        }

        clientId = augmentIdReference(clientId, component);
        Iterator messageIter = getMessageIter(context, clientId, component);


        assert(messageIter != null);
        if (!messageIter.hasNext()) {
            if (mustRender) {
                // no message to render, but must render anyway
                writer.startElement("span", component);
                writeIdAttributeIfNecessary(context, writer, component);
                writer.endElement("span");
            } // otherwise, return without rendering
            return;
        }
        FacesMessage curMessage = (FacesMessage) messageIter.next();
        if (curMessage.isRendered() && !message.isRedisplay()) {
            return;
        }
        curMessage.rendered();

        String severityStyle = null;
        String severityStyleClass = null;
        boolean showSummary = message.isShowSummary();
        boolean showDetail = message.isShowDetail();

        // make sure we have a non-null value for summary and
        // detail.
        String summary = (null != (summary = curMessage.getSummary())) ?
                  summary : "";
        // Default to summary if we have no detail
        String detail = (null != (detail = curMessage.getDetail())) ?
                 detail : summary;

        if (curMessage.getSeverity() == FacesMessage.SEVERITY_INFO) {
            severityStyle =
                  (String) component.getAttributes().get("infoStyle");
            severityStyleClass = (String)
                  component.getAttributes().get("infoClass");
        } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_WARN) {
            severityStyle =
                  (String) component.getAttributes().get("warnStyle");
            severityStyleClass = (String)
                  component.getAttributes().get("warnClass");
        } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_ERROR) {
            severityStyle =
                  (String) component.getAttributes().get("errorStyle");
            severityStyleClass = (String)
                  component.getAttributes().get("errorClass");
        } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_FATAL) {
            severityStyle =
                  (String) component.getAttributes().get("fatalStyle");
            severityStyleClass = (String)
                  component.getAttributes().get("fatalClass");
        }

        String style = (String) component.getAttributes().get("style");
        String styleClass = (String) component.getAttributes().get("styleClass");
        String dir = (String) component.getAttributes().get("dir");
        String lang = (String) component.getAttributes().get("lang");
        String title = (String) component.getAttributes().get("title");

        // if we have style and severityStyle
        if ((style != null) && (severityStyle != null)) {
            // severityStyle wins
            style = severityStyle;
        }
        // if we have no style, but do have severityStyle
        else if ((style == null) && (severityStyle != null)) {
            // severityStyle wins
            style = severityStyle;
        }

        // if we have styleClass and severityStyleClass
        if ((styleClass != null) && (severityStyleClass != null)) {
            // severityStyleClass wins
            styleClass = severityStyleClass;
        }
        // if we have no styleClass, but do have severityStyleClass
        else if ((styleClass == null) && (severityStyleClass != null)) {
            // severityStyleClass wins
            styleClass = severityStyleClass;
        }

        //Done intializing local variables. Move on to rendering.

        boolean wroteSpan = false;
        if (styleClass != null
             || style != null
             || dir != null
             || lang != null
             || title != null
             || mustRender) {
            writer.startElement("span", component);
            writeIdAttributeIfNecessary(context, writer, component);

            wroteSpan = true;
            if (style != null) {
                writer.writeAttribute("style", style, "style");
            }
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }
            if (dir != null) {
                writer.writeAttribute("dir", dir, "dir");
            }
            if (lang != null) {
                writer.writeAttribute(RenderKitUtils.prefixAttribute("lang", writer),
                     lang,
                     "lang");
            }
            if (title != null) {
                writer.writeAttribute("title", title, "title");
            }

        }

        Object val = component.getAttributes().get("tooltip");
        boolean isTooltip = (val != null) && Boolean.valueOf(val.toString());

        boolean wroteTooltip = false;
        if (showSummary && showDetail && isTooltip) {

            if (!wroteSpan) {
                writer.startElement("span", component);
            }
            if (title == null || title.length() == 0) {
                writer.writeAttribute("title", summary, "title");
            }
            writer.flush();
            writer.writeText("\t", component, null);
            wroteTooltip = true;
        } else if (wroteSpan) {
            writer.flush();
        }

        if (!wroteTooltip && showSummary) {
            writer.writeText("\t", component, null);
            writer.writeText(summary, component, null);
            writer.writeText(" ", component, null);
        }
        if (showDetail) {
            writer.writeText(detail, component, null);
        }

        if (wroteSpan || wroteTooltip) {
            writer.endElement("span");
        }

    }

    protected Iterator getMessageIter(FacesContext context,
                                      String forComponent,
                                      UIComponent component) {

        Iterator messageIter;
        // Attempt to use the "for" attribute to locate
        // messages.  Three possible scenarios here:
        // 1. valid "for" attribute - messages returned
        //    for valid component identified by "for" expression.
        // 2. zero length "for" expression - global errors
        //    not associated with any component returned
        // 3. no "for" expression - all messages returned.
        if (null != forComponent) {
            if (forComponent.length() == 0) {
                messageIter = context.getMessages(null);
            } else {
                UIComponent result = getForComponent(context, forComponent,
                                                     component);
                if (result == null) {
                    messageIter = Collections.EMPTY_LIST.iterator();
                } else {
                    messageIter =
                          context.getMessages(result.getClientId(context));
                }
            }
        } else {
            messageIter = context.getMessages();
        }
        return messageIter;

    }

    protected UIComponent getForComponent(FacesContext context,
                                          String forComponent,
                                          UIComponent component) {

        if (null == forComponent || forComponent.length() == 0) {
            return null;
        }

        if (forComponent.equals("dn_0_0")) {
            System.out.println("");
        }

        UIComponent result = null;
        UIComponent currentParent = component;
        try {
            // Check the naming container of the current
            // component for component identified by
            // 'forComponent'
            while (currentParent != null) {
                // If the current component is a NamingContainer,
                // see if it contains what we're looking for.
                
                result = currentParent.findComponent(forComponent);
//                List<UIComponent> children = currentParent.getChildren();
//                for (UIComponent child : children) {
//                    if (child.getClientId(context).equals(forComponent)) {
//                        result = child;
//                        break;
//                    }
//                }

                if (result != null) {
                    break;
                }
                // if not, start checking further up in the view
                currentParent = currentParent.getParent();
            }

            // no hit from above, scan for a NamingContainer
            // that contains the component we're looking for from the root.
            if (result == null) {
                result =
                      findUIComponentBelow(context.getViewRoot(), forComponent);
            }
        } catch (Exception e) {
            // ignore - log the warning
        }
        // log a message if we were unable to find the specified
        // component (probably a misconfigured 'for' attribute
        if (result == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(MessageUtils.getExceptionMessageString(
                      MessageUtils.COMPONENT_NOT_FOUND_IN_VIEW_WARNING_ID,
                      forComponent));
            }
        }
        return result;

    }

    private static UIComponent findUIComponentBelow(UIComponent startPoint,
                                                    String forComponent) {

        UIComponent retComp = null;
        if (startPoint.getChildCount() > 0) {
            List<UIComponent> children = startPoint.getChildren();
            for (int i = 0, size = children.size(); i < size; i++) {
                UIComponent comp = children.get(i);

                if (comp instanceof NamingContainer) {
                    try {
                        retComp = comp.findComponent(forComponent);
                    } catch (IllegalArgumentException iae) {
                        continue;
                    }
                }

                if (retComp == null) {
                    if (comp.getChildCount() > 0) {
                        retComp = findUIComponentBelow(comp, forComponent);
                    }
                }

                if (retComp != null) {
                    break;
                }
            }
        }
        return retComp;

    }

//    @Override
//    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
//        System.out.println("class: " + uiComponent.getClass().getName());
//        UIMessage msg = (UIMessage) uiComponent;
//        System.out.println("for: " + msg.getFor());
//        System.out.println("client id: " + augmentIdReference(msg.getFor(), uiComponent));
//        if (msg.getFor().equals("dn_0_0")) {
//            System.out.println("");
//        }
//        Iterator<FacesMessage> messages = facesContext.getMessages();
//        while (messages.hasNext()) {
//            FacesMessage m = messages.next();
//            System.out.println(m.getDetail() + "\t" + m.getSummary());
//        }
//        System.out.println("****");
//        super.encodeBegin(facesContext, uiComponent);
//    }
//
//    @Override
//    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
//        super.encodeChildren(context, component);
//    }
//
//    @Override
//    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
//        super.encodeEnd(facesContext, uiComponent);
//    }

    @Override
    protected String augmentIdReference(String forValue, UIComponent fromComponent) {
        if (forValue.equals("dn_0_0") || forValue.equals("asdf")) {
            System.out.println("");
        }
        int forSuffix = forValue.lastIndexOf(UIViewRoot.UNIQUE_ID_PREFIX);
        if (forSuffix <= 0) {
            // if the for-value doesn't already have a suffix present
            String id = fromComponent.getId();
            if (id != null) {
                int idSuffix = id.lastIndexOf(UIViewRoot.UNIQUE_ID_PREFIX);
                if (idSuffix > 0) {
                    // but the component's own id does have a suffix
                    forValue += id.substring(idSuffix);
                }
            }
        }
        return forValue;

    }
}
