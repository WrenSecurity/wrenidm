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
 * Copyright 2011-2016 ForgeRock AS.
 * Portions Copyright 2018-2023 Wren Security.
 */

require.config({
    map: {
        "*" : {
            "Footer": "org/forgerock/openidm/ui/common/components/Footer",
            "ThemeManager": "org/forgerock/openidm/ui/common/util/ThemeManager",
            "UserProfileView": "org/forgerock/commons/ui/user/profile/UserProfileView",
            "LoginView": "org/forgerock/openidm/ui/common/login/LoginView",
            "LoginDialog": "org/forgerock/openidm/ui/common/login/LoginDialog",
            "RegisterView": "org/forgerock/openidm/ui/user/anonymousProcess/SelfRegistrationView",
            "ForgotUsernameView": "org/forgerock/commons/ui/user/anonymousProcess/ForgotUsernameView",
            "PasswordResetView": "org/forgerock/openidm/ui/user/anonymousProcess/PasswordResetView",
            "KBADelegate": "org/forgerock/commons/ui/user/delegates/KBADelegate",
            "NavigationFilter" : "org/forgerock/commons/ui/common/components/navigation/filters/RoleFilter"
        }
    },
    paths: {
        i18next: "libs/i18next",
        backbone: "libs/backbone",
        underscore : "libs/underscore",
        lodash: "libs/lodash",
        js2form: "libs/js2form",
        form2js: "libs/form2js",
        spin: "libs/spin",
        jquery: "libs/jquery",
        xdate: "libs/xdate",
        doTimeout: "libs/jquery.ba-dotimeout",
        handlebars: "libs/handlebars",
        bootstrap: "libs/bootstrap",
        "bootstrap-dialog": "libs/bootstrap-dialog",
        placeholder: "libs/jquery.placeholder",
        moment: "libs/moment",
        contentflow: "libs/contentflow",
        selectize : "libs/selectize",
        "backgrid": "libs/backgrid",
        "backgrid-filter": "libs/backgrid-filter",
        "backgrid-paginator": "libs/backgrid-paginator",
        faiconpicker: "libs/fontawesome-iconpicker",
        d3 : "libs/d3",
        dimple : "libs/dimple",
        jsonEditor: "libs/jsoneditor",
        dragula : "libs/dragula"
    },

    shim: {
        backbone: {
            deps: ["underscore"],
            exports: "Backbone"
        },
        js2form: {
            exports: "js2form"
        },
        form2js: {
            exports: "form2js"
        },
        jsonEditor: {
            exports: "JSONEditor"
        },
        contentflow: {
            exports: "contentflow"
        },
        spin: {
            exports: "spin"
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
        moment: {
            exports: "moment"
        },
        dimple: {
            exports: "dimple",
            deps: ["d3"]
        },
        d3: {
            exports: "d3"
        },
        selectize: {
            deps: ["jquery"]
        },
        bootstrap: {
            deps: ["jquery"]
        },
        'bootstrap-dialog': {
            deps: ["jquery", "underscore","backbone", "bootstrap"]
        },
        placeholder: {
            deps: ["jquery"]
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
        }
    }
});

require([
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/EventManager",

    "org/forgerock/commons/ui/common/main",
    "org/forgerock/openidm/ui/common/main",
    "config/main",

    "jquery",
    "underscore",
    "lodash",
    "backbone",
    "handlebars",
    "i18next",
    "spin",
    "placeholder"
], function (Constants, EventManager) {
    EventManager.sendEvent(Constants.EVENT_DEPENDENCIES_LOADED);
});
