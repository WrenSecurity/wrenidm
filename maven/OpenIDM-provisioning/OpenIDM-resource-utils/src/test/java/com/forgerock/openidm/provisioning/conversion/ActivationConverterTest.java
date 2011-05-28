package com.forgerock.openidm.provisioning.conversion;

import com.forgerock.openidm.xml.ns._public.common.common_1.ActivationType;
import javax.xml.namespace.QName;
import junit.framework.Assert;




import org.junit.Test;
import org.w3c.dom.Node;

/**
 *
 * @author elek
 */
public class ActivationConverterTest {

    public ActivationConverterTest() {
    }

    @Test
    public void testConversion() {
        ActivationType type = new ActivationType();
        type.setEnabled(Boolean.FALSE);

        ActivationConverter converter = new ActivationConverter();
        Node node = converter.convertToXML(null, type);
        System.out.println(node.getLocalName());
        ActivationType n = (ActivationType) converter.convertToJava(node);

        Assert.assertFalse(n.isEnabled());
    }
}
