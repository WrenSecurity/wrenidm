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
package com.forgerock.openidm.staging;

import com.forgerock.openidm.staging.consumer.ModelService;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import com.forgerock.openidm.xml.ns._public.model.staging_1.StagingPortType;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyModificationType.Value;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import com.forgerock.openidm.xml.schema.XPathType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceRef;
import org.apache.commons.lang.Validate;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
@WebService(serviceName = "stagingService", portName = "stagingPort", endpointInterface = "com.forgerock.openidm.xml.ns._public.model.staging_1.StagingPortType", targetNamespace = "http://openidm.forgerock.com/xml/ns/public/model/staging-1.wsdl") //, wsdlLocation = "META-INF/wsdl/xml/ns/private/model/stagingWrapper.wsdl"
@Stateless
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class StagingService implements StagingPortType {

    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StagingService.class);
    //@WebServiceRef(wsdlLocation = "META-INF/wsdl/modelWrapperStaging.wsdl")
    private ModelService service_1;// = new ModelService();

    public void setService_1(ModelService service_1) {
        this.service_1 = service_1;
    }

    protected Document getXmlDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder loader = factory.newDocumentBuilder();
        return loader.newDocument();

    }

    public ObjectStageType createObjectStage(String objectType) {
        Validate.notNull(objectType);
        ObjectStageType objectStageType = (new ObjectFactory()).createObjectStageType();

        if (objectType.equals(Utils.getObjectType("UserType"))) {
            UserType userType = new ObjectFactory().createUserType();
            objectStageType.setObject(new ObjectFactory().createUser(userType));
        }
        if (objectType.equals(Utils.getObjectType("AccountType"))) {
            AccountShadowType accountType = new ObjectFactory().createAccountShadowType();
            objectStageType.setObject(new ObjectFactory().createAccount(accountType));
        }
        if (objectType.equals(Utils.getObjectType("ResourceType"))) {
            ResourceType resourceType = new ObjectFactory().createResourceType();
            objectStageType.setObject(new ObjectFactory().createResource(resourceType));
        }

        return objectStageType;
    }

    public ObjectStageType getObjectStage(String oid, PropertyReferenceListType resolve) {
        Validate.notNull(oid);
//      WS implementation does not allow null parameters
//        if (null != resolve) {
//            throw new IllegalArgumentException("Parameter resolve is unsupported, yet!");
//        }

        ObjectType objectType = null;

        try { // Call Web Service Operation
            ModelPortType port = service_1.getModelPort();
            ObjectContainerType result = port.getObject(oid, resolve);
            objectType = result.getObject();
        } catch (Exception ex) {
            logger.error("getObject for oid = {} failed", oid);
            logger.error("Exception was: ", ex);
            return null;
        }

        JAXBElement element = null;
        ObjectStageType ost = null;
        if (objectType instanceof UserType) {
            ost = createObjectStage(Utils.getObjectType("UserType"));
            element = new ObjectFactory().createUser((UserType) objectType);
        }
        if (objectType instanceof AccountShadowType) {
            ost = createObjectStage(Utils.getObjectType("AccountType"));
            element = new ObjectFactory().createAccount((AccountShadowType) objectType);
        }
        if (objectType instanceof ResourceType) {
            ost = createObjectStage(Utils.getObjectType("ResourceType"));
            element = new ObjectFactory().createResource((ResourceType) objectType);
        }

        ost.setObject(element);
        //setting oldObject

        //TODO: Check this invalid code
        //ost.getAny().add(objectType);
        return ost;

    }

    /**
     * set PropertyChangeType variable with required type of operation (add, delete, replace) for each field in ObjectType
     *
     * @param oldObject
     * @param newObject
     * @return PropertyChangeType
     */
    private List<PropertyModificationType> setPropertyChangeTypeList(ObjectType oldObject, ObjectType newObject) {
        List<PropertyModificationType> changes = new ArrayList<PropertyModificationType>();
        Field[] field = null;

        if (newObject instanceof UserType) {
            field = UserType.class.getDeclaredFields();
        }
        if (newObject instanceof AccountShadowType) {
            field = AccountShadowType.class.getDeclaredFields();
        }
        if (newObject instanceof ResourceType) {
            field = ResourceType.class.getDeclaredFields();
        }

        for (int i = 0; i < field.length; i++) {
            PropertyModificationType propertyChangeType = new PropertyModificationType();

            String newValue = (String) Utils.getPropertySilent(newObject, field[i].getName());
            String oldValue = (String) Utils.getPropertySilent(oldObject, field[i].getName());
            if (oldValue != null || newValue != null) {
                if (newValue == null && !(oldValue.equals(newValue))) {
                    propertyChangeType.setModificationType(PropertyModificationTypeType.delete);
                    logger.info("[StagingService] propertyChangeTypeType equals delete ");
                }
                if (oldValue == null && !(newValue.equals(oldValue))) {
                    propertyChangeType.setModificationType(PropertyModificationTypeType.add);
                    logger.info("[StagingService] propertyChangeTypeType equals add ");
                }
                if (oldValue != null && newValue != null && !(oldValue.equals(newValue))) {
                    propertyChangeType.setModificationType(PropertyModificationTypeType.replace);
                    logger.info("[StagingService] propertyChangeTypeType equals replace ");
                }               
		    if (null != propertyChangeType.getModificationType()) {
                    Document doc = null;
                    try {
                        doc = getXmlDocument();
                    } catch (ParserConfigurationException ex) {
                        logger.error("Failed to parse", ex);
                        throw new RuntimeException("Failed to parse", ex);
                    }
                    XPathType xpath = new XPathType(field[i].getName());
                    propertyChangeType.setPath(xpath.toElement(SchemaConstants.NS_C, "path", doc));
                    Element element = doc.createElement(field[i].getName());
                    doc.appendChild(element);
                    element.setTextContent(newValue);
                    if (propertyChangeType.getValue() == null) {
                        propertyChangeType.setValue(new Value());
                    }
                    propertyChangeType.getValue().getAny().add(doc.getDocumentElement());
                    changes.add(propertyChangeType);
                }
            }
        }

        return changes;
    }

    public String submitObjectStage(ObjectStageType stage) {
        Validate.notNull(stage);

        ObjectType stageObjectType = stage.getObject().getValue();
        ObjectType oldObject = (ObjectType) stage.getAny().get(0);

        List<PropertyModificationType> propertyChangeTypeList = setPropertyChangeTypeList(oldObject, stageObjectType);

        try { // Call Web Service Operation
            ModelPortType port = service_1.getModelPort();
            ObjectModificationType objectChange = new ObjectModificationType();
            objectChange.setOid(stageObjectType.getOid());
            objectChange.getPropertyModification().clear();
            objectChange.getPropertyModification().addAll(propertyChangeTypeList);
            port.modifyObject(objectChange);
        } catch (Exception ex) {
            logger.error("[StagingService] submitObjectStage failed");
            logger.error("Exception was: ", ex);
            return null;
        }

        return stageObjectType.getOid();
    }

    public void addUserStageAccount(Holder<ObjectStageType> stage, String resourceOid) {
        Validate.notNull(stage);
        Validate.notNull(resourceOid);
        Validate.notNull(stage.value);
        Validate.notNull(stage.value.getObject());

        if (!(stage.value.getObject().getValue() instanceof UserType)) {
            throw new IllegalArgumentException("Supported is only UserType as a stage object, provided argument was of type " + stage.value.getObject().getValue());
        }

        ResourceType resource;
        //1. get resource
        try { // Call Web Service Operation
            ModelPortType port = service_1.getModelPort();
            PropertyReferenceListType resolve = new PropertyReferenceListType();
            ObjectContainerType result = port.getObject(resourceOid, resolve);
            resource = (ResourceType) result.getObject();

        } catch (Exception ex) {
            logger.error("getObject for oid = {} failed", resourceOid);
            logger.error("Exception was: ", ex);
            return;
        }
        //2. create new empty account
        AccountShadowType account = (new ObjectFactory()).createAccountShadowType();
        //3. set resource to account
        account.setResource(resource);
        //4. add account to user stage
        UserType user = ((UserType) stage.value.getObject().getValue());
        user.getAccount().add(account);

    }
}
