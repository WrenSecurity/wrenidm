/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock AS. All Rights Reserved
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

if (!request.additionalParameters || !request.additionalParameters.taskId) {
    throw "Required param: taskId";
}

(function () {
    var getUserByUserName = function(userName) {
        var params = {
                "_queryId": "for-userName",
                "uid": userName
            },
            result = openidm.query("managed/user", params),
            user = null;

        if (result.result && result.result.length === 1) {
            user = result.result[0];
        }
        return user;
    },
    getDisplayableOf = function(user) {
        if (user.givenName || user.sn) {
            return user.givenName + " " + user.sn;
        } else {
            return user.userName ? user.userName : user._id;
        }
    },
    usersToAdd = {},
    availableUsersToAssign,
    candidateUsers = [],
    candidateUser,
    candidateGroups = [],
    result,
    user,
    username,
    task = openidm.read("workflow/taskinstance/" + request.additionalParameters.taskId);

    if (!task) {
        throw "Task Not Found";
    }

    // Collect candidate users
    candidateUsers = task.candidates.candidateUsers;
    candidateUsers.forEach(user => {
        usersToAdd[candidateUser] = user;
    });

    // Collect users from candidate groups
    candidateGroups = task.candidates.candidateGroups;
    candidateGroups.forEach(group => {
        result = openidm.query("managed/role/" + group + "/members", { "_queryFilter": "true" }, ["_id", "userName", "givenName", "sn"]);
        if (result.result) {
            result.result.forEach(user => {
                usersToAdd[user.userName] = user;
            });
        }
    });

    availableUsersToAssign = [];
    for (username in usersToAdd) {
        user = getUserByUserName(username);
        if (user) {
            availableUsersToAssign.push({ _id: user._id, username: username, displayableName: getDisplayableOf(user) });
        }
    }

    // Add internal users
    internalUsers = openidm.query("repo/internal/user", { "_queryFilter": "true" });
    if (internalUsers.result) {
        internalUsers.result.forEach(user => {
            availableUsersToAssign.push({ _id: user._id, username: user.userName, displayableName: getDisplayableOf(user) });
        });
    }

    return availableUsersToAssign;
}());
