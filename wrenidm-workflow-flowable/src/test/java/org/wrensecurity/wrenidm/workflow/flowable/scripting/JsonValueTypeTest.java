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
 * Portions Copyright 2021 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable.scripting;

import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntity;
import org.flowable.variable.api.types.ValueFields;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wrensecurity.wrenidm.workflow.flowable.impl.variable.JsonValueType;

public class JsonValueTypeTest {
    @Test
    public void testValues() throws Exception {
        JsonValueType testable = new JsonValueType();
        ValueFields testField = new TestValueFields();
        testable.setValue(null, testField);
        Assert.assertNull(testable.getValue(testField));

        JsonValue expected = new JsonValue("Test", new JsonPointer("flowable"));
        testable.setValue(expected, testField);
        JsonValue actual = (JsonValue) testable.getValue(testField);
        Assert.assertEquals(actual.getObject(), expected.getObject());
        Assert.assertEquals(actual.getPointer().toString(), expected.getPointer().toString());
    }

    class TestValueFields implements ValueFields {
        private String value = null;

        public String getName() {
            return null;
        }

        public String getTextValue() {
            return value;
        }

        public void setTextValue(String textValue) {
            value = textValue;
        }

        public String getTextValue2() {
            return null;
        }

        public void setTextValue2(String textValue2) {
        }

        public Long getLongValue() {
            return null;
        }

        public void setLongValue(Long longValue) {
        }

        public Double getDoubleValue() {
            return null;
        }

        public void setDoubleValue(Double doubleValue) {
        }

        public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
        }

        public Object getCachedValue() {
            return null;
        }

        public void setCachedValue(Object deserializedObject) {
        }

        @Override
        public byte[] getBytes() {
            return null;
        }

        @Override
        public void setBytes(byte[] bytes) {
        }

        @Override
        public String getProcessInstanceId() {
            return null;
        }

        @Override
        public String getExecutionId() {
            return null;
        }

        @Override
        public String getTaskId() {
            return null;
        }

        @Override
        public String getScopeId() {
            return null;
        }

        @Override
        public String getSubScopeId() {
            return null;
        }

        @Override
        public String getScopeType() {
            return null;
        }
    }
}
