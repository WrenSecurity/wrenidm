/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 * Portions Copyright 2024 Wren Security.
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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface to help handle SQLExceptions across different DB implementations.
 */
public interface SQLExceptionHandler {

    /**
     * Query if a given exception signifies a well known error type.
     *
     * <p>
     * Allows table handlers to abstract database specific differences in reporting errors.
     *
     * @param exception the exception thrown by the database
     * @param errorType the error type to test against
     * @return true if the exception matches the error type passed
     */
    boolean isErrorType(SQLException exception, ErrorType errorType);

    /**
     * As whether a given exception should be retried.
     *
     * @param exception the exception thrown by the database
     * @param connection where the failure occured, used for additional context
     * @return true if the expectation is that transaction should be retried by the application
     */
    boolean isRetryable(SQLException exception, Connection connection);

}