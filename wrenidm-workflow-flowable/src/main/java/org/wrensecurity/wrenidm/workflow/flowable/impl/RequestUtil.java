/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2012-2016 ForgeRock AS.
 * Portions Copyright 2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl;

import static org.forgerock.json.JsonValueFunctions.deepTransformBy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.forgerock.json.JsonValue;
import org.forgerock.json.JsonValueException;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.openidm.util.DateUtil;
import org.forgerock.util.Function;
import org.joda.time.DateTime;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Class containing request-related utility methods.
 */
public class RequestUtil {

    private static final DatePropertyTransformer datePropertyTransformer = new DatePropertyTransformer();

    /**
     * Get map with content of the specified request.
     * Date string values are parsed into {@link Date} instances.
     */
    public static Map<String, Object> getRequestContent(CreateRequest request) {
        if (!request.getContent().isNull()) {
            JsonValue content = request.getContent().as(deepTransformBy(datePropertyTransformer));
            return new HashMap<>(content.asMap());
        } else {
            return new HashMap<>(1);
        }
    }

    /**
     * Get map with process instance / task specific query variables.
     */
    public static Map<String, String> getQueryVariables(QueryRequest request) {
        Map<String, String> result = new HashMap<>();
        for (Entry<String, String> entry : request.getAdditionalParameters().entrySet()) {
            if (entry.getKey().startsWith(WorkflowConstants.VARIABLE_QUERY_PREFIX)) {
                result.put(entry.getKey().substring(WorkflowConstants.VARIABLE_QUERY_PREFIX.length()), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Function for transforming ISO 8601 date string values into corresponding {@link Date} instances.
     */
    private static class DatePropertyTransformer implements Function<JsonValue, JsonValue, JsonValueException> {

        @Override
        public JsonValue apply(JsonValue value) throws JsonValueException {
            if (value == null) {
                return null;
            }
            if (value.isString()) {
                DateTime date = DateUtil.getDateUtil().parseIfDate(value.asString());
                if (date != null) {
                    return new JsonValue(date.toDate(), value.getPointer());
                }
            }
            return value;
        }
    }

}
