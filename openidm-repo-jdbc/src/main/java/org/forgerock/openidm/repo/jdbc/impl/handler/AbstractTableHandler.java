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
package org.forgerock.openidm.repo.jdbc.impl.handler;

import static org.forgerock.openidm.repo.QueryConstants.QUERY_ID;
import static org.forgerock.openidm.repo.QueryConstants.SORT_KEYS;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.forgerock.openidm.repo.jdbc.ErrorType;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.TableHandler;
import org.forgerock.openidm.repo.jdbc.impl.CleanupHelper;
import org.forgerock.openidm.repo.jdbc.impl.DefaultSQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.openidm.repo.jdbc.impl.mapper.ResultMapper;
import org.forgerock.openidm.repo.jdbc.impl.statement.NamedParameterSupport;
import org.forgerock.openidm.repo.jdbc.impl.statement.PreparedSql;
import org.forgerock.openidm.smartevent.EventEntry;
import org.forgerock.openidm.smartevent.Name;
import org.forgerock.openidm.smartevent.Publisher;
import org.forgerock.openidm.util.ResourceUtil;
import org.forgerock.util.query.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common superclass with shared logic for {@link TableHandler} implementations.
 */
public abstract class AbstractTableHandler implements TableHandler {

    /**
     * Monitoring event name prefix.
     */
    private static final String EVENT_RAW_QUERY_PREFIX = "openidm/internal/repo/jdbc/raw/query/";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final SQLExceptionHandler exceptionHandler;

    public AbstractTableHandler(SQLExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler != null ? exceptionHandler : new DefaultSQLExceptionHandler();
    }

    /**
     * Resolve safe SQL replacement tokens (tokens that don't have to be escaped).
     *
     * @return map with replacement tokens that can be safely replaced in SQL
     */
    protected abstract Map<String, String> resolveReplacementTokens();

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

    /**
     * Create prepared statement for the given prepared SQL string with its parameters. This method expects SQL
     * parameters to be properly casted.
     *
     * @param preparedSql prepared SQL string with positional parameters
     * @param connection current database connection
     * @return prepared statement with parameters set
     * @throws InternalServerErrorException in case of parameter type inconsistency
     * @throws SQLException in case of DB failure
     */
    protected final PreparedStatement createPreparedStatement(PreparedSql preparedSql, Connection connection)
            throws BadRequestException, InternalServerErrorException, SQLException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(preparedSql.getSqlString());
            NamedParameterSupport.applyStatementParams(preparedStatement, preparedSql.getParameters());
            return preparedStatement;
        } catch (SQLException ex) {
            CleanupHelper.loggedClose(preparedStatement);
            logger.debug("DB reported failure preparing query: {} "
                    + " with params: {} error code: {} sqlstate: {} message: {} ",
                    preparedSql.getSqlString(), preparedSql.getParameters(),
                    ex.getErrorCode(), ex.getSQLState(), ex.getMessage(), ex);
            throw new InternalServerErrorException("DB reported failure preparing query.");
        }
    }

    /**
     * Render query filter as SQL query.
     *
     * @param queryFilter the query filter to render
     * @param sortKeys sort keys
     * @param sqlParams query parameters to be used as named parameters
     * @return SQL builder instance that is able to produce the final SQL string
     */
    protected abstract SQLBuilder resolveQueryFilter(QueryFilter<JsonPointer> queryFilter,
            List<SortKey> sortKeys, Map<String, Object> sqlParams);

    /**
     * Resolve sort keys for the query filter request making sure there is always a stable iteration
     * order by including sort key for object identifier.
     *
     * @param params query request parameters
     * @return list of sort keys
     */
    @SuppressWarnings("unchecked")
    protected final List<SortKey> resolveSortKeys(Map<String, Object> params) {
        var sortKeys = (List<SortKey>) params.get(SORT_KEYS);
        if (sortKeys == null) {
            sortKeys = Collections.EMPTY_LIST;
        }
        var containsId = sortKeys.stream().anyMatch(sortKey -> {
            return ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(sortKey.getField());
        });
        if (!containsId) {
            sortKeys = Stream.concat(
                        sortKeys.stream(),
                        Stream.of(SortKey.ascendingOrder(Constants.OBJECT_ID)))
                    .collect(Collectors.toList());
        }
        return sortKeys;
    }

    /**
     * Create result set mapper for the given result set meta data.
     *
     * @param metaData result set meta data
     * @return result mapper instance
     * @throws SQLException in case of DB failure
     */
    protected abstract ResultMapper<Map<String, Object>> createResultMapper(ResultSetMetaData metaData)
            throws SQLException;

    /**
     * Extract normalized (lowercase) column names.
     *
     * @param metaData the current result set meta data
     * @return result set column names
     * @throws SQLException in case of SQL error
     */
    protected final Collection<String> extractColumnNames(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        Set<String> columnNames = new TreeSet<String>();
        for (int idx = 1; idx <= columnCount; idx++) {
            columnNames.add(metaData.getColumnName(idx).toLowerCase());
        }
        return columnNames;
    }

    @Override
    public final boolean isErrorType(SQLException exception, ErrorType errorType) {
        return exceptionHandler.isErrorType(exception, errorType);
    }

    @Override
    public final boolean isRetryable(SQLException exception, Connection connection) {
        return exceptionHandler.isRetryable(exception, connection);
    }

}
