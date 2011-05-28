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
import java.util.ArrayList;
import java.util.List;
import javax.management.relation.Role;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import com.forgerock.openidm.app.service.UserDetailsService;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.w3c.dom.Element;

/**
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class InternalAuthenticationProvider implements AuthenticationProvider {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(InternalAuthenticationProvider.class);
    @Autowired
    private UserDetailsService userManagerService;
    private int loginTimeout;
    private int maxFailedLogins;

    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public void setMaxFailedLogins(int maxFailedLogins) {
        this.maxFailedLogins = maxFailedLogins;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (StringUtils.isBlank((String) authentication.getPrincipal()) || StringUtils.isBlank((String) authentication.getCredentials())) {
            throw new BadCredentialsException("Invalid username/password");
        }

        User user = null;
        List<GrantedAuthority> grantedAuthorities = null;
        try {
            user = userManagerService.getUser((String) authentication.getPrincipal());
            authenticateUser(user, (String) authentication.getCredentials());
        } catch (BadCredentialsException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Can't get user with username '{}'. Unknown error occured, reason {}.",
                    new Object[]{authentication.getPrincipal(), ex.getMessage()});
            logger.debug("Can't authenticate user '{}'.", new Object[]{authentication.getPrincipal()}, ex);
            throw new AuthenticationServiceException("Currently we are unable to process your request. Kindly try again later.");
        }

        if (user != null) {
            List<Role> roles = new ArrayList<Role>(0);   //user.getAssociatedRoles();
            grantedAuthorities = new ArrayList<GrantedAuthority>(roles.size());
            for (Role role : roles) {
                GrantedAuthority authority = new GrantedAuthorityImpl(role.getRoleName());
                grantedAuthorities.add(authority);
            }
            grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        } else {
            throw new BadCredentialsException("Invalid username/password");
        }

        return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), grantedAuthorities);
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return true;
    }

    private void authenticateUser(User user, String password) throws BadCredentialsException {
        if (user == null) {
            throw new BadCredentialsException("Invalid username/password.");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("User is disabled.");
        }

        Credentials credentials = user.getCredentials();
        if (credentials.getFailedLogins() >= maxFailedLogins) {
            long lockedTill = credentials.getLastFailedLoginAttempt() + (loginTimeout * 60000);
            if (lockedTill > System.currentTimeMillis()) {
                long time = (lockedTill - System.currentTimeMillis()) / 60000;
                throw new BadCredentialsException("User is locked, please wait " + time + " minute(s)");
            }
        }

        String pwd = credentials.getPassword();
        if (pwd == null) {
            throw new BadCredentialsException("User doesn't have defined password.");
        }

        String encodedPwd = null;
        if ("hash".equals(credentials.getEncoding())) {
            encodedPwd = Credentials.hashWithSHA2(password);
        } else if ("base64".equals(credentials.getEncoding())) {
            encodedPwd = Base64.encode(password);
        }

        if (encodedPwd == null || encodedPwd.isEmpty()) {
            throw new BadCredentialsException("Couldn't authenticate user, reason: couldn't encode password.");
        }

        if (encodedPwd.equals(pwd)) {
            if (credentials.getFailedLogins() > 0) {
                credentials.clearFailedLogin();
                userManagerService.updateUser(user);
            }
            return;
        }

        throw new BadCredentialsException("Invalid username/password.");
    }
}
