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
 * Copyright 2011-2016 ForgeRock AS.
 * Portions Copyright 2018-2024 Wren Security.
 */
package org.forgerock.openidm.repo.jdbc.impl.query;

import static org.forgerock.openidm.repo.QueryConstants.PAGED_RESULTS_OFFSET;
import static org.forgerock.openidm.repo.QueryConstants.PAGE_SIZE;
import static org.forgerock.openidm.repo.QueryConstants.QUERY_EXPRESSION;
import static org.forgerock.openidm.repo.QueryConstants.QUERY_FILTER;
import static org.forgerock.openidm.repo.QueryConstants.QUERY_ID;
import static org.forgerock.openidm.repo.QueryConstants.RESOURCE_NAME;
import static org.forgerock.openidm.repo.QueryConstants.SORT_KEYS;
import static org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterSupport.applyStatementParams;
import static org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterSupport.prepareSqlString;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterSql;
import org.forgerock.openidm.repo.jdbc.impl.statement.PreparedSql;
import org.forgerock.openidm.smartevent.EventEntry;
import org.forgerock.openidm.smartevent.Name;
import org.forgerock.openidm.smartevent.Publisher;
import org.forgerock.util.query.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table query and command handling logic.
 *
 * <p>
 * This class simply serves as a place to offload query and command handling logic from {@link TableHandler}s
 * to keep them more simple and to share logic between mapped and generic implementations.
 *
 * @param <T> result object type
 */
public class TableQueryHandler<T> {

    /**
     * Monitoring event name prefix.
     */
    private static final String EVENT_RAW_QUERY_PREFIX = "openidm/internal/repo/jdbc/raw/query/";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> replacementTokens;

    private final Map<String, NamedParameterSql> queryConfig;

    private final Map<String, NamedParameterSql> commandConfig;

    private final QueryFilterResolver filterResolver;

    private final ResultMapperFactory<T> mapperFactory;

    public TableQueryHandler(
            Map<String, String> replacementTokens,
            Map<String, NamedParameterSql> queryConfig,
            Map<String, NamedParameterSql> commandConfig,
            QueryFilterResolver filterResolver,
            ResultMapperFactory<T> mapperFactory) {
        this.replacementTokens = replacementTokens;
        this.queryConfig = queryConfig;
        this.commandConfig = commandConfig;
        this.filterResolver = filterResolver;
        this.mapperFactory = mapperFactory;
    }

    /**
     * @see TableHandler#queryCount(String, Map, Connection)
     */
    public List<T> query(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        // create named parameters map that will be used to populate prepared statement
        Map<String, Object> sqlParams = new HashMap<>(params);
        sqlParams.put(RESOURCE_NAME, type);

        // determine paging parameters
        int pageSize = (Integer) params.get(PAGE_SIZE);
        if (pageSize <= 0) {
            sqlParams.put(PAGE_SIZE, Integer.MAX_VALUE);
            sqlParams.put(PAGED_RESULTS_OFFSET, 0);
        } else {
            sqlParams.put(PAGE_SIZE, pageSize);
            sqlParams.put(PAGED_RESULTS_OFFSET, params.get(PAGED_RESULTS_OFFSET));
        }

        // resolve query SQL
        NamedParameterSql querySql;
        if (params.get(QUERY_FILTER) != null) {
            @SuppressWarnings("unchecked")
            QueryFilter<JsonPointer> queryFilter = (QueryFilter<JsonPointer>) params.get(QUERY_FILTER);
            querySql = NamedParameterSql.parse(
                    filterResolver.resolveQueryFilter(queryFilter, resolveSortKeys(params), sqlParams).toSQL(),
                    replacementTokens);
        } else if (params.get(QUERY_ID) != null) {
            String queryId = (String) params.get(QUERY_ID);
            querySql = queryConfig.get(queryId);
            if (querySql == null) {
                throw new BadRequestException("The passed query identifier " + queryId
                        + " does not match any configured queries on the JDBC repository service.");
            }
        } else if (params.get(QUERY_EXPRESSION) != null) {
            querySql = NamedParameterSql.parse((String) params.get(QUERY_EXPRESSION));
        } else {
            throw new BadRequestException("Either " + QUERY_ID + ", " + QUERY_EXPRESSION + ", or "
                    + QUERY_FILTER + " to identify/define a query must be passed in the parameters. "
                    + params);
        }

        PreparedSql preparedSql = prepareSqlString(querySql, sqlParams);

        List<T> result = new ArrayList<>();
        EventEntry measure = startQueryMeasure(params, querySql.getSqlString());
        try (var queryStatement = connection.prepareStatement(preparedSql.getSqlString())) {
            applyStatementParams(queryStatement, preparedSql.getParameters());
            try (var resultSet = queryStatement.executeQuery()) {
                var resultMapper = mapperFactory.createResultMapper(resultSet.getMetaData());
                while (resultSet.next()) {
                    result.add(resultMapper.map(resultSet));
                }
            }
            measure.setResult(result);
        } catch (IOException ex) {
            throw new InternalServerErrorException("Failed to convert result objects for query "
                    + querySql.getSqlString() + " with params: " + params + " message: "
                    + ex.getMessage(), ex);
        } finally {
            measure.end();
        }

        return result;
    }

    /**
     * Resolve sort keys for the query filter request making sure there is always a stable iteration
     * order by including sort key for object identifier.
     *
     * @param params query request parameters
     * @return list of sort keys
     */
    @SuppressWarnings("unchecked")
    private List<SortKey> resolveSortKeys(Map<String, Object> params) {
        var sortKeys = (List<SortKey>) params.get(SORT_KEYS);
        if (sortKeys == null) {
            sortKeys = Collections.EMPTY_LIST;
        }
        var containsId = sortKeys.stream().anyMatch(sortKey -> {
            return Constants.OBJECT_ID.equals(sortKey.getField().toString());
        });
        if (!containsId) {
            sortKeys = Stream.concat(sortKeys.stream(), Stream.of(SortKey.ascendingOrder(Constants.OBJECT_ID)))
                    .collect(Collectors.toList());
        }
        return sortKeys;
    }

    /**
     * @see TableHandler#queryCount(String, Map, Connection)
     */
    public Integer queryCount(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        // create named parameters map that will be used to populate prepared statement
        Map<String, Object> sqlParams = new HashMap<>(params);
        sqlParams.put(RESOURCE_NAME, type);

        // resolve query SQL
        NamedParameterSql countSql = null;
        if (params.get(QUERY_ID) != null) {
            countSql = queryConfig.get(params.get(QUERY_ID) + "-count");
            if (countSql == null) {
                return null; // no count query defined
            }
        } else if (params.get(QUERY_FILTER) != null) {
            @SuppressWarnings("unchecked")
            QueryFilter<JsonPointer> queryFilter = (QueryFilter<JsonPointer>) params.get(QUERY_FILTER);
            countSql = NamedParameterSql.parse(
                    filterResolver.resolveQueryFilter(queryFilter, null, sqlParams).toCountSQL(),
                    replacementTokens);
        }

        if (countSql == null) {
            return null; // no count query defined
        }

        PreparedSql preparedSql = prepareSqlString(countSql, sqlParams);

        EventEntry measure = startQueryMeasure(Map.of(QUERY_ID, "queryCount"), countSql.getSqlString());
        try (var countStatement = connection.prepareStatement(preparedSql.getSqlString())) {
            applyStatementParams(countStatement, preparedSql.getParameters());
            try (var resultSet = countStatement.executeQuery()) {
                while (!resultSet.next()) {
                    return null; // result should not be empty
                }
                var count = resultSet.getInt(1); // expecting only single column
                measure.setResult(count);
                return count;
            }
        } catch (IOException ex) {
            throw new InternalServerErrorException("Failed to convert result objects for query "
                    + countSql.getSqlString() + " with params: " + params + " message: "
                    + ex.getMessage(), ex);
        } finally {
            measure.end();
        }
    }

    /**
     * @see TableHandler#command(String, Map, Connection)
     */
    public Integer command(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        // create named parameters map that will be used to populate prepared statement
        Map<String, Object> sqlParams = new HashMap<>(params);
        sqlParams.put(RESOURCE_NAME, type);

        // resolve command SQL
        NamedParameterSql commandSql = null;
        if (params.get("commandId") != null) {
            commandSql = commandConfig.get(params.get("commandId"));
        } else if (params.get("commandExpression") != null) {
            commandSql = NamedParameterSql.parse((String) params.get("commandExpression"));
        } else {
            throw new BadRequestException("Either commandId or commandExpression "
                    + " to identify/define a query must be passed in the parameters. " + params);
        }

        if (commandSql == null) {
            throw new BadRequestException("The passed command identifier " + params.get("commandId")
                    + " does not match any configured commands on the JDBC repository service.");
        }

        PreparedSql preparedSql = prepareSqlString(commandSql, sqlParams);

        EventEntry measure = startQueryMeasure(params, commandSql.getSqlString());
        try (var commandStatement = connection.prepareStatement(preparedSql.getSqlString())) {
            applyStatementParams(commandStatement, preparedSql.getParameters());
            int result = commandStatement.executeUpdate();
            measure.setResult(result);
            return result;
        } catch (SQLException ex) {
            logger.debug("DB reported failure preparing command: {} with params: {} error code: {} sqlstate: {} " +
                    "message: {}", commandSql.getSqlString(), params, ex.getErrorCode(), ex.getSQLState(), ex.getMessage(), ex);
            throw new InternalServerErrorException("DB reported failure preparing command.");
        } finally {
            measure.end();
        }
    }

    /**
     * Start smart event measure for a query defined by the given params and parsed SQL.
     *
     * @param params query parameters
     * @param querySql parsed query SQL
     * @return smart event measure
     */
    protected final EventEntry startQueryMeasure(Map<String, Object> params, String querySql) {
        String queryId = (String) params.get(QUERY_ID);
        return Publisher.start(Name.get(queryId != null
                ? EVENT_RAW_QUERY_PREFIX + queryId
                : EVENT_RAW_QUERY_PREFIX + "_query_expression"), querySql, null);
    }

}
