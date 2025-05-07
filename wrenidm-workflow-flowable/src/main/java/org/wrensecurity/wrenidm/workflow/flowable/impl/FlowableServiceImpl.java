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
 * Copyright 2012-2016 ForgeRock AS.
 * Portions Copyright 2020-2025 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl;

import static org.forgerock.json.JsonValueFunctions.enumConstant;
import static org.forgerock.openidm.util.ResourceUtil.notSupported;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.flowable.common.engine.impl.scripting.ResolverFactory;
import org.flowable.common.engine.impl.scripting.ScriptBindingsFactory;
import org.flowable.compatibility.DefaultFlowable5CompatibilityHandler;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.osgi.OsgiScriptingEngines;
import org.flowable.osgi.blueprint.ProcessEngineFactory;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResourceHandler;
import org.forgerock.json.resource.QueryResponse;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.RequestHandler;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.UpdateRequest;
import org.forgerock.openidm.config.enhanced.EnhancedConfig;
import org.forgerock.openidm.config.enhanced.InvalidException;
import org.forgerock.openidm.core.IdentityServer;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.datasource.DataSourceService;
import org.forgerock.openidm.router.IDMConnectionFactory;
import org.forgerock.openidm.router.RouteService;
import org.forgerock.script.ScriptRegistry;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrensecurity.wrenidm.workflow.flowable.impl.identity.IdmEngineConfigurator;
import org.wrensecurity.wrenidm.workflow.flowable.impl.scripting.IdmExpressionManager;
import org.wrensecurity.wrenidm.workflow.flowable.impl.scripting.IdmScriptResolverFactory;
import org.wrensecurity.wrenidm.workflow.flowable.impl.session.IdmSessionFactory;
import org.wrensecurity.wrenidm.workflow.flowable.impl.variable.JsonValueType;

/**
 * Service providing workflow process engine.
 */
@Component(
        name = FlowableServiceImpl.PID,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = {
            ServerConstants.ROUTER_PREFIX + "=" + FlowableServiceImpl.ROUTER_PREFIX
        })
@ServiceVendor(ServerConstants.SERVER_VENDOR_NAME)
@ServiceDescription("Workflow Service")
public class FlowableServiceImpl implements RequestHandler {

    protected static final Logger logger = LoggerFactory.getLogger(FlowableServiceImpl.class);
    // PID cannot be changed because there is hardcoded prefix for configuration (see ConfigBootstrapHelper.qualifyPid(String))
    public final static String PID = "org.forgerock.openidm.workflow";
    public final static String ROUTER_PREFIX = "/workflow*";

    //~ JSON configuration keys
    public static final String CONFIG_LOCATION = "location";
    public static final String CONFIG_ENGINE = "engine";
    public static final String CONFIG_ENGINE_URL = "engine/url";
    public static final String CONFIG_ENGINE_USERNAME = "engine/username";
    public static final String CONFIG_ENGINE_PASSWORD = "engine/password";
    public static final String CONFIG_MAIL = "mail";
    public static final String CONFIG_MAIL_HOST = "host";
    public static final String CONFIG_MAIL_PORT = "port";
    public static final String CONFIG_MAIL_USERNAME = "username";
    public static final String CONFIG_MAIL_PASSWORD = "password";
    public static final String CONFIG_MAIL_STARTTLS = "starttls";
    public static final String CONFIG_TABLE_PREFIX = "tablePrefix";
    public static final String CONFIG_TABLE_PREFIX_IS_SCHEMA = "tablePrefixIsSchema";
    public static final String CONFIG_HISTORY = "history";
    public static final String CONFIG_USE_DATASOURCE = "useDataSource";
    public static final String CONFIG_WORKFLOWDIR = "workflowDirectory";
    public static final String LOCALHOST = "localhost";
    public static final int DEFAULT_MAIL_PORT = 25;

    private boolean localProcessEngine = true; // Flag indicating origin of the process engine

    @Reference(
            name = "processEngine",
            service = ProcessEngine.class,
            unbind = "unbindProcessEngine",
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.STATIC,
            target = "(!(wrenidm.flowable.engine=true))") // Avoid registering the self made service
    private ProcessEngine processEngine;

    /**
     * RepositoryService is a dependency of ConfigurationAdmin. Referencing the service here ensures the
     * availability of this service during activation and deactivation to support the persistence of
     * worfklowInstallerConfig.
     */
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private RepositoryService repositoryService = null;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, bind = "bindConfigAdmin", unbind = "unbindConfigAdmin")
    private ConfigurationAdmin configurationAdmin = null;

    private final Map<String, DataSourceService> dataSourceServices = new ConcurrentHashMap<>();

    @Reference(
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            unbind = "unbindDataSourceService",
            policy = ReferencePolicy.DYNAMIC)
    protected void bindDataSourceService(DataSourceService service, Map<String, Object> properties) {
        dataSourceServices.put(properties.get(ServerConstants.CONFIG_FACTORY_PID).toString(), service);
    }

    protected void unbindDataSourceService(DataSourceService service, Map<String, Object> properties) {
        for (Map.Entry<String, DataSourceService> entry : dataSourceServices.entrySet()) {
            if (service.equals(entry.getValue())) {
                dataSourceServices.remove(entry.getKey());
                break;
            }
        }
    }

    @Reference(target = "(" + ServerConstants.ROUTER_PREFIX + "=/managed)")
    private RouteService routeService;

    @Reference(policy = ReferencePolicy.STATIC)
    IDMConnectionFactory connectionFactory;

    @Reference(policy = ReferencePolicy.DYNAMIC)
    private volatile EnhancedConfig enhancedConfig;

    private final IdmExpressionManager expressionManager = new IdmExpressionManager();
    private final IdmSessionFactory sessionFactory = new IdmSessionFactory();
    private ProcessEngineFactory processEngineFactory;
    private Configuration worfklowInstallerConfig;
    private RequestHandler workflowResource;

    //~ Configuration variables
    private EngineLocation location = EngineLocation.embedded;
    private String mailHost = LOCALHOST;
    private int mailPort = DEFAULT_MAIL_PORT;
    private String mailUsername;
    private String mailPassword;
    private boolean starttls;
    private String tablePrefix;
    private boolean tablePrefixIsSchema;
    private String historyLevel;
    private String useDataSource;
    private String workflowDir;

    private enum EngineLocation {
        embedded, local, remote
    }

    @Override
    public Promise<ActionResponse, ResourceException> handleAction(Context context, ActionRequest request) {
        return workflowResource.handleAction(context, request);
    }

    @Override
    public Promise<ResourceResponse, ResourceException> handleCreate(Context context, CreateRequest request) {
        return workflowResource.handleCreate(context, request);
    }

    @Override
    public Promise<ResourceResponse, ResourceException> handleDelete(Context context, DeleteRequest request) {
        return workflowResource.handleDelete(context, request);
    }

    @Override
    public Promise<ResourceResponse, ResourceException> handlePatch(Context context, PatchRequest request) {
        return notSupported(request).asPromise();
    }

    @Override
    public Promise<QueryResponse, ResourceException> handleQuery(Context context, QueryRequest request, QueryResourceHandler handler) {
        return workflowResource.handleQuery(context, request, handler);
    }

    @Override
    public Promise<ResourceResponse, ResourceException> handleRead(Context context, ReadRequest request) {
        return workflowResource.handleRead(context, request);
    }

    @Override
    public Promise<ResourceResponse, ResourceException> handleUpdate(Context context, UpdateRequest request) {
        return workflowResource.handleUpdate(context, request);
    }

    @Activate
    void activate(ComponentContext context) {
        logger.debug("Activating Service with configuration {}.", context.getProperties());
        try {
            parseConfiguration(context);
            switch (location) {
                case embedded: // Create embedded process engine
                    StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
                    // Prepare data store instance
                    configuration.setDataSource(new DataSourceProxy());

                    // Configure database-related properties
                    configuration.setDatabaseSchemaUpdate("true");
                    configuration.setDatabaseTablePrefix(tablePrefix);
                    configuration.setTablePrefixIsSchema(tablePrefixIsSchema);

                    // Configure custom IdM engine
                    configuration.setIdmEngineConfigurator(new IdmEngineConfigurator(connectionFactory));

                    // Register IdM session factory
                    configuration.addCustomSessionFactory(sessionFactory);

                    // Configure custom expression manager (providing IdM service)
                    configuration.setExpressionManager(expressionManager);

                    // Configure email-related properties
                    configuration.setMailServerHost(mailHost);
                    configuration.setMailServerPort(mailPort);
                    configuration.setMailServerUseTLS(starttls);
                    if (mailUsername != null) {
                        configuration.setMailServerUsername(mailUsername);
                    }
                    if (mailPassword != null) {
                        configuration.setMailServerPassword(mailPassword);
                    }

                    // Configure history level
                    if (historyLevel != null) {
                        configuration.setHistory(historyLevel);
                    }

                    // Allow async workflows
                    configuration.setAsyncExecutorActivate(true);

                    // Disable useless services
                    configuration.setDisableEventRegistry(true);

                    // Enable v5 compatibility mode
                    configuration.setFlowable5CompatibilityEnabled(true);
                    configuration.setFlowable5CompatibilityHandler(new DefaultFlowable5CompatibilityHandler());

                    // Retrieve process engine instance
                    processEngineFactory = new ProcessEngineFactory();
                    processEngineFactory.setProcessEngineConfiguration(configuration);
                    processEngineFactory.setBundle(context.getBundleContext().getBundle());
                    processEngineFactory.init();
                    processEngine = processEngineFactory.getObject();

                    // Register custom script resolver
                    List<ResolverFactory> resolverFactories = configuration.getResolverFactories();
                    resolverFactories.add(new IdmScriptResolverFactory());
                    configuration.setResolverFactories(resolverFactories);
                    configuration.getVariableTypes().addType(new JsonValueType());
                    configuration.setScriptingEngines(new OsgiScriptingEngines(new ScriptBindingsFactory(configuration, resolverFactories)));

                    // Register the OSGi service to enable deployment of BAR or BPMN files
                    Dictionary<String, String> engineProperties = new Hashtable<>();
                    engineProperties.put(Constants.SERVICE_PID, "org.forgerock.openidm.workflow.flowable.engine");
                    engineProperties.put("wrenidm.flowable.engine", "true");
                    context.getBundleContext().registerService(ProcessEngine.class.getName(), processEngine, engineProperties);

                    // Install workflow files
                    if (configurationAdmin != null) {
                        try {
                            worfklowInstallerConfig = configurationAdmin.createFactoryConfiguration("org.apache.felix.fileinstall", null);
                            Dictionary<String, Object> props = worfklowInstallerConfig.getProperties();
                            if (props == null) {
                                props = new Hashtable<>();
                            }
                            props.put("felix.fileinstall.poll", "2000");
                            props.put("felix.fileinstall.noInitialDelay", "true");
                            props.put("felix.fileinstall.dir", IdentityServer.getFileForInstallPath(workflowDir).getAbsolutePath());
                            props.put("felix.fileinstall.filter", ".*\\.bar|.*\\.xml");
                            props.put("felix.fileinstall.bundles.new.start", "true");
                            props.put("config.factory-pid", "flowable");
                            worfklowInstallerConfig.update(props);
                        } catch (IOException e) {
                            logger.error("An error occured when installing workflow files.", e);
                        }
                    }

                    // Create workflow resource with running process engine
                    workflowResource = new FlowableResource(processEngine);
                    logger.debug("Workflow service has been successfully activated.");
                    break;
                case local: // ProcessEngine is connected using OSGi reference
                    workflowResource = new FlowableResource(processEngine);
                    break;
                default:
                    throw new InvalidException("Invalid process engine location: '" + location + "'.");
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to activate workflow service.", e);
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to activate workflow service.", e);
            throw new RuntimeException(e);
        }
    }

    @Deactivate
    void deactivate(ComponentContext context) {
        logger.debug("Deactivating Service {}.", context.getProperties());
        // Destroy workflow installer service
        if (worfklowInstallerConfig != null) {
            try {
                worfklowInstallerConfig.delete();
            } catch (IOException e) {
                logger.error("Unable to destroy workflow installer service.", e);
            }
            worfklowInstallerConfig = null;
        }
        // Destroy process engine factory
        if (processEngineFactory != null) {
            try {
                processEngineFactory.destroy();
            } catch (Exception e) {
                logger.error("Failed to destroy process engine factory.", e);
            }
        }
        logger.debug("Workflow service has been successfully deactivated.");
    }

    /**
     * Parse workflow engine configuration.
     */
    private void parseConfiguration(ComponentContext context) {
        JsonValue config = enhancedConfig.getConfigurationAsJson(context);
        if (!config.isNull()) {
            location = config.get(CONFIG_LOCATION)
                    .defaultTo(EngineLocation.embedded.name())
                    .as(enumConstant(EngineLocation.class));
            useDataSource = config.get(CONFIG_USE_DATASOURCE).defaultTo("default").asString();
            JsonValue mailconfig = config.get(CONFIG_MAIL);
            if (mailconfig.isNotNull()) {
                mailHost = mailconfig.get(CONFIG_MAIL_HOST).defaultTo(LOCALHOST).asString();
                mailPort = mailconfig.get(CONFIG_MAIL_PORT).defaultTo(DEFAULT_MAIL_PORT).asInteger();
                mailUsername = mailconfig.get(CONFIG_MAIL_USERNAME).asString();
                mailPassword = mailconfig.get(CONFIG_MAIL_PASSWORD).asString();
                starttls = mailconfig.get(CONFIG_MAIL_STARTTLS).defaultTo(false).asBoolean();
            }
            tablePrefix = config.get(CONFIG_TABLE_PREFIX).defaultTo("").asString();
            tablePrefixIsSchema = config.get(CONFIG_TABLE_PREFIX_IS_SCHEMA).defaultTo(false).asBoolean();
            historyLevel = config.get(CONFIG_HISTORY).asString();
            workflowDir = config.get(CONFIG_WORKFLOWDIR).defaultTo("workflow").asString();
        }
    }

    @Reference(
            name = "ScriptRegistryService",
            service = ScriptRegistry.class,
            unbind = "unbindScriptRegistry",
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            target = "(service.pid=org.forgerock.openidm.script)")
    protected void bindScriptRegistry(ScriptRegistry scriptRegistry) {
        this.sessionFactory.setScriptRegistry(scriptRegistry);
    }

    protected void unbindScriptRegistry(ScriptRegistry scriptRegistry) {
        this.sessionFactory.setScriptRegistry(null);
    }

    @Reference(
            name = "JavaDelegateServiceReference",
            service = JavaDelegate.class,
            unbind = "unbindService",
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    public void bindService(JavaDelegate delegate, Map<String, Object> props) {
        expressionManager.bindService(delegate, props);
    }

    public void unbindService(JavaDelegate delegate, Map<String, Object> props) {
        expressionManager.unbindService(delegate, props);
    }

    //~ Methods for binding / unbinding references

    protected void bindProcessEngine(ProcessEngine processEngine) {
        if (processEngine == null) {
            this.processEngine = processEngine;
            localProcessEngine = false;
        }
    }
    protected void unbindProcessEngine(ProcessEngine processEngine) {
        if (!localProcessEngine) {
            this.processEngine = null;
            this.workflowResource = null;
        }
    }

    public void bindConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configurationAdmin = configAdmin;
    }
    public void unbindConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configurationAdmin = null;
    }

    /**
     * DataSource implementation that proxies all requests to the chosen DataSource referenced by the
     * <em>useDataSource</em> configuration setting.
     */
    private class DataSourceProxy implements DataSource {

        private DataSource getDataSource() {
            if (useDataSource == null) {
                throw new IllegalStateException("No datasource service specified.");
            } else if (!dataSourceServices.containsKey(useDataSource)) {
                throw new IllegalStateException("Datasource \"" + useDataSource + "\" does not exist.");
            }
            return dataSourceServices.get(useDataSource).getDataSource();
        }

        @Override
        public Connection getConnection() throws SQLException {
            return getDataSource().getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return getDataSource().getLogWriter();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            getDataSource().setLogWriter(out);
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            getDataSource().setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return getDataSource().getLoginTimeout();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return getDataSource().getParentLogger();
        }

        @Override
        public <T> T unwrap(Class<T> clazz) throws SQLException {
            return getDataSource().unwrap(clazz);
        }

        @Override
        public boolean isWrapperFor(Class<?> clazz) throws SQLException {
            return getDataSource().isWrapperFor(clazz);
        }
    }
}
