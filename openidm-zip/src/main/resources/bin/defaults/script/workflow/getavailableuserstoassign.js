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
        "code": 403,
        "message": "Access denied"
    };
}

if (!request.additionalParameters || !request.additionalParameters.taskId) {
    throw {
        "code": 400,
        "message": "Required param: taskId"
    };
}

(function () {
    const getUserByUserName = userName => {
        const params = {
            "_queryId": "for-userName",
            "uid": userName
        };
        const users = openidm.query("managed/user", params);
        return users.result[0];
    };

    const getDisplayName = user => {
        if (user.givenName || user.sn) {
            return [user.givenName, user.sn].join(" ");
        }
        return user.userName ? user.userName : user._id;
    };

    // Fetch the task
    const task = openidm.read(
        `workflow/taskinstance/${encodeURIComponent(request.additionalParameters.taskId)}`
    );
    if (!task) {
        throw "Task Not Found";
    }

    const candidateUsers = {};

    // Collect users from candidate groups
    task.candidates.candidateGroups.forEach(groupName => {
        openidm.query(
            `managed/role/${encodeURIComponent(groupName)}/authzMembers`,
            { "_queryFilter": "true" },
            ["_id", "userName", "givenName", "sn"]
        ).result.forEach(user => {
            candidateUsers[user.userName] = user;
        });
    });


    // Collect candidate users
    task.candidates.candidateUsers.forEach(userName => {
        if (!candidateUsers[userName]) {
            const user = getUserByUserName(userName);
            if (user) {
                candidateUsers[userName] = user;
            }
        }
    });

    // Map candidates to the expected result format
    const result = Object.values(candidateUsers).map(user => ({
        _id: user._id,
        username: user.userName,
        displayName: getDisplayName(user)
    }));

    // Add internal users to the result
    const internalUsers = openidm.query("repo/internal/user", { "_queryFilter": "true" });
    internalUsers.result.forEach(user => {
        result.push({ _id: user._id, username: user.userName, displayName: getDisplayName(user) });
    });

    return result;
}());
