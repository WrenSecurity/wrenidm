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
 * Portions Copyright 2018-2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.resource;

import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.resource.Responses.newQueryResponse;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnCollection;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnInstance;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.form.DateFormType;
import org.flowable.engine.impl.form.EnumFormType;
import org.flowable.engine.impl.form.FormPropertyHandler;
import org.flowable.engine.impl.form.TaskFormHandler;
import org.flowable.engine.repository.ProcessDefinition;
import org.forgerock.http.routing.UriRouterContext;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResourceHandler;
import org.forgerock.json.resource.QueryResponse;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.UpdateRequest;
import org.forgerock.services.context.Context;
import org.forgerock.services.context.SecurityContext;
import org.forgerock.util.promise.Promise;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.command.GetProcessCommand;
import org.wrensecurity.wrenidm.workflow.flowable.impl.command.GetTaskFormHandlerCommand;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.DateFormTypeMixIn;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.EnumFormTypeMixIn;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.FormPropertyHandlerMixIn;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.UserTaskMixIn;

/**
 * Resource handling queries related to the task definition.
 */
public class TaskDefinitionResource implements CollectionResourceProvider {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(UserTask.class, UserTaskMixIn.class)
            .addMixIn(EnumFormType.class, EnumFormTypeMixIn.class)
            .addMixIn(DateFormType.class, DateFormTypeMixIn.class)
            .addMixIn(FormPropertyHandler.class, FormPropertyHandlerMixIn.class)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private final ProcessEngine processEngine;

    public TaskDefinitionResource(ProcessEngine processEngine) {
        this.processEngine = processEngine;
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
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            if (!WorkflowConstants.QUERY_ALL_IDS.equals(request.getQueryId())) {
                return new BadRequestException("Unknown query-id").asPromise();
            }
            // Get BPMN process definition
            String processDefinitionId = ((UriRouterContext) context).getUriTemplateVariables().get("procdefid");
            Process process = processEngine.getManagementService().executeCommand(new GetProcessCommand(processDefinitionId));
            // Process user tasks
            for (UserTask task : process.findFlowElementsOfType(UserTask.class)) {
                JsonValue value = json(mapper.convertValue(task, Map.class));
                ResourceResponse response = newResourceResponse(task.getId(), null, value);
                // Add form key
                String taskFormKey = processEngine.getFormService().getTaskFormKey(processDefinitionId, task.getId());
                response.getContent().add(WorkflowConstants.FORM_RESOURCE_KEY_ATTR, taskFormKey);
                // Add form handler
                TaskFormHandler formHandler = processEngine.getManagementService().executeCommand(
                        new GetTaskFormHandlerCommand(processDefinitionId, task.getId()));
                response.getContent().add(WorkflowConstants.FORM_PROPERTIES_ATTR, json(mapper.convertValue(formHandler, Map.class)));
                handler.handleResource(response);
            }
            return newQueryResponse().asPromise();
        } catch (IllegalArgumentException ex) {
            return new InternalServerErrorException(ex.getMessage(), ex).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, String resourceId, ReadRequest request) {
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            // Get process definition
            String processDefinitionId = ((UriRouterContext) context).getUriTemplateVariables().get("procdefid");
            ProcessDefinition processDefinition = ((RepositoryServiceImpl) processEngine.getRepositoryService())
                    .getDeployedProcessDefinition(processDefinitionId);
            Process process = processEngine.getManagementService().executeCommand(new GetProcessCommand(processDefinitionId));
            // Get user task from the process definition
            UserTask task = (UserTask) process.getFlowElement(resourceId);
            if (task == null) {
                throw new NotFoundException("Task definition for " + resourceId + " was not found");
            }
            JsonValue response = buildResponse(task, processDefinition);
            return newResourceResponse(task.getId(), null, response).asPromise();
        } catch (ResourceException ex) {
            return ex.asPromise();
        } catch (FlowableObjectNotFoundException ex) {
            return new NotFoundException(ex.getMessage()).asPromise();
        } catch (IllegalArgumentException ex) {
            return new InternalServerErrorException(ex.getMessage(), ex).asPromise();
        } catch (Exception ex) {
            return new InternalServerErrorException(ex.getMessage(), ex).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> updateInstance(Context context, String resourceId, UpdateRequest request) {
        return notSupportedOnInstance(request).asPromise();
    }

    /**
     * Prepare resource response for the specified user task.
     *
     * @param task user task definition to be returned as response
     * @param processDefinition process definition related to the user task
     * @return task definition serialized as JSON value
     */
    private JsonValue buildResponse(UserTask task, ProcessDefinition processDefinition) throws IOException {
        JsonValue content = json(mapper.convertValue(task, Map.class));
        // Add task form key
        String taskFormKey = processEngine.getFormService().getTaskFormKey(processDefinition.getId(), task.getId());
        content.add(WorkflowConstants.FORM_RESOURCE_KEY_ATTR, taskFormKey);
        // Add form handler
        TaskFormHandler formHandler = processEngine.getManagementService().executeCommand(
                new GetTaskFormHandlerCommand(processDefinition.getId(), task.getId()));
        content.add(WorkflowConstants.FORM_PROPERTIES_ATTR, json(mapper.convertValue(formHandler, Map.class)));
        // Add form related data
        if (taskFormKey != null) {
            try (InputStream formData = processEngine.getRepositoryService().getResourceAsStream(
                    processDefinition.getDeploymentId(), taskFormKey);
                    Scanner scanner = new Scanner(new InputStreamReader(formData)).useDelimiter("\\A")) {
                String formTemplate = scanner.hasNext() ? scanner.next() : "";
                content.put(WorkflowConstants.FORM_GENERATION_TEMPLATE_ATTR, formTemplate);
            }
        }
        return content;
    }

}
