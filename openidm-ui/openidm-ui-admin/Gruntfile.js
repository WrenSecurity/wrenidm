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
    var forgeRockCommonsDirectory = process.env.FORGEROCK_UI_SRC + "/forgerock-ui-commons";

    common(grunt, {
        watchCompositionDirs: [
            forgeRockCommonsDirectory + "/src/main/js",
            forgeRockCommonsDirectory + "/src/main/resources"
        ],
        deployDirectory: "admin/default",
        lessPlugins: [new (require("less-plugin-clean-css"))({})],
        copyLibs: [
            // JS - npm
            { src: "node_modules/backbone-relational/backbone-relational.js", dest: "target/www/libs/backbone-relational-0.9.0-min.js" }, // Not actually minified
            { src: "node_modules/backgrid-filter/backgrid-filter.min.js", dest: "target/www/libs/backgrid-filter-0.3.7-min.js" },
            { src: "node_modules/backgrid-paginator/backgrid-paginator.min.js", dest: "target/www/libs/backgrid-paginator-0.3.5-min.js" },
            { src: "node_modules/backgrid/lib/backgrid.min.js", dest: "target/www/libs/backgrid-0.3.5-min.js" },
            { src: "node_modules/d3/d3.min.js", dest: "target/www/libs/d3-3.5.5-min.js" },
            { src: "node_modules/dragula/dist/dragula.min.js", dest: "target/www/libs/dragula-3.6.7-min.js" },
            { src: "node_modules/moment-timezone/builds/moment-timezone-with-data.min.js", dest: "target/www/libs/moment-timezone-with-data-0.5.4-min.js" },
            { src: "node_modules/qunit/qunit/qunit.js", dest: "target/www/libs/qunit-1.15.0.js" }, // Actually 2.15.0
            { src: "node_modules/sinon/pkg/sinon-1.15.4.js", dest: "target/www/libs/sinon-1.15.4.js" },

            // JS - custom
            { src: "libs/js/bootstrap-datetimepicker-4.14.30-min.js", dest: "target/www/libs/bootstrap-datetimepicker-4.14.30-min.js" },
            { src: "libs/js/bootstrap-tabdrop-1.0.js", dest: "target/www/libs/bootstrap-tabdrop-1.0.js" },
            { src: "libs/js/dimple-2.1.2-min.js", dest: "target/www/libs/dimple-2.1.2-min.js" },
            { src: "libs/js/fontawesome-iconpicker-1.0.0-min.js", dest: "target/www/libs/fontawesome-iconpicker-1.0.0-min.js" },
            { src: "libs/js/jquery-cron-f831f2.js", dest: "target/www/libs/jquery-cron-f831f2.js" },
            { src: "libs/js/jsoneditor-0.7.9-min.js", dest: "target/www/libs/jsoneditor-0.7.9-min.js" },
            { src: "libs/js/ldapjs-filter-2253-min.js", dest: "target/www/libs/ldapjs-filter-2253-min.js" },

            // CSS - npm
            { src: "node_modules/backgrid-filter/backgrid-filter.css", dest: "target/www/css/backgrid-filter-0.3.7-min.css" },
            { src: "node_modules/backgrid-paginator/backgrid-paginator.min.css", dest: "target/www/css/backgrid-paginator-0.3.5-min.css" },
            { src: "node_modules/backgrid/lib/backgrid.min.css", dest: "target/www/css/backgrid-0.3.5-min.css" },
            { src: "node_modules/qunit/qunit/qunit.css", dest: "target/www/css/qunit-1.15.0.css" }, // Actually 2.15.0
            { src: "node_modules/dragula/dist/dragula.min.css", dest: "target/www/css/dragula-3.6.7-min.css" },

            // CSS - custom
            { src: "libs/css/bootstrap-datetimepicker-4.14.30-min.css", dest: "target/www/css/bootstrap-datetimepicker-4.14.30-min.css" },
            { src: "libs/css/fontawesome-iconpicker-1.0.0-min.css", dest: "target/www/css/fontawesome-iconpicker-1.0.0-min.css" },

            // Codemirror
            { src: "node_modules/codemirror/addon/display/placeholder.js", dest: "target/www/libs/codemirror/addon/display/placeholder.js" },
            { src: "node_modules/codemirror/lib/codemirror.js", dest: "target/www/libs/codemirror/lib/codemirror.js" },
            { src: "node_modules/codemirror/mode/groovy/groovy.js", dest: "target/www/libs/codemirror/mode/groovy/groovy.js" },
            { src: "node_modules/codemirror/mode/javascript/javascript.js", dest: "target/www/libs/codemirror/mode/javascript/javascript.js" },
            { src: "node_modules/codemirror/mode/xml/xml.js", dest: "target/www/libs/codemirror/mode/xml/xml.js" },
            { src: "node_modules/codemirror/lib/codemirror.css", dest: "target/www/css/codemirror/codemirror.css" }
        ]
    });
};
