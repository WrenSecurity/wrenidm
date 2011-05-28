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
import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.api.exceptions.OpenIDMException;
import com.forgerock.openidm.provisioning.exceptions.ValidationException;
import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Write out the values form a ResourceObject in xml format.
 *
 * @author elek
 */
public class ObjectValueWriter {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(ObjectValueWriter.class);
    private static ObjectValueWriter instance = new ObjectValueWriter();

    public static ObjectValueWriter getInstance() {
        return instance;
    }

    public void write(ResourceObject ro, Element destination) throws OpenIDMException {
        Document doc = destination.getOwnerDocument();
        for (ResourceAttribute attribute : ro.getValues()) {
            if (attribute.getDefinition() == null) {
                throw new IllegalArgumentException("Value present without type definition " + attribute);
            }
            for (Node value : attribute.getValues()) {
                destination.appendChild(doc.importNode(value, true));
            }
        }

    }

    /**
     * Merge ResourceObject values to an XML objects.
     * s
     * @param ro
     * @param destination
     * @throws OpenIDMException
     * @todo implement merge
     */
    public void merge(ResourceObject ro, List<Element> destination, boolean doFilter) throws OpenIDMException {
        //TODO instead of the clear + add we need real merge algorithm
        destination.clear();
        for (ResourceAttribute attribute : ro.getValues()) {
            //only store thre required attribtues
            if (doFilter && !attribute.getDefinition().isStoredInRepository()) {
                continue;
            }
            QName qn = attribute.getDefinition().getQName();
            for (Node value : attribute.getValues()) {
                Document doc = value.getOwnerDocument();
                destination.add((Element) doc.importNode(value, true));
            }
        }

    }

    /**
     * Merge ResourceAttribute values to an XML objects.
     * s
     * @param ro
     * @param destination
     * @throws OpenIDMException
     * @todo implement merge
     */
    public void merge(ResourceAttribute attribute, List<Element> destination, boolean doFilter) throws OpenIDMException {

        //only store thre required attribtues
        if (doFilter && !attribute.getDefinition().isStoredInRepository()) {
            return;
        }
        for (Node value : attribute.getValues()) {
            Document doc = value.getOwnerDocument();
            destination.add((Element) doc.importNode(value, true));
        }

    }

    /**
     * Merge result values from the ResourceObject to a real shadow object.
     *
     * @param ro
     * @param shadow
     */
    public void postProcessShadow(ResourceObject ro, ResourceObjectShadowType shadow) {
        merge(ro, shadow.getAttributes().getAny(), true);
    }

    public ResourceObject readValues(ResourceObjectDefinition definition, List<Element> values) throws OpenIDMException {
        return readValues(definition, values, false);

    }

    /**
     * Read values to a ResoureObjectDefinition.
     *
     * @param schema
     * @param type
     * @param values
     * @return
     * @throws OpenIDMException
     */
    public ResourceObject readValues(ResourceObjectDefinition definition, List<Element> values, boolean strict) throws OpenIDMException {
        List<ResourceAttribute> attributeValues = new ArrayList<ResourceAttribute>(0);
        ResourceObject obj = new ResourceObject(definition);
        for (Element e : values) {
            List<Node> value = new ArrayList<Node>(0);
            ResourceAttribute attribute = null;

            boolean found = false;
            for (ResourceAttributeDefinition attrDef : obj.getDefinition().getAttributesCopy()) {
                if (e.getLocalName().equalsIgnoreCase(attrDef.getQName().getLocalPart()) && e.getNamespaceURI().equalsIgnoreCase(attrDef.getQName().getNamespaceURI())) {
                    if (null == attribute) {
                        attribute = new ResourceAttribute(attrDef, value);
                        attributeValues.add(attribute);
                        obj.addValue(attribute);
                    }
                    value.add(e);
                    found = true;
                }
            }

            if (strict && !found) {
                throw new OpenIDMException("No type for value in schema " + e.getNamespaceURI() + ":" + e.getTagName());
            }
        }
        return obj;
    }

    /**
     * Create a new ResourceObject based on a schema and attributes of shadow instance.
     * 
     * @param shadow
     * @param schema
     * @return
     * @throws OpenIDMException
     */
    public ResourceObject buildResourceObject(ResourceObjectShadowType shadow, ResourceSchema schema) throws OpenIDMException {
        return readValues(schema.getObjectDefinition(shadow.getObjectClass()), shadow.getAttributes().getAny());
    }

    // TODO: exceptions shoudl not be used to control the code flow, but this
    // method was way too simplistic to be useful. In case that the validation
    // fails it does not provide any reasonable information. Therefore using
    // exception was the easiest way how to refactor the code now.
    public boolean validateBeforeCreate(ResourceObject object) throws ValidationException {
        for (ResourceAttributeDefinition def : object.getDefinition().getRequiredAttributsForCreate()) {
            if (null == def) {
                continue;
            }
            boolean contains = false;
            for (ResourceAttribute attr : object.getValues()) {
                if (attr.getDefinition().is(def)) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                continue;
            }
            logger.info("Missing required attribute: {}", def.getNativeAttributeName());
            ValidationException ex = new ValidationException("Missing required attribute " + def.getNativeAttributeName());
            ex.addFailedAttribute(def.getQName());
            throw ex;
        }
        return true;
    }
}
