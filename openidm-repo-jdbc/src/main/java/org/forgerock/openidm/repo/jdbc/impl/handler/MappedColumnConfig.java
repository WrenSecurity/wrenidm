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
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2024 Wren Security.
 */
package org.forgerock.openidm.repo.jdbc.impl.handler;

import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.InvalidException;

/**
 * Parsed table column mapping configuration.
 *
 * <p>
 * This class corresponds to <code>$.resourceMapping.explicitMapping[*].objectToColumn</code> properties of
 * {@code JDBCRepoService}'s service configuration.
 */
public class MappedColumnConfig {

    /**
     * Property value type.
     */
    public enum ValueType {

        STRING, NUMBER, BOOLEAN, JSON_MAP, JSON_LIST;

    }

    public static final String COLUMN_NAME = "column";
    public static final String VALUE_TYPE = "valueType";
    public static final String JAVA_TYPE = "javaType";

    public final JsonPointer propertyName;
    public final ValueType valueType;
    public final Class<?> javaType;
    public final String columnName;

    public MappedColumnConfig(JsonPointer propertyName, String columnName,
            ValueType valueType, Class<?> javaType) {
        this.propertyName = propertyName;
        this.columnName = columnName;
        this.valueType = valueType;
        this.javaType = javaType;
    }

    /**
     * Determine whether the column holds compound JSON value.
     *
     * @return true if the value is compound JSON value (basically object or array).
     */
    public boolean isJson() {
        return valueType == ValueType.JSON_LIST || valueType == ValueType.JSON_MAP;
    }

    /**
     * Parse column mapping configuration.
     *
     * @param name property name (JSON pointer)
     * @param columnConfig JSON object with column configuration.
     * @return parsed column configuration
     */
    public static MappedColumnConfig parse(String name, JsonValue columnConfig) {
        if (columnConfig.isList()) {
            return parseList(name, columnConfig);
        } else if (columnConfig.isMap()) {
            return parseMap(name, columnConfig);
        } else {
            return new MappedColumnConfig(
                    new JsonPointer(name),
                    columnConfig.required().asString(),
                    ValueType.STRING,
                    String.class);
        }
    }

    /**
     * Parse java value class name (used as a type hint when mapping JDBC types).
     *
     * @param className java class name
     * @return java class that for the stored value
     */
    private static Class<?> parseClass(String className) {
        // intentionally avoid Class#forName
        switch (className) {
        case "java.lang.Integer":
            return Integer.class;
        case "java.lang.Long":
            return Long.class;
        case "java.lang.Double":
            return Double.class;
        case "java.lang.String":
            return String.class;
        default:
            throw new InvalidException("Unsupported java class name " + className);
        }
    }

    /**
     * Parse list based column configuration.
     *
     * Definition:
     *
     * <pre>
     *   "propertyPointer": ["columnName", "valueType"],
     *   "propertyPointer": ["columnName", "valueType", "javaType"]
     * </pre>
     *
     * Example:
     *
     * <pre>
     *   "foo": ["foo", "STRING"],
     *   "bar": ["bar", "NUMBER", "java.lang.Double"]
     * </pre>
     */
    private static MappedColumnConfig parseList(String name, JsonValue columnConfig) {
        int size = columnConfig.asList().size();
        if (size < 2 || size > 3) {
            throw new InvalidException("Explicit table mapping has invalid entry for "
                    + name + ", expecting [column name, value type, java type] but contains "
                    + columnConfig.asList());
        }
        return new MappedColumnConfig(
                new JsonPointer(name),
                columnConfig.get(0).required().asString(),
                ValueType.valueOf(columnConfig.get(1).asString()),
                size > 2 ? parseClass(columnConfig.get(2).asString()) : null);
    }

    /**
     * Parse map based column configuration.
     *
     * Definition:
     *
     * <pre>
     *   "propertyPointer": {
     *     "type": "VALUE_TYPE",
     *   },
     *   "propertyPointer": {
     *     "type": "VALUE_TYPE",
     *     "javaType": "JAVA_CLASS"
     *   },
     * </pre>
     *
     * Example:
     *
     * <pre>
     *   "foo": {
     *     "type": "NUMBER",
     *     "javaType": "java.lang.Double"
     *   }
     * </pre>
     */
    private static MappedColumnConfig parseMap(String name, JsonValue columnConfig) {
        String valueType = columnConfig.get("type").asString(); // short name
        if (columnConfig.isDefined(VALUE_TYPE)) {
            valueType = columnConfig.get(VALUE_TYPE).asString();
        }
        return new MappedColumnConfig(
                new JsonPointer(name),
                columnConfig.get(COLUMN_NAME).required().asString(),
                valueType != null ? ValueType.valueOf(valueType) : ValueType.STRING,
                columnConfig.isDefined(JAVA_TYPE) ? parseClass(columnConfig.get(JAVA_TYPE).asString()) : null);
    }

}
