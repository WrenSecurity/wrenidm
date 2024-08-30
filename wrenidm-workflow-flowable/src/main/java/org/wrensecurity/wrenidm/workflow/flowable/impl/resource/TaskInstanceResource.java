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
 * Copyright 2012-2016 ForgeRock AS.
 * Portions Copyright 2017-2024 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.resource;

import static org.forgerock.json.JsonValue.array;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.resource.Responses.newActionResponse;
import static org.forgerock.json.resource.Responses.newQueryResponse;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnCollection;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnInstance;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.TaskService;
import org.flowable.engine.form.TaskFormData;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
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
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.TaskEntityMixIn;

/**
 * Resource handling queries related to the task instances.
 */
public class TaskInstanceResource implements CollectionResourceProvider {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(TaskEntityImpl.class, TaskEntityMixIn.class)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private final ProcessEngine processEngine;

    public TaskInstanceResource(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionCollection(Context context, ActionRequest request) {
        return notSupportedOnCollection(request).asPromise();
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionInstance(Context context, String resourceId, ActionRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            // Get task
            TaskService taskService = processEngine.getTaskService();
            Task task = taskService.createTaskQuery().taskId(resourceId).singleResult();
            if (task == null) {
                return new NotFoundException().asPromise();
            }
            // Process task action
            if ("claim".equals(request.getAction())) {
                taskService.claim(resourceId, request.getContent().expect(Map.class).asMap().get("userId").toString());
            } else if ("complete".equals(request.getAction())) {
                taskService.complete(resourceId, request.getContent().expect(Map.class).asMap(), true);
            } else {
                return new BadRequestException("Unknown action '" + request.getAction() + "'.").asPromise();
            }
            // Handle response
            Map<String, String> result = new HashMap<>(1);
            result.put("Task action performed", request.getAction());
            return newActionResponse(new JsonValue(result)).asPromise();
        } catch (Exception ex) {
            return new InternalServerErrorException(ex.getMessage(), ex).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> createInstance(Context context, CreateRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> deleteInstance(Context context, String resourceId, DeleteRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            Task task = processEngine.getTaskService().createTaskQuery().taskId(resourceId).singleResult();
            if (task == null) {
                return new NotFoundException("Task " + resourceId + " not found.").asPromise();
            }
            JsonValue deletedTask = json(mapper.convertValue(task, Map.class));
            processEngine.getTaskService().deleteTask(resourceId,
                    request.getAdditionalParameter(WorkflowConstants.DELETE_REASON_ATTR));
            return newResourceResponse(task.getId(), null, deletedTask).asPromise();
        } catch (FlowableObjectNotFoundException e) {
            return new NotFoundException(e.getMessage()).asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> patchInstance(Context context, String resourceId, PatchRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<QueryResponse, ResourceException> queryCollection(Context context, QueryRequest request, QueryResourceHandler handler) {
        if (!WorkflowConstants.QUERY_ALL_IDS.equals(request.getQueryId()) &&
                !WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
            return new BadRequestException("Unknown query '" + request.getQueryId() + "' to get task instances.").asPromise();
        }
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            TaskQuery query = processEngine.getTaskService().createTaskQuery();
            if (WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
                applyRequestParams(query, request);
                applySortKeys(query, request);
            }
            for (Task task : query.list()) {
                JsonValue value = json(mapper.convertValue(task, Map.class));
                if (DelegationState.PENDING == task.getDelegationState()) {
                    value.add(WorkflowConstants.DELEGATE_ATTR, task.getAssignee());
                } else {
                    value.add(WorkflowConstants.ASSIGNEE_ATTR, task.getAssignee());
                }
                handler.handleResource(newResourceResponse(task.getId(), null, value));
            }
            return newQueryResponse().asPromise();
        } catch (NotSupportedException e) {
            return e.asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, String resourceId, ReadRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            Task task = processEngine.getTaskService().createTaskQuery().taskId(resourceId).singleResult();
            if (task == null) {
                return new NotFoundException().asPromise();
            }
            JsonValue value = json(mapper.convertValue(task, Map.class));
            // Add form properties
            TaskFormData formData = processEngine.getFormService().getTaskFormData(task.getId());
            List<Map<String, String>> formProperties = formData.getFormProperties().stream()
                    .map(property -> Collections.singletonMap(property.getId(), property.getValue()))
                    .collect(Collectors.toList());
            value.put(WorkflowConstants.FORM_PROPERTIES_ATTR, formProperties);
            // Add assignee
            if (DelegationState.PENDING == task.getDelegationState()) {
                value.put(WorkflowConstants.DELEGATE_ATTR, task.getAssignee());
            } else {
                value.put(WorkflowConstants.ASSIGNEE_ATTR, task.getAssignee());
            }
            // Add task variables
            Map<String, Object> variables = processEngine.getTaskService().getVariables(task.getId());
            if (variables.containsKey(WorkflowConstants.OPENIDM_CONTEXT)){
                variables.remove(WorkflowConstants.OPENIDM_CONTEXT);
            }
            value.put(WorkflowConstants.VARIABLES_ATTR, variables);
            // Add task candidates (users and groups)
            value.put("candidates", getTaskCandidates(task).getObject());
            return newResourceResponse(task.getId(), null, value).asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> updateInstance(Context context, String resourceId, UpdateRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            Task task = processEngine.getTaskService().createTaskQuery().taskId(resourceId).singleResult();
            if (task == null) {
                return new NotFoundException().asPromise();
            }
            Map<String, Object> content = request.getContent().expect(Map.class).asMap();
            if (content.containsKey(WorkflowConstants.ASSIGNEE_ATTR)) {
                Object assignee = content.get(WorkflowConstants.ASSIGNEE_ATTR);
                task.setAssignee(assignee != null ? assignee.toString() : null);
            }
            if (content.get(WorkflowConstants.DESCRIPTION_ATTR) != null) {
                task.setDescription(content.get(WorkflowConstants.DESCRIPTION_ATTR).toString());
            }
            if (content.get(WorkflowConstants.NAME_ATTR) != null) {
                task.setName(content.get(WorkflowConstants.NAME_ATTR).toString());
            }
            if (content.get(WorkflowConstants.OWNER_ATTR) != null) {
                task.setOwner(content.get(WorkflowConstants.OWNER_ATTR).toString());
            }
            processEngine.getTaskService().saveTask(task);
            JsonValue result = new JsonValue(Map.of("Task updated", resourceId));
            return newResourceResponse(resourceId, null, result).asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    /**
     * Get candidate users and groups for the specified task.
     *
     * @param task task to get candidates
     * @return JSON value with candidate users and groups
     */
    private JsonValue getTaskCandidates(Task task) {
        Set<String> candidateUsers = new HashSet<>();
        Set<String> candidateGroups = new HashSet<>();
        List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(task.getId());
        for (IdentityLink identityLink : identityLinks) {
            if (identityLink.getUserId() != null) {
                candidateUsers.add(identityLink.getUserId());
            }
            if (identityLink.getGroupId() != null) {
                candidateGroups.add(identityLink.getGroupId());
            }
        }
        return json(object(
                field("candidateUsers", array(candidateUsers.toArray())),
                field("candidateGroups", array(candidateGroups.toArray()))));
    }

    /**
     * Restrict search using the specified request parameters.
     *
     * @param query query to apply request parameters
     * @param request request to get parameters to restrict search
     */
    private void applyRequestParams(TaskQuery query, QueryRequest request) {
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
                    String taskCandidateGroup = param.getValue();
                    String[] taskCandidateGroups = taskCandidateGroup.split(",");
                    if (taskCandidateGroups.length > 1) {
                        query.taskCandidateGroupIn(List.of(taskCandidateGroups));
                    } else {
                        query.taskCandidateGroup(taskCandidateGroup);
                    }
                    break;
                case WorkflowConstants.CANDIDATE_USER_ATTR:
                    query.taskCandidateUser(param.getValue());
                    break;
                case WorkflowConstants.CANDIDATE_OR_ASSIGNED_ATTR:
                    query.taskCandidateOrAssigned(param.getValue());
                    break;
                case WorkflowConstants.RESOURCE_ID:
                    query.taskId(param.getValue());
                    break;
                case WorkflowConstants.NAME_ATTR:
                    query.taskName(param.getValue());
                    break;
                case WorkflowConstants.OWNER_ATTR:
                    query.taskOwner(param.getValue());
                    break;
                case WorkflowConstants.DESCRIPTION_ATTR:
                    query.taskDescription(param.getValue());
                    break;
                case WorkflowConstants.PRIORITY_ATTR:
                    query.taskPriority(Integer.parseInt(param.getValue()));
                    break;
                case WorkflowConstants.UNASSIGNED_ATTR:
                    if (Boolean.parseBoolean(param.getValue())) {
                        query.taskUnassigned();
                    }
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
    private void applySortKeys(TaskQuery query, QueryRequest request) throws NotSupportedException {
        for (SortKey key : request.getSortKeys()) {
            if (key.getField() == null || key.getField().isEmpty()) {
                continue;
            }
            switch (key.getField().toString().substring(1)) { // Remove leading JsonPointer slash
                case WorkflowConstants.RESOURCE_ID:
                    query.orderByTaskId();
                    break;
                case WorkflowConstants.NAME_ATTR:
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
                case WorkflowConstants.CREATE_TIME_ATTR:
                    query.orderByTaskCreateTime();
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                    query.orderByProcessInstanceId();
                    break;
                case WorkflowConstants.EXECUTION_ID_ATTR:
                    query.orderByExecutionId();
                    break;
                case WorkflowConstants.DUE_DATE_ATTR:
                    query.orderByTaskDueDate();
                    break;
                case WorkflowConstants.TENANT_ID_ATTR:
                    query.orderByTenantId();
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
