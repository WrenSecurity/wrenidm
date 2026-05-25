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

import java.util.List;
import org.forgerock.json.JsonPointer;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.SQLRenderer;
import org.forgerock.util.query.QueryFilter;
import org.forgerock.util.query.QueryFilterVisitor;

/**
 * Query filter visitor for atomic value assertion filter components.
 */
public abstract class SQLRendererFieldFilterVisitor implements
        QueryFilterVisitor<SQLRenderer<String>, NamedParameterCollector, JsonPointer> {

    @Override
    public final SQLRenderer<String> visitAndFilter(NamedParameterCollector collector,
            List<QueryFilter<JsonPointer>> subFilters) {
        throw new UnsupportedOperationException("unable to handle non-field filter");
    }

    @Override
    public final SQLRenderer<String> visitOrFilter(NamedParameterCollector collector,
            List<QueryFilter<JsonPointer>> subFilters) {
        throw new UnsupportedOperationException("unable to handle non-field filter");
    }

    @Override
    public final SQLRenderer<String> visitNotFilter(NamedParameterCollector collector,
            QueryFilter<JsonPointer> subFilter) {
        throw new UnsupportedOperationException("unable to handle non-field filter");
    }

    @Override
    public final SQLRenderer<String> visitBooleanLiteralFilter(NamedParameterCollector collector,
            boolean value) {
        throw new UnsupportedOperationException("unable to handle non-field filter");
    }

}
