package com.forgerock.openidm.model.test.mock;

import com.forgerock.openidm.provisioning.service.ProvisioningService;
import com.forgerock.openidm.provisioning.service.ResourceAccessInterface;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;

/**
 * 
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
public class ProvisioningServiceMock extends ProvisioningService {

    private ResourceAccessInterface rai;

    public void setRai(ResourceAccessInterface rai) {
        this.rai = rai;
    }

    @Override
    protected ResourceAccessInterface getResourceAccessInterface(String resourceOID, ResourceType inputResource) throws com.forgerock.openidm.xml.ns._public.provisioning.provisioning_1.FaultMessage {
        return rai;
    }

    @Override
    protected ResourceAccessInterface getResourceAccessInterface(ResourceObjectShadowType shadow) throws com.forgerock.openidm.xml.ns._public.provisioning.provisioning_1.FaultMessage {
        return rai;
    }
}
