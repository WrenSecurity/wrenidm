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
package org.forgerock.openidm.repo.jdbc.impl.query;

import java.util.List;
import java.util.Map;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.util.query.QueryFilter;

/**
 * Functional interface for transforming query filters to SQL queries.
 */
@FunctionalInterface
public interface QueryFilterResolver {

    /**
     * Render query filter as SQL query.
     *
     * @param queryFilter the query filter to render
     * @param sortKeys sort keys
     * @param sqlParams query parameters to be used as named parameters
     * @return SQL builder instance that is able to produce the final SQL string
     */
    SQLBuilder resolveQueryFilter(QueryFilter<JsonPointer> queryFilter, List<SortKey> sortKeys,
            Map<String, Object> sqlParams);

}
