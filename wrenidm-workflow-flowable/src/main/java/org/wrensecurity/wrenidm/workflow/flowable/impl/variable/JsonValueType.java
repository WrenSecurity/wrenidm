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
 * Copyright 2012-2015 ForgeRock AS.
 * Portions Copyright 2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.variable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.flowable.variable.api.types.ValueFields;
import org.flowable.variable.api.types.VariableType;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;

/**
 * Custom variable type representing {@link JsonValue} instance.
 */
public class JsonValueType implements VariableType {

    private static final String POINTER_ATTR = "pointer";
    private static final String VALUE_ATTR = "value";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getTypeName() {
        return "jsonvalue";
    }

    @Override
    public boolean isCachable() {
        return false;
    }

    @Override
    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }
        return JsonValue.class.isAssignableFrom(value.getClass());
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        if (value == null) {
            valueFields.setTextValue(null);
        } else {
            try {
                Map<String, Object> jsonValue = new HashMap<>(2);
                jsonValue.put(POINTER_ATTR, ((JsonValue) value).getPointer().toString());
                jsonValue.put(VALUE_ATTR, ((JsonValue) value).getObject());
                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, jsonValue);
                valueFields.setTextValue(writer.toString());
            } catch (IOException e) {
                valueFields.setTextValue(null);
            }
        }
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        String serializedValue = valueFields.getTextValue();
        if (serializedValue != null) {
            try {
                Object parsedValue = mapper.readValue(serializedValue, Object.class);
                if (parsedValue instanceof Map) {
                    return new JsonValue(((Map<?, ?>) parsedValue).get(VALUE_ATTR),
                            new JsonPointer((String) ((Map<?, ?>) parsedValue).get(POINTER_ATTR)));
                } else if (parsedValue == null) {
                    return new JsonValue(null);
                }
            } catch (IOException e) { /* NO-OP */ }
        }
        return null;
    }

}
