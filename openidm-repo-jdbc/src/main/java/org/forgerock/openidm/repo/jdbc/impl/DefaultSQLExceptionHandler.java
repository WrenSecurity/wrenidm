/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 * Portions Copyright 2018 Wren Security.
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
package org.forgerock.openidm.repo.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;
import org.forgerock.openidm.repo.jdbc.ErrorType;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;

/**
 * Default {@link SQLExceptionHandler} to help handle {@code SQLException}s across different DB implementations.
 *
 * <p>
 * Specific implementations and/or overrides may be needed for supported databases.
 */
// XXX This class did not undergo refactor like handlers where the default implementation is no longer MySQL
public class DefaultSQLExceptionHandler implements SQLExceptionHandler {

    @Override
    public boolean isErrorType(SQLException ex, ErrorType errorType) {
        return XOpenErrorMapping.isErrorType(ex, errorType);
    }

    @Override
    public boolean isRetryable(SQLException ex, Connection connection) {
        // These are known re-tryable for MySQL. Other DBs may need specific sql exception handler defnitions.
        if (isErrorType(ex, ErrorType.CONNECTION_FAILURE) || isErrorType(ex, ErrorType.DEADLOCK_OR_TIMEOUT)
                || isErrorType(ex, ErrorType.CANT_CHANGE_TX_ISOLATION)) {
            return true;
        } else {
            return false;
        }
    }

}
