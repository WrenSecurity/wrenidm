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
 * Copyright 2026 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.query;

import java.util.regex.Pattern;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;

/**
 * Utility methods for SQL/JSON path based queries (ISO/IEC 9075-2).
 *
 * <p>
 * Reference for the implementation:
 *
 * <ul>
 * <li>https://www.postgresql.org/docs/current/datatype-json.html#DATATYPE-JSONPATH
 * </ul>
 */
public class SQLJSONUtils {

    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("(?:[0-9]|[1-9]\\d+)");

    private SQLJSONUtils() {
    }

    /**
     * Convert JSON pointer to SQL/JSON path expression.
     *
     * <p>
     * Decimal pointer tokens (e.g. <code>123</code> in <code>/foo/123</code>) are always interpreted as array indexes.
     * Hence querying objects with numeric property names is not supported.
     *
     * @param pointer JSON pointer to convert
     * @return SQL/JSON path expression
     */
    public static String toSqlJsonPath(JsonPointer pointer) {
        StringBuilder builder = new StringBuilder("$");
        for (String token : pointer) {
            if (ARRAY_INDEX_PATTERN.matcher(token).matches()) {
                builder.append("[").append(token).append("]");
            } else {
                builder.append(".").append(new JsonValue(token).toString());
            }
        }
        return builder.toString();
    }

}
