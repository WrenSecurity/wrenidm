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
 * Portions Copyright 2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.identity;

import static org.forgerock.openidm.util.ContextUtil.createInternalContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.engine.impl.GroupQueryImpl;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.util.query.QueryFilter;

/**
 * Component handling flowable group queries.
 */
public class IdmGroupQuery extends GroupQueryImpl {

    private static final long serialVersionUID = 1L;

    private final Connection connection;

    public IdmGroupQuery(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Group> executeList(CommandContext commandContext) {
        // Search for roles of a specific user
        if (this.userId != null) {
            List<Group> groups = new ArrayList<>();
            for (JsonValue role : getUserRoles(this.userId)) {
                groups.add(new IdmGroup(role));
            }
            return groups;
        }

        // Perform standard role search
        QueryRequest request = Requests.newQueryRequest("managed/role");
        applyQueryFilter(request);
        applyQueryFields(request);
        Collection<ResourceResponse> roles = new ArrayList<>();
        try {
            connection.query(createInternalContext(), request, roles);
        } catch (ResourceException e) {
            throw new RuntimeException("Error during group resolution", e);
        }
        return roles.stream()
                .map(role -> new IdmGroup(role.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        return executeList(commandContext).size();
    }

    @Override
    public Group executeSingleResult(CommandContext commandContext) {
        if (this.id == null) {
            throw new UnsupportedOperationException("Single result only supported for ID based search");
        }
        List<Group> groups = executeList(commandContext);
        return !groups.isEmpty() ? groups.get(0) : null;
    }

    @Override
    public GroupQuery groupId(String id) {
        if (this.userId != null) {
            throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both groupMember and groupId");
        }
        return super.groupId(id);
    }

    @Override
    public GroupQuery groupIds(List<String> ids) {
        if (this.userId != null) {
            throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both groupMember and groupIds");
        }
        return super.groupIds(ids);
    }

    @Override
    public GroupQuery groupName(String name) {
        throw new UnsupportedOperationException("Filtering by group name is not supported");
    }

    @Override
    public GroupQuery groupNameLike(String nameLike) {
        throw new UnsupportedOperationException("Filtering by group nameLike is not supported");
    }

    @Override
    public GroupQuery groupNameLikeIgnoreCase(String nameLikeIgnoreCase) {
        throw new UnsupportedOperationException("Filtering by group nameLikeIgnoreCase is not supported");
    }

    @Override
    public GroupQuery groupType(String type) {
        throw new UnsupportedOperationException("Filtering by group type is not supported");
    }

    @Override
    public GroupQuery groupMember(String userId) {
        if (this.id != null) {
            throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both groupId and groupMember");
        }
        if (this.ids != null) {
            throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both groupIds and groupMember");
        }
        return super.groupMember(userId);
    }

    @Override
    public GroupQuery groupMembers(List<String> userIds) {
        throw new UnsupportedOperationException("Filtering by group members is not supported");
    }

    private JsonValue getUserRoles(String userName) {
        QueryRequest request = Requests.newQueryRequest("managed/user");
        request.setQueryId("for-userName");
        try {
            request.setAdditionalParameter("uid", this.userId);
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }
        request.addField(
            new JsonPointer(IdmIdentityService.ROLES_ATTR, "*", IdmIdentityService.ID_ATTR),
            new JsonPointer(IdmIdentityService.ROLES_ATTR, "*", IdmIdentityService.NAME_ATTR)
        );
        List<ResourceResponse> users = new ArrayList<>();
        try {
            connection.query(createInternalContext(), request, users);
        } catch (ResourceException e) {
            throw new RuntimeException("Unable to fetch user roles", e);
        }
        return !users.isEmpty()
                ? new IdmUser(users.get(0).getContent().get(IdmIdentityService.ROLES_ATTR))
                : new JsonValue(null);
    }

    private void applyQueryFilter(QueryRequest request) {
        Collection<QueryFilter<JsonPointer>> subFilters = new ArrayList<QueryFilter<JsonPointer>>();
        if (this.id != null) {
            subFilters.add(QueryFilter.equalTo(new JsonPointer(IdmIdentityService.ID_ATTR), this.id));
        }
        if (this.ids != null) {
            Collection<QueryFilter<JsonPointer>> idSubfilters = ids.stream()
                .map(id -> QueryFilter.equalTo(new JsonPointer(IdmIdentityService.ID_ATTR), id))
                .collect(Collectors.toList());
            subFilters.add(QueryFilter.or(idSubfilters));
        }
        request.setQueryFilter(subFilters.size() > 0 ? QueryFilter.and(subFilters) : QueryFilter.alwaysTrue());
    }

    private void applyQueryFields(QueryRequest request) {
        request.addField(IdmIdentityService.ID_ATTR, IdmIdentityService.NAME_ATTR);
    }

}
