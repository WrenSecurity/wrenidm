/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2024 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.refactor.statement;

/**
 * Information about single named SQL parameter as parsed from parameter token name.
 *
 * @see NamedParameterSql
 */
public class NamedParameterToken {

    private final String token;

    private final String name;

    private final Class<?> javaType;

    private final boolean list;

    /**
     * Create new parameter token information.
     *
     * @param token the original token name
     * @param name the name of the parameter
     * @param javaType the parameter's value java type
     * @param list whether the parameter represents list of values
     */
    public NamedParameterToken(String token, String name, Class<?> javaType, boolean list) {
        this.token = token;
        this.name = name;
        this.javaType = javaType;
        this.list = list;
    }

    /**
     * Get the full parameter token name.
     *
     * @return the full token name
     */
    public String getToken() {
        return token;
    }

    /**
     * Get simple parameter name (i.e. parameter name without its type hints).
     *
     * @return the parameter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get parameter's value java type used to convert String parameter values.
     *
     * @return the parameter's value java type
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Whether the parameter is a collection parameter that should be expanded when preparing the actual statement.
     *
     * @return true if the parameter is collection based parameter
     */
    public boolean isList() {
        return list;
    }

    /**
     * Parse parameter token name into a parameter token value object.
     *
     * <p>
     * Parameter token name (from <code>${token-name}</code>) can contain:
     *
     * <ul>
     * <li><code>list:</code> &ndash; list type prefix (optional)
     * <li><code>int:</code> &ndash; integer type prefix (optional)
     * <li><i>name</code> &ndash; actual parameter name
     *
     * @param tokenName the parameter token name
     * @return the parsed parameter token
     */
    public static NamedParameterToken parse(String tokenName) {
        String name = tokenName;
        boolean list = false;
        if (name.startsWith(NamedParameterSql.PREFIX_LIST + ":")) {
            list = true;
            name = name.substring(NamedParameterSql.PREFIX_LIST.length() + 1);
        }

        Class<?> javaType = null;
        if (name.startsWith(NamedParameterSql.PREFIX_INT + ":")) {
            javaType = Integer.class;
            name = name.substring(NamedParameterSql.PREFIX_INT.length() + 1);
        }

        return new NamedParameterToken(tokenName, name, javaType, list);
    }

}
