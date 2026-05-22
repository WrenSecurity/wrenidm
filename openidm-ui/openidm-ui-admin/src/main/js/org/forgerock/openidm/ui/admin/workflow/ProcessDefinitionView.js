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
 * Portions Copyright 2023-2026 Wren Security.
 */

define([
    "lodash",
    "org/forgerock/commons/ui/common/main/AbstractView",
    "org/forgerock/commons/ui/common/main/EventManager",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/util/UIUtils",
    "org/forgerock/commons/ui/common/main/AbstractModel",
    "org/forgerock/openidm/ui/admin/util/WorkflowUtils"
], function(_, AbstractView, eventManager, constants, UIUtils, AbstractModel, WorkflowUtils) {
    var ProcessModel = AbstractModel.extend({ url: "/openidm/workflow/processdefinition" }),
        ProcessDefinitionView = AbstractView.extend({
            template: "templates/admin/workflow/ProcessDefinitionViewTemplate.html",

            events: {

            },
            render: function(args, callback) {
                this.model = new ProcessModel();
                this.model.id = args[0];

                this.model.fetch({ data: { _fields: ",diagram" }}).then(_.bind(function () {
                    this.data.processDefinition = this.model.toJSON();
                    this.data.diagramDataUri = "data:image/png;base64," + this.model.get("/diagram");

                    this.parentRender(_.bind(function(){
                        if (callback) {
                            callback();
                        }
                    }, this));
                }, this));
            }
        });

    return new ProcessDefinitionView();
});
