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
package org.forgerock.openidm.repo.jdbc.impl.refactor.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.openidm.repo.jdbc.impl.refactor.handler.MappedColumnConfig;
import org.forgerock.openidm.util.JsonUtil;

/**
 * Mapped table result handler that maps columns based on provided column mapping configuration.
 */
public class MappedResultMapper implements ResultMapper<Map<String, Object>> {

    private final ObjectMapper objectMapper;

    private final Collection<MappedColumnConfig> columnConfigs;

    /**
     * Create new result mapper using the given object mapper and column mapping that will be used for
     * result set extraction.
     *
     * <p>
     * Column mapping configuration should be contain only columns that are actually present in the
     * processed result set.
     *
     * @param objectMapper the object mapper instance
     * @param columnConfigs the column mapping to use to extract data from the result set
     */
    public MappedResultMapper(ObjectMapper objectMapper, Collection<MappedColumnConfig> columnConfigs) {
        this.objectMapper = objectMapper;
        this.columnConfigs = columnConfigs;
    }

    @Override
    public Map<String, Object> map(ResultSet resultSet) throws SQLException, IOException {
        JsonValue result = new JsonValue(new LinkedHashMap<String, Object>());
        for (MappedColumnConfig config : columnConfigs) {
            Object value = null;
            switch (config.valueType) {
            case STRING:
                value = resultSet.getString(config.columnName);
                if (JsonUtil.isEncrypted((String) value)) {
                    value = convertToJson(config.columnName, "encrypted", (String) value, Map.class).asMap();
                }
                break;
            case NUMBER:
                // convert to narrow down data type to Integer/Long/Double (Float is not used by Jackson)
                value = resultSet.getString(config.columnName);
                if (value != null) {
                    value = convertToJson(config.columnName, config.valueType.name(), (String) value, Number.class)
                            .getObject();
                }
                break;
            case BOOLEAN:
                value = resultSet.getObject(config.columnName);
                if (value instanceof Number) {
                    value = ((Number) value).intValue() == 1;
                } else if (value instanceof Boolean) {
                    value = ((Boolean) value).booleanValue();
                } else if (value != null) {
                    throw new InternalServerErrorException("Unsupported boolean value class " + value.getClass().getName());
                }
                break;
            case JSON_LIST:
                value = convertToJson(config.columnName, config.valueType.name(),
                        resultSet.getString(config.columnName), List.class).asList();
                break;
            case JSON_MAP:
                value = convertToJson(config.columnName, config.valueType.name(),
                        resultSet.getString(config.columnName), Map.class).asMap();
                break;
            default:
                throw new InternalServerErrorException("Unsupported DB column type " + config.valueType);
            }
            result.putPermissive(config.propertyName, value);
        }
        return result.asMap();
    }

    private <T> JsonValue convertToJson(String name, String nameType, String value, Class<T> valueType)
            throws InternalServerErrorException {
        if (value != null) {
            try {
                return new JsonValue(objectMapper.readValue(value, valueType));
            } catch (IOException e) {
                throw new InternalServerErrorException("Unable to map " + nameType + " value for " + name, e);
            }
        }
        return new JsonValue(null);
    }

}
