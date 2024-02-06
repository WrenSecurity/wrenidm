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

import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common superclass for database connection providers that want to have custom initialization on the
 * first open connection and custom teardown logic when the last connection gets closed. This helps
 * with starting and stopping the database only once per test run.
 */
public abstract class AbstractTestConnectionProvider {

    private static final Map<Object, AtomicInteger> CONNECTION_COUNTERS = new HashMap<>();

    /**
     * Request connection for the database.
     */
    public final Connection getConnection() throws Exception {
        synchronized (getClass()) {
            var providerKey = getClass();
            if (!CONNECTION_COUNTERS.containsKey(providerKey)) {
                CONNECTION_COUNTERS.put(providerKey, new AtomicInteger());
            }
            var counter = CONNECTION_COUNTERS.get(providerKey);
            try {
                var connection = openConnection(counter.get() == 0);
                return (Connection) Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class<?>[] { Connection.class },
                        (proxy, method, args) -> {
                            if (method.getName().equals("close")) {
                                closeConnection(connection, counter.decrementAndGet() <= 0);
                                return null;
                            } else {
                                return method.invoke(connection, args);
                            }
                        });
            } finally {
                counter.incrementAndGet();
            }
        }
    }

    /**
     * Open new connection while indicating if it is the first one to be opened.
     */
    protected abstract Connection openConnection(boolean first) throws Exception;

    /**
     * Close active connection while indicating if it is the last open connection.
     */
    protected void closeConnection(Connection connection, boolean last) throws Exception {
        connection.close(); // no teardown by default
    }

    /**
     * Get class-path resource path.
     *
     * <p>Can be used by subclasses to get location of a potential DB initialization script.
     */
    protected String getResourcePath(String name) throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource(name);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource '" + name + "' not found");
        }
        return Path.of(resourceUrl.toURI()).toString();
    }

}
