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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.UserQueryImpl;
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
public class IdmUserQuery extends UserQueryImpl {

    private static final long serialVersionUID = 1L;

    private static final Context context = ContextUtil.createInternalContext();

    private final Connection connection;

    public IdmUserQuery(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> executeList(CommandContext commandContext) {
        // FIXME: handle filtering
        QueryRequest request = Requests.newQueryRequest("managed/user");
        request.setQueryId(WorkflowConstants.QUERY_ALL_IDS);
        List<User> result = new ArrayList<>();
        QueryResourceHandler handler = new UserQueryResourceHandler(result);
        try {
            connection.query(context, request, handler);
            return result;
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        // FIXME: handle filtering
        try {
            QueryRequest request = Requests.newQueryRequest("managed/user");
            if (getId() == null) {
                request.setQueryId(WorkflowConstants.QUERY_ALL_IDS);
            } else {
                request.setQueryId("for-userName");
                request.setAdditionalParameter("uid", getId());
            }
            Collection<ResourceResponse> result = new ArrayList<>();
            connection.query(context, request, result);
            return result.size();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User executeSingleResult(CommandContext commandContext) {
        return readUser(getId());
    }

    /**
     * Read IdM user with the specified identifier (username).
     */
    private User readUser(String id) {
        try {
            QueryRequest request = Requests.newQueryRequest("managed/user");
            request.setQueryId("for-userName");
            request.setAdditionalParameter("uid", id);
            List<ResourceResponse> users = new ArrayList<>();
            connection.query(context, request, users);
            return !users.isEmpty() ? new IdmUser(users.get(0).getContent()) : null;
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }

    }

    private class UserQueryResourceHandler implements QueryResourceHandler {

        private final List<User> users;

        public UserQueryResourceHandler(List<User> users) {
            this.users = users;
        }

        @Override
        public boolean handleResource(ResourceResponse resource) {
            return users.add(readUser(resource.getContent().get(WorkflowConstants.RESOURCE_ID).asString()));
        }
    }

}
