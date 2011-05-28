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
package com.forgerock.openidm.app.web;

import com.forgerock.openidm.web.security.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
//@ManagedBean
//@Scope("session")
@ManagedBean
@SessionScoped
public class PreferencesBean implements Serializable {

    public static final String code_id = "$Id$";
    private static final long serialVersionUID = 3765375463873658375L;
    private String version = "0.0.0.0.0.1 :))";
    private String menuPosition;
    private String styleLibrary = "forgerock";
    private String logoutLink = "logout.iface";
    private String helpLink = "welcomeICEfaces.iface";
    private String serverName = "Sandbox";
    private String standardConsoleLink = "step1.iface";
    private String currentColor;

    public PreferencesBean() {
        this.menuPosition = "left";
        this.currentColor = "gray";
    }

    public String getStyleLibrary() {
        return styleLibrary;
    }

    public boolean getIsUserLoggedIn() {
        return isUserInRole("ROLE_USER") || isUserInRole("ROLE_ADMIN");
    }

    protected boolean isUserInRole(final String role) {
        final Collection<GrantedAuthority> grantedAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (Iterator it = grantedAuthorities.iterator(); it.hasNext();) {
            final GrantedAuthority ga = (GrantedAuthority) it.next();
            if (role.equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public String getMenuPosition() {
        return menuPosition;
    }

    public void rightMenu() {
        menuPosition = "right";
    }

    public void leftMenu() {
        menuPosition = "left";
    }    

    public String getCurrentColor() {
        return currentColor;
    }

    public void grayTheme() {
        currentColor = "gray";
    }

    public void blueTheme() {
        currentColor = "blue";
    }

    public String getHelpLink() {
        return helpLink;
    }

    public String getLogoutLink() {
        return logoutLink;
    }

    public String getServerName() {
        return serverName;
    }

    public String getShortUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            return "Not Logged in";
        }

        if (principal instanceof User) {
            User user = (User) principal;
            if (user.getFullName() != null) {
                return user.getFullName();
            }
            
            return user.getName();
        }

        return principal.toString();
    }

    public String getStandardConsoleLink() {
        return standardConsoleLink;
    }

    public String getVersion() {
        return version;
    }
}
