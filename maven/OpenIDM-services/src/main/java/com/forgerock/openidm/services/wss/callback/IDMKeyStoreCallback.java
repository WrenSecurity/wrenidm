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
package com.forgerock.openidm.services.wss.callback;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.security.cert.KeyStoreManager;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class IDMKeyStoreCallback implements CallbackHandler {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(IDMKeyStoreCallback.class);

    @Override
    public void handle(Callback[] clbcks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < clbcks.length; i++) {
            if (clbcks[i] instanceof KeyStoreCallback) {
                KeyStoreCallback cb = (KeyStoreCallback) clbcks[i];

            } else if (clbcks[i] instanceof PrivateKeyCallback) {
                PrivateKeyCallback cb = (PrivateKeyCallback) clbcks[i];
                cb.setKeystore(KeyStoreManager.getKeyStore());
                cb.setKey(getKey(cb.getAlias()));
            }
        }
    }

    private PrivateKey getKey(String alias) {
        try {
            KeyStore keystore = KeyStoreManager.getKeyStore();
            Key key = keystore.getKey(null != alias ? alias : "xws-security-client", "changeit".toCharArray());
            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            }
        } catch (Exception ex) {
            logger.error("Failed retrive PrivateKey", ex);
        }
        return null;
    }
}
