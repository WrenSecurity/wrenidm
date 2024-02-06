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
package org.forgerock.openidm.repo.jdbc.impl.query;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.forgerock.openidm.repo.jdbc.impl.mapper.ResultMapper;

/**
 * Functional interface for creating {@link ResultMapper}s based on result set meta data.
 *
 * @param <T> result object type
 */
@FunctionalInterface
public interface ResultMapperFactory<T> {

    /**
     * Create new result mapper based on the provided result set meta data.
     *
     * @param metaData result set meta data
     * @return result mapper instance
     * @throws SQLException in case of DB failure
     */
    ResultMapper<T> createResultMapper(ResultSetMetaData metaData) throws SQLException;

}
