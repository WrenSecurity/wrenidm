/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All Rights Reserved
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

package org.forgerock.openidm.salesforce.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.JsonResource;
import org.forgerock.json.resource.JsonResourceException;
import org.forgerock.openidm.config.JSONEnhancedConfig;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.salesforce.internal.async.AbstractAsyncResourceProvider;
import org.forgerock.openidm.salesforce.internal.async.AsyncBatchResourceProvider;
import org.forgerock.openidm.salesforce.internal.async.AsyncBatchResultResourceProvider;
import org.forgerock.openidm.salesforce.internal.async.AsyncJobResourceProvider;
import org.forgerock.openidm.salesforce.internal.data.GenericResourceProvider;
import org.forgerock.openidm.salesforce.internal.data.QueryResourceProvider;
import org.forgerock.openidm.salesforce.internal.data.SObjectsResourceProvider;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A NAME does ...
 * 
 * @author Laszlo Hordos
 */
@Component(name = SalesforceRequestHandler.PID, immediate = true,
        policy = ConfigurationPolicy.REQUIRE)
@Service
@Properties({
    @Property(name = Constants.SERVICE_VENDOR, value = ServerConstants.SERVER_VENDOR_NAME),
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "Salesforce sample"),
    @Property(name = ServerConstants.ROUTER_PREFIX, value = "salesforce") })
public class SalesforceRequestHandler implements JsonResource {

    public static final String PID = "org.forgerock.openidm.salesforce";

    /**
     * Setup logging for the {@link SalesforceRequestHandler}.
     */
    final static Logger logger = LoggerFactory.getLogger(SalesforceRequestHandler.class);

    private Map<Pattern, JsonResource> routes = new HashMap<Pattern, JsonResource>();

    private SalesforceConnection connection = null;

    @Activate
    void activate(ComponentContext context) throws Exception {
        JsonValue configuration = JSONEnhancedConfig.newInstance().getConfigurationAsJson(context);

        connection =
                new SalesforceConnection(parseConfiguration(configuration
                        .get("configurationProperties")));
        connection.test();

        AbstractAsyncResourceProvider async = new AsyncJobResourceProvider(connection);

        routes.put(Pattern.compile("(\\Qasync/job\\E)"), async);
        routes.put(Pattern.compile("\\Qasync/job/\\E(([^/]+))"), async);

        async = new AsyncBatchResourceProvider(connection);

        routes.put(Pattern.compile("\\Qasync/job/\\E(([^/]+))\\Q/batch\\E"), async);
        routes.put(Pattern.compile("\\Qasync/job/\\E([^/]+)\\Q/batch/\\E(([^/]+))"), async);

        async = new AsyncBatchResultResourceProvider(connection);

        routes.put(Pattern.compile("\\Qasync/job/\\E([^/]+)\\Q/batch/\\E(([^/]+))\\Q/result\\E"),
                async);
        routes.put(Pattern
                .compile("\\Qasync/job/\\E([^/]+)\\Q/batch/\\E([^/]+)\\Q/result/\\E(([^/]+))"),
                async);

        SObjectsResourceProvider data = new SObjectsResourceProvider(connection);

        routes.put(Pattern.compile("\\Qsobjects/\\E(([^/])+)"), data);
        routes.put(Pattern.compile("\\Qsobjects/\\E([^/]+)/(([^/]+))"), data);

        routes.put(Pattern.compile("(licensing|connect|search|tooling|chatter|recent)((\\/.*)+)"),
                new GenericResourceProvider(connection));

        routes.put(Pattern.compile("query"), new QueryResourceProvider(connection));

        logger.info("OAUTH Token: {}", connection.getOAuthUser().getAuthorization());
    }

    @Deactivate
    void deactivate(ComponentContext context) throws Exception {
        routes.clear();
        if (null != connection) {
            connection.dispose();
            connection = null;
        }
    }

    public SalesforceConfiguration parseConfiguration(JsonValue config) {
        return SalesforceConnection.mapper.convertValue(
                config.required().expect(Map.class).asMap(), SalesforceConfiguration.class);
    }

    @Override
    public JsonValue handle(JsonValue request) throws JsonResourceException {
        String id = request.get("id").required().asString();
        if (id.endsWith("/")) {
            id = id.substring(0, id.length() - 1);
        }
        for (Map.Entry<Pattern, JsonResource> entry : routes.entrySet()) {
            Matcher matcher = entry.getKey().matcher(id);
            if (matcher.matches()) {
                ServerContext.build(matcher);
                try {
                    return entry.getValue().handle(request);
                } finally {
                    ServerContext.clear();
                }
            }
        }
        throw new JsonResourceException(JsonResourceException.NOT_FOUND, "Route not found: " + id);
    }
}
