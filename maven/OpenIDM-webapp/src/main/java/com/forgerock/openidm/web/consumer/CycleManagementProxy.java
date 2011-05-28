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
package com.forgerock.openidm.web.consumer;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import com.forgerock.openidm.xml.ns._public.model.cycle_management_1.CycleManagementPortType;
import com.forgerock.openidm.xml.ns._public.model.cycle_management_1.FaultMessage;

/**
 * Temporary solution to initialize Cycle Management service on start
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
public class CycleManagementProxy {

    private static final Logger logger = Logger.getLogger(CycleManagementProxy.class.getName());

    public CycleManagementProxy() {
        
    }

    public void init() throws FaultMessage {
        Runnable initRunnable = new InitRunnable();
        Thread initThread = new Thread (initRunnable);
        initThread.setDaemon(true);
        initThread.start();
        logger.info("Init thread started");
    }

}

class InitRunnable implements Runnable {
    public final static String DEPLOYMENT_SERVICE_MBEAN = "com.sun.jbi:JbiName=server,ServiceName=DeploymentService,ControlType=DeploymentService,ComponentType=System";
    public final static String SA_NAME = "openidm-assembly";

    private static final Logger logger = Logger.getLogger(InitRunnable.class.getName());

    public void run() {
        boolean succeeded = false;
        logger.fine("Init thread running");
        CycleManagementService cms = new CycleManagementService();
        CycleManagementPortType port = cms.getCycleManagementPort();
        javax.xml.ws.Holder<com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType> empty = new javax.xml.ws.Holder<com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType>();
        empty.value = new com.forgerock.openidm.xml.ns._public.common.common_1.EmptyType();
        while (!succeeded) {
            logger.fine("Calling init");
            try {
                if (isAssemblyStarted()) {
                    logger.info("Assembly is started, call init.");
                    port.init(empty);
                    succeeded = true;
                    logger.info("CycleManagement init invoke SUCCEEDED.");
                } else {
                    logger.info("Assembly not yet started, checking again in a little while.");
                }
            } catch (Exception ex) {
                logger.info("CycleManagement init invoke failed, waiting to re-try. : " + ex.getMessage());
            }
            if (!succeeded) {
                try {
                    java.lang.Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    logger.info("Sleep interrupted, stopping init attempts " + ex.getMessage());
                    succeeded = true;
                }
            }
        }
    }

    boolean isAssemblyStarted() {
        MBeanServer mBeanServer = getMBeanServer();
        Object result = null;
        if (mBeanServer != null) {
            try {
                ObjectName mBeanName = new ObjectName(DEPLOYMENT_SERVICE_MBEAN);
                result = mBeanServer.invoke(mBeanName, "getState", new Object[]{SA_NAME}, new String[] {"java.lang.String"});
                logger.fine("State of service assembly " + SA_NAME + ": " + result);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to retrieve service assembly state." + ex.getMessage(), ex);
            }
        }
        if (result != null) {
            String state = (String) result;
            return "Started".equalsIgnoreCase(state);
        } else {
            return false;
        }
    }

    MBeanServer getMBeanServer() {
        MBeanServer mBeanServer = null;
        java.util.ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
        if(!mBeanServers.isEmpty()) {
            mBeanServer = mBeanServers.get(0);
        }
        if(mBeanServer == null) {
             logger.warning("No MBean server found.");
        }
        return mBeanServer;
    }
}
