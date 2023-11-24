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
 * Copyright 2014-2016 ForgeRock AS.
 * Portions Copyright 2018-2023 Wren Security.
 */

require.config({
    map: {
        "*" : {
            "Footer": "org/forgerock/openidm/ui/common/components/Footer",
            "ThemeManager": "org/forgerock/openidm/ui/common/util/ThemeManager",
            "LoginView": "org/forgerock/openidm/ui/common/login/LoginView",
            "LoginDialog": "org/forgerock/openidm/ui/common/login/LoginDialog",
            "NavigationFilter" : "org/forgerock/commons/ui/common/components/navigation/filters/RoleFilter",
            // TODO: Remove this when there are no longer any references to the "underscore" dependency
            "underscore": "lodash"
        }
    },
    paths: {
        i18next: "libs/i18next",
        backbone: "libs/backbone",
        "backbone.paginator": "libs/backbone.paginator",
        "backbone-relational": "libs/backbone-relational",
        "backgrid": "libs/backgrid",
        "backgrid-filter": "libs/backgrid-filter",
        "backgrid-paginator": "libs/backgrid-paginator",
        "backgrid-selectall": "libs/backgrid-select-all",
        lodash: "libs/lodash",
        js2form: "libs/js2form",
        form2js: "libs/form2js",
        spin: "libs/spin",
        jquery: "libs/jquery",
        cron: "libs/jquery-cron",
        xdate: "libs/xdate",
        doTimeout: "libs/jquery.ba-dotimeout",
        handlebars: "libs/handlebars",
        "bootstrap-tabdrop": "libs/bootstrap-tabdrop",
        bootstrap: "libs/bootstrap",
        "bootstrap-dialog": "libs/bootstrap-dialog",
        "bootstrap-datetimepicker": "libs/bootstrap-datetimepicker",
        placeholder: "libs/jquery.placeholder",
        selectize : "libs/selectize",
        d3 : "libs/d3",
        moment: "libs/moment",
        "moment-timezone": "libs/moment-timezone-with-data",
        jsonEditor: "libs/jsoneditor",
        "ldapjs-filter": "libs/ldapjs-filter",
        faiconpicker: "libs/fontawesome-iconpicker",
        dimple : "libs/dimple",
        sinon : "libs/sinon",
        dragula : "libs/dragula"
    },

    shim: {
        underscore: {
            exports: "_"
        },
        backbone: {
            deps: ["underscore"],
            exports: "Backbone"
        },
        "backbone.paginator": {
            deps: ["backbone"]
        },
        "backgrid": {
            deps: ["jquery", "underscore", "backbone"],
            exports: "Backgrid"
        },
        "backgrid-filter": {
            deps: ["backgrid"]
        },
        "backgrid-paginator": {
            deps: ["backgrid", "backbone.paginator"]
        },
        "backgrid-selectall": {
            deps: ["backgrid"]
        },
        js2form: {
            exports: "js2form"
        },
        form2js: {
            exports: "form2js"
        },
        spin: {
            exports: "spin"
        },
        jsonEditor: {
            deps: ["handlebars"],
            init: function (Handlebars) {
                window.Handlebars = Handlebars;
                return this.JSONEditor;
            },
            exports: "JSONEditor"
        },
        cron: {
            deps: ["jquery"]
        },
        xdate: {
            exports: "xdate"
        },
        doTimeout: {
            deps: ["jquery"],
            exports: "doTimeout"
        },
        handlebars: {
            exports: "handlebars"
        },
        i18next: {
            deps: ["jquery", "handlebars"],
            exports: "i18n"
        },
        moment: {
            exports: "moment"
        },
        "moment-timezone": {
            deps: ["moment"],
            exports: "moment-timezone"
        },
        selectize: {
            deps: ["jquery"]
        },
        d3: {
            exports: "d3"
        },
        sinon: {
            exports: "sinon"
        },
        dimple: {
            exports: "dimple",
            deps: ["d3"]
        },
        bootstrap: {
            deps: ["jquery"],
            init: function ($) {
                $.fn.popover.Constructor.DEFAULTS.trigger = 'hover focus';
                return this.bootstrap;
            }
        },
        placeholder: {
            deps: ["jquery"]
        },
        'bootstrap-dialog': {
            deps: ["jquery", "underscore","backbone", "bootstrap"]
        },
        'bootstrap-tabdrop': {
            deps: ["jquery", "bootstrap"]
        }
    }
});

require([
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/EventManager",
    "jsonEditor",
    "org/forgerock/openidm/ui/common/util/JSONEditorSetupUtils",

    "org/forgerock/commons/ui/common/main",
    "org/forgerock/openidm/ui/common/main",
    "org/forgerock/openidm/ui/admin/main",
    "config/main",

    "jquery",
    "underscore",
    "backbone",
    "handlebars",
    "i18next",
    "spin",
    "placeholder",
    "selectize"
], function(
        Constants,
        EventManager,
        JSONEditor) {

    EventManager.sendEvent(Constants.EVENT_DEPENDENCIES_LOADED);

    JSONEditor.defaults.options.theme = 'bootstrap3';
    JSONEditor.defaults.options.iconlib = "fontawesome4";
});
