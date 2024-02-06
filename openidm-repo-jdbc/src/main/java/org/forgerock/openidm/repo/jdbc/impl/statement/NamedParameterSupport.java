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
package org.forgerock.openidm.repo.jdbc.impl.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.openidm.repo.util.TokenHandler;

/**
 * Convenient utility methods for handling {@link NamedParameterSql} statements.
 */
public class NamedParameterSupport {

    /**
     * Convert request parameter value to the required java type.
     *
     * @param value request parameter value
     * @param type target parameter java type
     * @return converted parameter value
     */
    private static Object convertSqlParam(String value, Class<?> type) {
        if (type == null) {
            return value; // leave the type as is
        } else if (type == Integer.class) {
            return Integer.valueOf(value);
        } else {
            throw new IllegalStateException("Unknown parameter type " + type);
        }
    }

    /**
     * Prepare SQL string by substituting named parameters with a list of a positional parameters. Null parameter
     * values are not supported by this method.
     *
     * <p>
     * If necessary, type conversion is performed (only string values are considered for type conversion).
     * Parameters are collected under their fully qualified token name as they might be used multiple times
     * with different type hints in the query.
     *
     * @param parsedSql parsed SQL string with named parameters
     * @param sqlParams named SQL parameter map
     * @return prepared SQL string with its positional parameters
     * @throws BadRequestException
     */
    public static PreparedSql prepareSqlString(NamedParameterSql parsedSql, Map<String, Object> sqlParams)
            throws BadRequestException {
        String sqlString = parsedSql.getSqlString();

        Map<String, Integer> listSizes = new HashMap<>();

        List<Object> resultParams = new ArrayList<>();
        for (var paramType : parsedSql.getParamTokens()) {
            // get original parameter value
            Object paramValue = sqlParams.get(paramType.getName());

            // do not allow null values
            if (paramValue == null) {
                throw new BadRequestException("Missing entry in params passed to query for token "
                        + paramType.getToken());
            }

            // convert string based values if necessary
            if (paramValue instanceof String) {
                String stringValue = (String) paramValue;
                if (paramType.isList()) {
                    paramValue = List.of(stringValue.split(",")).stream()
                            .map(value -> convertSqlParam(value, paramType.getJavaType()))
                            .collect(Collectors.toList());
                } else {
                    paramValue = convertSqlParam((String) paramValue, paramType.getJavaType());
                }
            }

            if (paramValue instanceof List<?>) {
                listSizes.put(paramType.getToken(), ((List<?>) paramValue).size());
                ((List<?>) paramValue).forEach(resultParams::add);
            } else {
                resultParams.add(paramValue);
            }
        }

        if (!listSizes.isEmpty()) {
            sqlString = new TokenHandler().replaceListTokens(sqlString, listSizes, "?");
        }

        return new PreparedSql(sqlString, resultParams);
    }

    /**
     * Set prepared statement parameters based on the provided value types. Null values are not supported and
     * will cause {@link InternalServerErrorException}
     *
     * @param statement the statement that should be populated with the provided parameters
     * @param parameters the list of parameters to apply
     * @throws InternalServerErrorException in case of invalid parameter type
     * @throws SQLException in case of DB failure
     */
    // TODO This method will need to be updated to support vendor specific SQL mapping (see PreparedSql's TODO)
    public static void applyStatementParams(PreparedStatement statement, List<Object> parameters)
            throws InternalServerErrorException, SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            applyStatementParameter(statement, i + 1, parameters.get(i));
        }
    }

    /**
     * Set prepared statement parameter based on the value type. Null values are not supported and will cause
     * {@link InternalServerErrorException}.
     *
     * @param statement the statement for which the parameter should set
     * @param index the parameter index
     * @param value the parameter value to set
     * @throws InternalServerErrorException in case of invalid parameter type
     * @throws SQLException in case of DB failure
     */
    private static void applyStatementParameter(PreparedStatement statement, int index, Object value)
            throws InternalServerErrorException, SQLException {
        if (value instanceof Integer) {
            statement.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            statement.setLong(index, (Long) value);
        } else if (value instanceof Float) {
            statement.setFloat(index, (Float) value);
        } else if (value instanceof Double) {
            statement.setDouble(index, (Double) value);
        } else if (value instanceof Boolean) {
            statement.setBoolean(index, (Boolean) value);
        } else if (value instanceof String) {
            statement.setString(index, (String) value);
        } else {
            var type = value != null ? value.getClass().getName() : "null";
            throw new InternalServerErrorException("Unsupported parameter type: " +  type);
        }
    }

}
