
package com.forgerock.openidm.provisioning.integration.identityconnector;

import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.xml.ns._public.common.common_1.ActivationType;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import java.util.Set;
import javax.xml.namespace.QName;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

/**
 * Adapter interface to add more functionality without commit to ICF.
 * 
 * @author elek
 */
public class LdapActivityAdapter {
 
    public void preConvertAttributes(ConnectorFacade connector, ResourceObject resourceObject, Set<Attribute> attributes) {
        QName activationName = new QName(SchemaConstants.NS_C,"activation");
        //TODO the following part is under development. Currently the tests of provisioning is failed
        //if the implementations is turned on. It should be fixed

//        //TODO check if it's an OpenDJ server and not an other ldap server
//        ResourceAttribute activationAttr = resourceObject.getValue(activationName);
//        if (activationAttr!=null){
//            ActivationType act = activationAttr.getSingleJavaValue(ActivationType.class);
//            //. If this attribute exists in the user's entry with any value other than "false", then the account will be disabled. I
//            attributes.add(AttributeBuilder.build("ds-pwp-account-disabled", act.isEnabled() ? "false" : "true"));
//        }
//        resourceObject.removeValue(activationName);
    }

}
