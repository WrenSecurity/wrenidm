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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.engine.impl.GroupQueryImpl;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.openidm.util.ContextUtil;
import org.forgerock.services.context.Context;
import org.forgerock.util.query.QueryFilter;

/**
 * Component handling flowable group queries.
 */
public class IdmGroupQuery extends GroupQueryImpl {

    private static final long serialVersionUID = 1L;

    private static final Context context = ContextUtil.createInternalContext();

    private final Connection connection;

    public IdmGroupQuery(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Group> executeList(CommandContext commandContext) {
        QueryRequest request = Requests.newQueryRequest("managed/role");
        applyQueryRequestFilter(request);
        applyQueryRequestFields(request);
        Collection<ResourceResponse> roles = new ArrayList<>();
        try {
            connection.query(context, request, roles);
            return roles.stream()
                    .filter(role -> filterRolesByMember(role))
                    .map(role -> new IdmGroup(role.getContent()))
                    .collect(Collectors.toList());
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        QueryRequest request = Requests.newQueryRequest("managed/role");
        applyQueryRequestFilter(request);
        applyQueryRequestFields(request);
        Collection<ResourceResponse> roles = new ArrayList<>();
        try {
            connection.query(context, request, roles);
            return roles.stream()
                    .filter(role -> filterRolesByMember(role))
                    .collect(Collectors.toList())
                    .size();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group executeSingleResult(CommandContext commandContext) {
        try {
            ReadRequest request = Requests.newReadRequest("managed/role", this.id);
            request.addField(IdmIdentityService.ID_ATTR, IdmIdentityService.NAME_ATTR);
            ResourceResponse role = connection.read(context, request);
            return new IdmGroup(role.getContent());
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GroupQuery groupName(String name) {
        throw new UnsupportedOperationException("Filtering by group name is not supported.");
    }

    @Override
    public GroupQuery groupNameLike(String nameLike) {
        throw new UnsupportedOperationException("Filtering by group nameLike is not supported.");
    }

    @Override
    public GroupQuery groupNameLikeIgnoreCase(String nameLikeIgnoreCase) {
        throw new UnsupportedOperationException("Filtering by group nameLikeIgnoreCase is not supported.");
    }

    @Override
    public GroupQuery groupType(String type) {
        throw new UnsupportedOperationException("Filtering by group type is not supported.");
    }

    @Override
    public GroupQuery groupMembers(List<String> userIds) {
        throw new UnsupportedOperationException("Filtering by group members is not supported.");
    }

    private void applyQueryRequestFilter(QueryRequest request) {
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

    private void applyQueryRequestFields(QueryRequest request) {
        request.addField(IdmIdentityService.ID_ATTR, IdmIdentityService.NAME_ATTR);
        if (this.userId != null) {
            request.addField(new JsonPointer(IdmIdentityService.MEMBERS_ATTR, "*", IdmIdentityService.USERNAME_ATTR));
        }
    }

    private boolean filterRolesByMember(ResourceResponse role) {
        if (this.userId == null) {
            return true;
        }
        for (JsonValue member : role.getContent().get(IdmIdentityService.MEMBERS_ATTR)) {
            if (member.get(IdmIdentityService.USERNAME_ATTR).asString().equals(this.userId)) {
                return true;
            }
        }
        return false;
    }
}
