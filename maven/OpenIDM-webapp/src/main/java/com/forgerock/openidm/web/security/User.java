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

import java.io.Serializable;

/**
 *
 * @author Vilo Repan
 */
public class User implements Serializable {

    private String oid;
    private String name;
    private String givenName;
    private String familyName;
    private String fullName;
    private String honorificPrefix;
    private String honorificSuffix;
    private Credentials credentials;
    private boolean enabled;

    public User(String oid, String name, boolean enabled) {
        if (oid == null || oid.isEmpty()) {
            throw new IllegalArgumentException("User oid can't be null, nor empty.");
        }
        if (name == null) {
            throw new IllegalArgumentException("User name can't be null.");
        }
        this.oid = oid;
        this.name = name;
        this.enabled = enabled;
    }

    public String getOid() {
        return oid;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
    }

    public void setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
    }

    public Credentials getCredentials() {
        if (credentials == null) {
            credentials = new Credentials();
        }
        
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }
}
