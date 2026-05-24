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
import java.util.regex.Pattern;
import org.forgerock.json.JsonPointer;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.SQLRenderer;
import org.forgerock.openidm.repo.util.StringSQLRenderer;

/**
 * JSON field query filter visitor.
 *
 * <p>
 * This field filter visitor works only with compound value (JSON_LIST and JSON_MAP) columns.
 * Rendered filter format is based on SQL/JSON paths and functions.
 */
public class JsonFieldFilterVisitor extends SQLRendererFieldFilterVisitor {

    protected final MappedColumnConfig columnConfig;

    protected final ObjectMapper objectMapper;

    public JsonFieldFilterVisitor(MappedColumnConfig columnConfig, ObjectMapper objectMapper) {
        this.columnConfig = columnConfig;
        this.objectMapper = objectMapper;
    }

    private SQLRenderer<String> visitValueAssertion(NamedParameterCollector collector, String operand,
            JsonPointer field, Object valueAssertion) {
        if (!columnConfig.isJson()) {
            throw new UnsupportedOperationException("Primitive value filter not supported");
        }
        return visitJsonAssertion(collector, operand, toRelativePointer(field), valueAssertion);
    }

    protected SQLRenderer<String> visitJsonAssertion(NamedParameterCollector collector, String operand,
            JsonPointer nestedPath, Object valueAssertion) {

        String pathExpression = SQLJSONUtils.toSqlJsonPath(nestedPath) +
                " ? (@ " + operand + " " + toJsonValue(valueAssertion) + ")";

        String paramName = collector.register("v", pathExpression);
        return new StringSQLRenderer(
                "JSON_EXISTS(" + columnConfig.columnName + ", ${" + paramName + "})");
    }

    protected JsonPointer toRelativePointer(JsonPointer field) {
        return field.relativePointer(field.size() - columnConfig.propertyName.size());
    }

    protected String toJsonValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unexpected JSON conversion error", e);
        }
    }

    @Override
    public SQLRenderer<String> visitEqualsFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "==", field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitExtendedMatchFilter(NamedParameterCollector collector, JsonPointer field,
            String operator, Object valueAssertion) {
        throw new UnsupportedOperationException("Extended match filter not supported on this endpoint");
    }

    @Override
    public SQLRenderer<String> visitGreaterThanFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, ">", field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitGreaterThanOrEqualToFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, ">=", field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitLessThanFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "<", field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitLessThanOrEqualToFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "<=", field, valueAssertion);
    }

    @Override
    public SQLRenderer<String> visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
        return new StringSQLRenderer(columnConfig.columnName + " IS NOT NULL");
    }

    @Override
    public SQLRenderer<String> visitStartsWithFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "like_regex", field,
                "^" + Pattern.quote(String.valueOf(valueAssertion)));
    }

    @Override
    public SQLRenderer<String> visitContainsFilter(NamedParameterCollector collector, JsonPointer field,
            Object valueAssertion) {
        return visitValueAssertion(collector, "like_regex", field,
                Pattern.quote(String.valueOf(valueAssertion)));
    }

}
