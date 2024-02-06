/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 * Portions Copyright 2023 Wren Security.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.repo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for resolving and replacing named parameters (substitution tokens) in query strings.
 *
 * <p>
 * Substitution tokens are in the format of <code>${token-name}</code>.
 */
public class TokenHandler {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    /**
     * Extracts all the token names in the query string of format <code>${token-name}</code>
     *
     * @param queryString the query with tokens
     * @return the list of token names in the order they appear in the {@code queryString}
     */
    public List<String> extractTokens(String queryString) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(queryString);
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return tokens;
    }

    /**
     * Replaces tokens of format <code>${token-name}</code> in a query string with the specified
     * replacement string for all tokens.
     *
     * @param queryString the query string with tokens to replace
     * @param replacement the replacement string
     * @param excludePrefixes optional array of prefixes that, if found as part of a token, will
     * not be replaced
     * @return the query string with all tokens replaced
     */
    public String replaceTokens(String queryString, String replacement, String ... excludePrefixes) {
        Matcher matcher = TOKEN_PATTERN.matcher(queryString);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String tokenName = matcher.group(1);
            if (tokenName != null) {
                matcher.appendReplacement(result, "");
                if (hasTokenPrefix(tokenName, excludePrefixes)) {
                    result.append("${" + tokenName + "}");
                } else {
                    result.append(replacement);
                }
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Test whether the given token string has one of the provided prefixes in the format
     * <code>prefix:name</code>.
     *
     * @param token string token to test
     * @param prefixes token prefixes to search for
     * @return true if the token has one of the provided prefixes
     */
    private boolean hasTokenPrefix(String token, String ... prefixes) {
        String[] tokenParts = token.split(":", 2);
        if (tokenParts.length > 1) {
            for (String prefix : prefixes) {
                if (prefix.equals(tokenParts[0]) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Replace requested tokens in a query string with tokens of format <code>${token-name}</code>
     * with the given replacements, which may again be tokens (e.g. in another format)
     * or values. Tokens that have no replacement defined stay in the original token format.
     *
     * <p>
     * <b>CAUTION:</b> This method does not do any escaping or format checking and it is the
     * responsibility of the caller to provide safe and sanitized replacement values.
     *
     * @param queryString the query with tokens of format <code>${token-name}</code>
     * @param replacements the replacement strings, where the key is the token name in the query string,
     * and the value is the string to replace it with
     * @return the query with any defined replacement values/tokens replaced, and the remaining tokens
     * left in the original format
     */
    public String replaceSomeTokens(String queryString, Map<String, String> replacements) {
        Matcher matcher = TOKEN_PATTERN.matcher(queryString);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String tokenName = matcher.group(1);
            if (tokenName != null) {
                String replacement = replacements.get(tokenName);
                if (replacement == null) {
                    // if replacement not specified, keep the original token
                    replacement = "${" + tokenName + "}";
                }
                matcher.appendReplacement(result, "");
                result.append(replacement);
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Replaces requested tokens in a query string with tokens of format <code>${token-name}</code>
     * where token-name represents a list of values. The ${code numberOfReplacements} map tells
     * how many replacements to produce (comma-separated) for each token.  The replacement
     * (for all tokens) is provided. Tokens that have no replacement defined stay in the
     * original token format.
     *
     * @param queryString the query with tokens of format tokens <code>${token-name}</code>
     * @param numberOfReplacements the number of replacements to replace a <code>${token-name}</code> with
     * @param replacement the replacement string that will be repeated as specified by the number of
     * replacements
     * @return the query with any defined replacement values replaced, and the remaining tokens
     * left in the original format
     */
    public String replaceListTokens(String queryString, Map<String, Integer> numberOfReplacements, String replacement) {
        Matcher matcher = TOKEN_PATTERN.matcher(queryString);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String tokenName = matcher.group(1);
            if (tokenName != null) {
                matcher.appendReplacement(result, "");
                Integer length = numberOfReplacements.get(tokenName);
                if (length != null) {
                    for (int i = 0; i < length; i++) {
                        result.append(replacement);
                        if (i != length - 1) {
                            result.append(", ");
                        }
                    }
                } else {
                    result.append("${" + tokenName + "}");
                }
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
