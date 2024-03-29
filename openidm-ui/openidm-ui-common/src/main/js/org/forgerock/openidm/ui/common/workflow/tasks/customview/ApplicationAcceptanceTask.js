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
    "js2form",
    "org/forgerock/openidm/ui/common/workflow/tasks/AbstractTaskForm",
    "org/forgerock/commons/ui/common/util/DateUtil",
    "org/forgerock/commons/ui/common/main/Configuration"
], function(js2form, AbstractTaskForm, DateUtil, conf) {
    var ApplicationAcceptanceTask = AbstractTaskForm.extend({
        template: "templates/workflow/tasks/customview/ApplicationAcceptanceTemplate.html",

        reloadData: function() {
            var self = this;
            js2form(this.$el[0], this.task);
            this.$el.find("input[name=taskName]").val(this.task.name);
            this.$el.find("input[name=createTime]").val(DateUtil.formatDate(this.task.createTime));

            if (this.$el.find("input[name=assignee]").val() === "null") {
                this.$el.find("input[name=assignee]").val("");
            }

            this.$el.find("input[name=userData]").val(this.task.variables.user.givenName + " " + this.task.variables.user.familyName);
            this.$el.find("input[name=requestedApplicationName]").val(this.task.variables.application.name);
        }
    });

    return new ApplicationAcceptanceTask();
});
