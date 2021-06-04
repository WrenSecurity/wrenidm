/**
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
 */

var common = require('../Gruntfile-common');

module.exports = function(grunt) {
    var forgeRockCommonsDirectory = process.env.FORGEROCK_UI_SRC + "/forgerock-ui-commons",
        forgeRockUserDirectory = process.env.FORGEROCK_UI_SRC + "/forgerock-ui-user";

    common(grunt, {
        watchCompositionDirs: [
            forgeRockCommonsDirectory + "/src/main/js",
            forgeRockCommonsDirectory + "/src/main/resources",
            forgeRockUserDirectory + "/src/main/js",
            forgeRockUserDirectory + "/src/main/resources"
        ],
        deployDirectory: "selfservice/default",
        eslintFormatter: require.resolve("eslint-formatter-warning-summary"),
        lessPlugins: [new (require("less-plugin-clean-css"))({})],
        copyLibs: [
            // JS - npm
            { src: "node_modules/backgrid/lib/backgrid.min.js", dest: "target/www/libs/backgrid-0.3.5-min.js" },
            { src: "node_modules/backgrid-filter/backgrid-filter.min.js", dest: "target/www/libs/backgrid-filter-0.3.7-min.js" },
            { src: "node_modules/backgrid-paginator/backgrid-paginator.min.js", dest: "target/www/libs/backgrid-paginator-0.3.5-min.js" },
            { src: "node_modules/d3/d3.min.js", dest: "target/www/libs/d3-3.5.5-min.js" },
            { src: "node_modules/qunit/qunit/qunit.js", dest: "target/www/libs/qunit-1.15.0.js" }, // Actually 2.15.0
            { src: "node_modules/sinon/pkg/sinon-1.15.4.js", dest: "target/www/libs/sinon-1.15.4.js" },

            // JS - custom
            { src: "libs/js/contentflow.js", dest: "target/www/libs/contentflow.js" },
            { src: "libs/js/dimple-2.1.2-min.js", dest: "target/www/libs/dimple-2.1.2-min.js" },
            { src: "libs/js/fontawesome-iconpicker-1.0.0-min.js", dest: "target/www/libs/fontawesome-iconpicker-1.0.0-min.js" },
            { src: "libs/js/jquery-ui-1.11.1-min.js", dest: "target/www/libs/jquery-ui-1.11.1-min.js" },
            { src: "libs/js/jsoneditor-0.7.9-min.js", dest: "target/www/libs/jsoneditor-0.7.9-min.js" },

            // CSS - npm
            { src: "node_modules/backgrid/lib/backgrid.min.css", dest: "target/www/css/backgrid-0.3.5-min.css" },
            { src: "node_modules/backgrid-filter/backgrid-filter.css", dest: "target/www/css/backgrid-filter-0.3.7-min.css" },
            { src: "node_modules/backgrid-paginator/backgrid-paginator.min.css", dest: "target/www/css/backgrid-paginator-0.3.5-min.css" },
            { src: "node_modules/qunit/qunit/qunit.css", dest: "target/www/css/qunit-1.15.0.css" }, // Actually 2.15.0

            // CSS - custom
            { src: "libs/css/fontawesome-iconpicker-1.0.0-min.css", dest: "target/www/css/fontawesome-iconpicker-1.0.0-min.css" },
        ],
    });
};
