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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Vilo Repan
 */
public class Credentials {

    public static String MESSAGE_DIGEST_TYPE = "SHA-256";
    private String password;
    private String encoding;
    private int failedLogins = 0;
    private long lastFailedLoginAttempt;

    public long getLastFailedLoginAttempt() {
        return lastFailedLoginAttempt;
    }

    public void setLastFailedLoginAttempt(long lastFailedLoginAttempt) {
        this.lastFailedLoginAttempt = lastFailedLoginAttempt;
    }

    public int getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(int failedLogins) {
        this.failedLogins = failedLogins;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password, String encoding) {
        this.password = password;
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public void addFailedLogin() {
        failedLogins++;
    }

    public void clearFailedLogin() {
        failedLogins = 0;
    }

    public static String hashWithSHA2(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance(MESSAGE_DIGEST_TYPE);
            byte[] bytes = md.digest(text.getBytes("utf-8"));

            builder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                builder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        return builder.toString();
    }
}
