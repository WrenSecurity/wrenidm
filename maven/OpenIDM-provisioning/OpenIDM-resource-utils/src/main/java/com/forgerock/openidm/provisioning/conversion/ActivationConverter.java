package com.forgerock.openidm.provisioning.conversion;

import com.forgerock.openidm.xml.ns._public.common.common_1.ActivationType;
import com.forgerock.openidm.xml.schema.SchemaConstants;

/**
 * ActiovationType converter.
 *
 * @author elek
 */
@Deprecated
public class ActivationConverter extends JAXBConverter {

    public ActivationConverter() {
        super(SchemaConstants.NS_C, ActivationType.class);
    }
}
