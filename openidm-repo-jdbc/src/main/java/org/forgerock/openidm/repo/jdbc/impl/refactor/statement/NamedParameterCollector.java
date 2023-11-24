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
package org.forgerock.openidm.repo.jdbc.impl.refactor.statement;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple convenient map wrapper for collecting sequence of named SQL parameters.
 */
public class NamedParameterCollector {

    private final Map<String, AtomicInteger> counters = new TreeMap<String, AtomicInteger>();

    private final Map<String, Object> parameters;

    /**
     * Create new collector with the backing parameter map.
     *
     * @param parameters parameter map that will store collected parameter token-value pairs
     */
    public NamedParameterCollector(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Register parameter value with the given token name prefix.
     *
     * @param prefix parameter token name prefix
     * @param parameterValue parameter value to register
     * @return generated token name
     */
    public String register(String prefix, Object parameterValue) {
        String parameterKey = generate(prefix);
        parameters.put(parameterKey, parameterValue);
        return parameterKey;
    }

    /**
     * Generate parameter name with the given token name prefix.
     *
     * @param prefix parameter token name prefix
     * @return generated token name
     */
    public String generate(String prefix) {
        var counter = counters.get(prefix);
        if (counter == null) {
            counter = new AtomicInteger();
            counters.put(prefix, counter);
        }
        return prefix + counter.incrementAndGet();
    }

}
