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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.forgerock.json.JsonPointer;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig.ValueType;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.SQLRenderer;
import org.forgerock.openidm.repo.util.StringSQLRenderer;

/**
 * Simple (primitive) field query filter visitor.
 *
 * <p>
 * This field filter visitor works only with primitive value columns. Compound values (JSON_LIST and JSON_MAP)
 * are not supported.
 *
 * @see JsonFieldFilterVisitor
 */
public class SimpleFieldFilterVisitor extends SQLRendererFieldFilterVisitor {

    protected final MappedColumnConfig columnConfig;

    protected final ObjectMapper objectMapper;

    public SimpleFieldFilterVisitor(MappedColumnConfig columnConfig, ObjectMapper objectMapper) {
        this.columnConfig = columnConfig;
        this.objectMapper = objectMapper;
    }

    protected SQLRenderer<String> visitValueAssertion(NamedParameterCollector collector, String operand,
            Object valueAssertion) {
        if (columnConfig.isJson()) {
            throw new UnsupportedOperationException("Compound value filter not supported");
        }

        if (isNumeric(valueAssertion) && columnConfig.valueType == ValueType.NUMBER) {
            return visitNumericAssertion(collector, operand, valueAssertion);
        }

        if (valueAssertion instanceof Boolean && columnConfig.valueType == ValueType.BOOLEAN) {
            return visitBooleanAssertion(collector, operand, valueAssertion);
        }

        String paramName = collector.register("v", toStringValue(valueAssertion));
        return new StringSQLRenderer(
                columnConfig.columnName
                + " " + operand + " "
                + "${" + paramName + "}");
    }

    protected SQLRenderer<String> visitNumericAssertion(NamedParameterCollector collector, String operand,
            Object valueAssertion) {
        // convert column value to DECIMAL to ensure correct operator behavior
        String paramName = collector.register("v", valueAssertion);
        return new StringSQLRenderer(
                "CAST(" + columnConfig.columnName + " AS DECIMAL)"
                + " " + operand + " "
                + "CAST(${" + paramName + "} AS DECIMAL)");
    }

    protected SQLRenderer<String> visitBooleanAssertion(NamedParameterCollector collector, String operand,
            Object valueAssertion) {
        // convert column value to SMALLINT to ensure database vendor support
        String paramName = collector.register("v", ((Boolean) valueAssertion).booleanValue() ? 1 : 0);
        return new StringSQLRenderer(
                "CAST(" + columnConfig.columnName + " AS BIT)"
                + " " + operand + " "
                + "${" + paramName + "}");
    }

    protected boolean isNumeric(final Object valueAssertion) {
        return valueAssertion instanceof Integer
                || valueAssertion instanceof Long
                || valueAssertion instanceof Float
                || valueAssertion instanceof Double;
    }

    protected String toStringValue(Object valueAssertion) {
        try {
            return valueAssertion instanceof String stringAssertion
                    ? stringAssertion
                    : objectMapper.writeValueAsString(valueAssertion);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unexpected JSON conversion error", e);
        }
    }

    @Override
    public SQLRenderer<String> visitContainsFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "LIKE", "%" + valueAssertion + "%");
    }

    @Override
    public SQLRenderer<String> visitEqualsFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "=", valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitExtendedMatchFilter(NamedParameterCollector collector, JsonPointer field,
            String operator, Object valueAssertion) {
        throw new UnsupportedOperationException("Extended match filter not supported on this endpoint");
    }

    @Override
    public SQLRenderer<String> visitGreaterThanFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, ">", valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitGreaterThanOrEqualToFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, ">=", valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitLessThanFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "<", valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitLessThanOrEqualToFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "<=", valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
        return new StringSQLRenderer(columnConfig.columnName + " IS NOT NULL");
    }

    @Override
    public SQLRenderer<String> visitStartsWithFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "LIKE", valueAssertion + "%");
    }

}
