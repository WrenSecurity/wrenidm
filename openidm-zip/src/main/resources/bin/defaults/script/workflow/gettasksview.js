/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2024 Wren Security
 */

if (request.method !== "query") {
    throw {
        "code" : 403,
        "message" : "Access denied"
    };
}

(function () {
    var processInstances = {},
        taskDefinitions = {},
        usersWhoCanBeAssignedToTask = {},
        getProcessInstance = function(processInstanceId) {
            var processInstance;
            if (!processInstances[processInstanceId]) {
                processInstance = openidm.read("workflow/processinstance/" + processInstanceId);
                processInstances[processInstanceId] = processInstance;
            }
            return processInstances[processInstanceId];
        },

        getTaskDefinition = function(processDefinitionId, taskDefinitionKey) {
            var taskDefinition;
            var key = processDefinitionId + "|" + taskDefinitionKey;
            if (!taskDefinitions[key]) {
                taskDefinition = openidm.read("workflow/processdefinition/" + processDefinitionId + "/taskdefinition/" + taskDefinitionKey)
                taskDefinitions[key] = taskDefinition;
            }
            return taskDefinitions[key];
        },
        getUsersWhoCanBeAssignedToTask = function(taskId) {
            var usersWhoCanBeAssignedToTaskQueryParams = {
                    "_queryId": "getavailableuserstoassign",
                    "taskId": taskId
                },
                isTaskManager = false,
                i,
                usersWhoCanBeAssignedToTaskResult = { users : [] };

            if (!usersWhoCanBeAssignedToTask[taskId]) {

                for(i = 0; i < context.security.authorization.roles.length; i++) {
                    if(context.security.authorization.roles[i] === 'openidm-tasks-manager') {
                        isTaskManager = true;
                        break;
                    }
                }

                if(isTaskManager) {
                    usersWhoCanBeAssignedToTaskResult = openidm.query("endpoint/getavailableuserstoassign", usersWhoCanBeAssignedToTaskQueryParams);
                }
                usersWhoCanBeAssignedToTask[taskId] = usersWhoCanBeAssignedToTaskResult;
            }
            return usersWhoCanBeAssignedToTask[taskId];
        },

        userName = context.security.authenticationId,
        tasks,
        taskId,
        task,
        userAssignedTasksQueryParams,
        userCandidateTasksQueryParams,
        taskDefinition,
        taskInstance,
        processInstance,
        i,
        view = {};

    if (request.additionalParameters.viewType === 'assignee') {
        userAssignedTasksQueryParams = {
            "_queryId": "filtered-query",
            "assignee": userName
        };
        tasks = openidm.query("workflow/taskinstance", userAssignedTasksQueryParams).result;
    } else {
        userCandidateTasksQueryParams = {
          "_queryId": "filtered-query",
          "taskCandidateUser": userName
        };
        tasks = openidm.query("workflow/taskinstance", userCandidateTasksQueryParams).result;
    }

    //building view

    for (i = 0; i < tasks.length; i++) {
        taskId = tasks[i]._id;
        task = openidm.read("workflow/taskinstance/"+taskId);

        if (!view[task._id]) {
            view[task._id] = {name : task.name, tasks : []};
        }
        view[task._id].tasks.push(task);
    }

    for (taskDefinition in view) {
        if (view.hasOwnProperty(taskDefinition)) {
            for (i = 0; i < view[taskDefinition].tasks.length; i++) {
                taskInstance = view[taskDefinition].tasks[i];
                processInstance = getProcessInstance(taskInstance.processInstanceId);
                view[taskDefinition].tasks[i].businessKey = processInstance.businessKey;
                view[taskDefinition].tasks[i].startTime = processInstance.startTime;
                view[taskDefinition].tasks[i].startUserId = processInstance.startUserId;
                view[taskDefinition].tasks[i].startUserDisplayable = userName;
                view[taskDefinition].tasks[i].processDefinitionId = processInstance.processDefinitionId;
                view[taskDefinition].tasks[i].taskDefinition = getTaskDefinition(taskInstance.processDefinitionId, taskInstance.taskDefinitionKey);
                view[taskDefinition].tasks[i].usersToAssign = getUsersWhoCanBeAssignedToTask(taskInstance._id);
            }
        }
    }

    //return value
    return [view];

}());
