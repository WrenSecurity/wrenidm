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
package com.forgerock.openidm.web.controller;

import com.forgerock.openidm.common.web.FacesUtils;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.util.DOMUtil;
import com.forgerock.openidm.util.QNameUtil;
import com.forgerock.openidm.util.Variable;
import com.forgerock.openidm.util.XPathUtil;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.web.XPathVariables;
import com.forgerock.openidm.web.consumer.ModelService;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import com.forgerock.openidm.xml.schema.ExpressionHolder;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;
import javax.xml.bind.JAXBException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Katuska
 */
@Controller("xpathDebugPageController")
@Scope("session")
public class XPathDebugPageController {

    private String expresion;
    private List<SelectItem> type;
    private XPathVariables variable1;
    private XPathVariables variable2;
    private XPathVariables variable3;
    private XPathVariables variable4;
    private List<SelectItem> returnTypeList;
    private String returnType;
    private List<XPathVariables> variables = new ArrayList<XPathVariables>();
    private String result;
    private static transient final org.slf4j.Logger logger = TraceManager.getTrace(XPathDebugPageController.class);

    public XPathDebugPageController() {
    }

    public String prepareXpathDebugPage() {
        if (variable1 == null) {
            variable1 = new XPathVariables();
        }
        if (variable2 == null) {
            variable2 = new XPathVariables();
        }
        if (variable3 == null) {
            variable3 = new XPathVariables();
        }
        if (variable4 == null) {
            variable4 = new XPathVariables();
        }
        return "/xpathDebugPage";

    }

    public ExpressionHolder getExpressionHolderFromExpresion() {
        logger.debug("getExpressionHolder start");
        if (expresion == null || "".equals(expresion)) {
            FacesUtils.addErrorMessage("Expresion cannot be null.");
        }

        Document doc = DOMUtil.getDocument();
        Element element = doc.createElement("valueExpresion");
        element.setTextContent(expresion);
        ExpressionHolder expressionHolder = new ExpressionHolder(element);
        logger.debug("expression holder: {}", expressionHolder.getFullExpressionAsString());
        logger.debug("getExpressionHolder end");
        return expressionHolder;
    }

    public QName getQNameForVariable(String variable) {
        logger.debug("getQNameForVariable start");
        ExpressionHolder expressionHolder = getExpressionHolderFromExpresion();
        Map<String, String> namespaceMap = expressionHolder.getNamespaceMap();

        //StringBuilder sb;

        if (variable.contains(":")) {
            String[] variableNS = variable.split(":");
            String namespace = namespaceMap.get(variableNS[0]);
            return new QName(namespace, variableNS[1]);
        } else {
            QName qname = new QName(variable);
            return qname;
        }
    }

    public Map<QName, Variable> getVariableValue() throws JAXBException {
        logger.debug("getVariableValue start");
        variables.add(variable1);
        variables.add(variable2);
        variables.add(variable3);
        variables.add(variable4);
        Map<QName, Variable> variableMap = new HashMap<QName, Variable>();
        for (XPathVariables variable : variables) {
            if (StringUtils.isNotEmpty(variable.getVariableName())) {
                if (variable.getType().equals("Object")) {
                    try {
                        ModelService service = new ModelService();
                        ModelPortType port = service.getModelPort();
                        ObjectContainerType objectContainer = port.getObject(variable.getValue(), new PropertyReferenceListType());
                        ObjectType objectType = objectContainer.getObject();
                        // Variable only accepts String or Node, but here we will get a JAXB object. Need to convert it.
                        Element jaxbToDom = JAXBUtil.jaxbToDom(objectType, SchemaConstants.I_OBJECT, null);
                        // TODO: May need to add xsi:type attribute here
                        variableMap.put(getQNameForVariable(variable.getVariableName()), new Variable(jaxbToDom, false));
                    } catch (FaultMessage ex) {
                        logger.error("Failed to get variable value");
                        logger.error("Exception was: ", ex.getFaultInfo().getMessage());
                    }
                }
                if (variable.getType().equals("String")) {
                    variableMap.put(getQNameForVariable(variable.getVariableName()), new Variable(variable.getValue(), false));
                }
            }
        }
//        logger.info("variable value {}", variableMap.get(QNameUtil.uriToQName("http://xxx.com/")));
//        logger.info("getVariableValue end");
        return variableMap;
    }

    public String evaluate() throws JAXBException {
        logger.debug("evaluate start");
        ExpressionHolder expressionHolder = getExpressionHolderFromExpresion();
        if (returnType.equals("Boolean")) {
            Boolean boolResult = (Boolean) XPathUtil.evaluateExpression(getVariableValue(), expressionHolder, XPathConstants.BOOLEAN);
            result = String.valueOf(boolResult);
        }
        if (returnType.equals("Number")) {
            Double doubleResult = (Double) XPathUtil.evaluateExpression(getVariableValue(), expressionHolder, XPathConstants.NUMBER);
            result = String.valueOf(doubleResult);
        }
        if (returnType.equals("String") || returnType.equals("DomObjectModel")) {
            result = (String) XPathUtil.evaluateExpression(getVariableValue(), expressionHolder, XPathConstants.STRING);
        }
        
        if (returnType.equals("Node")){
            Node nodeResult = (Node) XPathUtil.evaluateExpression(getVariableValue(), expressionHolder, XPathConstants.NODE);
            result = DOMUtil.printDom(nodeResult).toString();
        }
        if (returnType.equals("NodeList")){
            NodeList nodeListResult = (NodeList) XPathUtil.evaluateExpression(getVariableValue(), expressionHolder, XPathConstants.NODESET);
            StringBuffer strBuilder = new StringBuffer();
            for (int i=0; i< nodeListResult.getLength(); i++){
                strBuilder.append(DOMUtil.printDom(nodeListResult.item(i)));
            }
            result = strBuilder.toString();
        }

        logger.debug("result is: {}", result);
        logger.debug("evaluate end");
        return "";

    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<SelectItem> getType() {
        if (type == null) {
            type = new ArrayList<SelectItem>();
            type.add(new SelectItem("Object"));
            type.add(new SelectItem("String"));
        }
        return type;
    }

    public void setType(List<SelectItem> type) {
        this.type = type;
    }

    public String getExpresion() {
        return expresion;
    }

    public void setExpresion(String expresion) {
        this.expresion = expresion;
    }

    public XPathVariables getVariable1() {
        return variable1;
    }

    public void setVariable1(XPathVariables variable1) {
        this.variable1 = variable1;
    }

    public XPathVariables getVariable2() {
        return variable2;
    }

    public void setVariable2(XPathVariables variable2) {
        this.variable2 = variable2;
    }

    public XPathVariables getVariable3() {
        return variable3;
    }

    public void setVariable3(XPathVariables variable3) {
        this.variable3 = variable3;
    }

    public XPathVariables getVariable4() {
        return variable4;
    }

    public void setVariable4(XPathVariables variable4) {
        this.variable4 = variable4;
    }

    public List<XPathVariables> getVariables() {
        return variables;
    }

    public void setVariables(List<XPathVariables> variables) {
        this.variables = variables;
    }

    public List<SelectItem> getReturnTypeList() {
        if (type == null) {

            returnTypeList = new ArrayList<SelectItem>();
            returnTypeList.add(new SelectItem("String"));
            returnTypeList.add(new SelectItem("Number"));
            returnTypeList.add(new SelectItem("Node"));
            returnTypeList.add(new SelectItem("NodeList"));
            returnTypeList.add(new SelectItem("Boolean"));
            returnTypeList.add(new SelectItem("DomObjectModel"));
        }
        return returnTypeList;
    }

    public void setReturnTypeList(List<SelectItem> returnTypeList) {
        this.returnTypeList = returnTypeList;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
