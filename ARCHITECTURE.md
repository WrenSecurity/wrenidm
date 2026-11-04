<!--
  The contents of this file are subject to the terms of the Common Development and
  Distribution License (the License). You may not use this file except in compliance with the
  License.

  You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
  specific language governing permission and limitations under the License.

  When distributing Covered Software, include this CDDL Header Notice in each file and include
  the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
  Header, with the fields enclosed by brackets [] replaced by your own identifying
  information: "Portions copyright [year] [name of copyright owner]".

  Copyright 2026 Wren Security. All rights reserved.
-->

# Wren:IDM architecture

An overview for contributors and for tools that analyse the code: what the parts are, how a request
travels through them, and where to look. It describes the code as it is, including parts inherited
from OpenIDM that are older than the rest.

## Overview

Wren:IDM is an identity management server: a configurable data model of managed objects (users,
roles, assignments, ...), synchronization and reconciliation with external systems through the
Identity Connector Framework (Wren:ICF), workflow, password and data policies, user self-service,
and an administration and end-user UI. Almost everything is exposed as one REST API.

* Java 17, an OSGi application on Apache Felix with an embedded Jetty 12 (Felix HTTP), started from
  a distribution zip (`startup.sh`) or the Docker image - not a WAR.
* Maven multi-module build (`pom.xml`), parent `org.wrensecurity:wrensec-parent`, groupId
  `org.wrensecurity.wrenidm`.
* Every component is a resource on an internal CREST (common REST) router; the HTTP API, scripts
  and Java services all talk to each other through it.
* Much of the behaviour is configuration and JavaScript rather than Java: JSON files in `conf/`
  and scripts in `bin/defaults/script/` (authorization, policies, roles, UI helpers).
* The code has two generations: `org.forgerock.openidm.*` inherited from OpenIDM (artifacts
  `openidm-*`), and `org.wrensecurity.wrenidm.*` added by Wren Security (artifacts `wrenidm-*`,
  e.g. the Flowable workflow and Jetty configuration).

User documentation - a reference for each component's configuration and REST API, and the Upgrade
Guide - is in the separate repository
[WrenSecurity/wrenidm-docs](https://github.com/WrenSecurity/wrenidm-docs); samples are in
[WrenSecurity/wrenidm-cookbook](https://github.com/WrenSecurity/wrenidm-cookbook). System
tests that run against a started Wren:IDM over REST are in the `wrenidm-test` module of
[orchitech/wrensec-test](https://github.com/orchitech/wrensec-test).

## Modules

| Module | Content |
|---|---|
| `openidm-zip` | assembles the distribution (`src/main/assembly/zip.xml`); holds the default `conf/`, scripts (`bin/defaults/script/`, `script/`), database scripts (`db/`), keystores (`security/`), start scripts |
| `openidm-launcher` | `org.forgerock.commons.launcher.Main`: starts Felix, reads `bin/launcher.json` and `conf/boot/boot.properties` |
| `openidm-system` | `IdentityServer` (boot properties, install and project locations), `ServerConstants`, property substitution |
| `openidm-config`, `openidm-enhanced-config` | loading `conf/*.json` into OSGi Configuration Admin, storing configuration in the repository, `/config`, decrypting and substituting values for components |
| `openidm-router` | the internal CREST router, route registration, `router.json` filters |
| `openidm-api-servlet`, `openidm-servlet`, `openidm-servlet-registrator` | the `/openidm` REST servlet and the external filter chain; static UI servlet; servlet filter registration |
| `wrenidm-jetty-config` | Jetty ports and HTTPS / mutual TLS connectors |
| `openidm-authnfilter`, `openidm-identity-provider` | authentication filter and modules, `/authentication`; social / OAuth identity providers |
| `openidm-core` | managed objects (`org.forgerock.openidm.managed`), relationships, synchronization and reconciliation (`org.forgerock.openidm.sync`) |
| `openidm-provisioner`, `openidm-provisioner-openicf` | `/system`: connectors to external systems through Wren:ICF, liveSync |
| `openidm-repo`, `openidm-repo-jdbc` | repository API and the JDBC implementation (`/repo`) |
| `openidm-script` | script engines (Rhino JavaScript, Groovy), script bindings, `/script` |
| `openidm-customendpoint` | scripted endpoints `/endpoint/<name>` |
| `openidm-policy` | `/policy`, validation of objects and properties |
| `openidm-scheduler`, `openidm-quartz-fragment` | Quartz scheduler, scheduled jobs, task scanner; repository job store |
| `wrenidm-workflow-flowable` | workflow on Flowable, `/workflow` |
| `openidm-selfservice` | user self-service (registration, password reset, forgotten username, KBA) |
| `openidm-audit` | audit events and handlers, `/audit` |
| `openidm-crypto`, `openidm-keystore`, `openidm-security` | encryption and hashing, keystore and truststore, `/security` |
| `openidm-cluster` | cluster membership and failover in the repository |
| `openidm-infoservice`, `openidm-maintenance` | `/info`, `/health`; maintenance mode and the inherited update mechanism |
| `openidm-external-email`, `openidm-external-rest`, `openidm-messaging` | outbound email and REST calls, JMS subscribers |
| `openidm-smartevent` | internal event publishing and statistics (monitoring) |
| `openidm-felix-webconsole` | security provider for the Felix web console (`/system/console`) |
| `openidm-shell` | command-line tool `cli.sh` (`bin/openidm.jar`): config import and export, encryption, connector configuration |
| `openidm-ui` | the UIs: `openidm-ui-common`, `openidm-ui-admin`, `openidm-ui-enduser`, `openidm-ui-api` |
| `openidm-util` | shared utilities, `CryptoService` interface, scripted router filter |
| `custom-scripted-connector-bundler` | generator for custom Groovy-based connectors |

## Runtime and configuration

* **Start.** `startup.sh [-p <project>]` runs `org.forgerock.commons.launcher.Main` with
  `bin/launcher.json`, which installs the bundles from `bundle/` at start levels (system and
  configuration first, then the repository, Declarative Services, configuration admin, cluster,
  most services, Jetty and finally the scheduler). The install location holds the binaries; the
  project location (`-p`, by default the install location) holds `conf/`, `script/` and
  `workflow/`, and overrides the defaults. A restart is requested with exit code 255.
* **Boot properties.** `conf/boot/boot.properties`: ports (`openidm.port.http` 8080,
  `openidm.port.https` 8443, `openidm.port.mutualauth` 8444), keystore and truststore
  (`security/keystore.jceks`, `security/truststore`), key aliases, node ID. Values may be
  obfuscated (`OBF:`, `CRYPT:`). `conf/config.properties` configures Felix;
  `conf/system.properties` sets system properties, including IDM switches. `IdentityServer.getProperty`
  - and `&{...}` substitution - look in system properties first (`-D`, `OPENIDM_OPTS`,
  `conf/system.properties`), then in `boot.properties`, so any boot property can be overridden per
  node without editing files.
* **Launcher options.** Besides `-p` (project), `Main` accepts `-w` (working location: Felix cache
  and working data), `-c` (another `launcher.json`), `-s` (OSGi storage) and `-P` (framework
  parameters); the locations are available as `launcher.install.location`,
  `launcher.project.location` and `launcher.working.location`.
* **Components.** Services are OSGi Declarative Services components
  (`org.osgi.service.component.annotations`). The component name is its configuration PID, and
  a file `conf/<name>.json` configures PID `org.forgerock.openidm.<name>`
  (`ConfigBootstrapHelper`, `JSONConfigInstaller`); `conf/<factory>-<alias>.json` creates an
  instance of factory PID `org.forgerock.openidm.<factory>` (e.g. `endpoint-linkedView.json`,
  `info-ping.json`). Components read their JSON through `JSONEnhancedConfig`, which substitutes
  `&{property}` references and decrypts `$crypto` values.
* **Configuration store.** Felix FileInstall watches `conf/` and loads the files into
  Configuration Admin, whose persistence manager (`RepoPersistenceManager`) stores them in the
  repository (`config/...`, table `configobjects`). The sync works both ways: `JSONConfigInstaller`
  writes changes made through `/config` back to `conf/<name>.json` and deletes the file when the
  configuration is deleted, and deleting a file deletes the configuration.
  `felix.fileinstall.enableConfigSave=false` in `conf/config.properties` stops the write-back.
  File loading and repository storage can be switched off in `conf/system.properties`
  (`openidm.fileinstall.enabled`, `openidm.config.repo.enabled`). The repository and data source
  configuration (`conf/repo.jdbc.json`, `conf/datasource.jdbc-*.json`) is read directly from the
  files at boot. Properties that a bundle declares sensitive (a
  `MetaDataProvider` named in `org/forgerock/metadata/bundle.json`) are encrypted when stored.
* **Distribution defaults.** `openidm-zip/src/main/resources/conf/` contains the default
  configuration: authentication, router, managed objects, repository (in-memory H2), audit,
  policy, scheduler, workflow, UI contexts, and the endpoints and info services the UI uses. There
  is no default `sync.json`, connector (`provisioner.openicf-*.json`), schedule or self-service
  process configuration; projects add them. The inherited samples were removed for licence
  reasons (`samples/README.md`; they remain on the `sustaining/5.x` branch); new ones are in
  `wrenidm-cookbook`.
* **Cluster.** Several nodes can share one repository. `ClusterManager` (`conf/cluster.json`)
  checks each instance in under `repo/cluster/states/<node>`; when a node goes down, the others
  recover its scheduled jobs (`RepoJobStore`) and configuration changes are propagated through
  cluster events.

## Request handling

HTTP is served by Felix HTTP Jetty with servlets registered through the OSGi HTTP Whiteboard.

| Path | Served by |
|---|---|
| `/openidm/*` | REST API: `ServletComponent` (`openidm-api-servlet`) - a CHF servlet with the authentication filter in front of the external CREST connection |
| `/admin` | admin UI, `ResourceServlet` (`openidm-servlet`), `conf/ui.context-admin.json` |
| `/` | end-user (self-service) UI, `conf/ui.context-selfservice.json` |
| `/api` | API explorer (Swagger UI) over the OpenAPI description the REST servlet produces, `conf/ui.context-api.json` |
| `/system/console` | Felix web console, Basic authentication against `conf/felix.webconsole.json` |

Ports: 8080 (HTTP), 8443 (HTTPS, client certificate optional) and 8444 (HTTPS, client certificate
required; the only port where `CLIENT_CERT` authentication is accepted). Servlet filters can be
added with `conf/servletfilter-*.json` (by default CORS).

A REST request then passes:

1. **Authentication** (`openidm-authnfilter`), which establishes the `SecurityContext` - user ID,
   resource and roles - or rejects the request.
2. **The external filter chain** (`ServletConnectionFactory`, `openidm-api-servlet`): a filter
   returning 503 until startup finishes, the maintenance filter, trace logging, the audit filter,
   then the filters from `conf/router.json`. These are scripted filters with an optional
   `pattern`, `methods`, `condition` and `onRequest` / `onResponse` / `onFailure` scripts. By
   default: authorization (`router-authz.js`, see [Authorization](#authorization)), policy
   validation of managed, system and internal objects (`policyFilter.js`), and hashing of internal
   user passwords.
3. **The router** (`openidm-router`, `RouterRegistryImpl`): every OSGi service exposing a CREST
   `RequestHandler` or resource provider with the property `openidm.router.prefix`
   (`ServerConstants.ROUTER_PREFIX`) is bound at that path.

The same chain serves internal calls. Java services use `IDMConnectionFactory`:
`getConnection()` marks the call as internal, `getExternalConnection()` keeps the caller's
context so the call is treated as the caller's own. Scripts call the router through
`openidm.create/read/update/patch/query/delete/action(...)`. The `router-authz.js` filter runs
only when `context.caller.external` is true or the call comes from self-service - internal calls
are not authorized.

Main resources on the router:

| Path | Implementation |
|---|---|
| `managed/<type>[/<id>[/<relationship>]]` | `ManagedObjectService`, `ManagedObjectSet`, `*RelationshipProvider` (`openidm-core`) |
| `sync`, `recon` | `SynchronizationService`, `ReconciliationService` (`openidm-core`) |
| `system`, `system/<connector>/<objectClass>` | `SystemObjectSetService` (`openidm-provisioner`), `OpenICFProvisionerService` (`openidm-provisioner-openicf`) |
| `repo/*` (e.g. `repo/internal/user`, `repo/link`, `repo/relationships`) | `JDBCRepoService` (`openidm-repo-jdbc`) |
| `config` | `ConfigObjectService` (`openidm-config`) |
| `authentication` | `AuthenticationService` (`openidm-authnfilter`) |
| `script` (`compile`, `eval`) | `ScriptRegistryService` (`openidm-script`) |
| `endpoint/<name>` | `EndpointsService` (`openidm-customendpoint`), `conf/endpoint-<name>.json` |
| `policy` | `PolicyService` (`openidm-policy`) and `policy.js` |
| `scheduler`, `taskscanner` | `SchedulerService`, `TaskScannerService` (`openidm-scheduler`) |
| `workflow/{processdefinition,processinstance,taskinstance,...}` | `FlowableServiceImpl`, `FlowableResource` (`wrenidm-workflow-flowable`) |
| `selfservice/<process>`, `selfservice/kba`, `selfservice/user` | `SelfService`, `KbaService`, `UserUpdateService` (`openidm-selfservice`) |
| `identityProviders` | `IdentityProviderService` (`openidm-identity-provider`) |
| `audit/<topic>` | `AuditServiceImpl` (`openidm-audit`) |
| `security/keystore`, `security/truststore` | `SecurityManager` (`openidm-security`) |
| `cluster` | `ClusterManager` (`openidm-cluster`) |
| `info/<name>`, `health/*` | `InfoService`, `HealthService` (`openidm-infoservice`) |
| `maintenance`, `maintenance/update` | `MaintenanceService`, `UpdateService` (`openidm-maintenance`) |
| `external/email`, `external/rest` | `EmailServiceImpl`, `RestService` |

## Authentication

* `AuthenticationService` (`conf/authentication.json`) builds the filter from the CAF/JASPI
  library: a session module and a list of authentication modules tried in order. `AuthFilterWrapper`
  is the filter the REST servlet uses.
* Session: `JWT_SESSION`, a JWT in a cookie, encrypted with the HTTPS key pair (alias
  `openidm-localhost` - replacing it invalidates sessions) and signed with an HMAC key (alias
  `openidm-jwtsessionhmac-key`); by default 120 minutes at most, 30 minutes idle. Sessions are
  not stored on the server.
* Modules (`IDMAuthModule`): `STATIC_USER` (by default `anonymous` with role `openidm-reg`),
  `MANAGED_USER` (users in `managed/user`), `INTERNAL_USER` (`repo/internal/user`, e.g.
  `openidm-admin`), `CLIENT_CERT` (role `openidm-cert`), `OAUTH`, `OPENID_CONNECT` and
  `SOCIAL_PROVIDERS` (from `identityProviders`), `OPENAM_SESSION`, `IWA`, `TRUSTED_ATTRIBUTE`,
  `PASSTHROUGH` (authenticate against a connector).
* Credentials come from `X-OpenIDM-Username` / `X-OpenIDM-Password` headers or Basic
  authentication. Each module maps the authenticated user to roles (from a property such as
  `authzRoles`, or by group mapping) and may run an `augmentSecurityContext` script.
  `X-OpenIDM-NoSession` skips issuing a session.
* Re-authentication: the default `MANAGED_USER` module marks protected attributes
  (`auth/customAuthz.setProtectedAttributes`, e.g. the password); `router-authz.js`
  (`reauthIfProtectedAttributeChange`) requires `authentication?_action=reauthenticate` with the
  current password in `X-OpenIDM-Reauth-Password` before they change.
* Internal users and roles (`openidm-admin`, `openidm-authorized`, `openidm-reg`, `openidm-cert`,
  `openidm-tasks-manager`) are seeded by `db/<database>/scripts/openidm.sql`.

## Authorization

* External requests are authorized only by the router filter `bin/defaults/script/router-authz.js`.
  It loads the rules from `script/access.js` of the project: entries with `pattern`
  (`excludePatterns`), `roles`, `methods`, `actions` and an optional `customAuthz` expression
  built from functions in `router-authz.js` (`ownDataOnly`, `restrictPatchToFields`,
  `onlyEditableManagedObjectProperties`, `isMyTask`, `isAllowedToStartProcess`,
  `disallowQueryExpression`, ...). A request matching no entry is denied (403).
* Non-read requests must carry `X-Requested-With`, `Authorization` or `X-OpenIDM-Username`
  (protection against cross-site requests).
* `openidm-admin` may do almost everything; end users (`openidm-authorized`) get their own data
  and selected operations; `openidm-reg` (anonymous) gets self-service processes and public
  information.
* Workflow has an extra layer: `conf/process-access.json` lists which roles may start which process
  definitions.
* Because `access.js` lives in the project, deployments carry their own copy; changes to the
  default reach only new projects.

## Managed objects

* `conf/managed.json` defines the object types (by default `user`, `role`, `assignment`) and
  their schema. `ManagedObjectService` routes `managed/<type>` to a `ManagedObjectSet` per type,
  which stores the objects in the repository (`repo/managed/<type>`).
* Per type, scripts hook into operations: `onCreate`, `onRead`, `onUpdate`, `onDelete`,
  `postCreate`, `postUpdate`, `postDelete`, `onValidate`, `onRetrieve`, `onStore`, `onSync`,
  plus custom `actions`. Per property (`SchemaField`): `scope` (`private` properties are not
  returned over REST), `encryption`, `secureHash`, `isVirtual` (computed by `onRetrieve`),
  `returnByDefault`, `policies`, `userEditable`, `viewable`.
* Changes to managed objects trigger implicit synchronization (`sync` actions `notifyCreate`,
  `notifyUpdate`, `notifyDelete`).
* **Relationships.** A property of type `relationship` references another object
  (`{"_ref": "managed/role/<id>", "_refProperties": {...}}`). Edges are stored in
  `repo/relationships`, exposed as sub-resources (`managed/user/<id>/roles`) by
  `CollectionRelationshipProvider` / `SingletonRelationshipProvider`, and kept bidirectional with
  `reverseRelationship` / `reversePropertyName`. Default relationships: `user.roles` ↔
  `role.members`, `user.authzRoles` ↔ `role.authzMembers`, `user.manager` ↔ `user.reports`,
  `role.assignments` ↔ `assignment.roles`.
* **Roles and assignments.** Users receive roles directly, conditionally (a query filter on the
  role, `roles/conditionalRoles.js`), temporally (`temporalConstraints`, enforced by scheduled
  jobs from `roles/postOperation-roles.js`) or by inheritance from related objects
  (`roles/inheritedRoles.js`, opt-in); the grant type is in `_refProperties._grantType`. The
  virtual properties `effectiveRoles` and `effectiveAssignments` (scripts in
  `bin/defaults/script/roles/`) compute what applies; assignments carry the attributes that
  mappings push to target systems.
* `authzRoles` decides the authorization roles of a managed user and may reference internal roles
  (`repo/internal/role`, e.g. `openidm-admin`) as well as managed roles; `roles` are business
  roles for provisioning.

## Synchronization and provisioning

* **Connectors.** `conf/provisioner.openicf-<name>.json` configures a Wren:ICF connector instance,
  exposed at `system/<name>/<objectClass>`. Connector bundles are in `connectors/` (LDAP, CSV,
  database table, scripted Groovy, SSH, XML, Kerberos are shipped); remote connector servers are
  configured in `conf/provisioner.openicf.connectorinfoprovider.json`, also as groups for failover
  and load balancing (`remoteConnectorServersGroups`). Groovy-based custom
  connectors can be generated with `custom-scripted-connector-bundler`.
* **Mappings.** `conf/sync.json` holds mappings between a source and a target (`managed/...` or
  `system/...`): property mappings with transform scripts, correlation queries or scripts, valid
  source and target conditions, and policies that map each **situation** (`CONFIRMED`, `FOUND`,
  `ABSENT`, `MISSING`, `AMBIGUOUS`, `UNQUALIFIED`, `SOURCE_MISSING`, ...) to an **action**
  (`CREATE`, `UPDATE`, `DELETE`, `LINK`, `UNLINK`, `EXCEPTION`, `IGNORE`, ...). Source and target
  objects are linked in the `links` table (`repo/link`).
* **Reconciliation** (`recon`) compares whole sets: a source phase, then a target phase, in
  parallel tasks. Runs are kept in memory (`openidm.recon.maxcompletedruns`, 100); older runs and
  runs before a restart are known only from the `recon` audit topic.
* **Implicit sync** propagates changes to managed objects as they happen; **liveSync**
  (`system/<name>/<objectClass>?_action=liveSync`, usually scheduled) polls a connector for
  changes. Failure handling is configurable (`syncFailureHandler`: retries, then dead-letter
  queue, log or script); without it a failing change is retried forever and blocks later ones.

## Repository

* `openidm-repo-jdbc` is the only repository implementation. `conf/repo.jdbc.json` maps resource
  paths to tables (`JDBCRepoService.getTableHandler`): an exact key first, otherwise a key that
  is a string prefix of the path (a trailing `/*` is ignored) - when several keys match, which one
  wins is not defined, so avoid overlapping keys. Relationships use the key `relationship`.
  **Generic** tables store objects as JSON with a
  `*properties` table of searchable properties (`genericobjects`, `managedobjects`,
  `relationships`, `schedulerobjects`, `clusterobjects`, ...); **explicit** tables map properties
  to columns (`links`, `internaluser`, `internalrole`, `audit*`, `securitykeys`,
  `uinotification`). `config` always uses `configobjects`.
* Queries are `_queryFilter` expressions translated to SQL (`*SQLQueryFilterVisitor`), or named
  `queryId`s with SQL in `repo.jdbc.json`. A `_queryExpression` parameter runs SQL supplied by
  the caller, and the `command` action runs the named `commands` from `repo.jdbc.json`; access
  rules block both for non-administrators (`disallowQueryExpression()`,
  `disallowCommandAction()`). Vendor-specific handlers are in
  `org.forgerock.openidm.repo.jdbc.impl.vendor`.
* Supported databases: PostgreSQL (preferred for production), MySQL, Oracle, Microsoft SQL
  Server, DB2, and H2. Each has its schema and configuration in
  `openidm-zip/src/main/resources/db/<database>/` (`scripts/openidm.sql` or `openidm.pgsql`,
  `conf/repo.jdbc.json`, `conf/datasource.jdbc-default.json`). The default configuration uses an
  in-memory H2 database, for development and demonstration only.
* Data sources: `conf/datasource.jdbc-<name>.json` (HikariCP pool, JNDI or OSGi data source).
  Flowable keeps its own tables in the same database (`db/<database>/scripts/flowable.*.sql`, or
  created automatically).

## Scripting

* `openidm-script` runs JavaScript (Rhino) and Groovy through the script registry from
  wrensec-commons. Scripts are referenced from configuration as `{"type": "text/javascript",
  "file": "..."}` or with inline `source`, and found along `conf/script.json` `sources`: the
  project's `script/`, the project root, the installation root, and finally
  `bin/defaults/script/` of the installation. Customised
  scripts go to the project's `script/`, not to `bin/defaults/script/`.
* Bindings: `openidm.*` (router calls, `encrypt`, `decrypt`, `hash`, `isEncrypted`, `matches`),
  `identityServer.*` (properties, locations), `logger`, `context`, `request`, and per-hook
  variables (`object`, `oldObject`, `source`, `target`, ...).
* Scripts are **not sandboxed**: Rhino exposes Java classes and Groovy runs as normal code. Whoever
  can change configuration or scripts, or call `script?_action=eval`, can run code in the server.
* Scripts appear in: router filters and authorization, managed object hooks, mappings, custom
  endpoints (`endpoint-*.json`), policies, scheduled jobs, workflow, info services,
  authentication augmentation, connectors (scripted Groovy).

## Workflow

`wrenidm-workflow-flowable` embeds the Flowable engine (`conf/workflow.json`). BPMN definitions
are deployed from the project's `workflow/` directory. The REST API (`workflow/processdefinition`,
`processinstance`, `taskinstance`, with history and dead-letter jobs) is used by the UI; IDM users
and roles are exposed to Flowable as its identity service (`identity/`), and process scripts can
call the router. Who may start a process is set in `conf/process-access.json`; who may see and
complete tasks is checked in `router-authz.js` (`isMyTask`, `canUpdateTask`). Sync policies can
start workflows (`workflow/triggerWorkflowFromSync.js`).

## Self-service

`openidm-selfservice` runs the self-service processes of the commons self-service library:
registration, password reset and forgotten username (`conf/selfservice-<process>.json`, route
`selfservice/<process>`), with stages such as captcha, email validation, knowledge-based
authentication (`conf/selfservice.kba.json`), user details and social registration
(`SocialUserDetailsStage`). The process state travels in signed and encrypted tokens between
stages (stateless storage). `selfservice/user` lets users update their own profile.

## Other services

* **Policy.** `openidm-policy` with `bin/defaults/script/policy.js` (`conf/policy.json`): named
  policies (`required`, `unique`, `minimum-length`, `at-least-X-capitals`, ...) attached to
  properties in `managed.json`, checked by `policyFilter.js` on create and update, by
  `ManagedObjectSet` for the patched properties of a managed object patch, and available as
  `policy/<resource>?_action=validateObject|validateProperty`. The boot property
  `openidm.policy.enforcement.enabled` switches enforcement off.
* **Scheduler.** `openidm-scheduler` (Quartz 1.8; `conf/scheduler.json`, `conf/schedule-<name>.json`):
  jobs invoke a service (`sync` for reconciliation, `provisioner` for liveSync, `script`,
  `taskscanner`) on `scheduler/job`. Only schedules with `"persisted": true` are stored in the
  repository, run once per cluster and fail over; others run in memory on every node.
  `concurrentExecution` defaults to false; `openidm.scheduler.execute.persistent.schedules`
  keeps a node from running persistent schedules.
  The task scanner runs scripts over objects matching a query (e.g. temporal role expiry).
* **Audit.** `openidm-audit` (`conf/audit.json`): topics `access`, `activity`, `authentication`,
  `config`, `recon`, `sync`. Handlers: from the commons audit library JSON, CSV, Syslog, JMS,
  Elasticsearch and Splunk; IDM's own `RepositoryAuditEventHandler` (the `audit*` tables) and
  `RouterAuditEventHandler` (forwards events to a router path). By default `json` and `repo`
  receive all topics and queries go to `repo`. `filterPolicies` remove passwords and session
  tokens from records.
* **Crypto and keys.** `openidm-crypto` encrypts values as `{"$crypto": {...}}` with keys from the
  keystore (default alias `openidm-sym-default`) and hashes with a salted digest (SHA-256 by
  default; SHA-384, SHA-512 and the weak MD5 and SHA-1 can be configured). `openidm-keystore`
  creates missing keys and a self-signed HTTPS certificate on first start; `openidm-security`
  manages the keystore and truststore over REST.
* **Info and health.** `info/ping`, `info/login`, `info/version` (scripts in
  `bin/defaults/script/info/`), `health/os|memory|jdbc|recon`; `HealthService` tracks the server
  state (starting, ready, not ready) from a list of required services.
* **Maintenance.** `maintenance?_action=enable|disable` rejects writes and actions with 503 (reads
  and queries still pass), except on audit and maintenance paths and for the update process. The inherited update mechanism (`maintenance/update`, `update.json`,
  `conf/*.patch`, `db/<database>/scripts/updates/`) dates from OpenIDM 4.5 / 5.0 and is not
  maintained.
* **Felix web console** (`/system/console`) manages the OSGi framework - installing bundles
  included - with credentials from `conf/felix.webconsole.json`.

## User interface

`openidm-ui` contains single-page applications built on `wrensec-ui-commons` (Backbone,
Handlebars, RequireJS, i18next, LESS): `openidm-ui-admin` (`/admin`: managed objects, mappings,
connectors, roles, scheduler, self-service, workflow, settings), `openidm-ui-enduser` (`/`: login,
profile, self-service, tasks) and `openidm-ui-api` (`/api`). `openidm-ui-common` holds shared code
and the only translation file. They are built by npm and gulp from Maven and use the REST API like
any other client; UI-specific endpoints are in `conf/endpoint-*.json` and
`bin/defaults/script/ui/`. Deployments customise a UI without rebuilding it: `ResourceServlet`
serves a file from the context's `extensionDir` (`ui/<context>/extension`) before falling back to
`defaultDir` (`ui/<context>/default`). UI settings: `conf/ui-configuration.json`, `ui-themeconfig.json`,
`ui-dashboard.json`.

## Extension points

Most customisation is configuration and scripts in the project; Java extensions are OSGi bundles
dropped into `bundle/`.

| Mechanism | Purpose |
|---|---|
| `conf/managed.json` and its scripts | data model, hooks, virtual properties, custom actions |
| `conf/sync.json` and its scripts | mappings, transformations, correlation, situation actions |
| `conf/endpoint-<name>.json` | custom REST endpoints in JavaScript or Groovy |
| `conf/router.json` | scripted filters on any request |
| `script/access.js` | authorization rules |
| `conf/policy.json`, `additionalFiles` | custom validation policies |
| `conf/schedule-<name>.json` | scheduled scripts and tasks |
| `workflow/` (BPMN `.xml`, `.bar`) | workflow processes |
| connectors (`connectors/`, scripted Groovy) | integration with external systems |
| OSGi service with `openidm.router.prefix` | a Java resource on the router |
| audit event handlers | audit sinks |

## Build and tests

See [`AGENTS.md`](AGENTS.md) for commands and conventions.
