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
package org.forgerock.openidm.info.health.api;

import org.forgerock.api.annotations.Description;
import org.forgerock.api.annotations.ReadOnly;
import org.forgerock.openidm.info.health.DatabaseInfoResourceProvider;

/**
 * {@link DatabaseInfoResourceProvider} API POJO.
 */
public class HikariCPDatabaseInfoResource {

    private int idleConnections;
    private int activeConnections;
    private int totalConnections;
    private int threadsAwaitingConnection;

    @Description("Idle connections count")
    @ReadOnly
    public int getIdleConnections() {
        return idleConnections;
    }

    @Description("Active connections count")
    @ReadOnly
    public int getActiveConnections() {
        return activeConnections;
    }

    @Description("Total connections count")
    @ReadOnly
    public int getTotalConnections() {
        return totalConnections;
    }

    @Description("Number of threads waiting for a connection")
    @ReadOnly
    public int getThreadsAwaitingConnection() {
        return threadsAwaitingConnection;
    }

}
