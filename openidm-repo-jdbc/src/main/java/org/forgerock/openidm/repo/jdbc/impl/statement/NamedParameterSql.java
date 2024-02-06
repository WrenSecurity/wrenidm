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
package org.forgerock.openidm.repo.jdbc.impl.statement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.forgerock.openidm.repo.util.TokenHandler;

/**
 * Value class for a SQL statement with named parameter placeholders.
 *
 * <p>
 * Example SQL statement:
 * <code>SELECT * FROM foo WHERE name = ${name} OR value = ${int:value}</code>
 *
 * <p>
 * Named parameters (tokens) are either unqualified (string type) or with type specified prefix:
 *
 * <ul>
 * <li><code>${int:NAME}</code> &ndash; integer type parameter
 * <li><code>${list:NAME}</code> &ndash; list based parameter (will be exploded based on the input list length)
 * </ul>
 *
 * <p>
 * Parsed SQL statement contains all non-list parameters replaced with <code>?</code>. List parameter
 * replacement is performed before prepared statement creation when the actual number of substituted
 * values is known.
 */
public class NamedParameterSql {

    /**
     * Integer parameter prefix.
     */
    public static final String PREFIX_INT = "int";

    /**
     * List parameter prefix.
     */
    public static final String PREFIX_LIST = "list";

    private final String sqlString;

    private final List<NamedParameterToken> paramTokens;

    public NamedParameterSql(String sql) {
        TokenHandler tokenHandler = new TokenHandler();

        this.sqlString = tokenHandler.replaceTokens(sql, "?", PREFIX_LIST);
        this.paramTokens = tokenHandler.extractTokens(sql).stream()
                .map(NamedParameterToken::parse).collect(Collectors.toList());
    }

    /**
     * Get the SQL string with simple tokens replaced by <code>?</code>.
     *
     * @return parsed SQL string
     */
    public String getSqlString() {
        return sqlString;
    }

    /**
     * Get parsed named SQL parameter tokens.
     *
     * @return list with SQL parameter tokens
     */
    public List<NamedParameterToken> getParamTokens() {
        return paramTokens;
    }

    @Override
    public String toString() {
        // for logging purposes
        return sqlString;
    }

    /**
     * Parse the given SQL statement into a {@link NamedParameterSql}.
     *
     * <p>
     * All parameters <code>${param}</code> are interpreted as named query parameters that
     * resolve to prepared statement parameters. Parameters representing database identifiers
     * MUST be resolved before calling this method.
     *
     * @param sql the SQL statement to parse
     * @return parsed SQL statement instance
     */
    public static NamedParameterSql parse(String sql) {
        return parse(sql, null);
    }

    /**
     * Parse the given SQL statement into a {@link NamedParameterSql} after replacing the
     * specified static tokens.
     *
     * <p>
     * <b>CAUTION:</b> Replaced tokens are meant to be database identifier that does not
     * need any escaping.
     *
     * @param sql the SQL statement to parse
     * @param replacements replacement tokens (database identifiers)
     * @return parsed SQL statement instance
     */
    public static NamedParameterSql parse(String sql, Map<String, String> replacements) {
        TokenHandler tokenHandler = new TokenHandler();

        if (replacements != null) {
            sql = tokenHandler.replaceSomeTokens(sql, replacements);
        }

        return new NamedParameterSql(sql);
    }

}
