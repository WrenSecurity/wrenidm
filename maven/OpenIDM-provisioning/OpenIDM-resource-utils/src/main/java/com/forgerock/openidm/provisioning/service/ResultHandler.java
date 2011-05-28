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

package com.forgerock.openidm.provisioning.service;

import com.forgerock.openidm.provisioning.objects.ResourceObject;

/**
 * Classes implementing this interface are used to handle iterative results.
 *
 * It is only used to handle iterative search results now. It may be resused for
 * other purposes as well.
 * 
 * @author semancik
 */
public interface ResultHandler {

    /**
     * Handle a single result.
     * @param object Resource object to process.
     * @return true if the operation shoudl proceed, false if it should stop
     */
    public boolean handle(ResourceObject object);
    
}
