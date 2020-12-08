/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2013-2016 ForgeRock AS.
 * Portions Copyright 2020 Wren Security
 */

package org.forgerock.openidm.auth.modules;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;

import org.forgerock.caf.authentication.api.AuthenticationException;
import org.forgerock.caf.authentication.api.MessageInfoContext;
import org.forgerock.caf.authentication.framework.AuditTrail;
import org.forgerock.http.protocol.Request;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openidm.auth.Authenticator;
import org.forgerock.openidm.auth.Authenticator.AuthenticatorResult;
import org.forgerock.openidm.auth.AuthenticatorFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
* Test the DelegatedAuthModule.
*/
public class DelegatedAuthModuleTest {

    private DelegatedAuthModule module;

    private Authenticator authenticator;

    @BeforeMethod
    public void setUp() throws ResourceException, AuthenticationException {
        AuthenticatorFactory authenticatorFactory = mock(AuthenticatorFactory.class);
        authenticator = mock(Authenticator.class);
        when(authenticatorFactory.apply(any(JsonValue.class))).thenReturn(authenticator);

        module = new DelegatedAuthModule(authenticatorFactory, IDMAuthModule.DELEGATED);
        module.initialize(null, null, null, json(object(field("queryOnResource", ""))).asMap());
    }

    @Test
    public void shouldValidateRequestWhenUsernameHeaderIsNull() throws AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();

        Request request = new Request();

        given(messageInfo.getRequest()).willReturn(request);
        request.getHeaders().put("X-OpenIDM-Username", null);
        request.getHeaders().put("X-OpenIDM-Password", "PASSWORD");

        //When
        AuthStatus authStatus = module.validateRequest(messageInfo, clientSubject, serviceSubject)
                .getOrThrowUninterruptibly();

        //Then
        verifyZeroInteractions(authenticator);
        assertTrue(clientSubject.getPrincipals().isEmpty());
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenUsernameHeaderIsEmptyString() throws AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();

        Request request = new Request();

        given(messageInfo.getRequest()).willReturn(request);
        request.getHeaders().put("X-OpenIDM-Username", "");
        request.getHeaders().put("X-OpenIDM-Password", "PASSWORD");

        //When
        AuthStatus authStatus = module.validateRequest(messageInfo, clientSubject, serviceSubject)
                .getOrThrowUninterruptibly();

        //Then
        verifyZeroInteractions(authenticator);
        assertTrue(clientSubject.getPrincipals().isEmpty());
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenPasswordHeaderIsNull() throws AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();

        Request request = new Request();

        given(messageInfo.getRequest()).willReturn(request);
        request.getHeaders().put("X-OpenIDM-Username", "USERNAME");
        request.getHeaders().put("X-OpenIDM-Password", null);

        //When
        AuthStatus authStatus = module.validateRequest(messageInfo, clientSubject, serviceSubject)
                .getOrThrowUninterruptibly();

        //Then
        verifyZeroInteractions(authenticator);
        assertTrue(clientSubject.getPrincipals().isEmpty());
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenPasswordHeaderIsEmptyString() throws AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();

        Request request = new Request();

        given(messageInfo.getRequest()).willReturn(request);
        request.getHeaders().put("X-OpenIDM-Username", "USERNAME");
        request.getHeaders().put("X-OpenIDM-Password", "");

        //When
        AuthStatus authStatus = module.validateRequest(messageInfo, clientSubject, serviceSubject)
                .getOrThrowUninterruptibly();

        //Then
        verifyZeroInteractions(authenticator);
        assertTrue(clientSubject.getPrincipals().isEmpty());
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test(enabled = true)
    public void shouldValidateRequestWhenAuthenticationSuccessful() throws ResourceException, AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        AuthenticatorResult authResult = mock(AuthenticatorResult.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        Map<String, Object> messageInfoMap = new HashMap<String, Object>();
        Map<String, Object> auditInfoMap = new HashMap<String, Object>();

        Request request = new Request();

        given(messageInfo.getRequest()).willReturn(request);
        request.getHeaders().put("X-OpenIDM-Username", "USERNAME");
        request.getHeaders().put("X-OpenIDM-Password", "PASSWORD");
        given(messageInfo.getRequestContextMap()).willReturn(messageInfoMap);
        messageInfoMap.put(AuditTrail.AUDIT_INFO_KEY, auditInfoMap);

        given(authResult.isAuthenticated()).willReturn(true);
        given(authenticator.authenticate(eq("USERNAME"), eq("PASSWORD"),
                any())).willReturn(authResult);

        //When
        AuthStatus authStatus = module.validateRequest(messageInfo, clientSubject, serviceSubject)
                .getOrThrowUninterruptibly();

        //Then
        assertEquals("USERNAME", clientSubject.getPrincipals().iterator().next().getName());
        assertEquals(authStatus, AuthStatus.SUCCESS);
    }

    @Test(enabled = true)
    public void shouldValidateRequestWhenAuthenticationFailed() throws ResourceException, AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        AuthenticatorResult authResult = mock(AuthenticatorResult.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        Map<String, Object> messageInfoMap = new HashMap<String, Object>();
        Map<String, Object> auditInfoMap = new HashMap<String, Object>();

        Request request = new Request();

        given(messageInfo.getRequest()).willReturn(request);
        request.getHeaders().put("X-OpenIDM-Username", "USERNAME");
        request.getHeaders().put("X-OpenIDM-Password", "PASSWORD");
        given(messageInfo.getRequestContextMap()).willReturn(messageInfoMap);
        messageInfoMap.put(AuditTrail.AUDIT_INFO_KEY, auditInfoMap);

        given(authResult.isAuthenticated()).willReturn(false);
        given(authenticator.authenticate(eq("USERNAME"), eq("PASSWORD"),
                any())).willReturn(authResult);

        //When
        AuthStatus authStatus = module.validateRequest(messageInfo, clientSubject, serviceSubject)
                .getOrThrowUninterruptibly();

        //Then
        assertTrue(clientSubject.getPrincipals().isEmpty());
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldSecureResponse() throws AuthException {

        //Given
        MessageInfoContext messageInfo = mock(MessageInfoContext.class);
        Subject serviceSubject = new Subject();

        //When
        AuthStatus authStatus = module.secureResponse(messageInfo, serviceSubject).getOrThrowUninterruptibly();

        //Then
        assertEquals(authStatus, AuthStatus.SEND_SUCCESS);
        verifyZeroInteractions(messageInfo);
    }
}
