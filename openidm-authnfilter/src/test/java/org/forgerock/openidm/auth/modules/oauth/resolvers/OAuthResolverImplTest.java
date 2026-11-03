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
 * Copyright 2026 Wren Security.
 */
package org.forgerock.openidm.auth.modules.oauth.resolvers;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.testng.Assert.assertEquals;

import org.forgerock.http.Client;
import org.forgerock.http.protocol.Response;
import org.forgerock.http.protocol.Status;
import org.forgerock.openidm.auth.modules.oauth.exceptions.OAuthVerificationException;
import org.testng.annotations.Test;

public class OAuthResolverImplTest {

    private static OAuthResolverImpl resolver(final Status status, final Object body) {
        final Client client = new Client((context, request) -> {
            final Response response = new Response(status);
            response.setEntity(body);
            return Response.newResponsePromise(response);
        });
        return new OAuthResolverImpl("test", json(object(
                field(OAuthResolver.AUTHENTICATION_ID, "sub"),
                field(OAuthResolver.USER_INFO_ENDPOINT, "https://idp.example.com/userinfo"))), client);
    }

    @Test
    public void shouldReturnSubject() throws Exception {
        assertEquals(resolver(Status.OK, object(field("sub", "alice"))).validateIdentity("token"), "alice");
    }

    @Test(expectedExceptions = OAuthVerificationException.class)
    public void shouldFailOnErrorStatus() throws Exception {
        resolver(Status.UNAUTHORIZED, object(field("sub", "alice"))).validateIdentity("token");
    }

    @Test(expectedExceptions = OAuthVerificationException.class)
    public void shouldFailOnMissingSubject() throws Exception {
        resolver(Status.OK, object(field("error", "invalid_token"))).validateIdentity("token");
    }

    @Test(expectedExceptions = OAuthVerificationException.class)
    public void shouldFailOnNonJsonBody() throws Exception {
        resolver(Status.OK, "not json").validateIdentity("token");
    }
}
