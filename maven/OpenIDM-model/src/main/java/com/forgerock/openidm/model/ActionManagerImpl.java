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
package com.forgerock.openidm.model;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.model.xpath.SchemaHandling;
import com.forgerock.openidm.provisioning.service.ProvisioningService;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import com.forgerock.openidm.xml.ns._public.provisioning.provisioning_1.ProvisioningPortType;
import java.util.Map;

/**
 *
 * @author Vilo Repan
 */
public class ActionManagerImpl<T extends Action> implements ActionManager {

    private static transient Trace trace = TraceManager.getTrace(ActionManagerImpl.class);
    //private static final String EJB_SPRING_CONTEXT_BEAN = "ejb-context";
    private Map<String, Class<T>> actionMap;
    private ModelPortType model;
    private ProvisioningPortType provisioning;
    private SchemaHandling schemaHandling;

    @Override
    public void setActionMapping(Map actionMap) {
        this.actionMap = actionMap;
    }

    @Override
    public Action getActionInstance(String uri) {
        Class clazz = actionMap.get(uri);
        if (clazz == null) {
            return null;
        }

        Action action = null;
        try {
            action = (Action) clazz.newInstance();
            ((AbstractAction)action).setModel((ModelService) model);
            ((AbstractAction)action).setProvisioning((ProvisioningService) provisioning);
            ((AbstractAction)action).setSchemaHandling(schemaHandling);
        } catch (InstantiationException ex) {
            trace.error("Couln't create action instance, reason: {}.", ex.getMessage());
            trace.debug("Couln't create action instance.", ex);
        } catch (IllegalAccessException ex) {
            trace.error("Couln't create action instance, reason: {}.", ex.getMessage());
            trace.debug("Couln't create action instance.", ex);
        }

        //TODO: Solve problem how to inject required objects into actions and not to depend on FActory required by EJB and make test working
//        Action action = null;
//        try {
//            BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
//            BeanFactoryReference bfr = locator.useBeanFactory(EJB_SPRING_CONTEXT_BEAN);
//            BeanFactory fac = bfr.getFactory();
//            if (!(fac instanceof ApplicationContext)) {
//                throw new IllegalStateException("Bean '" + EJB_SPRING_CONTEXT_BEAN +
//                        "' is not type of ApplicationContext.");
//            }
//
//            ApplicationContext context = (ApplicationContext) fac;
//            AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
//            //set to true after removing model service from ejb
//            Object object = factory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
//            if (object instanceof Action) {
//                action = (Action) object;
//                //action.setModel((ModelService)model);
//            } else {
//                throw new IllegalArgumentException("Uri '" + uri + "' maps on action class" +
//                        " which doesn't implement com.forgerock.openidm.model.Action interface.");
//            }
//        } catch (Exception ex) {
//            trace.error("Couln't create action instance, reason: {}.", ex.getMessage());
//            trace.debug("Couln't create action instance.", ex);
//        }

        return action;
    }

    public void setModel(ModelPortType model) {
        this.model = model;
    }

    public void setProvisioning(ProvisioningPortType provisioning) {
        this.provisioning = provisioning;
    }

    public void setSchemaHandling(SchemaHandling schemaHandling) {
        this.schemaHandling = schemaHandling;
    }

}
