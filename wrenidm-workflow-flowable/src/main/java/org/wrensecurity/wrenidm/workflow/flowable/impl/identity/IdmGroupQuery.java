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
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.idm.api.Group;
import org.flowable.idm.engine.impl.GroupQueryImpl;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResourceHandler;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.openidm.util.ContextUtil;
import org.forgerock.services.context.Context;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Component handling flowable user queries.
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
        request.setQueryId(WorkflowConstants.QUERY_ALL_IDS);
        List<Group> roles = new ArrayList<>();
        QueryResourceHandler handler = new RoleQueryResourceHandler(roles);
        try {
            connection.query(context, request, handler);
            return roles;
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        try {
            QueryRequest request = Requests.newQueryRequest("managed/role");
            if (null == getId()) {
                request.setQueryId(WorkflowConstants.QUERY_ALL_IDS);
            } else {
                request.setQueryId("get-by-field-value");
                request.setAdditionalParameter("field", "id");
                request.setAdditionalParameter("value", getId());
            }
            Collection<ResourceResponse> roles = new ArrayList<>();
            connection.query(context, request, roles);
            return roles.size();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group executeSingleResult(CommandContext commandContext) {
        return readRole(getId());
    }

    /**
     * Read IdM role with the specified identifier.
     */
    private Group readRole(String id) {
        try {
            QueryRequest request = Requests.newQueryRequest("managed/role");
            request.setQueryId("get-by-field-value");
            request.setAdditionalParameter("value", id);
            request.setAdditionalParameter("field", "id");
            List<ResourceResponse> roles = new ArrayList<>();
            connection.query(context, request, roles);
            return !roles.isEmpty() ? new IdmGroup(roles.get(0).getContent()) : null;
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    private class RoleQueryResourceHandler implements QueryResourceHandler {

        private final List<Group> roles;

        public RoleQueryResourceHandler(List<Group> roles) {
            this.roles = roles;
        }

        @Override
        public boolean handleResource(ResourceResponse resource) {
            return roles.add(readRole(resource.getContent().get(WorkflowConstants.RESOURCE_ID).asString()));
        }

    }
}
