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
 * Copyright 2023 Wren Security.
 */
const {
    useBuildScripts,
    useEslint,
    useLocalResources,
    useModuleResources,
    useLessStyles,
    useBuildRequire
} = require("@wrensecurity/commons-ui-build");
const express = require('express');
const gulp = require("gulp");
const { runQunitPuppeteer, printResultSummary } = require("node-qunit-puppeteer");
const { join } = require("path");

const TARGET_PATH = "target/www";
const TESTS_PATH = "target/test";

const MODULE_RESOURCES = {
    "backbone-relational/backbone-relational.js": "libs/backbone-relational.js",
    "backgrid-filter/backgrid-filter.css": "css/backgrid-filter.css",
    "backgrid-filter/backgrid-filter.min.js": "libs/backgrid-filter.js",
    "backgrid-paginator/backgrid-paginator.min.css": "css/backgrid-paginator.css",
    "backgrid-paginator/backgrid-paginator.min.js": "libs/backgrid-paginator.js",
    "backgrid/lib/backgrid.min.css": "css/backgrid.css",
    "backgrid/lib/backgrid.min.js": "libs/backgrid.js",
    "d3/d3.min.js": "libs/d3.js",
    "dragula/dist/dragula.min.css": "css/dragula.css",
    "dragula/dist/dragula.min.js": "libs/dragula.js",
    "moment-timezone/builds/moment-timezone-with-data.min.js": "libs/moment-timezone-with-data.js",
    "qunit/qunit/qunit.css": "css/qunit.css",
    "qunit/qunit/qunit.js": "libs/qunit.js",
    //~ Code mirror resources
    "codemirror/addon/display/placeholder.js": "libs/codemirror/addon/display/placeholder.js",
    "codemirror/lib/codemirror.css": "css/codemirror/codemirror.css",
    "codemirror/lib/codemirror.js": "libs/codemirror/lib/codemirror.js",
    "codemirror/mode/groovy/groovy.js": "libs/codemirror/mode/groovy/groovy.js",
    "codemirror/mode/javascript/javascript.js": "libs/codemirror/mode/javascript/javascript.js",
    "codemirror/mode/xml/xml.js": "libs/codemirror/mode/xml/xml.js",
};

const LOCAL_RESOURCES = {
    "css/*": "css",
    "js/*": "libs"
};

const TEST_RESOURCES = {
    "src/test/qunit/**": "",
    "../openidm-ui-common/src/test/qunit/tests/**": "",
    "../openidm-ui-common/src/test/qunit/index.html": "",
    "../openidm-ui-common/src/test/qunit/testRunner.js": ""
};

gulp.task("eslint", useEslint({ src: "src/{main/js,test/qunit}/**/*.js" }));

gulp.task("build:assets", useLocalResources({ "src/main/resources/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:scripts", useLocalResources({ "src/main/js/**/*.js": "" }, { dest: TARGET_PATH }));

gulp.task("build:compose", useLocalResources({ "target/ui-compose/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:libs", async () => {
    await useModuleResources(MODULE_RESOURCES, { path: __filename, dest: TARGET_PATH })();
    await useLocalResources(LOCAL_RESOURCES, { base: "libs", dest: TARGET_PATH })();
});

gulp.task("build:styles", useLessStyles({
    "target/www/css/structure.less": "css/structure.css",
    "target/www/css/theme.less": "css/theme.css"
}, { base: join(TARGET_PATH, "css"), dest: TARGET_PATH }));

gulp.task("build:bundle", useBuildRequire({
    base: TARGET_PATH,
    src: "src/main/js/main.js",
    dest: join(TARGET_PATH, "main.js"),
    exclude: [
        // Excluded from optimization so that the UI can be customized without having to repackage it.
        "config/AppConfiguration",
        // Exclude mock project dependencies to create a more representative bundle.
        "sinon"
    ]
}));

gulp.task("test:scripts", useLocalResources(TEST_RESOURCES, { dest: TESTS_PATH }));

gulp.task("test:sinon", useBuildScripts({
    src: require.resolve("sinon/pkg/sinon.js"),
    dest: join(TARGET_PATH, "libs"),
    plugins: []
}));

gulp.task("test:qunit", async () => {
    const server = express();
    server.use(express.static(join(__dirname, 'target')));
    const listener = server.listen(0);
    let result;
    try {
        result = await runQunitPuppeteer({
            targetUrl: `http://localhost:${listener.address().port}/test/index.html`,
            puppeteerArgs: [
                "--allow-file-access-from-files",
                "--no-sandbox"
            ]
        });
    } finally {
        listener.close();
    }
    printResultSummary(result, console);
});

gulp.task("build", gulp.series(
    gulp.parallel(
        "build:assets",
        "build:scripts",
        "build:compose",
        "build:libs",
        "test:sinon"
    ),
    gulp.parallel(
        "build:styles",
        "build:bundle"
    )
));

gulp.task("test", gulp.series(
    "test:scripts",
    "test:qunit"
));

gulp.task("watch", () => {
    gulp.watch("src/main/js/**", gulp.parallel("build:scripts"));
});

gulp.task("default", gulp.series("eslint", "build", "test"));
