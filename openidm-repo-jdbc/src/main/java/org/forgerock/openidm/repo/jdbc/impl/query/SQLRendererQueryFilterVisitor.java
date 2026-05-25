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
import org.forgerock.openidm.repo.util.Clauses;
import org.forgerock.openidm.repo.util.SQLRenderer;
import org.forgerock.util.query.QueryFilter;
import org.forgerock.util.query.QueryFilterVisitor;

/**
 * {@link SQLRenderer} based query filter visitor with customizable field-based filter rendering.
 */
public class SQLRendererQueryFilterVisitor implements
        QueryFilterVisitor<SQLRenderer<String>, NamedParameterCollector, JsonPointer> {

    private final FieldFilterVisitorResolver fieldVisitorResolver;

    /**
     * Create visitor instance.
     *
     * @param fieldVisitorResolver field filter visitor resolver
     */
    public SQLRendererQueryFilterVisitor(FieldFilterVisitorResolver fieldVisitorResolver) {
        this.fieldVisitorResolver = fieldVisitorResolver;
    }

    @Override
    public SQLRenderer<String> visitAndFilter(NamedParameterCollector collector,
            List<QueryFilter<JsonPointer>> subFilters) {
        return Clauses.and(subFilters.stream()
                .map(filter -> Clauses.where(filter.accept(this, collector).toSQL()))
                .toList());
    }

    @Override
    public SQLRenderer<String> visitOrFilter(NamedParameterCollector collector,
            List<QueryFilter<JsonPointer>> subFilters) {
        return Clauses.or(subFilters.stream()
                .map(filter -> Clauses.where(filter.accept(this, collector).toSQL()))
                .toList());
    }

    @Override
    public SQLRenderer<String> visitBooleanLiteralFilter(NamedParameterCollector collector,
            boolean value) {
        return Clauses.where(value ? "1 = 1" : "1 <> 1");
    }

    @Override
    public SQLRenderer<String> visitContainsFilter(NamedParameterCollector collector,
            JsonPointer field, Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitContainsFilter(collector, field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitEqualsFilter(NamedParameterCollector collector,
            JsonPointer field, Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitEqualsFilter(collector, field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitExtendedMatchFilter(NamedParameterCollector collector,
            JsonPointer field, String operator, Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitExtendedMatchFilter(collector, field, operator,
                valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitGreaterThanFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitGreaterThanFilter(collector, field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitGreaterThanOrEqualToFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitGreaterThanOrEqualToFilter(collector, field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitLessThanFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitLessThanFilter(collector, field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitLessThanOrEqualToFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitLessThanOrEqualToFilter(collector, field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitNotFilter(NamedParameterCollector collector, QueryFilter<JsonPointer> subFilter) {
        return Clauses.not(subFilter.accept(this, collector).toSQL());
    }

    @Override
    public SQLRenderer<String> visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
        return fieldVisitorResolver.resolve(field).visitPresentFilter(collector, field);
    }

    @Override
    public SQLRenderer<String> visitStartsWithFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return fieldVisitorResolver.resolve(field).visitStartsWithFilter(collector, field, valueAssertion);
    }

}
