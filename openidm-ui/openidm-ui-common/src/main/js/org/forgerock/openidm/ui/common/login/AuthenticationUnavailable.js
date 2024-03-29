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
 */

define([
    "org/forgerock/commons/ui/common/main/AbstractView",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/EventManager",
    "org/forgerock/commons/ui/common/main/Configuration"
], function(AbstractView, constants, eventManager, conf) {
    var AuthenticationUnavailableView = AbstractView.extend({
        template: "templates/admin/login/AuthenticationUnavailableTemplate.html",
        baseTemplate: "templates/common/LoginBaseTemplate.html",

        events: {
            "click #loginLink":"login"
        },
        render: function(args, callback) {
            this.parentRender(function(){
                if (callback){
                    callback();
                }
            });
        },
        login: function(e){
            e.preventDefault();

            conf.globalData.authenticationUnavailable = false;
            eventManager.sendEvent(constants.ROUTE_REQUEST, {routeName: "login", args: []});
            location.reload();
        }
    });

    return new AuthenticationUnavailableView();
});
