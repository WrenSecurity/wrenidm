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

package com.forgerock.openidm.provisioning.synchronization;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.provisioning.service.ProvisioningService;

/**
 *
 * @author semancik
 */
public class SynchronizationProcessManager {

    private static final String THREAD_NAME = "openidm-synchronization";

    private SynchronizationProcess thread;
    private ProvisioningService provisioningService;

    public void setProvisioningService(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    private static final transient Trace logger = TraceManager.getTrace(SynchronizationProcessManager.class);
    private long JOIN_TIMEOUT = 5000;

    public SynchronizationProcessManager() {
        thread = null;
    }

    public void init() {
        logger.info("Synchronization Manager initialization");
        startThread();
    }

    public void shutdown() {
        logger.info("Synchronization Manager shutdown");
        stopThread();
    }

    private void startThread() {
        if (thread == null) {
            thread = new SynchronizationProcess(provisioningService);
            thread.setName(THREAD_NAME);
        }
        if (thread.isAlive()) {
            logger.warn("Attempt to start syncronization thread that is already running");
        } else {
            thread.start();
        }
    }

    private void stopThread() {
        if (thread == null) {
            logger.warn("Attempt to stop non-existing synchronization thread");
        } else {
            if (thread.isAlive()) {
                thread.disable();
                thread.interrupt();
                try {
                    thread.join(JOIN_TIMEOUT);
                } catch (InterruptedException ex) {
                    logger.warn("Wait to thread join in SynchronizationManager was interrupted");
                }
            } else {
                logger.warn("Attempt to stop a synchronization thread that is not alive");
            }
        }
    }

}
