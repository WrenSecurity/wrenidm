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
package org.forgerock.openidm.repo.jdbc.impl.vendor;

import java.util.Map;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.handler.GenericTableHandler;

/**
 * MySQL database {@link GenericTableHandler} implementation.
 */
public class MySQLGenericTableHandler extends GenericTableHandler {

    /**
     * Max allowed searchable length with default settings and utf8mb4 encoding.
     */
    private static final int MYSQL_SEARCHABLE_LENGTH = 768;

    public MySQLGenericTableHandler(
            String schemaName,
            JsonValue tableConfig,
            Map<String, String> queryConfig,
            Map<String, String> commandConfig,
            int batchSize,
            SQLExceptionHandler exceptionHandler) {
        super(schemaName, tableConfig, queryConfig, commandConfig, batchSize, exceptionHandler);
    }

    @Override
    protected int getSearchableLength() {
        return MYSQL_SEARCHABLE_LENGTH;
    }

}
