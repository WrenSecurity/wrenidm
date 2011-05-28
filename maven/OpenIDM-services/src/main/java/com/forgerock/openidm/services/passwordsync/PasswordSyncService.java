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
package com.forgerock.openidm.services.passwordsync;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.services.consumer.PasswordService;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectReferenceType;
import com.forgerock.openidm.xml.ns._public.provisioning.password_1.PasswordSyncPortType;
import com.forgerock.openidm.xml.ns._public.provisioning.password_1.ResponseCodeType;
import com.forgerock.openidm.xml.ns._public.provisioning.password_1.SynchronizePasswordRequestType;
import com.forgerock.openidm.xml.ns._public.provisioning.password_1.SynchronizePasswordResponseType;
import com.forgerock.openidm.xml.ns._public.model.password_1.PasswordSynchronizeRequestType;
import com.forgerock.openidm.xml.ns._public.provisioning.password_1.TestRequestType;
import com.forgerock.openidm.xml.ns._public.provisioning.password_1.TestResponseType;
import com.sun.xml.wss.XWSSecurityException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author laszlohordos
 */
@WebService(serviceName = "passwordSyncService", portName = "passwordSyncPort", endpointInterface = "com.forgerock.openidm.xml.ns._public.provisioning.password_1.PasswordSyncPortType", targetNamespace = "http://openidm.forgerock.com/xml/ns/public/provisioning/password-1.wsdl", wsdlLocation = "WEB-INF/wsdl/passwordSync.wsdl")
public class PasswordSyncService implements PasswordSyncPortType {

    @Resource
    WebServiceContext wsContext;
    //Logger
    private static final transient Trace logger = TraceManager.getTrace(PasswordSyncService.class);
    private PasswordService passwordService = new PasswordService();
    private Marshaller marshaller = null;

    private void debug(SynchronizePasswordRequestType body) {
        try {
            if (null == marshaller) {
                JAXBContext ctx = JAXBContext.newInstance(SynchronizePasswordRequestType.class);
                marshaller = ctx.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }

            JAXBElement<SynchronizePasswordRequestType> out = new JAXBElement<SynchronizePasswordRequestType>(new QName("http://openidm.forgerock.com/xml/ns/public/provisioning/password-1.xsd", "SynchronizePasswordRequestType"), SynchronizePasswordRequestType.class, null, body);

            marshaller.marshal(out, System.out);
        } catch (JAXBException ex) {
            Logger.getLogger(PasswordSyncService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public TestResponseType test(TestRequestType body) {
        TestResponseType response = new TestResponseType();
        response.setResult(ResponseCodeType.MALFORMED_REQUEST);
        try {
            X509Certificate principal = getPrincipal();
            if (null == principal) {
                response.setResult(ResponseCodeType.INVALID_SIGNATURE);
            } else if (null != body.getPassword()) {
                response.setDecryptedPassword(body.getPassword().getTextContent());
                response.setResult(ResponseCodeType.RECEIVED);
                response.setResourceName(principal.getSubjectX500Principal().getName(X500Principal.CANONICAL));
            }
        } catch (XWSSecurityException ex) {
            logger.error("Authentication Error", ex);
        } finally {
            return response;
        }
    }

    @Override
    public SynchronizePasswordResponseType synchronizePassword(SynchronizePasswordRequestType body) {
        SynchronizePasswordResponseType response = new SynchronizePasswordResponseType();
        response.setResult(ResponseCodeType.MALFORMED_REQUEST);
        try {
            X509Certificate principal = getPrincipal();
            if (null == principal) {
                response.setResult(ResponseCodeType.INVALID_SIGNATURE);
            }
            if (null != body.getPassword()) {
                PasswordSynchronizeRequestType request = new PasswordSynchronizeRequestType();

                //Get Remote IP from request
                HttpServletRequest exchange = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
                request.setEndpointName(exchange.getRemoteAddr());

                //Copy input ClientEndpoint
                request.setClientEndpoint(body.getClientEndpoint());

                //Copy identifiers
                //TODO Create an identifier type
                PasswordSynchronizeRequestType.Identifier i = new PasswordSynchronizeRequestType.Identifier();
                i.getAny().addAll(body.getIdentifier().getAny());
                request.setIdentifier(i);

                //TODO: Fix the queue
                request.setMessageid("????MissingBPEL???");


                //Copy the Timestamp from request now
                request.setPassword(body.getPassword());
                if (true) {
                    request.setTimestamp(body.getTimestamp());
                } else {
                    request.setTimestamp(Calendar.getInstance().getTimeInMillis());
                }

                ObjectReferenceType resRef = new ObjectReferenceType();
                resRef.setOid(getCN(principal));
                resRef.setType(new QName("http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd", "ResourceType"));

                request.setSubject(resRef);

                if (logger.isDebugEnabled()) {
                    debug(body);
                }

                //passwordService.getPasswordPort().synchronizePassword(new PasswordSynchronizeRequestType());
                passwordService.getPasswordPort().synchronizePassword(request);
                response.setResult(ResponseCodeType.RECEIVED);
            }
        } catch (XWSSecurityException ex) {
            logger.error("Authentication Error", ex);
        } catch (Throwable e) {
            logger.error("SynchronizePassword Error", e);
        } finally {
            return response;
        }
    }

    private X509Certificate getPrincipal() throws XWSSecurityException {
        X509Certificate principal = null;
        Subject sub = com.sun.xml.wss.SubjectAccessor.getRequesterSubject(wsContext);
        if (null != sub) {
            for (Object p : sub.getPublicCredentials()) {
                if (p instanceof X509Certificate) {
                    principal = (X509Certificate) p;
                    break;
                }
            }
        }
        return principal;
    }

    private String getCN(X509Certificate principal) {
        StringTokenizer tokens = new StringTokenizer(principal.getSubjectX500Principal().getName(X500Principal.CANONICAL), ",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.contains("cn=")) {
                return token.substring(3);
            }
        }
        //TODO: Throw error, Unknown Common Name
        return "";

    }
}
