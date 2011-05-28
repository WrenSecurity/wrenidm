/*
 *
 * Copyright (c) 2010 ForgeRock Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1.php or
 * OpenIDM/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at OpenIDM/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2010 [name of copyright owner]"
 *
 * $Id$
 */

package com.forgerock.openidm.web.component.syntax;

import com.icesoft.faces.component.style.OutputStyle;
import com.sun.faces.renderkit.html_basic.TextareaRenderer;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

/**
 *
 * @author Vilo Repan
 */
@FacesRenderer(componentFamily = HtmlInputTextarea.COMPONENT_FAMILY, rendererType = "InputSyntaxTextAreaRenderer")
public class InputSyntaxTextAreaRenderer extends TextareaRenderer {

    private static final String RESOURCE_PATH = "javax.faces.resource/codemirror/";
    private static final String JSF_EXTENSION = ".iface";

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        appendLinkToScript(RESOURCE_PATH + "js/codemirror.js" + JSF_EXTENSION, writer, component);
//        appendLinkToScript(RESOURCE_PATH + "js/jsfcustom.js" + JSF_EXTENSION, writer, component);

        writer.startElement("link", component);
        writer.writeAttribute("type", "text/css", null);
        writer.writeAttribute("rel", "stylesheet", null);
        writer.writeAttribute("href", RESOURCE_PATH + "css/docs.css" + JSF_EXTENSION, null);
        writer.endElement("link");

        writer.startElement("div", component);
        writer.writeAttribute("class", "border", null);

        super.encodeBegin(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        super.encodeEnd(context, component);

        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("div");

        String clientId = component.getClientId(context);
        String script = "var editor;\n" +
                "window.setTimeout('createEditor()', 500);\n" +
                "function createEditor() {\n" +
                "editor = CodeMirror.fromTextArea('" + clientId + "', {\n" +
                "height: \"700px\",\n" +
                "width: \"600px\",\n" +           
                "parserfile: \"parsexml.js" + JSF_EXTENSION + "\",\n" +
                "stylesheet: \"" + RESOURCE_PATH + "css/xmlcolors.css" + JSF_EXTENSION + "\",\n" +
                "path: \"" + RESOURCE_PATH + "js/\",\n" +
                "continuousScanning: 500,\n" +
                "lineNumbers: true\n" +
                "});\n" +
                "}\n" +
                "function updateTextarea() {\n" +
                "$('" + clientId + "').value='';\n" +
                "$('" + clientId + "').value = editor.getCode();\n" +
                "}";

        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeText(script, null);
        writer.endElement("script");
    }

    private void appendLinkToScript(String path, ResponseWriter writer, UIComponent component) throws IOException {
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeAttribute("src", path, null);
        writer.endElement("script");
    }
}
