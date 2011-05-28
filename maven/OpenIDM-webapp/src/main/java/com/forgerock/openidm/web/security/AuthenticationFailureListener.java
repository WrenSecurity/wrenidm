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
package com.forgerock.openidm.web.security;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.app.service.UserDetailsService;
import com.forgerock.openidm.common.web.FacesUtils;
import com.forgerock.openidm.logging.TraceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vilo Repan
 */
@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private static transient Trace trace = TraceManager.getTrace(AuthenticationFailureListener.class);
    @Autowired
    private UserDetailsService userManagerService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent evt) {
        AuthenticationException ex = evt.getException();
        FacesUtils.addErrorMessage(ex.getMessage());
        
        String username = evt.getAuthentication().getName();
        if (userManagerService == null) {
            trace.warn("Person repository is not available. Authentication failure for '" +
                    username + "' couldn't be logged.");
            return;
        }

        User user = userManagerService.getUser(username);
        if (user == null) {
            return;
        }

        Credentials credentials = user.getCredentials();
        credentials.addFailedLogin();
        credentials.setLastFailedLoginAttempt(System.currentTimeMillis());

        userManagerService.updateUser(user);
    }
}
