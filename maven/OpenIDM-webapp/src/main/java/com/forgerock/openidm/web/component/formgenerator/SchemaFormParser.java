package com.forgerock.openidm.web.component.formgenerator;

import com.forgerock.openidm.provisioning.schema.AccountObjectClassDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.provisioning.schema.util.DOMToSchemaParser;
import com.forgerock.openidm.provisioning.schema.util.SchemaParserException;
import com.forgerock.openidm.web.model.AccountShadowDto;
import com.forgerock.openidm.web.model.ResourceDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Vilo Repan
 */
public class SchemaFormParser {

    private static transient Logger logger = LoggerFactory.getLogger(SchemaFormParser.class);
    private QName defaultAccountType;
    private Map<SimpleKey, List<Object>> valueMap = new ValueHashMap<SimpleKey, List<Object>>();
    private String displayName;

    public List<ResourceAttributeDefinition> parseSchemaForAccount(AccountShadowDto account) throws SchemaParserException {
        if (account == null) {
            throw new IllegalArgumentException("Account shadow can't be null.");
        }

        logger.trace("Account contains default object class: " + account.getObjectClass());
        return parseSchemaForAccount(account, account.getObjectClass());
    }

    public List<ResourceAttributeDefinition> parseSchemaForAccount(AccountShadowDto account, QName accountType) throws SchemaParserException {
        if (account == null) {
            throw new IllegalArgumentException("Account shadow can't be null.");
        }

        List<Element> attrList = account.getAttributes();
        if (attrList != null) {
            createAttributeValueMap(attrList);
        }

        ResourceDto resource = account.getResource();
        if (resource == null) {
            throw new IllegalArgumentException("Resource dto can't be null.");
        }

        Element xsdSchema = resource.getSchema();
        if (xsdSchema == null) {
            throw new IllegalArgumentException("Resource doesn't contain schema element.");
        }

        if (accountType == null) {
            List<ResourceDto.AccountTypeDto> accountTypes = resource.getAccountTypes();
            for (ResourceDto.AccountTypeDto accountTypeDto : accountTypes) {
                if (accountTypeDto.isDefault()) {
                    accountType = accountTypeDto.getObjectClass();
                    break;
                }
            }
        }

        DOMToSchemaParser parser = new DOMToSchemaParser();
        ResourceSchema schema = parser.getSchema(resource.getSchema());

        if (accountType == null) {
            List<ResourceObjectDefinition> list = schema.getObjectClassesCopy();
            for (ResourceObjectDefinition object : list) {
                if (!(object instanceof AccountObjectClassDefinition)) {
                    continue;
                }

                AccountObjectClassDefinition def = (AccountObjectClassDefinition) object;
                if (def.isDefault()) {
                    accountType = def.getQName();
                    break;
                }
            }
        }

        if (accountType == null) {
            throw new com.forgerock.openidm.provisioning.schema.util.SchemaParserException("Account type was not defined.");
        }

        defaultAccountType = accountType;

        ResourceObjectDefinition definition = schema.getObjectDefinition(accountType);
        if (definition == null) {
            throw new com.forgerock.openidm.provisioning.schema.util.SchemaParserException("Account definition for type '" +
                    accountType + "' was not found.");
        }
        displayName = resource.getName() + ": " + definition.getName();

        List<ResourceAttributeDefinition> attributes = new ArrayList<ResourceAttributeDefinition>(definition.getAttributesCopy());
        for (ResourceAttributeDefinition def : attributes) {
            logger.trace("Attr. definition: " + def.getQName());
        }
        return attributes;
    }

    public QName getDefaultAccountType() {
        return defaultAccountType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<SimpleKey, List<Object>> getAttributeValueMap() {
        if (valueMap == null) {
            valueMap = new ValueHashMap<SimpleKey, List<Object>>();
        }

        return valueMap;
    }

    private void createAttributeValueMap(List<Element> attrList) {
        if (attrList == null) {
            return;
        }

        logger.trace("Attributes found in account:");
        List<Object> values = null;

        for (Element node : attrList) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                traceAttribute(node.getNamespaceURI(), node.getLocalName(), node.getTextContent());

                SimpleKey key = new SimpleKey(node.getNamespaceURI(), node.getLocalName());
                values = valueMap.get(key);
                if (values == null) {
                    values = new ArrayList<Object>();
                    valueMap.put(key, values);
                }
                values.add(node.getTextContent());
            }
        }
    }

    private void traceAttribute(String namespace, String name, String value) {
        if (!logger.isTraceEnabled()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(namespace);
        builder.append("}");
        builder.append(name);
        builder.append(": ");
        builder.append(value);

        logger.trace(builder.toString());
    }
}
