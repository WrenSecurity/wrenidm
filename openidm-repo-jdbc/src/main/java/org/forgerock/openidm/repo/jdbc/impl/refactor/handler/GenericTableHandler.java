/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 * Portions Copyright 2018 Wren Security.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2016 ForgeRock AS.
 * Portions Copyright 2024 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.refactor.handler;

import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openidm.repo.util.Clauses.where;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.PreconditionFailedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.SortKey;
import org.forgerock.openidm.repo.jdbc.Constants;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.SQLBuilder;
import org.forgerock.openidm.repo.jdbc.impl.refactor.mapper.ColumnResultMapper;
import org.forgerock.openidm.repo.jdbc.impl.refactor.mapper.ResultMapper;
import org.forgerock.openidm.repo.jdbc.impl.refactor.mapper.ResultMappers;
import org.forgerock.openidm.repo.jdbc.impl.refactor.query.GenericSQLQueryFilterVisitor;
import org.forgerock.openidm.repo.jdbc.impl.refactor.query.TableQueryHandler;
import org.forgerock.openidm.repo.jdbc.impl.refactor.statement.NamedParameterCollector;
import org.forgerock.openidm.repo.jdbc.impl.refactor.statement.NamedParameterSql;
import org.forgerock.openidm.repo.util.TokenHandler;
import org.forgerock.openidm.util.ResourceUtil;
import org.forgerock.util.query.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic table handler that supports objects stored as JSON string with a separate properies table used for
 * indexing and querying objects by property value filters.
 *
 * <p>
 * The additional properties table is not necessary for databases that support query conditions based on JSON
 * fields (e.g. PostgreSQL). Such databases have their own generic table handler implementation.
 */
public class GenericTableHandler extends AbstractTableHandler {

    /**
     * Well-known implicit SQL statement types.
     */
    protected enum ImplicitSqlType {
        READTYPE,
        CREATETYPE,
        READ,
        READFORUPDATE,
        CREATE,
        UPDATE,
        DELETE,
        PROPCREATE,
        PROPDELETE,
        QUERYALLIDS
    }

    /**
     * Maximum length of searchable properties.
     *
     * <p>
     * This is used to trim values due to database index size limitations.
     */
    protected static final int DEFAULT_SEARCHABLE_LENGTH = 2000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String schemaName;

    private final GenericTableConfig tableConfig;

    protected final Map<ImplicitSqlType, String> implicitSql;

    protected final TableQueryHandler<Map<String, Object>> queryHandler;

    private final int batchSize;

    private final GenericTypeResolver typeResolver;

    private final ResultMappers resultMappers;

    public GenericTableHandler(
            String schemaName,
            JsonValue tableConfig,
            Map<String, String> queryConfig,
            Map<String, String> commandConfig,
            int batchSize,
            SQLExceptionHandler exceptionHandler) {
        super(exceptionHandler);

        this.schemaName = schemaName;
        this.tableConfig = GenericTableConfig.parse(tableConfig);

        var replacementTokens = resolveReplacementTokens();

        this.implicitSql = initializeImplicitSql().entrySet().stream()
                .map(entry -> {
                    String resolved = new TokenHandler().replaceSomeTokens(entry.getValue(), replacementTokens);
                    return Map.entry(entry.getKey(), resolved);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.queryHandler = new TableQueryHandler<>(
                replacementTokens,
                queryConfig.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            return NamedParameterSql.parse(entry.getValue(), replacementTokens);
                        })),
                commandConfig.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            return NamedParameterSql.parse(entry.getValue(), replacementTokens);
                        })),
                this::resolveQueryFilter,
                this::createResultMapper);

        this.batchSize = batchSize > 1 ? batchSize : 0;

        this.typeResolver = new GenericTypeResolver(
                implicitSql.get(ImplicitSqlType.READTYPE),
                implicitSql.get(ImplicitSqlType.CREATETYPE));

        this.resultMappers = new ResultMappers(objectMapper);
    }

    @Override
    protected Map<String, String> resolveReplacementTokens() {
        return Map.ofEntries(
            Map.entry("_dbSchema", schemaName),
            Map.entry("_mainTable", this.tableConfig.mainTableName),
            Map.entry("_propTable", this.tableConfig.propTableName)
        );
    }

    /**
     * Initialize implicit SQL statements for the table handler.
     *
     * <p>
     * This method serves as extension point for vendor specific subclasses.
     *
     * @return mutable map with implicit SQL statements
     * @see ImplicitSqlType
     */
    protected Map<ImplicitSqlType, String> initializeImplicitSql() {
        Map<ImplicitSqlType, String> result = new EnumMap<>(ImplicitSqlType.class);

        // object types table
        result.put(ImplicitSqlType.CREATETYPE,
                "INSERT INTO ${_dbSchema}.objecttypes (objecttype) VALUES (?)");
        result.put(ImplicitSqlType.READTYPE,
                "SELECT id FROM ${_dbSchema}.objecttypes objtype "
                + "WHERE objtype.objecttype = ?");

        // main object table
        result.put(ImplicitSqlType.READ,
                "SELECT obj.rev, obj.fullobject "
                + "FROM ${_dbSchema}.objecttypes objtype, ${_dbSchema}.${_mainTable} obj "
                + "WHERE obj.objecttypes_id = objtype.id AND objtype.objecttype = ? AND obj.objectid  = ?");
        result.put(ImplicitSqlType.READFORUPDATE,
                "SELECT obj.* "
                + "FROM ${_dbSchema}.${_mainTable} obj "
                + "WHERE "
                    + "obj.objecttypes_id = ("
                        + "SELECT id FROM ${_dbSchema}.objecttypes objtype "
                        + "WHERE objtype.objecttype = ?"
                    + ") AND "
                    + "obj.objectid = ? "
                + "FOR UPDATE");
        result.put(ImplicitSqlType.CREATE,
                "INSERT INTO ${_dbSchema}.${_mainTable} ("
                    + "objecttypes_id, objectid, rev, fullobject"
                + ") VALUES ("
                    + "?, ?, ?, ?"
                + ")");
        result.put(ImplicitSqlType.UPDATE,
                "UPDATE ${_dbSchema}.${_mainTable} "
                + "SET "
                    + "objectid = ?, "
                    + "rev = ?, "
                    + "fullobject = ? "
                + "WHERE id = ?");
        result.put(ImplicitSqlType.DELETE,
                "DELETE FROM ${_dbSchema}.${_mainTable} "
                + "WHERE "
                    + "EXISTS ("
                        + "SELECT 1 FROM ${_dbSchema}.objecttypes objtype "
                        + "WHERE "
                            + "objtype.id = ${_mainTable}.objecttypes_id AND "
                            + "objtype.objecttype = ?"
                    + ") AND "
                    + "objectid = ? AND "
                    + "rev = ?");

        // indexed properties table
        result.put(ImplicitSqlType.PROPCREATE,
                "INSERT INTO ${_dbSchema}.${_propTable} ("
                    + "${_mainTable}_id, propkey, proptype, propvalue"
                + ") VALUES ("
                    + "?, ?, ?, ?"
                + ")");
        result.put(ImplicitSqlType.PROPDELETE,
                "DELETE FROM ${_dbSchema}.${_propTable} WHERE ${_mainTable}_id = ?");

        // default object queries
        result.put(ImplicitSqlType.QUERYALLIDS, "SELECT obj.objectid FROM ${_dbSchema}.${_mainTable} obj "
                + "INNER JOIN ${_dbSchema}.objecttypes objtype ON "
                    + "obj.objecttypes_id = objtype.id "
                + "WHERE objtype.objecttype = ${_resource}");

        return result;
    }

    /**
     * Resolve implicit SQL statement.
     *
     * @param type statement type
     * @param keys whether to return generated keys
     * @param connection current database connection
     * @return resolved prepared statement
     * @throws SQLException in case of DB failure
     */
    protected PreparedStatement resolveImplicitStatement(ImplicitSqlType type, boolean keys, Connection connection)
            throws SQLException {
        return keys
                ? connection.prepareStatement(implicitSql.get(type), Statement.RETURN_GENERATED_KEYS)
                : connection.prepareStatement(implicitSql.get(type));
    }

    @Override
    public ResourceResponse read(String fullId, String type, String localId, Connection connection)
            throws NotFoundException, IOException, SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        try (var readStatement = resolveImplicitStatement(ImplicitSqlType.READ, false, connection)) {
            logger.trace("Populating prepared statement {} for {}", readStatement, fullId);
            readStatement.setString(1, type);
            readStatement.setString(2, localId);

            logger.debug("Executing: {}", readStatement);
            try (var resultSet = readStatement.executeQuery()) {
                var resultMapper = resultMappers.forFullObject();
                while (resultSet.next()) {
                    results.add(resultMapper.map(resultSet));
                }
            }
        }

        if (results.isEmpty()) {
            throw new NotFoundException("Object " + fullId + " not found in " + type);
        }
        var result = results.get(0);
        String revision = (String) result.get(Constants.OBJECT_REV);
        logger.debug(" full id: {}, rev: {}, obj {}", fullId, revision, result);

        return newResourceResponse(localId, revision, new JsonValue(result));
    }

    @Override
    public void create(String fullId, String type, String localId, Map<String, Object> obj, Connection connection)
            throws PreconditionFailedException, InternalServerErrorException, IOException, SQLException {
        logger.debug("Create with fullid {}", fullId);

        long typeId = typeResolver.resolveTypeId(type, connection);
        String revision = "0";

        // update object properties
        obj.put(Constants.OBJECT_ID, localId);
        obj.put(Constants.OBJECT_REV, revision);

        // serialize full object state
        String fullObject = objectMapper.writeValueAsString(obj);

        try (var createStatement = resolveImplicitStatement(ImplicitSqlType.CREATE, true, connection)) {
            logger.trace("Populating statement {} with params {}, {}, {}, {}",
                    createStatement, typeId, localId, revision, fullObject);
            createStatement.setLong(1, typeId);
            createStatement.setString(2, localId);
            createStatement.setString(3, revision);
            createStatement.setString(4, fullObject);

            logger.debug("Executing: {}", createStatement);
            createStatement.executeUpdate();

            long databaseId;
            try (var generatedKeys = createStatement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new InternalServerErrorException("Object creation for " + fullId
                            + " failed to retrieve an assigned ID from the DB.");
                }
                databaseId = generatedKeys.getLong(1);
            }

            logger.debug("Created object for id {} with rev {}", fullId, revision);
            writeValueProperties(fullId, databaseId, new JsonValue(obj), connection);
        }
    }

    /**
     * Write properties of a given resource to the properties table and links them to the main table record.
     *
     * @param fullId the qualified identifier of the owner object
     * @param databaseId the generated identifier to link the properties table with the main table (foreign key)
     * @param value the JSON value with the properties to write
     * @param connection the DB connection
     * @throws SQLException if the insert failed
     */
    protected void writeValueProperties(String fullId, long databaseId, JsonValue value, Connection connection)
            throws SQLException {
        if (!tableConfig.containsSearchable) {
            return; // no searchable properties, no need to index
        }

        Map<JsonPointer, Object> pairs = new LinkedHashMap<JsonPointer, Object>();
        extractValueProperties(value, pairs::put);

        try (var createStatement = resolveImplicitStatement(ImplicitSqlType.PROPCREATE, false, connection)) {
            int batchingCount = 0;

            for (var pair : pairs.entrySet()) {
                // prepare index properties
                var object = pair.getValue();
                var idxkey = pair.getKey().toString();
                var idxtype = object != null ? object.getClass().getName() : null;
                var idxvalue = object != null ? StringUtils.left(object.toString(), getSearchableLength()) : null;

                // set statement parameters
                if (logger.isTraceEnabled()) {
                    logger.trace("Populating statement {} with params {}, {}, {}, {}",
                            createStatement, databaseId, idxkey, idxtype, idxvalue);
                }
                createStatement.setLong(1, databaseId);
                createStatement.setString(2, idxkey);
                createStatement.setString(3, idxtype);
                createStatement.setString(4, idxvalue);

                // handle statement execution
                if (batchSize > 0) {
                    createStatement.addBatch();
                    if (++batchingCount >= batchSize) {
                        int[] updates = createStatement.executeBatch();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Batch limit reached, update of objectproperties updated: {}",
                                    Arrays.asList(updates));
                        }
                        createStatement.clearBatch();
                        batchingCount = 0;
                    }
                } else {
                    createStatement.executeUpdate();
                }
            }

            if (batchingCount > 0) {
                int[] updates = createStatement.executeBatch();
                if (logger.isDebugEnabled()) {
                    logger.debug("Writing batch of objectproperties, updated: {}", Arrays.asList(updates));
                }
            }
        }
    }

    /**
     * Recursive function to extract searchable property values that should be indexed.
     *
     * @param value JSON value (array or object)
     * @param collector callback for collecting extracted property values
     */
    private void extractValueProperties(JsonValue json, BiConsumer<JsonPointer, Object> collector) {
        for (JsonValue entry : json) {
            JsonPointer pointer = entry.getPointer();
            if (!tableConfig.isSearchable(pointer)) {
                continue;
            }
            if (entry.isMap() || entry.isList()) {
                extractValueProperties(entry, collector);
                continue;
            }
            collector.accept(pointer, entry.getObject());
        }
    }

    /**
     * Remove properties of a resource stored under the specified database identifier from the properties table.
     *
     * @param databaseId the identifier that link the properties table with the main table (foreign key)
     * @param fullId the qualified identifier of the owner object
     * @param connection the DB connection
     * @throws SQLException if the insert failed
     */
    protected void clearValueProperties(String fullId, long databaseId, Connection connection) throws SQLException {
        try (var deleteStatement = resolveImplicitStatement(ImplicitSqlType.PROPDELETE, false, connection)) {
            logger.trace("Populating prepared statement {} with {}", deleteStatement, databaseId);
            deleteStatement.setLong(1, databaseId);
            int deleteCount = deleteStatement.executeUpdate();
            logger.trace("Deleted child rows: {} for: {}", deleteCount, fullId);
        }
    }

    /**
     * Read an object with <i>FOR UPDATE</i> lock applied.
     *
     * @param fullId qualified id of component type and id
     * @param type the qualifier of the object to retrieve
     * @param localId the identifier without the qualifier of the object to retrieve
     * @param connection database connection to use
     * @return the row as a map of column name/value pairs for the requested object
     * @throws NotFoundException if the requested object was not found in the DB
     * @throws java.sql.SQLException for general DB issues
     */
    protected Map<String, Object> readForUpdate(String fullId, String type, String localId, Connection connection)
            throws NotFoundException, SQLException {
        try (var readStatement = resolveImplicitStatement(ImplicitSqlType.READFORUPDATE, false, connection)) {
            logger.trace("Populating prepared statement {} for {}", readStatement, fullId);
            readStatement.setString(1, type);
            readStatement.setString(2, localId);

            logger.debug("Executing: {}", readStatement);
            try (var resultSet = readStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NotFoundException("Object " + fullId + " not found in " + type);
                }
                return new ColumnResultMapper(resultSet.getMetaData()).map(resultSet);
            }
        }
    }

    @Override
    public void update(String fullId, String type, String localId, String rev, Map<String, Object> obj,
            Connection connection) throws NotFoundException, PreconditionFailedException, BadRequestException,
            InternalServerErrorException, IOException, SQLException {
        logger.debug("Update with fullid {}", fullId);

        // read existing object state
        JsonValue existingObj = new JsonValue(readForUpdate(fullId, type, localId, connection));
        String existingRev = existingObj.get(Constants.RAW_OBJECT_REV).asString();
        long databaseId = existingObj.get(Constants.RAW_ID).asLong();
        long typeId = existingObj.get("objecttypes_id").asLong();
        logger.debug("Update existing object {} rev: {} db id: {}, object type db id: {}", fullId, existingRev,
                databaseId, typeId);

        // perform optimistic version locking
        if (!existingRev.equals(rev)) {
            throw new PreconditionFailedException("Update rejected as current Object revision " + existingRev
                    + " is different than expected by caller (" + rev + "), the object has changed since retrieval.");
        }

        // support changing object identifier
        String updatedId = (String) obj.get(Constants.OBJECT_ID);
        if (updatedId != null && !updatedId.equals(localId)) {
            logger.debug("Object identifier is changing from " + localId + " to " + updatedId);
        } else {
            updatedId = localId; // if it hasn't changed, use the existing ID
            obj.put(Constants.OBJECT_ID, updatedId); // ensure the ID is saved in the object
        }

        // handle revision update
        String updatedRev = Integer.toString(Integer.parseInt(rev) + 1);
        obj.put(Constants.OBJECT_REV, updatedRev);

        // serialize full object state
        String fullObject = objectMapper.writeValueAsString(obj);

        try (var updateStatement = resolveImplicitStatement(ImplicitSqlType.UPDATE, false, connection)) {
            logger.trace("Populating prepared statement {} with {} {} {} {} {}", updateStatement, fullId, updatedId,
                    updatedRev, fullObject, databaseId);
            updateStatement.setString(1, updatedId);
            updateStatement.setString(2, updatedRev);
            updateStatement.setString(3, fullObject);
            updateStatement.setLong(4, databaseId);
            logger.debug("Update statement: {}", updateStatement);

            int updateCount = updateStatement.executeUpdate();
            logger.trace("Updated rows: {} for {}", updateCount, fullId);
            if (updateCount != 1) {
                throw new InternalServerErrorException("Update execution did not result in updating 1 "
                        + "row as expected. Updated rows: " + updateCount);
            }
        }

        clearValueProperties(fullId, databaseId, connection);
        writeValueProperties(fullId, databaseId, new JsonValue(obj), connection);
    }

    @Override
    public void delete(String fullId, String type, String localId, String rev, Connection connection)
            throws SQLException, ResourceException {
        logger.debug("Delete with fullid {}", fullId);

        // read existing object state
        JsonValue existingObj = new JsonValue(readForUpdate(fullId, type, localId, connection));
        String existingRev = existingObj.get(Constants.RAW_OBJECT_REV).asString();

        // perform optimistic version locking
        if (!"*".equals(rev) && !existingRev.equals(rev)) {
            throw new PreconditionFailedException("Delete rejected as current Object revision " + existingRev
                    + " is different than the expected by caller " + rev + ", the object has changed since retrieval.");
        }

        // rely on ON DELETE CASCADE for connected object properties to be deleted
        try (var deleteStatement = resolveImplicitStatement(ImplicitSqlType.DELETE, false, connection)) {
            logger.trace("Populating prepared statement {} for {} {} {} {}", deleteStatement, fullId, type, localId, rev);
            deleteStatement.setString(1, type);
            deleteStatement.setString(2, localId);
            deleteStatement.setString(3, rev);
            logger.debug("Delete statement: {}", deleteStatement);

            int deletedRows = deleteStatement.executeUpdate();
            logger.trace("Deleted {} rows for id : {} {}", deletedRows, localId);
            if (deletedRows < 1) {
                throw new InternalServerErrorException("Deleting object for " + fullId + " failed, DB reported " +
                        deletedRows + " rows deleted");
            } else {
                logger.debug("Delete for id succeeded: {} revision: {}", localId, rev);
            }
        }
    }

    @Override
    public List<Map<String, Object>> query(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        return queryHandler.query(type, params, connection);
    }

    @Override
    protected ResultMapper<Map<String, Object>> createResultMapper(ResultSetMetaData metaData) throws SQLException {
        Collection<String> columnNames = extractColumnNames(metaData);
        if (columnNames.contains(ResultMappers.OBJECT_COLUMN)) {
            return resultMappers.forFullObject();
        } else if (columnNames.contains(ResultMappers.TOTAL_COLUMN)) {
            return resultMappers.forTotalCount();
        } else {
            return resultMappers.forObjectRef(columnNames.contains(Constants.RAW_OBJECT_REV));
        }
    }

    @Override
    protected SQLBuilder resolveQueryFilter(QueryFilter<JsonPointer> queryFilter, List<SortKey> sortKeys,
            Map<String, Object> sqlParams) {
        var builder = createSqlBuilder();

        var collector = new NamedParameterCollector(sqlParams);

        var visitor = createFilterVisitor(builder);
        builder.addColumn("obj.*")
                .from("${_dbSchema}.${_mainTable}", "obj")
                .join("${_dbSchema}.objecttypes", "objecttypes")
                    .on(where("obj.objecttypes_id = objecttypes.id")
                            .and("objecttypes.objecttype = ${_resource}"))
                .where(queryFilter.accept(visitor, collector));

        if (sortKeys != null) {
            for (var sortKey : sortKeys) {
                if (ResourceUtil.RESOURCE_FIELD_CONTENT_ID_POINTER.equals(sortKey.getField())) {
                    builder.orderBy("objectid", sortKey.isAscendingOrder());
                } else {
                    var tokenName = collector.register("s", sortKey.getField().toString());
                    var joinAlias = collector.generate("o");
                    builder.join("${_dbSchema}.${_propTable}", joinAlias)
                            .on(where(joinAlias + ".${_mainTable}_id = obj.id")
                                    .and(joinAlias + ".propkey = ${" + tokenName + "}"))
                            .orderBy(joinAlias + ".propvalue", sortKey.isAscendingOrder());
                }
            }
        }

        return builder;
    }

    /**
     * Create new {@link SQLBuilder} to render query filter queries.
     *
     * @return new SQLBuilder instance
     */
    protected SQLBuilder createSqlBuilder() {
        return new SQLBuilder() {
            @Override
            public String toSQL() {
                return "SELECT " + getColumns().toSQL()
                        + getFromClause().toSQL()
                        + getJoinClause().toSQL()
                        + getWhereClause().toSQL()
                        + getOrderByClause().toSQL()
                        + " LIMIT ${int:_pageSize} "
                        + " OFFSET ${int:_pagedResultsOffset}";
            }
        };
    }

    /**
     * Get the maximum allowed length of searchable field value. This depends on maximum allowed length
     * of indexable value in the database engine.
     *
     * @return the maximum length of searchable (indexable) value
     */
    protected int getSearchableLength() {
        return DEFAULT_SEARCHABLE_LENGTH;
    }

    /**
     * Create new {@link GenericSQLQueryFilterVisitor} to render query filter queries.
     *
     * @param builder SQL builder instance
     * @return new GenericSQLQueryFilterVisitor instance
     */
    protected GenericSQLQueryFilterVisitor createFilterVisitor(SQLBuilder builder) {
        return new GenericSQLQueryFilterVisitor(getSearchableLength(), builder);
    }

    @Override
    public Integer queryCount(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        return queryHandler.queryCount(type, params, connection);
    }

    @Override
    public Integer command(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        return queryHandler.command(type, params, connection);
    }

}
