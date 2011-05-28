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
package com.forgerock.openidm.security.cert;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample Class Doc
 * http://forums.oracle.com/forums/thread.jspa?threadID=1630768&tstart=0
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class KeyStoreManager {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(KeyStoreManager.class);
    public static final String KEYSTORE_PATH_PROPERTY = "javax.net.ssl.keyStore";
    public static final String TRUSTSTORE_PATH_PROPERTY = "javax.net.ssl.trustStore";
    public static final String CERTSTORE_PATH_PROPERTY = "javax.net.ssl.certStore";
    // The parsed key store backing this manager.
    private final static KeyStore keyStore;
    // The parsed key store backing this manager.
    private final static KeyStore trustStore;
    // The parsed key store backing this manager.
    private final static CertStore certStore = null;

    static {

        KeyStore keystore = null;
        try {
            String keystorePath = System.getProperty(KEYSTORE_PATH_PROPERTY);
            if (null != keystorePath) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(keystorePath);
                    keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keystore.load(is, "changeit".toCharArray());
                } catch (IOException ex) {
                    logger.error("File not found: {}", keystorePath, ex);
                } finally {
                    if (null != is) {
                        is.close();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Load TrustStore failed.", ex);
        } finally {
            keyStore = keystore;
        }


        KeyStore truststore = null;
        try {
            String truststorePath = System.getProperty(KEYSTORE_PATH_PROPERTY);
            if (null != truststorePath) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(truststorePath);
                    truststore = KeyStore.getInstance(KeyStore.getDefaultType());
                    truststore.load(is, "changeit".toCharArray());
                } catch (IOException ex) {
                    logger.error("File not found: {}", truststorePath, ex);
                } finally {
                    if (null != is) {
                        is.close();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Load TrustStore failed.", ex);
        } finally {
            trustStore = truststore;
        }

    }

    public static KeyStore getKeyStore() {
        return keyStore;
    }

    public static KeyStore getTrustStore() {
        return trustStore;
    }
}
