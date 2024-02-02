/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011-2015 ForgeRock AS. All rights reserved.
 * Portions Copyright 2023 Wren Security.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.repo.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.PreconditionFailedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.openidm.repo.QueryConstants;
import org.forgerock.util.query.QueryFilter;

/**
 * Handler responsible for performing SQL operations on the underlying data source.
 *
 * <p>There are two different strategies represented by the respective implementations:
 *
 * <ul>
 * <li><code>MappedTableHandler</code> &ndash; resource is being stored in its own dedicated
 * table where properties are mapped to table columns
 * <li><code>GenericTableHandler</code> &ndash; resource is being stored as JSON object in a
 * generic table
 * </ul>
 */
public interface TableHandler {

    /**
     * Get an object from the repository by its identifier. The returned object is not validated
     * against the current schema and may need processing to conform to an updated schema.
     *
     * <p>
     * The object will contain metadata properties, including object identifier {@code _id},
     * and object version {@code _rev} to enable optimistic concurrency.
     *
     * @param fullId the qualified identifier of the object to retrieve from the object set
     * @param type the qualifier of the object to retrieve
     * @param localId the identifier without the qualifier of the object to retrieve
     * @param connection database connection to use
     * @throws NotFoundException if the specified object could not be found
     * @throws IOException in case of JSON processing error
     * @throws SQLException if a DB failure was reported
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @return the requested object
     */
    ResourceResponse read(String fullId, String type, String localId, Connection connection)
            throws NotFoundException, IOException, SQLException;

    /**
     * Create a new object in the object set.
     *
     * <p>
     * This method mutates the provided object by setting the {@code _id} property to the
     * assigned identifier for the object and the {@code _rev} property to the revised object
     * version (for optimistic concurrency).
     *
     * @param fullId the client-generated identifier to use, or {@code null} if server-generated
     * identifier is requested
     * @param type the qualifier of the object to create
     * @param localId the identifier without the qualifier (if specified in {@code fullId} parameter)
     * @param obj the contents of the object to create in the object set
     * @param connection database connection to use
     * @throws PreconditionFailedException if an object with the same ID already exists
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @throws IOException in case of JSON processing error
     * @throws SQLException if a DB failure is reported
     */
    void create(String fullId, String type, String localId, Map<String, Object> obj, Connection connection)
            throws PreconditionFailedException, InternalServerErrorException, IOException, SQLException;

    /**
     * Update the specified object in the object set.
     *
     * <p>
     * This implementation requires MVCC and hence enforces that clients state what revision they expect
     * to be updating.
     *
     * <p>
     * This method mutates the provided object by updating {@code _rev} property value for the revised
     * object's version.
     *
     * @param fullId the identifier of the object to be updated
     * @param type the qualifier of the object to update
     * @param localId the identifier without the qualifier
     * @param rev the version of the object to update
     * @param obj the contents of the object to put in the object set
     * @param connection database connection to use
     * @throws NotFoundException if the specified object could not be found
     * @throws PreconditionFailedException if version did not match the existing object in the set
     * @throws BadRequestException if the passed identifier is invalid
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @throws IOException in case of JSON processing error
     * @throws SQLException if a DB failure is reported
     */
    void update(String fullId, String type, String localId, String rev, Map<String, Object> obj,
            Connection connection) throws NotFoundException, PreconditionFailedException,
            BadRequestException, InternalServerErrorException, IOException, SQLException;

    /**
     * Delete the specified object from the object set.
     *
     * @param fullId the identifier of the object to be deleted
     * @param type the qualifier of the object to delete
     * @param localId the identifier without the qualifier
     * @param rev the version of the object to delete or {@code *} to match any version
     * @param connection database connection to use
     * @throws NotFoundException if the specified object could not be found
     * @throws PreconditionFailedException if version did not match the existing object in the set
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @throws SQLException if a DB failure is reported
     */
    void delete(String fullId, String type, String localId, String rev, Connection connection)
            throws SQLException, ResourceException;

    /**
     * Perform a query on the specified object set and return the associated results.
     *
     * <p>
     * Queries are parametric; a set of named parameters is provided as the query criteria.
     * The query result is a JSON object structure composed of basic Java types.
     *
     * <p>
     * The query parameters map is a simple shallow map that consists of two types
     * of key-value pairs:
     *
     * <ul>
     * <li>meta-data about the query to perform (e.g query identifier, page size) &ndash;
     * see {@link QueryConstants}
     * <li>named parameters for the actual (SQL) prepared statement
     * </ul>
     *
     * @param type identifies the object type (qualifier) to query
     * @param params the parameters for the query to perform
     * @param connection database connection to use
     * @return list of matched records in JSON object structure format
     * @throws NotFoundException if the specified object could not be found
     * @throws BadRequestException if the specified params contain invalid arguments, e.g. a query id that
     * is not configured, a query expression that is invalid, or missing query substitution tokens
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @throws SQLException if a DB failure is reported
     */
    // XXX Would be much nicer, cleaner and safer to have a dedicated QueryParams value object
    List<Map<String, Object>> query(String type, Map<String, Object> params, Connection connection)
                throws SQLException, ResourceException;

    /**
     * Get number of objects that match query as specified by the provided parameters.
     *
     * <p>
     * Semantics of query parameters is the same as in {@link #query(String, Map, Connection)}.
     *
     * @param type identifies the object type (qualifier) to query
     * @param params the parameters for the query to perform
     * @param connection database connection to use
     * @return number of stored objects that match the specified query or null if the count can not
     * be determined
     * @throws BadRequestException if the specified params contain invalid arguments, e.g. a query id that
     * is not configured, a query expression that is invalid, or missing query substitution tokens
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @throws SQLException if a DB failure is reported
     */
    default Integer queryCount(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException {
        throw new UnsupportedOperationException(); // TODO remove default after dropping legacy handlers
    }

    /**
     * Perform the command on the specified target and return the number of affected objects.
     *
     * <p>
     * Commands are parametric; a set of named parameters is provided as the query criteria.
     * The command returns the number of records altered/updated/deleted.
     *
     * @param type identifies the object set to query
     * @param params the parameters of the query to perform
     * @param connection database connection to use
     * @return the number of records affected or {@code null} if unknown
     * @throws BadRequestException if the specified params contain invalid arguments, e.g. a query id that
     * is not configured, a query expression that is invalid, or missing query substitution tokens
     * @throws InternalServerErrorException if the operation failed because of a (possibly transient) failure
     * @throws SQLException  if a DB failure is reported
     */
    Integer command(String type, Map<String, Object> params, Connection connection)
            throws SQLException, ResourceException;

    /**
     * Build a raw query from the supplied filter.
     *
     * @param filter the query filter
     * @param replacementTokens a map to store any replacement tokens
     * @param params a map containing query parameters
     * @param count whether to render a query for total number of matched rows
     * @return the raw query string
     */
    @Deprecated
    default String renderQueryFilter(QueryFilter<JsonPointer> filter, Map<String, Object> replacementTokens,
            Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    /**
     *  Check if a given queryId exists in our set of known queries
     *
     * @param queryId Identifier for the query
     * @return true if queryId is available
     */
    @Deprecated
    default boolean queryIdExists(final String queryId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Check if a given exception signifies a well known error type.
     *
     * <p>
     * Allows table handlers to abstract database specific differences in reporting errors.
     *
     * @param exception the exception thrown by the database
     * @param errorType the error type to test against
     * @return true if the exception matches the error type passed
     */
    // XXX This is a strange method design... Wouldn't it be better to simply return ErrorType?
    boolean isErrorType(SQLException exception, ErrorType errorType);

    /**
     * Determine whether a given exception can be followed up by a operation retry.
     *
     * @param exception the exception thrown by the database
     * @param connection database connection where the failure occured (used for additional context)
     * @return true if the operation that lead to the error should be retried.
     */
    boolean isRetryable(SQLException exception, Connection connection);

}
