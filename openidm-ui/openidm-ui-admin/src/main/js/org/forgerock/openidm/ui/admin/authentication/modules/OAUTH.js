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
 * Portions Copyright 2023-2025 Wren Security.
 */

define([
    "jquery",
    "lodash",
    "form2js",
    "org/forgerock/openidm/ui/admin/authentication/AuthenticationAbstractView",
    "org/forgerock/openidm/ui/admin/util/CodeMirror"
], function($, _,
        Form2js,
        AuthenticationAbstractView,
        CodeMirror) {

    var OAuthView = AuthenticationAbstractView.extend({
        template: "templates/admin/authentication/modules/OAUTH.html",

        knownProperties: AuthenticationAbstractView.prototype.knownProperties.concat([
            "authTokenHeader",
            "authResolverHeader",
            "resolvers"
        ]),

        model: {
            defaultIcon: "<button class=\"btn btn-lg btn-default btn-block btn-social-provider\"><img src=\"images/forgerock_logo.png\">Sign in with OpenAM</button>"
        },

        getConfig: function () {
            var config = AuthenticationAbstractView.prototype.getConfig.call(this);

            if (this.model.iconCode) {
                config.properties.resolvers[0].icon = this.model.iconCode.getValue() || "";
            }

            if (_.has(config, "properties.resolvers[0].client_id")) {
                config.properties.resolvers[0].client_id = config.properties.resolvers[0].client_id.trim();
            }

            if (_.has(config, "properties.resolvers[0].client_secret")) {
                config.properties.resolvers[0].client_secret = config.properties.resolvers[0].client_secret.trim();
            } else if (_.has(this.data.config, "properties.resolvers[0].client_secret")) {
                // client_secret will be omitted from the config when it is left empty in the form
                // this will restore the previous value for it, if there had been one
                config.properties.resolvers[0].client_secret = this.data.config.properties.resolvers[0].client_secret;
            }

            return config;
        },

        render: function (args) {
            this.data = _.cloneDeep(args);
            if (!_.has(this.data, "config.properties.resolvers") || !this.data.config.properties.resolvers.length) {
                this.data.config.properties.resolvers = [{
                    name: "OAUTH"
                }];
            }
            this.data.userOrGroupValue = "userRoles";
            this.data.config.properties.resolvers = this.data.config.properties.resolvers || [{}];
            this.data.userOrGroupOptions = _.cloneDeep(AuthenticationAbstractView.prototype.userOrGroupOptions);
            this.data.customProperties = this.getCustomPropertiesList(this.knownProperties, this.data.config.properties || {});
            this.data.userOrGroupDefault = this.getUserOrGroupDefault(this.data.config || {});

            this.parentRender(() => {
                this.postRenderComponents({
                    "customProperties": this.data.customProperties,
                    "name": this.data.config.name,
                    "augmentSecurityContext": this.data.config.properties.augmentSecurityContext || {},
                    "userOrGroup": this.data.userOrGroupDefault
                });

                const iconCodeValue = this.data.config.properties.resolvers[0].icon ?? this.model.defaultIcon;
                this.model.iconCode = CodeMirror(this.$el.find(".button-html")[0], {
                    mode: "xml",
                    value: iconCodeValue,
                    lineWrapping: true
                });

            });
        }

    });

    return new OAuthView();
});
