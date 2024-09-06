/*
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
 * Copyright 2012-2015 ForgeRock AS.
 * Portions Copyright 2021-2024 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.identity;

import static org.forgerock.openidm.util.ContextUtil.createInternalContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.flowable.idm.engine.impl.UserQueryImpl;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;

/**
 * Component handling flowable user queries.
 */
public class IdmUserQuery extends UserQueryImpl {

    private static final long serialVersionUID = 1L;

    private final Connection connection;

    public IdmUserQuery(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> executeList(CommandContext commandContext) {
        // Search for users with a specific role
        if (this.groupId != null) {
            List<User> users = new ArrayList<>();
            for (JsonValue user : getRoleMembers(this.groupId)) {
                users.add(new IdmUser(user));
            }
            return users;
        }

        // Perform standard user search
        QueryRequest request = Requests.newQueryRequest("managed/user");
        try {
            applyQueryRequestParams(request);
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }
        applyQueryRequestFields(request);
        Collection<ResourceResponse> users = new ArrayList<>();
        try {
            connection.query(createInternalContext(), request, users);
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
        return users.stream()
                .map(user -> new IdmUser(user.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        return executeList(commandContext).size();
    }

    @Override
    public User executeSingleResult(CommandContext commandContext) {
        if (this.id == null) {
            throw new UnsupportedOperationException("Single result only supported for ID based search");
        }
        List<User> users = executeList(commandContext);
        return !users.isEmpty() ? users.get(0) : null;
    }

    @Override
    public UserQuery userId(String id) {
        if (this.groupId != null) {
            throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both memberOfGroup and userId");
        }
        return super.userId(id);
    }

    @Override
    public UserQuery memberOfGroup(String groupId) {
        if (this.id != null) {
            throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both userId and memberOfGroup");
        }
        return super.memberOfGroup(groupId);
    }

    @Override
    public UserQuery userIds(List<String> ids) {
        throw new UnsupportedOperationException("Filtering by userId is not supported");
    }

    @Override
    public UserQuery userIdIgnoreCase(String id) {
        throw new UnsupportedOperationException("Filtering by user userIdIgnoreCase is not supported");
    }

    @Override
    public UserQuery userFirstName(String firstName) {
        throw new UnsupportedOperationException("Filtering by user firstName is not supported");
    }

    @Override
    public UserQuery userFirstNameLike(String firstNameLike) {
        throw new UnsupportedOperationException("Filtering by user firstNameLike is not supported");
    }

    @Override
    public UserQuery userFirstNameLikeIgnoreCase(String firstNameLikeIgnoreCase) {
        throw new UnsupportedOperationException("Filtering by user firstNameLikeIgnoreCase is not supported");
    }

    @Override
    public UserQuery userLastName(String lastName) {
        throw new UnsupportedOperationException("Filtering by user lastName is not supported");
    }

    @Override
    public UserQuery userLastNameLike(String lastNameLike) {
        throw new UnsupportedOperationException("Filtering by user lastNameLike is not supported");
    }

    @Override
    public UserQuery userLastNameLikeIgnoreCase(String lastNameLikeIgnoreCase) {
        throw new UnsupportedOperationException("Filtering by user lastNameLikeIgnoreCase is not supported");
    }

    @Override
    public UserQuery userFullNameLike(String fullNameLike) {
        throw new UnsupportedOperationException("Filtering by user fullNameLike is not supported");
    }

    @Override
    public UserQuery userFullNameLikeIgnoreCase(String fullNameLikeIgnoreCase) {
        throw new UnsupportedOperationException("Filtering by user fullNameLikeIgnoreCase is not supported");
    }

    @Override
    public UserQuery userDisplayName(String displayName) {
        throw new UnsupportedOperationException("Filtering by user displayName is not supported");
    }

    @Override
    public UserQuery userDisplayNameLike(String displayNameLike) {
        throw new UnsupportedOperationException("Filtering by user displayNameLike is not supported");
    }

    @Override
    public UserQuery userDisplayNameLikeIgnoreCase(String displayNameLikeIgnoreCase) {
        throw new UnsupportedOperationException("Filtering by user displayNameLikeIgnoreCase is not supported");
    }

    @Override
    public UserQuery userEmail(String email) {
        throw new UnsupportedOperationException("Filtering by user email is not supported");
    }

    @Override
    public UserQuery userEmailLike(String emailLike) {
        throw new UnsupportedOperationException("Filtering by user emailLike is not supported");
    }

    @Override
    public UserQuery memberOfGroups(List<String> groupIds) {
        throw new UnsupportedOperationException("Filtering by user groupIds is not supported");
    }

    @Override
    public UserQuery tenantId(String tenantId) {
        throw new UnsupportedOperationException("Filtering by tenantId is not supported");
    }

    private JsonValue getRoleMembers(String roleId) {
        ReadRequest request = Requests.newReadRequest("managed/role", roleId);
        request.addField(
            new JsonPointer(IdmIdentityService.MEMBERS_ATTR, "*", IdmIdentityService.ID_ATTR),
            new JsonPointer(IdmIdentityService.MEMBERS_ATTR, "*", IdmIdentityService.USERNAME_ATTR),
            new JsonPointer(IdmIdentityService.MEMBERS_ATTR, "*", IdmIdentityService.GIVEN_NAME_ATTR),
            new JsonPointer(IdmIdentityService.MEMBERS_ATTR, "*", IdmIdentityService.SURNAME_ATTR),
            new JsonPointer(IdmIdentityService.MEMBERS_ATTR, "*", IdmIdentityService.MAIL_ATTR)
        );
        try {
            ResourceResponse result = connection.read(createInternalContext(), request);
            return result.getContent().get(IdmIdentityService.MEMBERS_ATTR);
        } catch (NotFoundException e) {
            return new JsonValue(null);
        } catch (ResourceException e) {
            throw new RuntimeException("Unable to fetch role members", e);
        }
    }

    private void applyQueryRequestParams(QueryRequest request) throws BadRequestException {
        if (this.id != null) {
            request.setQueryId("for-userName");
            request.setAdditionalParameter("uid", this.id);
        }
    }

    private void applyQueryRequestFields(QueryRequest request) {
        request.addField(
                IdmIdentityService.ID_ATTR,
                IdmIdentityService.USERNAME_ATTR,
                IdmIdentityService.GIVEN_NAME_ATTR,
                IdmIdentityService.SURNAME_ATTR,
                IdmIdentityService.MAIL_ATTR
        );
    }

}
