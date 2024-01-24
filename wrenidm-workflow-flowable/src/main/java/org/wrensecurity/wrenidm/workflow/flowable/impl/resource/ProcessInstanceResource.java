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
package org.wrensecurity.wrenidm.workflow.flowable.impl.resource;

import static org.forgerock.json.JsonValue.array;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.resource.Responses.newQueryResponse;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnCollection;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnInstance;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntityImpl;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.history.HistoricVariableInstanceQuery;
import org.flowable.variable.service.impl.persistence.entity.HistoricVariableInstanceEntity;
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
import org.forgerock.services.context.RootContext;
import org.forgerock.services.context.SecurityContext;
import org.forgerock.util.Function;
import org.forgerock.util.encode.Base64;
import org.forgerock.util.promise.NeverThrowsException;
import org.forgerock.util.promise.Promise;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.RequestUtil;
import org.wrensecurity.wrenidm.workflow.flowable.impl.FlowableContext;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.HistoricProcessInstanceMixIn;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.HistoricTaskInstanceEntityMixIn;

/**
 * Resource handling queries related to the running and history process instances.
 */
public class ProcessInstanceResource implements CollectionResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceResource.class);
    private static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(HistoricProcessInstanceEntityImpl.class, HistoricProcessInstanceMixIn.class)
            .addMixIn(HistoricTaskInstanceEntityImpl.class, HistoricTaskInstanceEntityMixIn.class)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private final ProcessEngine processEngine;
    private final Function<ProcessEngine, HistoricProcessInstanceQuery, NeverThrowsException> queryFunction;

    public ProcessInstanceResource(ProcessEngine processEngine, Function<ProcessEngine, HistoricProcessInstanceQuery,
            NeverThrowsException> queryFunction) {
        this.processEngine = processEngine;
        this.queryFunction = queryFunction;
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionCollection(Context context, ActionRequest request) {
        return notSupportedOnCollection(request).asPromise();
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionInstance(Context context, String resourceId, ActionRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> createInstance(Context context, CreateRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            // Extract base process instance parameters
            String key = removeRequestParameter(request, "_key");
            String businessKey = removeRequestParameter(request, "_businessKey");
            String processDefinitionId = removeRequestParameter(request, "_processDefinitionId");
            // Prepare request context
            Map<String, Object> variables = RequestUtil.getRequestContent(request);
            // Wrap the current CREST context in a special-purpose WorkflowContext so we can be sure of what we are deserializing later
            variables.put(WorkflowConstants.OPENIDM_CONTEXT, buildWorkflowContext(context).toJsonValue());
            // Create new process instance
            ProcessInstance instance;
            if (processDefinitionId == null) {
                instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, businessKey, variables);
            } else {
                instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey, variables);
            }
            if (instance == null) {
                return new InternalServerErrorException("Failed to create process instance.").asPromise();
            }
            // Prepare response
            Map<String, String> content = new HashMap<String, String>();
            content.put(WorkflowConstants.STATUS_ATTR, instance.isEnded() ? "ended" : "suspended");
            content.put(WorkflowConstants.PROCESS_INSTANCE_ID_ATTR, instance.getProcessInstanceId());
            content.put(WorkflowConstants.BUSINESS_KEY_ATTR, instance.getBusinessKey());
            content.put(WorkflowConstants.PROCESS_DEFINITION_ID_ATTR, instance.getProcessDefinitionId());
            content.put(WorkflowConstants.RESOURCE_ID, instance.getId());
            return newResourceResponse(instance.getId(), null, json(content)).asPromise();
        } catch (FlowableObjectNotFoundException e) {
            return new NotFoundException(e.getMessage(), e).asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> deleteInstance(Context context, String resourceId, DeleteRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            HistoricProcessInstance process = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(resourceId).singleResult();
            if (process == null) {
                return new NotFoundException().asPromise();
            }
            JsonValue value = json(mapper.convertValue(process, Map.class));
            processEngine.getRuntimeService().deleteProcessInstance(resourceId, "Deleted by Wren:IDM.");
            return newResourceResponse(process.getId(), null, value).asPromise();
        } catch (FlowableObjectNotFoundException e) {
            return new NotFoundException(e.getMessage(), e).asPromise();
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
            return new BadRequestException("Unknown query '" + request.getQueryId() + "' to get process instances.").asPromise();
        }
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            HistoricProcessInstanceQuery query = queryFunction.apply(processEngine);
            if (WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
                applyRequestParams(query, request);
                applySortKeys(query, request);
            }
            for (HistoricProcessInstance processInstance : query.list()) {
                // Fetch process variables
                ((HistoricProcessInstanceEntity) processInstance).setQueryVariables(getVariables(processInstance.getId(), null));
                // Serialize process instance into JSON value
                JsonValue value = json(mapper.convertValue(processInstance, Map.class));
                value.put(WorkflowConstants.PROCESS_DEFINITION_RESOURCE_NAME_ATTR, processInstance.getProcessDefinitionName());
                handler.handleResource(newResourceResponse(processInstance.getId(), null, value));
            }
            return newQueryResponse().asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, String resourceId, ReadRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            HistoricProcessInstance processInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(resourceId).singleResult();
            if (processInstance == null) {
                return new NotFoundException().asPromise();
            }
            // Fetch process variables
            ((HistoricProcessInstanceEntity) processInstance).setQueryVariables(getVariables(resourceId, null));
            // Serialize process instance into JSON value
            JsonValue content = json(mapper.convertValue(processInstance, Map.class));
            content.put(WorkflowConstants.PROCESS_DEFINITION_RESOURCE_NAME_ATTR, processInstance.getProcessDefinitionName());
            content.put("tasks", getTasksForProcess(processInstance.getId()).getObject());
            // Add diagram image to the result when requested
            if (request.getFields().contains(WorkflowConstants.DIAGRAM_ATTR)) {
                RepositoryServiceImpl repositoryService = (RepositoryServiceImpl) processEngine.getRepositoryService();
                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(
                                processInstance.getProcessDefinitionId());
                if (processDefinition != null && processDefinition.isGraphicalNotationDefined()) {
                    BpmnModel model = repositoryService.getBpmnModel(processDefinition.getId());
                    try (final InputStream is = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator()
                            .generateDiagram(model, "png", processEngine.getRuntimeService().getActiveActivityIds(resourceId), false)) {
                        final byte[] data = new byte[is.available()];
                        is.read(data);
                        content.put(WorkflowConstants.DIAGRAM_ATTR, Base64.encode(data));
                    }
                }
            }
            return newResourceResponse(processInstance.getId(), null, content).asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> updateInstance(Context context, String resourceId, UpdateRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    /**
     * Build workflow context with security context attributes from the specified context.
     *
     * @param context security context to get authenticaton-related attributes
     * @return built workflow context
     */
    private FlowableContext buildWorkflowContext(Context context) {
        RootContext root = context.asContext(RootContext.class);
        SecurityContext security = context.asContext(SecurityContext.class);
        return new FlowableContext(new SecurityContext(new RootContext(root.getId()), security.getAuthenticationId(),
                security.getAuthorization()));
    }

    /**
     * Get all tasks associated with the process with the specified identifier.
     *
     * @param processId identifier to get tasks to
     * @return process tasks represented as JSON array
     */
    private JsonValue getTasksForProcess(String processId) {
        HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery();
        JsonValue tasks = json(array());
        for (HistoricTaskInstance task : query.processInstanceId(processId).list()) {
            // Fetch task variables
            ((HistoricTaskInstanceEntity) task).setQueryVariables(getVariables(processId, task.getId()));
            // Serialize task instance into JSON value
            tasks.add(mapper.convertValue(task, Map.class));
        }
        return tasks;
    }

    /**
     * Restrict search using the specified request parameters.
     *
     * @param query query to apply request parameters
     * @param request request to get parameters to restrict search
     */
    private void applyRequestParams(HistoricProcessInstanceQuery query, QueryRequest request) {
        for (Entry<String, String> param : request.getAdditionalParameters().entrySet()) {
            switch (param.getKey()) {
                case WorkflowConstants.PROCESS_DEFINITION_ID_ATTR:
                    query.processDefinitionId(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_KEY_ATTR:
                    query.processDefinitionKey(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_BUSINESS_KEY_ATTR:
                    query.processInstanceBusinessKey(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                    query.processInstanceId(param.getValue());
                    break;
                case WorkflowConstants.SUPER_PROCESS_INSTANCE_ID_ATTR:
                    query.superProcessInstanceId(param.getValue());
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
                case WorkflowConstants.INVOLVED_USER_ID_ATTR:
                    query.involvedUser(param.getValue());
                    break;
                case WorkflowConstants.START_USER_ID_ATTR:
                    query.startedBy(param.getValue());
                    break;
                case WorkflowConstants.STARTED_AFTER_ATTR:
                    query.startedAfter(parseDate(param.getValue()));
                    break;
                case WorkflowConstants.STARTED_BEFORE_ATTR:
                    query.startedBefore(parseDate(param.getValue()));
                    break;
                case WorkflowConstants.FINISHED_AFTER_ATTR:
                    query.finishedAfter(parseDate(param.getValue()));
                    break;
                case WorkflowConstants.FINISHED_BEFORE_ATTR:
                    query.finishedBefore(parseDate(param.getValue()));
                    break;
            }
        }
        for (Entry<String, String> entry : RequestUtil.getQueryVariables(request).entrySet()) {
            query.variableValueEquals(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Apply sort keys from the specified request.
     *
     * @param query query to apply sort keys
     * @param request request to get sort keys
     */
    private void applySortKeys(HistoricProcessInstanceQuery query, QueryRequest request) throws NotSupportedException {
        for (SortKey key : request.getSortKeys()) {
            if (key.getField() == null || key.getField().isEmpty()) {
                continue;
            }
            switch (key.getField().toString().substring(1)) { // Remove leading JsonPointer slash
                case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                    query.orderByProcessInstanceId();
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_ID_ATTR:
                    query.orderByProcessDefinitionId();
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_BUSINESS_KEY_ATTR:
                    query.orderByProcessInstanceBusinessKey();
                    break;
                case WorkflowConstants.START_TIME_ATTR:
                    query.orderByProcessInstanceStartTime();
                    break;
                case WorkflowConstants.END_TIME_ATTR:
                    query.orderByProcessInstanceEndTime();
                    break;
                case WorkflowConstants.DURATION_IN_MILLIS_ATTR:
                    query.orderByProcessInstanceDuration();
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

    /**
     * Parse given date string in ISO format into {@link Date} instance.
     *
     * @param value date string in ISO format to parse
     * @return parsed date instance or null when parsing failed
     */
    private Date parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new DateTime(value).toDate();
        } catch (IllegalArgumentException e) {
            logger.warn("Parsing of date string '" + value + "' failed.", e);
        }
        return null;
    }

    /**
     * Get variables for the process instance with the specified identifier.
     * The search for variables can be restricted to a specific task using its identifier.
     * {@link WorkflowConstants#OPENIDM_CONTEXT} variable will not be returned.
     * @param processId Process instance identifier to get variables to. Never null.
     * @param taskId Task instance identifier to restrict variable search to specific task. Can be null.
     * @return List of {@link HistoricVariableInstanceEntity} instances. Never null.
     */
    private List<HistoricVariableInstanceEntity> getVariables(String processId, String taskId) {
        List<HistoricVariableInstanceEntity> result = new ArrayList<>();
        HistoricVariableInstanceQuery query = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery().excludeVariableInitialization().processInstanceId(processId);
        if (taskId != null) {
            query.taskId(taskId);
        }
        for (HistoricVariableInstance variable : query.list()) {
            if (WorkflowConstants.OPENIDM_CONTEXT.equals(variable.getVariableName())) {
                continue;  // Remove useless OPENIDM_CONTEXT
            }
            result.add((HistoricVariableInstanceEntity) variable);
        }
        return result;
    }

    /**
     * Remove the specified parameter from the specified request.
     *
     * @param request request instance to remove the specified parameter
     * @param parameter parameter to remove from the request
     * @return removed parameter value
     */
    private String removeRequestParameter(CreateRequest request, String parameter) {
        if (request.getContent().isNull()) {
            return null;
        }
        return (String) request.getContent().asMap().remove(parameter);
    }
}
