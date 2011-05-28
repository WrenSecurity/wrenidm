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
package com.forgerock.openidm.provisioning.aop;

import com.forgerock.openidm.api.exceptions.OpenIDMException;
import com.forgerock.openidm.provisioning.exceptions.InitialisationException;
import com.forgerock.openidm.provisioning.integration.identityconnector.IdentityConnector;
import com.forgerock.openidm.provisioning.integration.identityconnector.IdentityConnectorRAI;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.provisioning.service.AttributeChange;
import com.forgerock.openidm.provisioning.service.BaseResourceIntegration;
import com.forgerock.openidm.provisioning.service.DefaultResourceFactory;
import com.forgerock.openidm.provisioning.service.ResourceAccessInterface;
import com.forgerock.openidm.provisioning.service.ResourceFactory;
import com.forgerock.openidm.provisioning.service.ResultHandler;
import com.forgerock.openidm.provisioning.service.SynchronizationResult;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectChangeType;
import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceAccessConfigurationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectIdentificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceStateType.SynchronizationState;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceTestResultType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ScriptType;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * This class is for further use when the call can be intercepted by any
 * Aspect. It's just a gateway between the main provisioning and the
 * integrations project.
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class ResourceAccessAspect implements ResourceAccessInterface<BaseResourceIntegration> {

    public static final String code_id = "$Id$";

    private ResourceAccessInterface proxyClass = null;

    private ResourceFactory factory = new DefaultResourceFactory();

    ResourceAccessConfigurationType configuration = null;

    BaseResourceIntegration connector = null;

    @Override
    public ResourceObject add(OperationalResultType result, ResourceObject resourceObject, ResourceObjectShadowType shadow) throws OpenIDMException {
        return proxyClass.add(result, resourceObject, shadow);
    }


    @Override
    public ResourceObject modify(OperationalResultType result, ResourceObject identifier, ResourceObjectDefinition resourceObjectDefinition, Set<AttributeChange> changes) throws OpenIDMException {
        return proxyClass.modify(result, identifier, resourceObjectDefinition, changes);
    }

    @Override
    public void delete(OperationalResultType result, ResourceObject resourceObject) throws OpenIDMException {
        proxyClass.delete(result, resourceObject);
    }

    @Override
    public boolean test(OperationalResultType result, BaseResourceIntegration resource) throws OpenIDMException {
        return proxyClass.test(result, resource);
    }

    @Override
    public ResourceObject get(OperationalResultType result, ResourceObject resourceObject) throws OpenIDMException {
        return proxyClass.get(result, resourceObject);
    }

    
    @Override
    public void executeScript(OperationalResultType result, ScriptType script) throws OpenIDMException {
        proxyClass.executeScript(result, script);
    }

    @Override
    public ResourceObjectIdentificationType authenticate(OperationalResultType result, ResourceObject resourceObject) throws OpenIDMException {
        return proxyClass.authenticate(result, resourceObject);
    }

    @Override
    public ResourceSchema schema(OperationalResultType result, BaseResourceIntegration resource) throws OpenIDMException {
        return proxyClass.schema(result, resource);
    }

    @Override
    public Collection<ResourceObject> search(OperationalResultType result, ResourceObjectDefinition resourceObjectDefinition) throws OpenIDMException {
        return proxyClass.search(result, resourceObjectDefinition);
    }

    @Override
    public Method custom(OperationalResultType result, Object... input) throws OpenIDMException {
        return proxyClass.custom(result, input);
    }

    @Override
    public boolean configure(ResourceAccessConfigurationType configuration) throws InitialisationException {
        this.configuration = configuration;
        return true;
    }

    @Override
    public <T extends ResourceAccessInterface<BaseResourceIntegration>> T initialise(Class<T> type, BaseResourceIntegration resourceInstance) throws InitialisationException {
        IdentityConnector con = new IdentityConnector(resourceInstance);
        proxyClass = factory.checkout(IdentityConnectorRAI.class, con, null);
        connector = resourceInstance;
        return (T) this;
    }

    @Override
    public boolean dispose() {
        factory.checkin(proxyClass);
        return true;
    }

    @Override
    public Class<BaseResourceIntegration> getConnectorClass(String targetNamespace) {
        return BaseResourceIntegration.class;
    }

    @Override
    public BaseResourceIntegration getConnector() {
        return connector;
    }

    @Override
    public SynchronizationResult synchronize(SynchronizationState token, OperationalResultType result, ResourceObjectDefinition rod) throws OpenIDMException {
        return proxyClass.synchronize(token, result, rod);
    }

    @Override
    public ResourceTestResultType test() throws OpenIDMException {
        return proxyClass.test();
    }

    @Override
    public void iterativeSearch(OperationalResultType result, ResourceObjectDefinition resourceObjectDefinition, ResultHandler handler) throws OpenIDMException {
        proxyClass.iterativeSearch(result, resourceObjectDefinition, handler);
    }
}
