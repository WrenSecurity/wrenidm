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
 * "Portions Copyrighted 2011 [name of copyright owner]"
 * 
 * $Id$
 */
package com.forgerock.openidm.model;

import com.forgerock.openidm.provisioning.synchronization.SynchronizationProcessManager;
import com.forgerock.openidm.xml.ns._public.common.common_1.CycleListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.CycleType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.model.cycle_management_1.CycleManagementPortType;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 *
 * @author sleepwalker
 */
@WebService(serviceName = "cycleManagementService", portName = "cycleManagementPort", endpointInterface = "com.forgerock.openidm.xml.ns._public.model.cycle_management_1.CycleManagementPortType", targetNamespace = "http://openidm.forgerock.com/xml/ns/public/model/cycle-management-1.wsdl")//, wsdlLocation = "META-INF/wsdl/cycle-management-1.wsdl")
@Stateless
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class CycleManagementService implements CycleManagementPortType {

    public static final String SYNCHRONIZATIONCYCLE = "SynchronizationCycle";
    @Autowired(required = true)
    SynchronizationProcessManager synchronizationProcessManager;
    ObjectFactory objectFactory = new ObjectFactory();

    @Override
    public com.forgerock.openidm.xml.ns._public.common.common_1.CycleListType listCycles(com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType empty) throws com.forgerock.openidm.xml.ns._public.model.cycle_management_1.FaultMessage {
        CycleListType cycles = objectFactory.createCycleListType();
        CycleType cycle = objectFactory.createCycleType();
        cycle.setDisplayName("Synchronization Cycle");
        cycle.setName(SYNCHRONIZATIONCYCLE);
        cycles.getCycle().add(cycle);
        //TODO: set missing information, when it is provided by SynchronizationProcessManager
        return cycles;
    }

    @Override
    public com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType startCycle(java.lang.String name) throws com.forgerock.openidm.xml.ns._public.model.cycle_management_1.FaultMessage {
        if (SYNCHRONIZATIONCYCLE.equals(name)) {
            synchronizationProcessManager.init();
            return objectFactory.createEmptyType();
        } else {
            throw new IllegalArgumentException("Unknown cycle name " + name);
        }
    }

    @Override
    public com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType stopCycle(java.lang.String name) throws com.forgerock.openidm.xml.ns._public.model.cycle_management_1.FaultMessage {
        if (SYNCHRONIZATIONCYCLE.equals(name)) {
            synchronizationProcessManager.shutdown();
            return objectFactory.createEmptyType();
        } else {
            throw new IllegalArgumentException("Unknown cycle name " + name);
        }
    }

    @Override
    public void init(javax.xml.ws.Holder<com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType> empty) throws com.forgerock.openidm.xml.ns._public.model.cycle_management_1.FaultMessage {
        synchronizationProcessManager.init();
    }

    @Override
    public void shutdown(javax.xml.ws.Holder<com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType> empty) throws com.forgerock.openidm.xml.ns._public.model.cycle_management_1.FaultMessage {
        synchronizationProcessManager.shutdown();
    }
}
