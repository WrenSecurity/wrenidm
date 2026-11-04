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

# AGENTS.md

Guidance for AI agents (and humans) working in this repository. Start with
[`ARCHITECTURE.md`](ARCHITECTURE.md) for the module map, request handling and the main concepts.

## Build and test

Requirements: JDK 17 or later, Maven 3.6 or later. The UI build downloads Node and npm itself
(`frontend-maven-plugin`) and runs its tests in headless Chromium (Puppeteer).

```sh
mvn -B verify                                  # full build with tests, as in CI (.github/workflows/build-project.yml, Java 17, 21, 25)
mvn -B package -DskipTests                     # faster full build
mvn -B verify -pl openidm-core -am             # one module and what it depends on
mvn -B test -pl openidm-authnfilter -Dtest=OAuthResolverImplTest    # one test class
mvn -B verify -pl openidm-repo-jdbc -Prun-its  # repository tests against real databases (Testcontainers, needs Docker)
docker build -t wrenidm:local .                # Docker image from the sources (Dockerfile)
```

The distribution is `openidm-zip/target/wrenidm-<version>.zip`; unpack it and run
`wrenidm/startup.sh` (admin UI at `http://localhost:8080/admin`, `openidm-admin` /
`openidm-admin`). A new bundle module must be added as a dependency of `openidm-zip/pom.xml`, or it
is not shipped.

* Java tests use TestNG with Mockito and AssertJ. Put them next to the code under `src/test/java`
  in the same package.
* Repository unit tests run on HSQLDB; the vendor integration tests (`*IT.java` in
  `openidm-repo-jdbc`) run only with `-Prun-its`, not in CI.
* The default scripts in `bin/defaults/script/` are tested with Rhino by `ScriptRunnerTest`
  (`openidm-zip`): add a `<name>Test.js` under `openidm-zip/src/test/resources/bin/defaults/script/`
  and list it in `openidm-zip/src/test/resources/testRunner.js`.
* The UI build (`gulp`) runs ESLint and the QUnit tests (`openidm-ui/*/src/test/qunit/`) in every
  Maven build, also with `-DskipTests`.

## Code layout and conventions

* Follow the [Java Style Guide](https://github.com/WrenSecurity/wrensec-docs/wiki/Java-Style-Guide)
  and the [JavaScript Style Guide](https://github.com/WrenSecurity/wrensec-docs/wiki/JavaScript-Style-Guide);
  where they leave a choice open, follow the surrounding code. The
  [Contributor Guidelines](https://github.com/WrenSecurity/wrensec-docs/wiki/Contributor-Guidelines)
  cover the pull request process.
* Changes to existing modules stay in their `org.forgerock.openidm.*` packages. A new module is
  named `wrenidm-<name>` with packages `org.wrensecurity.wrenidm.<name>` (e.g.
  `wrenidm-workflow-flowable`). Configuration PIDs keep the `org.forgerock.openidm.` prefix -
  it is how `conf/<name>.json` maps to a PID.
* Every source file starts with the CDDL header in the format of the Java Style Guide (section 3.1),
  referring to `legal/CDDLv1.1.txt`. New files get `Copyright [year] Wren Security. All rights
  reserved.`. When changing an existing file, add `Portions Copyright [year] Wren Security.` below
  the other copyright lines, or extend the years of an existing one (`2017-2026`); never remove or
  reformat the copyright lines of others (older headers refer to `legal/CDDLv1.0.txt` - leave
  them).
* Components are OSGi Declarative Services with the standard annotations
  (`org.osgi.service.component.annotations`: `@Component`, `@Reference`, `@Activate`, ...), not
  new singletons or static lookups. Name the component with its configuration PID and use
  `configurationPolicy = REQUIRE` when it needs `conf/<name>.json`; read the configuration with
  `JSONEnhancedConfig`. Bundles are built by `maven-bundle-plugin`; check imports and embedded
  dependencies in the bundle's `pom.xml`.
* Talk to other components through the router (`IDMConnectionFactory`, or `openidm.*` in
  scripts) rather than by referencing their Java services, unless the code already does.
* REST endpoints: an OSGi service exposing a CREST `RequestHandler` (or a resource provider)
  with the property `openidm.router.prefix` (`ServerConstants.ROUTER_PREFIX`), e.g.
  `ServerConstants.ROUTER_PREFIX + "=/workflow*"`. A purely scripted endpoint needs only
  `conf/endpoint-<name>.json`. Every new endpoint needs an entry in `script/access.js`, or
  external callers are denied; give it the narrowest roles, methods and actions that work.
* Default configuration goes to `openidm-zip/src/main/resources/conf/`, default scripts to
  `openidm-zip/src/main/resources/bin/defaults/script/`. Do not add defaults to
  `openidm-zip/src/main/resources/script/` (it is the project's place for customised copies)
  except for `access.js`, which lives there.
* Configuration properties holding secrets are encrypted when the bundle declares them: a
  `MetaDataProvider` class named in `src/main/resources/org/forgerock/metadata/bundle.json` (the
  path is fixed; see `openidm-authnfilter`).
* Repository schema: a change to tables or `repo.jdbc.json` must be made for every database under
  `openidm-zip/src/main/resources/db/<database>/` (`h2`, `postgresql`, `mysql`, `oracle`,
  `mssql`, `db2`; `scripts/openidm.sql` or `openidm.pgsql` and `conf/repo.jdbc.json`), in the
  default `conf/repo.jdbc.json` (H2), and in the test schemas
  `openidm-repo-jdbc/src/test/resources/`. There is no automatic upgrade of existing
  installations; required manual steps go to the Upgrade Guide (see [Documentation](#documentation)).
* Managed object behaviour that is configuration-driven belongs to `managed.json` and scripts in
  `bin/defaults/script/` (e.g. `roles/`); keep new features opt-in through configuration where
  existing deployments would otherwise change behaviour.
* Dependencies: the build verifies PGP signatures of all dependencies against the Wren Security
  key whitelist (`pgpVerifyKeysVersion` in `pom.xml`). A new dependency needs its signing key in
  the whitelist first - a separate change to `wrensec-pgp-whitelist`, then a version bump here.
* User-facing UI text goes to `openidm-ui/openidm-ui-common/src/main/resources/locales/en/translation.json`
  (shared by the admin and end-user UI). Escape user-controlled values in Handlebars templates and
  dialogs.
* XML parsing of external input must disable external entities and DTDs. LDAP filters and DNs,
  SQL and query filters built from input must be escaped or parameterised (use `_queryFilter`
  and the query filter visitors, not string concatenation).
* Outbound HTTPS validates the server certificate and host name; do not add trust-all
  `TrustManager` or `HostnameVerifier` implementations.

## Documentation

User documentation lives in a separate repository,
[WrenSecurity/wrenidm-docs](https://github.com/WrenSecurity/wrenidm-docs) (Antora / AsciiDoc), and
samples in [WrenSecurity/wrenidm-cookbook](https://github.com/WrenSecurity/wrenidm-cookbook).

* A change to configuration, REST API, script bindings or behaviour visible to users needs a
  matching change of the component page in `modules/components/pages/<component>.adoc` - a
  separate pull request there. New component pages start with the block listing the OSGi service
  class, persistent identifier, configuration file and router mapping (see its `README.md`).
* A change that requires action on existing installations - configuration, `launcher.json`,
  database schema, renamed bundles, workflow definitions - needs a step in the Upgrade Guide,
  `modules/deployment/pages/upgrade.adoc`, under the version it is released in.
* The docs describe the code; when they disagree, check the code and fix whichever is wrong.
* Do not copy text, examples or samples from third-party documentation under licenses that
  forbid derivative works (such as CC BY-NC-ND) into code, comments, configuration or the docs,
  and do not link to it. This includes the samples removed from this repository. Write from the
  code in this repository, and present Wren:IDM only as a Wren Security project. Inherited names
  (`org.forgerock.*` packages and PIDs, `openidm-*` artifacts) stay as they are.

## Security

* Report suspected vulnerabilities privately as described in the Wren Security
  [security policy](https://github.com/WrenSecurity/.github/blob/main/SECURITY.md) - not in public
  issues or pull requests.
* Authorization of external requests is done only by `router-authz.js` with the rules in
  `script/access.js` (`ARCHITECTURE.md`, Authorization); internal calls are not authorized. When an
  endpoint acts on resources named in the request - patch `from` paths, process definition IDs,
  relationship references, query filters - authorize those resources as well, not just the path
  called (recent fixes in `git log` are examples). Rules for non-administrators must not allow
  `_queryExpression` (raw SQL) or repository commands (`disallowQueryExpression()`,
  `disallowCommandAction()`).
* Code that runs a request on behalf of a user should use the external connection
  (`getExternalConnection()`) so the user's authorization applies, not an internal one.
* Scripts are not sandboxed (`ARCHITECTURE.md`, Scripting): treat changes to who may change
  configuration, scripts, workflows or call `script` as security-relevant.
* Self-service endpoints are reachable anonymously (`openidm-reg`): avoid revealing whether users
  exist, and do not trust values the client sends back between stages.
* The defaults (`openidm-admin` / `openidm-admin`, `changeit` keystore passwords, the web console
  `admin` / `admin`, in-memory H2) are for development only; do not add new default credentials.
