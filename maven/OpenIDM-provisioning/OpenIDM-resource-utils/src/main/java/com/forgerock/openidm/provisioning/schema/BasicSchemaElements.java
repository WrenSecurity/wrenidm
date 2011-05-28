package com.forgerock.openidm.provisioning.schema;

import com.forgerock.openidm.xml.schema.SchemaConstants;
import javax.xml.namespace.QName;

/**
 * Add common schema elenets inherently exsits in all schema. (eg. activation tag)
 *
 * @author elek
 */
public class BasicSchemaElements {

    /**
     * Add common elements to schema of ResourceSchadow.
     * @param def
     */
    public static void addElementsToResourceSchema(ResourceObjectDefinition def) {
        ResourceAttributeDefinition act = new ResourceAttributeDefinition(new QName(SchemaConstants.NS_C, "activation"));
        act.setType(new QName(SchemaConstants.NS_C, "ActivationType"));
        def.addAttribute(act);


    }
}
