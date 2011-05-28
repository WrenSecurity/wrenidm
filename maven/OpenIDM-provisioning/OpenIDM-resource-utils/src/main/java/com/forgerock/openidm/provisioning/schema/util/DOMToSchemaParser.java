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
package com.forgerock.openidm.provisioning.schema.util;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.provisioning.exceptions.SchemaParserException;
import com.forgerock.openidm.provisioning.schema.AccountObjectClassDefinition;
import com.forgerock.openidm.provisioning.schema.AttributeFlag;
import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.util.DOMUtil;
import static com.forgerock.openidm.provisioning.schema.util.SchemaDOMElement.*;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import static javax.xml.XMLConstants.*;

/**
 * This class parse a {@link Document} and converts it to {@link ResourceSchema}
 * object. This must be two document because the {@Link ResourceType com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType}
 * object has {@link ResourceType#getSchema} and {@link ResourceType#getSchemaHandling} and
 * both inforamation must be parsed here.
 *
 * @author Vilo Repan
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class DOMToSchemaParser {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(DOMToSchemaParser.class);
    private ResourceSchema schema;

    public ResourceSchema getSchema(Element resourceSchema) throws SchemaParserException {
        return getSchema(resourceSchema, null);
    }

    public ResourceSchema getSchema(Element resourceSchema, Element schemaHandling) throws SchemaParserException {
        schema = initSchema(resourceSchema);

        XSSchemaSet set = parseSchema(resourceSchema);
        Iterator<XSComplexType> iterator = set.iterateComplexTypes();
        while (iterator.hasNext()) {
            XSComplexType complexType = iterator.next();
            if (complexType.getTargetNamespace().equals(schema.getResourceNamespace())) {
                schema.addObjectClass(createResourceObjectDefinition(complexType, resourceSchema));
            }
        }

//        if (schemaHandling != null) {
//            applySchemaHandling(schema, schemaHandling);
//        }

        return schema;
    }

    private void applySchemaHandling(ResourceSchema schema, Element schemaHandling) {
        NodeList children = schemaHandling.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!"accountType".equals(child.getNodeName()) || !SchemaConstants.NS_C.equals(child.getNamespaceURI())) {
                continue;
            }

            Node objectClass = findChildElement(child, new QName(SchemaConstants.NS_C, "objectClass"));
            ResourceObjectDefinition object = schema.getObjectDefinition(createQName(objectClass));
            if (object != null) {
                applyHandlingOnObject(object, child);
            } else {
                logger.debug("Resource object not found for schemahandling object '" +
                        objectClass.getTextContent() + "', skipping.");
            }
        }
    }

    private void applyHandlingOnObject(ResourceObjectDefinition object, Node node) {
        
    }

    private QName createQName(Node node) {
        String qualifiedName = node.getTextContent();
        int index = qualifiedName.indexOf(":");
        if (index == -1) {
            return new QName(node.getOwnerDocument().getNamespaceURI(), qualifiedName);
        }

        String namespace = node.lookupNamespaceURI(qualifiedName.substring(index));
        return new QName(namespace, qualifiedName.substring(index + 1, qualifiedName.length()));
    }

    private Node findChildElement(Node node, QName qname) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (qname.getLocalPart().equals(child.getNodeName()) &&
                    qname.getNamespaceURI().equals(child.getNamespaceURI())) {
                return child;
            }
        }
        return null;
    }

    private ResourceObjectDefinition createResourceObjectDefinition(XSComplexType complexType, Element document) {
        QName qname = new QName(complexType.getTargetNamespace(), complexType.getName());
        ResourceObjectDefinition definition = null;

        XSAnnotation annotation = complexType.getAnnotation();
        //nativeAttributeName
        Element nativeAttrElement = getAnnotationElement(annotation, A_NATIVE_OBJECT_CLASS);
        String nativeAttributeName = nativeAttrElement == null ? qname.getLocalPart() : nativeAttrElement.getTextContent();
        if (isAccountObject(annotation)) {
            definition = new AccountObjectClassDefinition(qname, nativeAttributeName);
        } else {
            //container
            boolean container = false;
            if (getAnnotationElement(annotation, A_CONTAINER) != null) {
                container = true;
            }

            definition = new ResourceObjectDefinition(qname, nativeAttributeName, container);
        }

        List<ResourceAttributeDefinition> list = null;
        XSContentType content = complexType.getContentType();
        if (content != null) {
            XSParticle particle = content.asParticle();
            if (particle != null) {
                XSTerm term = particle.getTerm();

                if (term.isModelGroup()) {
                    list = createResourceAttributeListFromGroup(term.asModelGroup());
                }
            }
        }

        if (list != null) {
            for (ResourceAttributeDefinition attribute : list) {
                definition.addAttribute(attribute);
            }
        }

        parseAnnotationForObjectClass(annotation, definition, document);

        return definition;
    }

    private List<ResourceAttributeDefinition> createResourceAttributeListFromGroup(XSModelGroup group) {
        List<ResourceAttributeDefinition> elementList = new ArrayList<ResourceAttributeDefinition>();

        XSParticle[] particles = group.getChildren();
        for (XSParticle p : particles) {
            XSTerm pterm = p.getTerm();
            if (pterm.isModelGroup()) {
                elementList.addAll(createResourceAttributeListFromGroup(pterm.asModelGroup()));
            }

            //xs:element inside complex type
            if (pterm.isElementDecl()) {
                XSAnnotation annotation = selectAnnotationToUse(p.getAnnotation(), pterm.getAnnotation());

                XSElementDecl element = pterm.asElementDecl();
                QName qname = new QName(element.getTargetNamespace(), element.getName());
                //nativeAttributeName
                Element nativeAttrElement = getAnnotationElement(annotation, A_NATIVE_ATTRIBUTE_NAME);
                String nativeAttributeName = nativeAttrElement == null ? qname.getLocalPart() : nativeAttrElement.getTextContent();

                ResourceAttributeDefinition attribute = new ResourceAttributeDefinition(qname, nativeAttributeName);
                XSType type = element.getType();
                attribute.setType(new QName(type.getTargetNamespace(), type.getName()));
                attribute.setMinOccurs(p.getMinOccurs());
                attribute.setMaxOccurs(p.getMaxOccurs());
                parseAnnotationForAttribute(annotation, attribute);

                elementList.add(attribute);
            }
        }

        return elementList;
    }

    private XSAnnotation selectAnnotationToUse(XSAnnotation particleAnnotation, XSAnnotation termAnnotation) {
        boolean useParticleAnnotation = false;
        if (particleAnnotation != null && particleAnnotation.getAnnotation() != null) {
            if (testAnnotationAppinfo(particleAnnotation)) {
                useParticleAnnotation = true;
            }
        }

        boolean useTermAnnotation = false;
        if (termAnnotation != null && termAnnotation.getAnnotation() != null) {
            if (testAnnotationAppinfo(termAnnotation)) {
                useTermAnnotation = true;
            }
        }

        if (useParticleAnnotation) {
            return particleAnnotation;
        }

        if (useTermAnnotation) {
            return termAnnotation;
        }

        return null;
    }

    private boolean testAnnotationAppinfo(XSAnnotation annotation) {
        Element appinfo = getAnnotationElement(annotation, new QName(W3C_XML_SCHEMA_NS_URI, "appinfo"));
        if (appinfo != null) {
            return true;
        }

        return false;
    }

    private QName getQNameForReference(String reference, Element element) {
        String[] array = reference.split(":");

        String namespace = element.lookupPrefix(array[0]);
        if (namespace == null) {
            namespace = element.getAttribute("xmlns:" + array[0]);
        }

        return new QName(namespace, array[1]);
    }

    private void parseAnnotationForObjectClass(XSAnnotation annotation, ResourceObjectDefinition objectClass, Element document) {
        if (annotation == null || annotation.getAnnotation() == null) {
            return;
        }

        //displayName
        ResourceAttributeDefinition displayName = getAnnotationReference(annotation, A_DISPLAY_NAME, document, objectClass);
        if (displayName != null) {
            displayName.setDisplayName(true);
        }

        //descriptionAttribute
        ResourceAttributeDefinition descriptionAttribute = getAnnotationReference(annotation, A_DESCRIPTION_ATTRIBUTE, document, objectClass);
        if (descriptionAttribute != null) {
            descriptionAttribute.setDescriptionAttribute(true);
        }

        //identifier
        ResourceAttributeDefinition identifier = getAnnotationReference(annotation, A_IDENTIFIER, document, objectClass);
        if (identifier != null) {
            identifier.setIdentifier(true);
        }

        //secondaryIdentifier
        ResourceAttributeDefinition secondaryIdentifier = getAnnotationReference(annotation, A_SECONDARY_IDENTIFIER, document, objectClass);
        if (secondaryIdentifier != null) {
            secondaryIdentifier.setSecondaryIdentifier(true);
        }

        //compositeIdentifier
        ResourceAttributeDefinition compositeIdentifier = getAnnotationReference(annotation, A_COMPOSITE_IDENTIFIER, document, objectClass);
        if (compositeIdentifier != null) {
            compositeIdentifier.setCompositeIdentifier(true);
        }

        if (objectClass instanceof AccountObjectClassDefinition) {
            AccountObjectClassDefinition accObjectClass = (AccountObjectClassDefinition) objectClass;
            //check if it's default account object class...
            Element accountType = getAnnotationElement(annotation, A_ACCOUNT_TYPE);
            if (accountType != null) {
                String defaultValue = accountType.getAttribute("default");
                if (defaultValue != null) {
                    accObjectClass.setDefault(Boolean.parseBoolean(defaultValue));
                }
            }
        }
    }

    private ResourceAttributeDefinition getAnnotationReference(XSAnnotation annotation, QName qname, Element document, ResourceObjectDefinition objectClass) {
        Element element = getAnnotationElement(annotation, qname);
        if (element != null) {
            String reference = element.getAttribute("ref");
            if (reference != null && !reference.isEmpty()) {
                return objectClass.getAttributeDefinition(getQNameForReference(reference, document));
            }
        }

        return null;
    }

    private void parseAttributeInformation(NodeList nodes, ResourceAttributeDefinition attribute) {
        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            
        }
    }

    private void parseAnnotationForAttribute(XSAnnotation annotation, ResourceAttributeDefinition attribute) {
        if (annotation == null || annotation.getAnnotation() == null) {
            return;
        }

        //flagList
        Element flagList = getAnnotationElement(annotation, A_ATTRIBUTE_FLAG);
        if (flagList != null && flagList.getTextContent() != null && !flagList.getTextContent().isEmpty()) {
            String flags = flagList.getTextContent();
            String[] array = flags.split(" ");
            for (String item : array) {
                attribute.getAttributeFlag().add(AttributeFlag.valueOf(item));
            }
        }

        //attributeDisplayName
        Element attributeDisplayName = getAnnotationElement(annotation, A_ATTRIBUTE_DISPLAY_NAME);
        if (attributeDisplayName != null) {
            attribute.setAttributeDisplayName(attributeDisplayName.getTextContent());
        }

        //help
        Element help = getAnnotationElement(annotation, A_HELP);
        if (help != null) {
            attribute.setHelp(help.getTextContent());
        }

        //encryption, classification
        Element encryption = getAnnotationElement(annotation, A_CA_ENCRYPTION);
        if (encryption != null && encryption.getTextContent() != null) {
            Element classificationLevel = getAnnotationElement(annotation, A_CA_CLASSIFICATION_LEVEL);
            String classification = classificationLevel == null ? null : classificationLevel.getTextContent();
            attribute.makeClassified(ResourceAttributeDefinition.Encryption.valueOf(encryption.getTextContent()), classification);
        }
    }

    private boolean isAccountObject(XSAnnotation annotation) {
        if (annotation == null || annotation.getAnnotation() == null) {
            return false;
        }

        Element accountType = getAnnotationElement(annotation, A_ACCOUNT_TYPE);
        if (accountType != null) {
            return true;
        }

        return false;
    }

    private Element getAnnotationElement(XSAnnotation annotation, QName qname) {
        if (annotation == null) {
            return null;
        }

        Element xsdAnnotation = (Element) annotation.getAnnotation();
        NodeList list = xsdAnnotation.getElementsByTagNameNS(qname.getNamespaceURI(), qname.getLocalPart());
        if (list != null && list.getLength() > 0) {
            return (Element) list.item(0);
        }

        return null;
    }

    private ResourceSchema initSchema(Element xsdSchema) {
        String targetNamespace = xsdSchema.getAttribute("targetNamespace");
        schema = new ResourceSchema(targetNamespace);

        // not working..
        // NodeList list = xsdSchema.getElementsByTagNameNS(W3C_XML_SCHEMA_NS_URI, "import");

        List<Element> importList = createImportList(xsdSchema);
        for (Element element : importList) {
            String namespace = element.getAttribute("namespace");
            if (namespace == null || namespace.isEmpty()) {
                continue;
            }

            if (namespace.equals(SchemaConstants.NS_RESOURCE) || namespace.equals(SchemaConstants.NS_C)) {
                continue;
            }

            schema.getImportList().add(namespace);
        }

        return schema;
    }

    private List<Element> createImportList(Element element) {
        List<Element> list = new ArrayList<Element>();

        NodeList children = element.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                if (!child.getLocalName().equals("import")) {
                    continue;
                }

                if (!W3C_XML_SCHEMA_NS_URI.equals(child.getNamespaceURI())) {
                    continue;
                }

                list.add((Element) child);
            }
        }

        return list;
    }

    private XSOMParser createSchemaParser() {
        XSOMParser parser = new XSOMParser();
        parser.setErrorHandler(new SchemaErrorLog());
        parser.setAnnotationParser(new DomAnnotationParserFactory());
        parser.setEntityResolver(SchemaConstants.getEntityResolver());

        return parser;
    }

    private XSSchemaSet parseSchema(Element schema) throws SchemaParserException {
        XSSchemaSet xss = null;
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(schema);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(out);

            trans.transform(source, result);

            XSOMParser parser = createSchemaParser();
            InputSource inSource = new InputSource(new ByteArrayInputStream(out.toByteArray()));
            inSource.setSystemId("SystemId"); //HACK: it's here to make entity resolver work...
            inSource.setEncoding("utf-8");
            parser.parse(inSource);
            xss = parser.getResult();
        } catch (Exception ex) {
            throw new SchemaParserException("Uknown error during resource xsd schema parsing: " + ex.getMessage(), ex);
        }

        return xss;
    }
}
