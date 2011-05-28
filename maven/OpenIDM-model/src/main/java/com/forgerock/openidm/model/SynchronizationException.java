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

import com.forgerock.openidm.xml.ns._public.common.common_1.FaultType;

/**
 *
 * @author Vilo Repan
 */
public class SynchronizationException extends Exception {

    private FaultType faultType;

    public SynchronizationException(String message) {
        super(message);
    }

    public SynchronizationException(String message, FaultType faultType) {
        this(message, null, faultType);
    }

    public SynchronizationException(String message, Throwable throwable) {
        this(message, throwable, null);
    }

    public SynchronizationException(String message, Throwable throwable, FaultType faultType) {
        super(message, throwable);
        this.faultType = faultType;
    }

    public FaultType getFaultType() {
        return faultType;
    }
}
