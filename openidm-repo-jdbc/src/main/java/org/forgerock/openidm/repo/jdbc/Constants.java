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
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2024 Wren Security.
 */

package org.forgerock.openidm.repo.jdbc;

/**
 * JDBC Repository Service constants.
 */
public class Constants {

    /**
     * DB Table column representing the Object revision.
     */
    public static final String RAW_OBJECT_REV = "rev";

    /**
     * ID of the row representing the Object within the DB Table.
     */
    public static final String RAW_ID = "id";

    /**
     * ObjectTypes ID of the Object within the DB Table.
     */
    public static final String RAW_OBJECTTYPES_ID = "objecttypes_id";

    /**
     * ID of the Mapped Object.
     */
    public static final String OBJECT_ID = "_id";

    /**
     * Revision of the Mapped Object
     */
    public static final String OBJECT_REV = "_rev";

}
