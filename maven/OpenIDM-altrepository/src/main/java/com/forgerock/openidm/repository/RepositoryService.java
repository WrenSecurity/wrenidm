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
package com.forgerock.openidm.repository;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.model.Account;
import com.forgerock.openidm.model.User;
import com.forgerock.openidm.model.Property;
import com.forgerock.openidm.model.Resource;
import com.forgerock.openidm.model.ResourceAccessConfiguration;
import com.forgerock.openidm.model.ResourceObjectShadow;
import com.forgerock.openidm.model.SimpleDomainObject;
import com.forgerock.openidm.model.StringProperty;
import com.forgerock.openidm.repository.spring.GenericDao;
import com.forgerock.openidm.util.DOMUtil;
import com.forgerock.openidm.util.DebugUtil;
import com.forgerock.openidm.util.StringBufferOutputStream;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.util.constants.OpenIdmConstants;
import com.forgerock.openidm.util.patch.PatchException;
import com.forgerock.openidm.util.patch.PatchXml;
import com.forgerock.openidm.util.patch.PatchingListener;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import java.io.IOException;
import javax.ejb.Stateless;
import javax.jws.WebService;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.w3c.dom.Document;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import javax.xml.bind.JAXBElement;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import javax.xml.namespace.QName;

/**
 * TODO
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@WebService(serviceName = "repositoryService", portName = "repositoryPort", endpointInterface = "com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType", targetNamespace = "http://openidm.forgerock.com/xml/ns/public/repository/repository-1.wsdl") //, wsdlLocation = "META-INF/wsdl/xml/ns/private/repository/repositoryWrapper.wsdl"
@Stateless
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Transactional
public class RepositoryService implements RepositoryPortType {

    //public static final String XML_NS_COMMON = "com.forgerock.openidm.xml.ns._public.common.common_1";
    public static final String code_id = "$Id$";
    Trace logger = TraceManager.getTrace(RepositoryService.class);
    @Autowired(required = true)
    private GenericDao genericDao;

    private void storeOrImport(SimpleDomainObject entity) throws FaultMessage {
        //OPENIDM-137 - import has to preserve oid and fail if importing object is already stored in db
        if (null != entity.getOid()) {
            if (null == genericDao.findById(entity.getOid())) {
                genericDao.persist(entity);
            } else {
                throw new FaultMessage("Could not import, because object already exists in store", new ObjectAlreadyExistsFaultType());
            }
        } else {
            genericDao.persist(entity);
        }
    }

    //copy method are preparation for refactoring for deep copy general method.
    private void copyProperties(List<Element> extension, List<Property> properties) {
        for (Element element : extension) {
            Property property = new StringProperty();
            property.setPropertyName(element.getTagName());
            property.setPropertyValue(element.getTextContent());
            properties.add(property);
        }
    }

    private void copyUserToUserType(User user, UserType userType) throws FaultMessage {

        //3. making shallow copy
        Utils.copyPropertiesSilent(userType, user);

        //HACK: review this
        if (user.getEmail() != null) {
            userType.getEMailAddress().add(user.getEmail());
        }

        //6. copying account's references
        Set<Account> accounts = user.getAccounts();
        List<AccountShadowType> accountShadowTypeList = new ArrayList<AccountShadowType>();
        List<ObjectReferenceType> accountsRef = new ArrayList<ObjectReferenceType>();
        for (Account account : accounts) {
            AccountShadowType accountType = new AccountShadowType();
            copyAccountToAccountShadowType(account, accountType);
            accountShadowTypeList.add(accountType);
            ObjectReferenceType objectReferenceType = new ObjectReferenceType();
            objectReferenceType.setOid(String.valueOf(account.getOid()));
            accountsRef.add(objectReferenceType);

        }
        userType.getAccountRef().addAll(accountsRef);

    }

    private void copyUserTypeToUser(UserType userType, User user) {
        String oid = userType.getOid();
        userType.setOid(null);
        Utils.copyPropertiesSilent(user, userType);
        if (null != oid) {
            user.setOid(UUID.fromString(oid));
        }
        userType.setOid(oid);

        if (null != userType.getExtension()) {
            List<Element> extension = userType.getExtension().getAny();
            List<Property<?>> properties = new LinkedList<Property<?>>();
            copyProperties(extension, properties);
            user.setProperties(properties);
        }

        //HACK: review this
        List<String> emails = userType.getEMailAddress();
        if (!emails.isEmpty()) {
            user.setEmail(emails.get(0));
        }
    }

    private void copyAccountToAccountShadowType(Account account, AccountShadowType accountShadowType) throws FaultMessage {
        //1. preparing object, that cannot be copied by shallow copy method
        Resource resource = account.getResource();
        Map<String, String> attributes = account.getAttributes();
        String objectClass = account.getObjectClass();

        //2. setting the properties to null (not to copy them by shallow method)
        account.setResource(null);
        account.setAttributes(null);
        account.setObjectClass(null);

        //3. making shallow copy
        Utils.copyPropertiesSilent(accountShadowType, account);

        //4. setting back 
        account.setResource(resource);
        account.setAttributes(attributes);
        account.setObjectClass(objectClass);

        //5. making copy of the remaining objects, that were not copied by shallow copy
        //5.a - resource copy - only resource ref will be created
        //ResourceType resourceType = (new ObjectFactory()).createResourceType();
        //copyResourceToResourceType(resource, resourceType);
        //accountShadowType.setResource(resourceType);
        ObjectReferenceType objectReferenceType = new ObjectReferenceType();
        objectReferenceType.setOid(String.valueOf(resource.getOid()));
        accountShadowType.setResourceRef(objectReferenceType);

        //5.b - attributes copy
        List<Element> attributesType = new ArrayList();
        for (String attributeName : attributes.keySet()) {
            String attributeValue = attributes.get(attributeName);
            String attributeNamespace = attributeName.substring(0, attributeName.lastIndexOf(":"));
            attributeName = attributeName.substring(attributeName.lastIndexOf(":") + 1);
            Document doc = DOMUtil.getDocument();
            doc.setDocumentURI(attributeNamespace);
            //TODO: generalize
            Node node = doc.createElementNS(attributeNamespace, "ns66:" + attributeName);
            node.setTextContent(attributeValue);
            doc.appendChild(node);
            //ElementImpl element = new ElementImpl(doc, attributeName);
            //element.setTextContent(attributeValue);
            attributesType.add(doc.getDocumentElement());
        }
        ResourceObjectShadowType.Attributes rost = new ResourceObjectShadowType.Attributes();
        rost.getAny().addAll(attributesType);
        accountShadowType.setAttributes(rost);

        //5.c - handling object class
        objectClass = account.getObjectClass();
        String objectClassNamespace = StringUtils.substring(objectClass, 0, StringUtils.lastIndexOf(objectClass, ":"));
        String objectClassName = StringUtils.substring(objectClass, StringUtils.lastIndexOf(objectClass, ":") + 1);
        accountShadowType.setObjectClass(new QName(objectClassNamespace, objectClassName));

    }

    private void copyResourceObjectShadowToResourceObjectShadowType(ResourceObjectShadow account, ResourceObjectShadowType accountShadowType) throws FaultMessage {
        //1. preparing object, that cannot be copied by shallow copy method
        Resource resource = account.getResource();
        Map<String, String> attributes = account.getAttributes();
        String objectClass = account.getObjectClass();

        //2. setting the properties to null (not to copy them by shallow method)
        account.setResource(null);
        account.setAttributes(null);
        account.setObjectClass(null);

        //3. making shallow copy
        Utils.copyPropertiesSilent(accountShadowType, account);

        //4. setting back
        account.setResource(resource);
        account.setAttributes(attributes);
        account.setObjectClass(objectClass);

        //5. making copy of the remaining objects, that were not copied by shallow copy
        //5.a - resource copy - only resource ref will be created
        //ResourceType resourceType = (new ObjectFactory()).createResourceType();
        //copyResourceToResourceType(resource, resourceType);
        //accountShadowType.setResource(resourceType);
        ObjectReferenceType objectReferenceType = new ObjectReferenceType();
        objectReferenceType.setOid(String.valueOf(resource.getOid()));
        accountShadowType.setResourceRef(objectReferenceType);

        //5.b - attributes copy
        List<Element> attributesType = new ArrayList();
        for (String attributeName : attributes.keySet()) {
            String attributeValue = attributes.get(attributeName);
            String attributeNamespace = attributeName.substring(0, attributeName.lastIndexOf(":"));
            attributeName = attributeName.substring(attributeName.lastIndexOf(":") + 1);
            Document doc = DOMUtil.getDocument();
            doc.setDocumentURI(attributeNamespace);
            //TODO: generalize
            Node node = doc.createElementNS(attributeNamespace, "ns66:" + attributeName);
            node.setTextContent(attributeValue);
            doc.appendChild(node);
            //ElementImpl element = new ElementImpl(doc, attributeName);
            //element.setTextContent(attributeValue);
            attributesType.add(doc.getDocumentElement());
        }
        ResourceObjectShadowType.Attributes rost = new ResourceObjectShadowType.Attributes();
        rost.getAny().addAll(attributesType);
        accountShadowType.setAttributes(rost);

        //5.c - handling object class
        objectClass = account.getObjectClass();
        String objectClassNamespace = StringUtils.substring(objectClass, 0, StringUtils.lastIndexOf(objectClass, ":"));
        String objectClassName = StringUtils.substring(objectClass, StringUtils.lastIndexOf(objectClass, ":") + 1);
        accountShadowType.setObjectClass(new QName(objectClassNamespace, objectClassName));

    }

    private void copyAccountShadowTypeToAccount(AccountShadowType accountShadowType, Account account) {
        ResourceObjectShadowType.Attributes attributesType = accountShadowType.getAttributes();
        accountShadowType.setAttributes(null);
        String oid = accountShadowType.getOid();
        if (null != oid) {
            account.setOid(UUID.fromString(oid));
        }
        accountShadowType.setOid(null);
        QName objectClass = accountShadowType.getObjectClass();

        Utils.copyPropertiesSilent(account, accountShadowType);
        accountShadowType.setAttributes(attributesType);
        accountShadowType.setOid(oid);
        accountShadowType.setObjectClass(objectClass);

        if (null != accountShadowType.getExtension()) {
            List<Element> extension = accountShadowType.getExtension().getAny();
            List<Property> properties = new LinkedList<Property>();
            copyProperties(extension, properties);
            account.setProperties(properties);
        }

        Map<String, String> attributes = new HashMap<String, String>(0);
        if (null != attributesType) {

            for (Object object : attributesType.getAny()) {

                Element element = (Element) object;

                //ElementImpl element = (ElementImpl) object;
                if (null != element.getNamespaceURI()) {
                    attributes.put(element.getNamespaceURI() + ":" + element.getLocalName(), element.getTextContent());
                } else {
                    attributes.put(element.getLocalName(), element.getTextContent());
                }
            }
        }
        account.setAttributes(attributes);

        //account's resource
        ObjectReferenceType resourceRef = accountShadowType.getResourceRef();
        SimpleDomainObject resource = genericDao.findById(UUID.fromString(resourceRef.getOid()));
        account.setResource((Resource) resource);

        //account's object class
        String objectClassString = objectClass.getNamespaceURI() + ":" + objectClass.getLocalPart();
        account.setObjectClass(objectClassString);
    }

    private void copyResourceObjectShadowTypeToResourceObjectShadow(ResourceObjectShadowType accountShadowType, ResourceObjectShadow account) {
        ResourceObjectShadowType.Attributes attributesType = accountShadowType.getAttributes();
        accountShadowType.setAttributes(null);
        String oid = accountShadowType.getOid();
        if (null != oid) {
            account.setOid(UUID.fromString(oid));
        }
        accountShadowType.setOid(null);
        QName objectClass = accountShadowType.getObjectClass();

        Utils.copyPropertiesSilent(account, accountShadowType);
        accountShadowType.setAttributes(attributesType);
        accountShadowType.setOid(oid);
        accountShadowType.setObjectClass(objectClass);

        if (null != accountShadowType.getExtension()) {
            List<Element> extension = accountShadowType.getExtension().getAny();
            List<Property> properties = new LinkedList<Property>();
            copyProperties(extension, properties);
            account.setProperties(properties);
        }

        Map<String, String> attributes = new HashMap<String, String>(0);
        if (null != attributesType) {

            for (Object object : attributesType.getAny()) {

                Element element = (Element) object;

                //ElementImpl element = (ElementImpl) object;
                if (null != element.getNamespaceURI()) {
                    attributes.put(element.getNamespaceURI() + ":" + element.getLocalName(), element.getTextContent());
                } else {
                    attributes.put(element.getLocalName(), element.getTextContent());
                }
            }
        }
        account.setAttributes(attributes);

        //account's resource
        ObjectReferenceType resourceRef = accountShadowType.getResourceRef();
        SimpleDomainObject resource = genericDao.findById(UUID.fromString(resourceRef.getOid()));
        account.setResource((Resource) resource);

        //account's object class
        String objectClassString = objectClass.getNamespaceURI() + ":" + objectClass.getLocalPart();
        account.setObjectClass(objectClassString);
    }

    private void copyResourceToResourceType(Resource resource, ResourceType resourceType) throws FaultMessage {
        Utils.copyPropertiesSilent(resourceType, resource);
        for (Property property : resource.getProperties()) {
            if ("schema".equals(property.getPropertyName())) {
                logger.debug("(String) property.getPropertyValue() = {}", (String) property.getPropertyValue());
                Document doc = DOMUtil.parseDocument((String) property.getPropertyValue());
                ResourceType.Schema schema = new ResourceType.Schema();
                List<Element> any = new ArrayList();
                any.add(doc.getDocumentElement());
                schema.getAny().addAll(any);
                resourceType.setSchema(schema);
            } else if ("schemaHandling".equals(property.getPropertyName())) {
                try {
                    SchemaHandling rsht = (SchemaHandling) JAXBUtil.unmarshal((String) property.getPropertyValue());
                    resourceType.setSchemaHandling(rsht);
                } catch (JAXBException ex) {
                    logger.error("Failed to unmarshal", ex);
                    throw new FaultMessage("Failed to unmarshal", new IllegalArgumentFaultType(), ex);
                }
            } else if ("configuration".equals(property.getPropertyName())) {
                try {
                    Configuration configuration = (Configuration) JAXBUtil.unmarshal((String) property.getPropertyValue());
                    resourceType.setConfiguration(configuration);
                } catch (JAXBException ex) {
                    logger.error("Failed to unmarshal", ex);
                    throw new FaultMessage("Failed to unmarshal", new IllegalArgumentFaultType(), ex);
                }
            } else if ("resourceAccessConfigurationRef".equals(property.getPropertyName())) {
                if (true) {
                    continue;
                }
                try {
                    JAXBElement<ResourceAccessConfigurationReferenceType> racReference = (JAXBElement<ResourceAccessConfigurationReferenceType>) JAXBUtil.unmarshal((String) property.getPropertyValue());
                    Object obj = racReference.getValue();
                    if (obj instanceof ResourceAccessConfigurationReferenceType) {
                        resourceType.setResourceAccessConfigurationRef((ResourceAccessConfigurationReferenceType) racReference.getValue());
                    }
                } catch (JAXBException ex) {
                    logger.error("Failed to unmarshal", ex);
                    throw new FaultMessage("Failed to unmarshal", new IllegalArgumentFaultType(), ex);
                }
            }
        }
    }

    private void copyResourceAccessConfigurationToResourceAccessConfiguration(ResourceAccessConfiguration resource, ResourceAccessConfigurationType resourceType) throws FaultMessage {
        Utils.copyPropertiesSilent(resourceType, resource);
        for (Property property : resource.getProperties()) {
            if ("schema".equals(property.getPropertyName())) {
                logger.debug("(String) property.getPropertyValue() = {}", (String) property.getPropertyValue());
                Document doc = DOMUtil.parseDocument((String) property.getPropertyValue());
                ResourceAccessConfigurationType.Schema schema = new ResourceAccessConfigurationType.Schema();
                List<Element> any = new ArrayList();
                any.add(doc.getDocumentElement());
                schema.getAny().addAll(any);
                resourceType.setSchema(schema);
            } else if ("schemaHandling".equals(property.getPropertyName())) {
                try {
                    SchemaHandling rsht = (SchemaHandling) JAXBUtil.unmarshal((String) property.getPropertyValue());
                    resourceType.setSchemaHandling(rsht);
                } catch (JAXBException ex) {
                    logger.error("Failed to unmarshal", ex);
                    throw new FaultMessage("Failed to unmarshal", new IllegalArgumentFaultType(), ex);
                }
            } else if ("configuration".equals(property.getPropertyName())) {
                try {
                    Configuration configuration = (Configuration) JAXBUtil.unmarshal((String) property.getPropertyValue());
                    resourceType.setConfiguration(configuration);
                } catch (JAXBException ex) {
                    logger.error("Failed to unmarshal", ex);
                    throw new FaultMessage("Failed to unmarshal", new IllegalArgumentFaultType(), ex);
                }
            } else if ("namespace".equals(property.getPropertyName())) {
                resourceType.setNamespace((String) property.getPropertyValue());
            }
        }
    }

    private void copyResourceAccessConfigurationTypeToResourceAccessConfiguration(ResourceAccessConfigurationType resourceType, ResourceAccessConfiguration resource) throws FaultMessage {
        String oid = resourceType.getOid();
        resourceType.setOid(null);
        Utils.copyPropertiesSilent(resource, resourceType);
        if (null != oid) {
            resource.setOid(UUID.fromString(oid));
        }
        resourceType.setOid(oid);
        List<Property> properties = new LinkedList<Property>();
        resource.setProperties(properties);

        //namespace property
        Property namespaceProperty = new StringProperty();
        namespaceProperty.setPropertyName("namespace");
        namespaceProperty.setPropertyValue(resourceType.getNamespace());
        properties.add(namespaceProperty);

        //extension processing begin
        if (null != resourceType.getExtension()) {
            List<Element> extension = resourceType.getExtension().getAny();
            copyProperties(extension, properties);
        }
        //extension processing end

        //schema processing begin
        ResourceAccessConfigurationType.Schema schema = resourceType.getSchema();

        // Schema is optional. It is almost always there, but in rare cases
        // it may not be there.
        if (schema != null && !schema.getAny().isEmpty()) {

            Document doc = DOMUtil.getDocument();
            for (Object object : schema.getAny()) {
                Element element = (Element) object;
                Node node = doc.importNode(element, true);
                doc.appendChild(node);
            }

            OutputStream fos = new StringBufferOutputStream();

            OutputFormat of = new OutputFormat("XML", "UTF-8", true);
            of.setIndent(1);
            of.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(fos, of);
            try {
                // As a DOM Serializer
                serializer.asDOMSerializer();
                serializer.serialize(doc.getDocumentElement());

            } catch (IOException ex) {
                logger.error("Failed to serialize", ex);
                throw new FaultMessage("Failed to serialize", new IllegalArgumentFaultType(), ex);
            }
            String strDoc = fos.toString();

            //xml document serialization end
            Property schemaProperty = new StringProperty();
            schemaProperty.setPropertyName("schema");
            schemaProperty.setPropertyValue(strDoc);
            properties.add(schemaProperty);
        }
        //schema processing end

        //schema handling begin
        try {
            SchemaHandling rsht = resourceType.getSchemaHandling();
            Property schemaHandlingProperty = new StringProperty();
            schemaHandlingProperty.setPropertyName("schemaHandling");
            schemaHandlingProperty.setPropertyValue(JAXBUtil.marshal(rsht));
            properties.add(schemaHandlingProperty);
        } catch (JAXBException ex) {
            logger.error("Failed to marshal", ex);
            throw new FaultMessage("Failed to marshal", new IllegalArgumentFaultType(), ex);
        }
        //schema handling end

        //configuration begin
        try {
            Configuration configuration = resourceType.getConfiguration();
            Property configurationProperty = new StringProperty();
            configurationProperty.setPropertyName("configuration");
            configurationProperty.setPropertyValue(JAXBUtil.marshal(configuration));
            properties.add(configurationProperty);
        } catch (JAXBException ex) {
            logger.error("Failed to marshal", ex);
            throw new FaultMessage("Failed to marshal", new IllegalArgumentFaultType(), ex);
        }
        //configuration end

        logger.info("properties.size() = " + properties.size());
        for (Property property : properties) {
            logger.info("Property name {}, value {}", property.getPropertyName(), property.getPropertyValue());
        }

    }

    private void copyResourceTypeToResource(ResourceType resourceType, Resource resource) throws FaultMessage {
        String oid = resourceType.getOid();
        resourceType.setOid(null);
        Utils.copyPropertiesSilent(resource, resourceType);
        if (null != oid) {
            resource.setOid(UUID.fromString(oid));
        }
        resourceType.setOid(oid);
        List<Property> properties = new LinkedList<Property>();
        resource.setProperties(properties);

        //extension processing begin
        if (null != resourceType.getExtension()) {
            List<Element> extension = resourceType.getExtension().getAny();
            copyProperties(extension, properties);
        }
        //extension processing end

        //resourceAccessConfigurationRef begin
        if (false && (null != resourceType.getResourceAccessConfigurationRef())) {
            try {
                JAXBElement<ResourceAccessConfigurationReferenceType> racReference = new JAXBElement(SchemaConstants.C_RAC_REF, ResourceAccessConfigurationReferenceType.class, null, resourceType.getResourceAccessConfigurationRef());
                Property resourceAccessConfigurationRef = new StringProperty();
                resourceAccessConfigurationRef.setPropertyName("resourceAccessConfigurationRef");
                resourceAccessConfigurationRef.setPropertyValue(JAXBUtil.marshal(racReference));
                properties.add(resourceAccessConfigurationRef);
            } catch (JAXBException ex) {
                logger.error("Failed to marshal", ex);
                throw new FaultMessage("Failed to marshal", new IllegalArgumentFaultType(), ex);
            }
        } else {
            //TODO: Invalid resource, can not be saved
            }
        //resourceAccessConfigurationRef end


        //schema processing begin
        ResourceType.Schema schema = resourceType.getSchema();

        // Schema is optional. It is almost always there, but in rare cases
        // it may not be there.
        if (schema != null && !schema.getAny().isEmpty()) {

            Document doc = DOMUtil.getDocument();
            for (Object object : schema.getAny()) {
                Element element = (Element) object;
                Node node = doc.importNode(element, true);
                doc.appendChild(node);
            }

            OutputStream fos = new StringBufferOutputStream();

            OutputFormat of = new OutputFormat("XML", "UTF-8", true);
            of.setIndent(1);
            of.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(fos, of);
            try {
                // As a DOM Serializer
                serializer.asDOMSerializer();
                serializer.serialize(doc.getDocumentElement());

            } catch (IOException ex) {
                logger.error("Failed to serialize", ex);
                throw new FaultMessage("Failed to serialize", new IllegalArgumentFaultType(), ex);
            }
            String strDoc = fos.toString();

            //xml document serialization end
            Property schemaProperty = new StringProperty();
            schemaProperty.setPropertyName("schema");
            schemaProperty.setPropertyValue(strDoc);
            properties.add(schemaProperty);
        }
        //schema processing end

        //schema handling begin
        try {
            SchemaHandling rsht = resourceType.getSchemaHandling();
            Property schemaHandlingProperty = new StringProperty();
            schemaHandlingProperty.setPropertyName("schemaHandling");
            schemaHandlingProperty.setPropertyValue(JAXBUtil.marshal(rsht));
            properties.add(schemaHandlingProperty);
        } catch (JAXBException ex) {
            logger.error("Failed to marshal", ex);
            throw new FaultMessage("Failed to marshal", new IllegalArgumentFaultType(), ex);
        }
        //schema handling end

        //configuration begin
        try {
            Configuration configuration = resourceType.getConfiguration();
            Property configurationProperty = new StringProperty();
            configurationProperty.setPropertyName("configuration");
            configurationProperty.setPropertyValue(JAXBUtil.marshal(configuration));
            properties.add(configurationProperty);
        } catch (JAXBException ex) {
            logger.error("Failed to marshal", ex);
            throw new FaultMessage("Failed to marshal", new IllegalArgumentFaultType(), ex);
        }
        //configuration end

        logger.info("properties.size() = " + properties.size());
        for (Property property : properties) {
            logger.info("Property name {}, value {}", property.getPropertyName(), property.getPropertyValue());
        }

    }

    public GenericDao getGenericDao() {
        return genericDao;
    }

    public void setGenericDao(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    private class RepositoryPatchingListener implements PatchingListener {

        private User user;

        public RepositoryPatchingListener(User user) {
            this.user = user;
        }

        @Override
        public boolean isApplicable(PropertyModificationType change) {
            //Model sends add account as add of accountRef
            Node node = change.getValue().getAny().get(0);
            String newValue = DOMUtil.serializeDOMToString(node);
            if (StringUtils.contains(newValue, "accountRef")) {
                Node oidNode = node.getAttributes().getNamedItem(OpenIdmConstants.ATTR_OID_NAME);
                //TODO: check if oid is really there, from GUI there could be also accountsRef without oid
                if (null == oidNode) {
                    return false;
                }
                String oid = oidNode.getNodeValue();
                Account account = (Account) genericDao.findById(UUID.fromString(oid));
                if (account == null) {
                    // Attepmt for a quick fix during a stressful period
                    // TODO: review
                    return false;
                }
                account.setUser(user);
                genericDao.merge(account);

                //account ref won't be added to patched xml
                return false;
            }

            return true;
        }

        @Override
        public void applied(PropertyModificationType change) {
            //no action required
        }
    }

    private void modifyUser(ObjectModificationType objectChange) throws FaultMessage {
        try {
            logger.info("[Repository] modifyUser begin");
            Validate.notNull(objectChange);
            SimpleDomainObject user = genericDao.findById(UUID.fromString(objectChange.getOid()));
            UserType userType = new UserType();
            copyUserToUserType((User) user, userType);
            PatchXml xmlPatchTool = new PatchXml();
            PatchingListener listener = new RepositoryPatchingListener((User) user);
            xmlPatchTool.setPatchingListener(listener);
            String xmlObject = xmlPatchTool.applyDifferences(objectChange, userType);
            JAXBElement<UserType> jaxb = (JAXBElement<UserType>) JAXBUtil.unmarshal(xmlObject);
            copyUserTypeToUser(jaxb.getValue(), (User) user);
            //mergeChanges(user, objectChange);
            genericDao.merge(user);
            logger.info("[Repository] modifyUser end");
        } catch (PatchException ex) {
            logger.error("Failed to patch user", ex);
            throw new FaultMessage(ex.getMessage(), new ReferentialIntegrityFaultType());
        } catch (JAXBException ex) {
            logger.error("Failed to marshall user", ex);
            throw new FaultMessage(ex.getMessage(), new ReferentialIntegrityFaultType());
        }

    }

    @Override
    public String addObject(ObjectContainerType objectContainer) throws FaultMessage {

        logger.info("### REPOSITORY # Enter addObject({})", DebugUtil.prettyPrint(objectContainer));

        if (objectContainer.getObject() instanceof UserType) {
            logger.info("[Repository] addUser begin");
            Validate.notNull(objectContainer);
            UserType userType = (UserType) objectContainer.getObject();
            User user = new User();
            copyUserTypeToUser(userType, user);
            storeOrImport(user);
            for (ObjectReferenceType ort : userType.getAccountRef()) {
                Account acc = (Account) genericDao.findById(UUID.fromString(ort.getOid()));
                acc.setUser(user);
                genericDao.merge(acc);
            }
            userType.setOid(user.getOid().toString());
            logger.info("[Repository] addUser end");

            logger.info("### REPOSITORY # Exit addObject(..) : {}", user.getOid().toString());

            return user.getOid().toString();

        }

        if (objectContainer.getObject() instanceof AccountShadowType) {
            logger.info("[Repository] addAccount begin");
            Validate.notNull(objectContainer);

            AccountShadowType accountShadowType = (AccountShadowType) objectContainer.getObject();
            Account account = new Account();
            copyAccountShadowTypeToAccount(accountShadowType, account);
            storeOrImport(account);
            accountShadowType.setOid(account.getOid().toString());
            logger.info("[Repository] addAccount end");

            logger.info("### REPOSITORY # Exit addObject(..) : {}", account.getOid().toString());

            return account.getOid().toString();

        }

        if (objectContainer.getObject() instanceof ResourceObjectShadowType) {
            logger.info("[Repository] addResourceObjectShadow begin");
            Validate.notNull(objectContainer);

            ResourceObjectShadowType accountShadowType = (ResourceObjectShadowType) objectContainer.getObject();
            ResourceObjectShadow account = new ResourceObjectShadow();
            copyResourceObjectShadowTypeToResourceObjectShadow(accountShadowType, account);
            storeOrImport(account);
            accountShadowType.setOid(account.getOid().toString());
            logger.info("[Repository] addResourceObjectShadow end");

            logger.info("### REPOSITORY # Exit addObject(..) : {}", account.getOid().toString());

            return account.getOid().toString();

        }

        if (objectContainer.getObject() instanceof ResourceType) {
            logger.info("[Repository] addResource begin");
            Validate.notNull(objectContainer);
            ResourceType resourceType = (ResourceType) objectContainer.getObject();
            Resource resource = new Resource();
            copyResourceTypeToResource(resourceType, resource);
//            logger.info("properties.size() = " + resource.getProperties().size());
//            for (Property property : resource.getProperties()) {
//                logger.info("Property name {}, value {}", property.getPropertyName(), property.getPropertyValue());
//            }
//
            storeOrImport(resource);
            resourceType.setOid(resource.getOid().toString());
            logger.info("[Repository] addResource end");

            logger.info("### REPOSITORY # Exit addObject(..) : {}", resource.getOid().toString());

            return resource.getOid().toString();
        }
        if (objectContainer.getObject() instanceof ResourceAccessConfigurationType) {
            logger.info("[Repository] addResourceAccessConfiguration begin");
            Validate.notNull(objectContainer);
            ResourceAccessConfigurationType resourceAccessConfigurationType = (ResourceAccessConfigurationType) objectContainer.getObject();
            ResourceAccessConfiguration resource = new ResourceAccessConfiguration();
            copyResourceAccessConfigurationTypeToResourceAccessConfiguration(resourceAccessConfigurationType, resource);
//            logger.info("properties.size() = " + resource.getProperties().size());
//            for (Property property : resource.getProperties()) {
//                logger.info("Property name {}, value {}", property.getPropertyName(), property.getPropertyValue());
//            }
//
            storeOrImport(resource);
            resourceAccessConfigurationType.setOid(resource.getOid().toString());
            logger.info("[Repository] addResourceAccessConfiguration end");

            logger.info("### REPOSITORY # Exit addObject(..) : {}", resource.getOid().toString());

            return resource.getOid().toString();
        }

        logger.info("### REPOSITORY # Exit addObject(..) : null");

        return null;

    }

    @Override
    public ObjectContainerType getObject(String oid, PropertyReferenceListType resolve) throws FaultMessage {

        logger.info("### REPOSITORY # Enter getObject({})", oid);

        logger.info("[Repository] getObject begin");

        Validate.notNull(oid);

        ObjectContainerType objectContainerType = new ObjectContainerType();
        SimpleDomainObject object = genericDao.findById(UUID.fromString(oid));
        if (null == object) {
            throw new FaultMessage("Object with oid = " + oid + " not found", new ObjectNotFoundFaultType());
        }
        if (object instanceof User) {
            User user = (User) object;
            UserType userType = new UserType();
            copyUserToUserType(user, userType);
            objectContainerType.setObject(userType);
            logger.info("### REPOSITORY # Exit getObject(..): {}", DebugUtil.prettyPrint(objectContainerType));
            return objectContainerType;
        }

        if (object instanceof Account) {
            Account account = (Account) object;
            AccountShadowType accountShadowType = new AccountShadowType();
            copyAccountToAccountShadowType(account, accountShadowType);
            objectContainerType.setObject(accountShadowType);
            logger.info("### REPOSITORY # Exit getObject(..): {}", DebugUtil.prettyPrint(objectContainerType));
            return objectContainerType;
        }

        if (object instanceof ResourceObjectShadow) {
            ResourceObjectShadow account = (ResourceObjectShadow) object;
            ResourceObjectShadowType accountShadowType = new ResourceObjectShadowType();
            copyResourceObjectShadowToResourceObjectShadowType(account, accountShadowType);
            objectContainerType.setObject(accountShadowType);
            if (logger.isInfoEnabled()) {
                logger.info("### REPOSITORY # Exit getObject(..): {}", DebugUtil.prettyPrint(objectContainerType));
            }
            return objectContainerType;
        }

        if (object instanceof Resource) {
            Resource resource = (Resource) object;
            ResourceType resourceType = new ResourceType();
            copyResourceToResourceType(resource, resourceType);
            objectContainerType.setObject(resourceType);
            logger.info("### REPOSITORY # Exit getObject(..): {}", DebugUtil.prettyPrint(objectContainerType));
            return objectContainerType;
        }

        if (object instanceof ResourceAccessConfiguration) {
            ResourceAccessConfiguration resource = (ResourceAccessConfiguration) object;
            ResourceAccessConfigurationType resourceType = new ResourceAccessConfigurationType();
            copyResourceAccessConfigurationToResourceAccessConfiguration(resource, resourceType);
            objectContainerType.setObject(resourceType);
            logger.info("### REPOSITORY # Exit getObject(..): {}", DebugUtil.prettyPrint(objectContainerType));
            return objectContainerType;
        }

        logger.error("### REPOSITORY # Fault getObject(..): Object with oid {} is not supported", oid);
        throw new FaultMessage("Object with oid " + oid + " is not supported", new UnsupportedObjectTypeFaultType());
    }

    private <T, U extends ObjectType> ObjectListType convertListOfObjects(List<T> objects, Class clazz) throws FaultMessage {
        logger.info("[Repository] convertListOfObjects begin");
        ObjectListType olt = new ObjectListType();

        for (T object : objects) {
            ObjectType objectType = null;
            try {
                objectType = (ObjectType) clazz.newInstance();
            } catch (InstantiationException ex) {
                logger.warn("Failed to copy properties for instances {}, {}. Error message was {}", new Object[]{object, objectType, ex.getMessage()});

            } catch (IllegalAccessException ex) {
                logger.warn("Failed to copy properties for instances {}, {}. Error message was {}", new Object[]{object, objectType, ex.getMessage()});

            }

            if (clazz.getSimpleName().equals("UserType")) {
                copyUserToUserType((User) object, (UserType) objectType);
            }
            if (clazz.getSimpleName().equals("AccountShadowType")) {
                copyAccountToAccountShadowType((Account) object, (AccountShadowType) objectType);
            }
            if (clazz.getSimpleName().equals("ResourceType")) {
                copyResourceToResourceType((Resource) object, (ResourceType) objectType);
            }

            olt.getObject().add(objectType);
        }
        logger.info("[Repository] convertListOfObjects end");
        return olt;

    }

    @Override
    public ObjectListType listObjects(String objectType) throws FaultMessage {

        logger.info("### REPOSITORY # Enter listObjects({})", objectType);
        try {
            if (Utils.getObjectType("UserType").equals(objectType)) {
                List<SimpleDomainObject> users = genericDao.findAllOfType("User");
                ObjectListType listOfObjects = convertListOfObjects(users, UserType.class);
                logger.info("### REPOSITORY # Exit listObjects(..) : {}", DebugUtil.prettyPrint(listOfObjects));
                return listOfObjects;
            }

            if (Utils.getObjectType("AccountType").equals(objectType)) {
                List<SimpleDomainObject> accounts = genericDao.findAllOfType("Account");
                ObjectListType listOfObjects = convertListOfObjects(accounts, AccountShadowType.class);
                logger.info("### REPOSITORY # Exit listObjects(..) : {}", DebugUtil.prettyPrint(listOfObjects));
                return listOfObjects;
            }

            if (Utils.getObjectType("ResourceType").equals(objectType)) {
                List<SimpleDomainObject> resources = genericDao.findAllOfType("Resource");
                ObjectListType listOfObjects = convertListOfObjects(resources, ResourceType.class);
                logger.info("### REPOSITORY # Exit listObjects(..) : {}", DebugUtil.prettyPrint(listOfObjects));
                return listOfObjects;
            }

        } catch (IllegalArgumentException ex) {
            throw new FaultMessage("Unsupported object type", new IllegalArgumentFaultType(), ex);
        }
        logger.error("### REPOSITORY # Fault getObject(..): Unsupported object type");
        throw new FaultMessage("Unsupported object type", new IllegalArgumentFaultType());
    }

    @Override
    public ObjectListType searchObjects(FilterType filter) throws FaultMessage {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void modifyObject(ObjectModificationType objectChange) throws FaultMessage {

        logger.info("### REPOSITORY # Enter modifyObject({})", DebugUtil.prettyPrint(objectChange));

        if (StringUtils.isEmpty(objectChange.getOid()) && objectChange.getPropertyModification().isEmpty()) {
            logger.warn("### REPOSITORY # Method modifyObject({}) was called with empty changes, Note: XMLUtil returns immediately if there are no results", DebugUtil.prettyPrint(objectChange));
            return;
        }

        SimpleDomainObject object;
        String oid = objectChange.getOid();

        if ((object = genericDao.findById(UUID.fromString(oid))) != null) {
            if (object instanceof User) {
                modifyUser(objectChange);
            } else {
                genericDao.merge(object);
            }
            logger.info("### REPOSITORY # Exit modifyObject(..)");
            return;
        }
        logger.error("### REPOSITORY # Fault modifyObject(..) : Object not modified");
        throw new FaultMessage("Object not modified", new ObjectNotFoundFaultType());
    }

    @Override
    public void deleteObject(String oid) throws FaultMessage {

        logger.info("### REPOSITORY # Enter deleteObject({})", oid);

        logger.info("[Repository] deleteObject begin");
        Validate.notNull(oid);

        SimpleDomainObject object;
        if ((object = genericDao.findById(UUID.fromString(oid))) != null) {
            genericDao.remove(object);
            logger.info("[Repository] deleteObject end");
            logger.info("### REPOSITORY # Exit deleteObject(..)");
            return;
        }

        logger.error("### REPOSITORY # Fault deleteObject(..) : Oid not found : {}", oid);
        throw new FaultMessage("Oid not found", new ObjectNotFoundFaultType());


    }

    @Override
    public PropertyAvailableValuesListType getPropertyAvailableValues(String oid, PropertyReferenceListType properties) throws FaultMessage {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public UserContainerType listAccountShadowOwner(String accountOid) throws FaultMessage {
        logger.info("[Repository] listAccountShadowOwner begin");
        Validate.notNull(accountOid);

        UserContainerType userContainerType = new UserContainerType();

        //TODO: reimplement - retrieve owner from DB, not by iteration over all users in Java
        if (genericDao.findById(UUID.fromString(accountOid)) != null) {

            List<SimpleDomainObject> users = genericDao.findAllOfType(User.class.getSimpleName());

            for (SimpleDomainObject user : users) {
                UserType userType = new UserType();
                Utils.copyPropertiesSilent(userType, user);
                Set<Account> accounts = (Set<Account>) ((User) user).getAccounts();
                for (Account account : accounts) {
                    if (account.getOid().equals(UUID.fromString(accountOid))) {
                        userContainerType.setUser(userType);
                    }
                }
            }
        } else {
            throw new FaultMessage("Account with oid " + accountOid + " does not exist", new ObjectNotFoundFaultType());
        }

        logger.info("[Repository] listAccountShadowOwner end");
        return userContainerType;
    }

    //TODO: roles etc
    @Override
    public ResourceObjectShadowListType listResourceObjectShadows(String resourceOid, String resourceObjectShadowType) throws FaultMessage {
        logger.info("[Repository] listResourceObjectShadows begin");
        Validate.notNull(resourceOid);
        Validate.notNull(resourceObjectShadowType);

        ResourceObjectShadowListType result = new ResourceObjectShadowListType();

        if (genericDao.findById(UUID.fromString(resourceOid)) != null) {

            if (Utils.getObjectType("AccountType").equals(resourceObjectShadowType)) {

                List<SimpleDomainObject> accounts = genericDao.findAllOfType(Account.class.getSimpleName());
                List<AccountShadowType> accountTypeList = new ArrayList<AccountShadowType>();
                for (SimpleDomainObject account : accounts) {
                    if (((Account) account).getResource().getOid().equals(UUID.fromString(resourceOid))) {
                        AccountShadowType accountShadowType = new AccountShadowType();
                        copyAccountToAccountShadowType((Account) account, accountShadowType);
                        accountTypeList.add(accountShadowType);
                    }
                }
                result.getObject().addAll(accountTypeList);
            }
        } else {
            throw new FaultMessage("Resource with oid " + resourceOid + " not found", new ObjectNotFoundFaultType());
        }
        return result;
    }
}
