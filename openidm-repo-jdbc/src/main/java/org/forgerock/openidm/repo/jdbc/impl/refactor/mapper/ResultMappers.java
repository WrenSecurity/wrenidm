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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.Map;

/**
 * This class provides convenience factory methods to instantiate basic well-defined result mappers.
 */
public class ResultMappers {

    /**
     * Column holding full object state in JSON format.
     */
    public static final String OBJECT_COLUMN = "fullobject";

    /**
     * Column holding the total number of matched rows.
     */
    public static final String TOTAL_COLUMN = "total";

    /**
     * Type reference for generic map-based JSON object.
     */
    private static final TypeReference<Map<String, Object>> OBJECT_TYPE_REF = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    /**
     * Create new results mapper factory instance.
     *
     * @param objectMapper object mapper to use when mapping the object state
     */
    public ResultMappers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Create mapper for full object state in <code>fullobject</code> column.
     *
     * @return full object state mapper
     */
    public ResultMapper<Map<String, Object>> forFullObject() {
        return rs -> objectMapper.readValue(rs.getString(OBJECT_COLUMN), OBJECT_TYPE_REF);
    }

    /**
     * Create simple mapper for <code>total</code> count.
     *
     * @return <i>count query</i> result mapper
     */
    public ResultMapper<Map<String, Object>> forTotalCount() {
        return rs -> Map.of(TOTAL_COLUMN, rs.getInt(TOTAL_COLUMN));
    }

    /**
     * Create mapper for simple object reference based on <code>objectid</code> and <code>rev</code> columns.
     *
     * @param revision whether the result set contains object revision
     * @return <i>object reference</i> result mapper
     */
    public ResultMapper<Map<String, Object>> forObjectRef(boolean revision) throws SQLException {
        return revision
                ? rs -> Map.of("_id", rs.getString("objectid"), "_rev", rs.getString("rev"))
                : rs -> Map.of("_id", rs.getString("objectid"));
    }

}
