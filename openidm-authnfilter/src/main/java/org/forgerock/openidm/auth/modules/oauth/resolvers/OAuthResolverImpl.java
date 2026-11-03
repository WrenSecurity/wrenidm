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
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2026 Wren Security.
 */
package org.forgerock.openidm.auth.modules.oauth.resolvers;

import static org.forgerock.json.JsonValue.json;

import java.io.IOException;
import java.net.URI;

import org.forgerock.http.Client;
import org.forgerock.http.header.GenericHeader;
import org.forgerock.http.protocol.Request;
import org.forgerock.http.protocol.Response;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.auth.modules.oauth.exceptions.OAuthVerificationException;

/**
 * Implementation for OAuth 2.0 resolvers.
 *
 * <p>A single instance is shared by all requests for the given identity provider, so it must not
 * hold any per-request state.
 */
public class OAuthResolverImpl implements OAuthResolver {

    private final JsonValue config;
    private final Client httpClient;
    private final String resolver;
    private final String subjectKey;

    public OAuthResolverImpl(final String resolver, final JsonValue resolverConfig, final Client httpClient) {
        this.config = resolverConfig;
        this.resolver = resolver;
        this.httpClient = httpClient;
        this.subjectKey = resolverConfig.get(AUTHENTICATION_ID).asString();
    }

    /**
     * Validates the supplied access token against this OAuth Idp.
     *
     * Validation is done by making a request to the user info endpoint
     * to determine if the provided access token is valid on the Idp.
     * If it is valid, a user profile is returned, from which we will extract
     * the subject that we will use to query a known profile later to determine
     * if there is an account associated with that subject.
     *
     * @param accessToken access token to verify.
     * @return the subject extracted from the user profile
     * @throws OAuthVerificationException if the accessToken is unable to be verified
     */
    @Override
    public String validateIdentity(final String accessToken) throws OAuthVerificationException {
        final JsonValue profile = sendGetRequest(URI.create(config.get(USER_INFO_ENDPOINT).asString()), accessToken);
        final JsonValue subject = profile.isMap() ? profile.get(subjectKey) : null;
        if (subject == null || !subject.isString() || subject.asString().isEmpty()) {
            throw new OAuthVerificationException("Unable to validate identity.");
        }
        return subject.asString();
    }

    @Override
    public String getName() {
        return this.resolver;
    }

    private JsonValue sendGetRequest(final URI uri, final String accessToken) throws OAuthVerificationException {
        final Request request = new Request()
                .setMethod("GET")
                .setUri(uri);
        request.getHeaders().put(new GenericHeader("Authorization", "Bearer " + accessToken));
        try (Response response = httpClient.send(request).getOrThrowUninterruptibly()) {
            if (!response.getStatus().isSuccessful()) {
                throw new OAuthVerificationException("User info request failed with status " + response.getStatus().getCode());
            }
            return json(response.getEntity().getJson());
        } catch (IOException e) {
            throw new OAuthVerificationException("Unable to read user info response", e);
        }
    }
}
