/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License
 * for the specific language governing permission and limitations under the
 * License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each
 * file and include the License file at legal/CDDLv1.0.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2016 ForgeRock AS.
 * Portions Copyright 2018-2024 Wren Security.
 */
package org.forgerock.openidm.info.health;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.resource.Responses.newResourceResponse;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.forgerock.api.annotations.ApiError;
import org.forgerock.api.annotations.Handler;
import org.forgerock.api.annotations.Operation;
import org.forgerock.api.annotations.Read;
import org.forgerock.api.annotations.Schema;
import org.forgerock.api.annotations.SingletonProvider;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.ServiceUnavailableException;
import org.forgerock.openidm.core.IdentityServer;
import org.forgerock.openidm.info.health.api.HikariCPDatabaseInfoResource;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component providing HikariCP MBean (JMX) monitoring metrics.
 */
@SingletonProvider(@Handler(
    id = "databaseInfoResourceProvider:0",
    title = "Health - Database connection pool statistics",
    description = "Provides DB connection pool statistics if enabled.",
    mvccSupported = false,
    resourceSchema = @Schema(fromType = HikariCPDatabaseInfoResource.class)))
public class DatabaseInfoResourceProvider extends AbstractInfoResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInfoResourceProvider.class);

    @Read(operationDescription = @Operation(
            description = "Read HikariCP DB connection pool statistics.",
            errors = {
                @ApiError(
                    code = ResourceException.UNAVAILABLE,
                    description = "If HikariCP is not configured as the data source connection pool."
                )
            }))
    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, ReadRequest request) {
        Boolean enabled = Boolean.parseBoolean(IdentityServer.getInstance().getProperty("wrenidm.hikaricp.statistics.enabled", "false"));
        if (!enabled) {
            return new ServiceUnavailableException("HikariCP statistics not enabled").asPromise();
        }
        try {
            ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (*)");
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> names = mBeanServer.queryNames(poolName, null);
            JsonValue results = new JsonValue(new HashMap<>());
            for (ObjectName name : names) {
                JsonValue result = json(object(
                        field("activeConnections", mBeanServer.getAttribute(name, "ActiveConnections")),
                        field("totalConnections", mBeanServer.getAttribute(name, "TotalConnections")),
                        field("idleConnections", mBeanServer.getAttribute(name, "IdleConnections")),
                        field("threadsAwaitingConnection", mBeanServer.getAttribute(name, "ThreadsAwaitingConnection"))
                ));
                results.put(name.getCanonicalName(), result.getObject());
            }
            return newResourceResponse("", "", results).asPromise();
        } catch (Exception e) {
            logger.error("Failed to get HikariCP statistics.", e);
            return new InternalServerErrorException("Failed to get HikariCP statistics.", e).asPromise();
        }
    }
}
