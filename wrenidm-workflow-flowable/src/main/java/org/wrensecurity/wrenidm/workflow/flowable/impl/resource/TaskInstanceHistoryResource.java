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
 * Copyright 2015 ForgeRock AS.
 * Portions Copyright 2024-2025 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.resource;

import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.resource.Responses.newQueryResponse;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnInstance;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngine;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntityImpl;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.json.resource.CountPolicy;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResourceHandler;
import org.forgerock.json.resource.QueryResponse;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.SortKey;
import org.forgerock.json.resource.UpdateRequest;
import org.forgerock.services.context.Context;
import org.forgerock.services.context.SecurityContext;
import org.forgerock.util.promise.Promise;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.RequestUtil;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.HistoricTaskInstanceEntityMixIn;

/**
 * Resource handling queries related to the historic task instances.
 */
public class TaskInstanceHistoryResource implements CollectionResourceProvider {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(HistoricTaskInstanceEntityImpl.class, HistoricTaskInstanceEntityMixIn.class)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private final ProcessEngine processEngine;

    public TaskInstanceHistoryResource(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionCollection(Context context, ActionRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionInstance(Context context, String resourceId, ActionRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> createInstance(Context context, CreateRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> deleteInstance(Context context, String resourceId, DeleteRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> patchInstance(Context context, String resourceId, PatchRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<QueryResponse, ResourceException> queryCollection(Context context, QueryRequest request, QueryResourceHandler handler) {
        if (!WorkflowConstants.QUERY_ALL_IDS.equals(request.getQueryId()) &&
                !WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
            return new BadRequestException("Unknown query '" + request.getQueryId() + "' to get historic task instances.").asPromise();
        }
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery();
            if (WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
                applyRequestParams(query, request);
                applySortKeys(query, request);
            }
            List<HistoricTaskInstance> results = request.getPageSize() == 0 ? query.list() : query.listPage(
                    request.getPagedResultsOffset(), request.getPageSize());
            for (HistoricTaskInstance task : results) {
                JsonValue value = json(mapper.convertValue(task, Map.class));
                handler.handleResource(newResourceResponse(task.getId(), null, value));
            }
            if (request.getPageSize() == 0) {
                return newQueryResponse().asPromise();
            }
            // Handle paging
            Integer totalCount = null;
            if (request.getTotalPagedResultsPolicy() != CountPolicy.NONE) {
               totalCount = Long.valueOf(query.count()).intValue();
            }
            int nextOffset = request.getPagedResultsOffset() + results.size();
            if (totalCount != null) {
                return newQueryResponse(totalCount > nextOffset ? String.valueOf(nextOffset) : null, CountPolicy.EXACT, totalCount).asPromise();
            } else {
                return newQueryResponse(results.size() >= request.getPageSize() ? String.valueOf(nextOffset) : null).asPromise();
            }
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, String resourceId, ReadRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> updateInstance(Context context, String resourceId, UpdateRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    /**
     * Restrict search using the specified request parameters.
     *
     * @param query query to apply request parameters
     * @param request request to get parameters to restrict search
     */
    private void applyRequestParams(HistoricTaskInstanceQuery query, QueryRequest request) {
        for (Entry<String, String> param : request.getAdditionalParameters().entrySet()) {
            switch (param.getKey()) {
                case WorkflowConstants.EXECUTION_ID_ATTR:
                    query.executionId(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_ID_ATTR:
                    query.processDefinitionId(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_KEY_ATTR:
                    query.processDefinitionKey(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                    query.processInstanceId(param.getValue());
                    break;
                case WorkflowConstants.ASSIGNEE_ATTR:
                    query.taskAssignee(param.getValue());
                    break;
                case WorkflowConstants.CANDIDATE_GROUP_ATTR:
                    String[] taskCandidateGroups = param.getValue().split(",");
                    if (taskCandidateGroups.length > 1) {
                        query.taskCandidateGroupIn(List.of(taskCandidateGroups));
                    } else {
                        query.taskCandidateGroup(param.getValue());
                    }
                    break;
                case WorkflowConstants.CANDIDATE_USER_ATTR:
                    query.taskCandidateUser(param.getValue());
                    break;
                case WorkflowConstants.TASK_ID_ATTR:
                    query.taskId(param.getValue());
                    break;
                case WorkflowConstants.TASK_NAME_ATTR:
                    query.taskName(param.getValue());
                    break;
                case WorkflowConstants.OWNER_ATTR:
                    query.taskOwner(param.getValue());
                    break;
                case WorkflowConstants.DESCRIPTION_ATTR:
                    query.taskDescription(param.getValue());
                    break;
                case WorkflowConstants.FINISHED_ATTR:
                    if (Boolean.parseBoolean(param.getValue())) {
                        query.finished();
                    }
                    break;
                case WorkflowConstants.UNFINISHED_ATTR:
                    if (Boolean.parseBoolean(param.getValue())) {
                        query.unfinished();
                    }
                    break;
                case WorkflowConstants.PROCESS_FINISHED_ATTR:
                    if (Boolean.parseBoolean(param.getValue())) {
                        query.processFinished();
                    }
                    break;
                case WorkflowConstants.PROCESS_UNFINISHED_ATTR:
                    if (Boolean.parseBoolean(param.getValue())) {
                        query.processUnfinished();
                    }
                    break;
                case WorkflowConstants.PRIORITY_ATTR:
                    query.taskPriority(Integer.parseInt(param.getValue()));
                    break;
                case WorkflowConstants.DELETE_REASON_ATTR:
                    query.taskDeleteReason(param.getValue());
                    break;
                case WorkflowConstants.TENANT_ID_ATTR:
                    query.taskTenantId(param.getValue());
                    break;
            }
        }
        for (Entry<String, String> entry : RequestUtil.getQueryVariables(request).entrySet()) {
            query.processVariableValueEquals(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Apply sort keys from the specified request.
     *
     * @param query query to apply sort keys
     * @param request request to get sort keys
     */
    private void applySortKeys(HistoricTaskInstanceQuery query, QueryRequest request) throws NotSupportedException {
        for (SortKey key : request.getSortKeys()) {
            if (key.getField() != null && !key.getField().isEmpty()) {
                switch (key.getField().toString().substring(1)) { // Remove leading JsonPointer slash
                    case WorkflowConstants.TASK_ID_ATTR:
                        query.orderByTaskId();
                        break;
                    case WorkflowConstants.TASK_NAME_ATTR:
                        query.orderByTaskName();
                        break;
                    case WorkflowConstants.DESCRIPTION_ATTR:
                        query.orderByTaskDescription();
                        break;
                    case WorkflowConstants.PRIORITY_ATTR:
                        query.orderByTaskPriority();
                        break;
                    case WorkflowConstants.ASSIGNEE_ATTR:
                        query.orderByTaskAssignee();
                        break;
                    case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                        query.orderByProcessInstanceId();
                        break;
                    case WorkflowConstants.EXECUTION_ID_ATTR:
                        query.orderByExecutionId();
                        break;
                    case WorkflowConstants.TENANT_ID_ATTR:
                        query.orderByTenantId();
                        break;
                    case WorkflowConstants.RESOURCE_ID:
                        query.orderByHistoricActivityInstanceId();
                        break;
                    case WorkflowConstants.PROCESS_DEFINITION_ID_ATTR:
                        query.orderByProcessDefinitionId();
                        break;
                    case WorkflowConstants.DURATION_IN_MILLIS_ATTR:
                        query.orderByHistoricTaskInstanceDuration();
                        break;
                    case WorkflowConstants.START_TIME_ATTR:
                        query.orderByTaskCreateTime();
                        break;
                    case WorkflowConstants.END_TIME_ATTR:
                        query.orderByHistoricTaskInstanceEndTime();
                        break;
                    case WorkflowConstants.OWNER_ATTR:
                        query.orderByTaskOwner();
                        break;
                    case WorkflowConstants.DUE_DATE_ATTR:
                        query.orderByTaskDueDate();
                        break;
                    case WorkflowConstants.DELETE_REASON_ATTR:
                        query.orderByDeleteReason();
                        break;
                    case WorkflowConstants.TASK_DEFINITION_KEY_ATTR:
                        query.orderByTaskDefinitionKey();
                        break;
                    default:
                        throw new NotSupportedException("Unsupported sort key '" + key.getField().toString().substring(1) + "'.");
                }
                if (key.isAscendingOrder()) {
                    query.asc();
                } else {
                    query.desc();
                }
            }
        }
    }
}
