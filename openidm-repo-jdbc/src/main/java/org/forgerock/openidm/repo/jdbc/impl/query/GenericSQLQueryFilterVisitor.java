/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 ForgeRock AS. All rights reserved.
 * Portions Copyright 2024 Wren Security.
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
package org.forgerock.openidm.repo.jdbc.impl.query;

import static org.forgerock.openidm.repo.util.Clauses.and;
import static org.forgerock.openidm.repo.util.Clauses.not;
import static org.forgerock.openidm.repo.util.Clauses.or;
import static org.forgerock.openidm.repo.util.Clauses.where;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.forgerock.json.JsonPointer;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.AbstractSQLQueryFilterVisitor;
import org.forgerock.openidm.repo.util.Clause;
import org.forgerock.openidm.util.ResourceUtil;
import org.forgerock.util.query.QueryFilter;
import org.forgerock.util.query.QueryFilterVisitor;

/**
 * {@link QueryFilterVisitor} for generating WHERE clause clause for generic table schema.
 *
 * <p>
 * Filter visitor does not support <i>contains</i> filters for collection members. Only simple <code>string</code>
 * based <i>contains</i> is supported.
 */
// TODO support collection based assertions
public class GenericSQLQueryFilterVisitor extends AbstractSQLQueryFilterVisitor<Clause, NamedParameterCollector> {

    private final int searchableLength;

    private final SQLBuilder builder;

    /**
     * Construct a QueryFilterVisitor to produce SQL for managed objects using the generic table structure.
     *
     * @param searchableLength the searchable length; properties longer than this will be trimmed to this length
     * @param builder the {@link SQLBuilder} to use to keep track of the select columns, table joins, and order by lists
     */
    public GenericSQLQueryFilterVisitor(final int searchableLength, SQLBuilder builder) {
        this.searchableLength = searchableLength;
        this.builder = builder;
    }

    private boolean isNumeric(final Object valueAssertion) {
        return valueAssertion instanceof Integer
                || valueAssertion instanceof Long
                || valueAssertion instanceof Float
                || valueAssertion instanceof Double;
    }

    private boolean isBoolean(final Object valueAssertion) {
        return valueAssertion instanceof Boolean;
    }

    private Object trimValue(final Object value) {
        if (isNumeric(value) || isBoolean(value)) {
            return value;
        } else {
            return StringUtils.left(value.toString(), searchableLength);
        }
    }

    /**
     * Generate the WHERE clause for properties table for a numeric value assertion.
     *
     * @param joinAlias the property table alias
     * @param operand the comparison operand
     * @param valueParam the value placeholder
     * @return SQL WHERE clause for properties table
     */
    protected Clause buildNumericValueClause(String propTable, String operand, String valueParam) {
        // XXX Should we distinguish between decimal and integer values? Jackson makes that distinction (7 vs 7.0).
        return where(propTable + ".proptype = 'java.lang.Integer'")
                .or(propTable + ".proptype = 'java.lang.Long'")
                // we can skip java.lang.Float as Jackson is not using it by default
                .or(propTable + ".proptype = 'java.lang.Double'")
                // CAST to DECIMAL as that is the most generic thing to do
                .and("CAST(" + propTable + ".propvalue AS DECIMAL) " + operand + " ${" + valueParam + "}");
    }

    /**
     * Generate the WHERE clause for properties table for a boolean value assertion.
     *
     * @param joinAlias the property table alias
     * @param operand the comparison operand
     * @param valueParam the value placeholder
     * @return SQL WHERE clause for properties table
     */
    protected Clause buildBooleanValueClause(String propTable, String operand, String valueParam) {
        return where(propTable + ".proptype = 'java.lang.Boolean'")
                .and(where(propTable + ".propvalue " + operand + " ${" + valueParam + "}"));
    }

    /**
     * Generate the WHERE clause for properties table for a string value assertion.
     *
     * @param joinAlias the property table alias
     * @param operand the comparison operand
     * @param valueParam the value placeholder
     * @return SQL WHERE clause for properties table
     */
    protected Clause buildStringValueClause(String joinAlias, String operand, String valueParam) {
        return where(joinAlias + ".propvalue " + operand + " ${" + valueParam + "}");
    }

    @Override
    public Clause visitValueAssertion(NamedParameterCollector collector, String operand, JsonPointer field, Object valueAssertion) {
        var valueParam = collector.register("v", convertValueAssertion(valueAssertion));

        if (ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(field)) {
            return where("obj.objectid " + operand + " ${" + valueParam + "}");
        }

        String propParam = collector.register("k", field.toString());
        String joinAlias = collector.generate("p");
        final Clause valueClause;
        if (isNumeric(valueAssertion)) {
            valueClause = buildNumericValueClause(joinAlias, operand, valueParam);
        } else if (isBoolean(valueAssertion)) {
            valueClause = buildBooleanValueClause(joinAlias, operand, valueParam);
        } else {
            valueClause = buildStringValueClause(joinAlias, operand, valueParam);
        }
        builder.leftJoin("${_dbSchema}.${_propTable}", joinAlias)
                .on(where(joinAlias + ".${_mainTable}_id = obj.id")
                        .and(where(joinAlias + ".propkey = ${" + propParam + "}")));
        return valueClause;
    }

    /**
     * Convert value assertion to SQL parameter type.
     *
     * @param valueAssertion value assertion to convert
     * @return converted assertion
     */
    protected Object convertValueAssertion(Object valueAssertion) {
        if (valueAssertion instanceof Boolean) {
            return ((Boolean) valueAssertion).booleanValue() ? "true" : "false";
        }
        return valueAssertion;
    }

    @Override
    public Clause visitAndFilter(NamedParameterCollector collector, List<QueryFilter<JsonPointer>> subfilters) {
        return and(subfilters.stream().map(filter -> filter.accept(this, collector)).collect(Collectors.toList()));
    }

    @Override
    public Clause visitOrFilter(NamedParameterCollector collector, List<QueryFilter<JsonPointer>> subfilters) {
        return or(subfilters.stream().map(filter -> filter.accept(this, collector)).collect(Collectors.toList()));
    }

    @Override
    public Clause visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
        if (ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(field)) {
            return where("(obj.objectid IS NOT NULL)"); // always TRUE -> NOT NULL is enforced by the schema
        } else {
            var propParam = collector.register("k", field.toString());
            var joinAlias = collector.generate("p");
            builder.leftJoin("${_dbSchema}.${_propTable}", joinAlias)
                    .on(where(joinAlias + ".${_mainTable}_id = obj.id")
                            .and(joinAlias + ".propkey = ${" + propParam + "}"));
            return where(joinAlias + ".propvalue IS NOT NULL");
        }
    }

    @Override
    public Clause visitBooleanLiteralFilter(NamedParameterCollector collector, boolean value) {
        return where(value ? "1 = 1" : "1 <> 1");
    }

    @Override
    public Clause visitNotFilter(NamedParameterCollector collector, QueryFilter<JsonPointer> subFilter) {
        return not(subFilter.accept(this, collector));
    }

    @Override
    public Clause visitContainsFilter(NamedParameterCollector collector, JsonPointer field, Object valueAssertion) {
        return super.visitContainsFilter(collector, field, trimValue(valueAssertion));
    }

    @Override
    public Clause visitEqualsFilter(NamedParameterCollector collector, JsonPointer field, Object valueAssertion) {
        return super.visitEqualsFilter(collector, field, trimValue(valueAssertion));
    }

    @Override
    public Clause visitStartsWithFilter(NamedParameterCollector collector, JsonPointer field, Object valueAssertion) {
        return super.visitStartsWithFilter(collector, field, trimValue(valueAssertion));
    }

}
