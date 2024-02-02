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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.forgerock.json.JsonPointer;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedConfigResolver;
import org.forgerock.openidm.repo.jdbc.impl.handler.MappedColumnConfig.ValueType;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.util.StringSQLQueryFilterVisitor;
import org.forgerock.openidm.repo.util.StringSQLRenderer;
import org.forgerock.util.query.QueryFilterVisitor;

/**
 * {@link QueryFilterVisitor} for generating WHERE clause for mapped table schema.
 */
public class MappedSQLQueryFilterVisitor extends StringSQLQueryFilterVisitor<NamedParameterCollector> {

    protected final MappedConfigResolver configResolver;

    protected final ObjectMapper objectMapper;

    public MappedSQLQueryFilterVisitor(MappedConfigResolver configResolver, ObjectMapper objectMapper) {
        this.configResolver = configResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public StringSQLRenderer visitValueAssertion(NamedParameterCollector collector, String operand, JsonPointer field,
            Object valueAssertion) {
        MappedColumnConfig config = configResolver.resolve(field);

        // convert column value to DECIMAL to ensure correct operator behavior
        if (isNumeric(valueAssertion) && config.valueType == ValueType.NUMBER) {
            String paramName = collector.register("v", valueAssertion);
            return new StringSQLRenderer(
                    "CAST(" + config.columnName + " AS DECIMAL)"
                    + " " + operand + " "
                    + "CAST(${" + paramName + "} AS DECIMAL)");
        }

        // convert column value to BIT to ensure database vendor support
        if (valueAssertion instanceof Boolean && config.valueType == ValueType.BOOLEAN) {
            String paramName = collector.register("v", ((Boolean) valueAssertion).booleanValue() ? 1 : 0);
            return new StringSQLRenderer(
                    "CAST(" + config.columnName + " AS BIT)"
                    + " " + operand + " "
                    + "CAST(${" + paramName + "} AS BIT)");
        }

        String paramValue;
        try {
            paramValue = valueAssertion instanceof String
                    ? (String) valueAssertion
                    : objectMapper.writeValueAsString(valueAssertion);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unexpected JSON conversion error", e);
        }

        String paramName = collector.register("v", paramValue);
        return new StringSQLRenderer(
                config.columnName
                + " " + operand + " "
                + "${" + paramName + "}");
    }


    @Override
    public StringSQLRenderer visitPresentFilter(NamedParameterCollector collector, JsonPointer field) {
        MappedColumnConfig config = configResolver.resolve(field);
        return new StringSQLRenderer(config.columnName + " IS NOT NULL");
    }

    protected boolean isNumeric(final Object valueAssertion) {
        return valueAssertion instanceof Integer
                || valueAssertion instanceof Long
                || valueAssertion instanceof Float
                || valueAssertion instanceof Double;
    }

}
