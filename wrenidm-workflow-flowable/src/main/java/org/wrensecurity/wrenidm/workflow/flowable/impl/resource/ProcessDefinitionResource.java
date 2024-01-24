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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.apache.ibatis.exceptions.PersistenceException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.form.StartFormData;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.form.DateFormType;
import org.flowable.engine.impl.form.DefaultStartFormHandler;
import org.flowable.engine.impl.form.EnumFormType;
import org.flowable.engine.impl.form.FormPropertyHandler;
import org.flowable.engine.impl.form.StartFormHandler;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.json.resource.ConflictException;
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
import org.forgerock.util.encode.Base64;
import org.forgerock.util.promise.Promise;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.command.GetStartFormHandlerCommand;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.DateFormTypeMixIn;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.EnumFormTypeMixIn;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.ProcessDefinitionMixIn;

/**
 * Resource handling queries related to the process definition.
 */
public class ProcessDefinitionResource implements CollectionResourceProvider {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(ProcessDefinitionEntityImpl.class, ProcessDefinitionMixIn.class)
            .addMixIn(EnumFormType.class, EnumFormTypeMixIn.class)
            .addMixIn(DateFormType.class, DateFormTypeMixIn.class)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private final ProcessEngine processEngine;

    public ProcessDefinitionResource(ProcessEngine processEngine) {
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
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            ProcessDefinition processDefinition = processEngine.getRepositoryService().getProcessDefinition(resourceId);
            if (processDefinition == null) {
                throw new NotFoundException("Missing process definition with ID '" + resourceId + "'.");
            }
            JsonValue response = buildResponse(processDefinition, request.getFields());
            processEngine.getRepositoryService().deleteDeployment(processDefinition.getDeploymentId(), false);
            return newResourceResponse(processDefinition.getId(), null, response).asPromise();
        } catch (FlowableObjectNotFoundException e) {
            return new NotFoundException(e.getMessage()).asPromise();
        } catch (PersistenceException e) {
            return new ConflictException("Cannot delete process definition with the running instances.", e).asPromise();
        } catch (ResourceException e) {
            return e.asPromise();
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
            return new BadRequestException("Unknown query '" + request.getQueryId() + "' to get process definitions.").asPromise();
        }
        try {
            Authentication.setAuthenticatedUserId(context.asContext(SecurityContext.class).getAuthenticationId());
            ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery();
            if (WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
                applyRequestParams(query, request);
            }
            List<ProcessDefinition> definitions = query.list();
            if (definitions != null) {
                for (ProcessDefinition definition : definitions) {
                    JsonValue value = json(mapper.convertValue(definition, Map.class));
                    handler.handleResource(newResourceResponse(definition.getId(), null, value));
                }
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
            ProcessDefinition processDefinition = ((RepositoryServiceImpl) processEngine.getRepositoryService())
                    .getDeployedProcessDefinition(resourceId);
            JsonValue response = buildResponse(processDefinition, request.getFields());
            return newResourceResponse(processDefinition.getId(), null, response).asPromise();
        } catch (FlowableObjectNotFoundException e) {
            return new NotFoundException(e.getMessage(), e).asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
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
    private void applyRequestParams(ProcessDefinitionQuery query, QueryRequest request) {
        for (Entry<String, String> param : request.getAdditionalParameters().entrySet()) {
            switch (param.getKey()) {
                case WorkflowConstants.DEPLOYMENT_ID_ATTR:
                    query.deploymentId(param.getValue());
                    break;
                case WorkflowConstants.CATEGORY_ATTR:
                    query.processDefinitionCategory(param.getValue());
                    break;
                case WorkflowConstants.CATEGORY_ATTR + WorkflowConstants.QUERY_LIKE_CONDITION:
                    query.processDefinitionCategoryLike(param.getValue());
                    break;
                case WorkflowConstants.RESOURCE_ID:
                    query.processDefinitionId(param.getValue());
                    break;
                case WorkflowConstants.KEY_ATTR:
                    query.processDefinitionKey(param.getValue());
                    break;
                case WorkflowConstants.KEY_ATTR + WorkflowConstants.QUERY_LIKE_CONDITION:
                    query.processDefinitionKeyLike(param.getValue());
                    break;
                case WorkflowConstants.NAME_ATTR:
                    query.processDefinitionName(param.getValue());
                    break;
                case WorkflowConstants.NAME_ATTR + WorkflowConstants.QUERY_LIKE_CONDITION:
                    query.processDefinitionNameLike(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_RESOURCE_NAME_ATTR:
                    query.processDefinitionResourceName(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_RESOURCE_NAME_ATTR + WorkflowConstants.QUERY_LIKE_CONDITION:
                    query.processDefinitionResourceNameLike(param.getValue());
                    break;
                case WorkflowConstants.VERSION_ATTR:
                    query.processDefinitionVersion(Integer.valueOf(param.getValue()));
                    break;
            }
        }
    }

    /**
     * Prepare resource response for the specified process definition.
     *
     * @param processDefinition process definition to be returned as response
     * @param fields requested fields
     * @return process definition serialized as JSON value
     */
    private JsonValue buildResponse(ProcessDefinition processDefinition, List<JsonPointer> fields) throws IOException {
        JsonValue content = new JsonValue(mapper.convertValue(processDefinition, Map.class));
        // Add start form related data
        if (processDefinition.hasStartFormKey()) {
            StartFormData startFormData = processEngine.getFormService().getStartFormData(processDefinition.getId());
            content.put(WorkflowConstants.FORM_RESOURCE_KEY_ATTR, startFormData.getFormKey());
            try (InputStream startForm = processEngine.getRepositoryService().getResourceAsStream(
                    processDefinition.getDeploymentId(), startFormData.getFormKey());
                    Scanner scanner = new Scanner(new InputStreamReader(startForm)).useDelimiter("\\A")) {
                String formTemplate = scanner.hasNext() ? scanner.next() : "";
                content.put(WorkflowConstants.FORM_GENERATION_TEMPLATE_ATTR, formTemplate);
            }
        }
        StartFormHandler startFormHandler = processEngine.getManagementService().executeCommand(new GetStartFormHandlerCommand(processDefinition));
        if (startFormHandler instanceof DefaultStartFormHandler) {
            List<FormPropertyHandler> handlers = ((DefaultStartFormHandler) startFormHandler).getFormPropertyHandlers();
            content.put(WorkflowConstants.FORM_PROPERTIES_ATTR, getFormHandlerData((handlers)));
        }
        // Add diagram if requested and exists
        if (fields.contains(WorkflowConstants.DIAGRAM_ATTR) && processDefinition.getDiagramResourceName() != null) {
            try (InputStream is = processEngine.getRepositoryService().getResourceAsStream(
                    processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName())) {
                final byte[] data = new byte[is.available()];
                is.read(data);
                content.put(WorkflowConstants.DIAGRAM_ATTR, Base64.encode(data));
            }
        }
        return content;
    }

    /**
     * Transform specified form property handlers into a collection of map entries with handler data.
     *
     * @param collection of form property handlers from the process definition
     * @return collection of serialized form handlers
     */
    private List<Map<String, Object>> getFormHandlerData(List<FormPropertyHandler> handlers) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (FormPropertyHandler handler : handlers) {
            Map<String, Object> entry = new HashMap<>();
            entry.put(WorkflowConstants.RESOURCE_ID, handler.getId());
            entry.put(WorkflowConstants.FORM_PROPERTY_DEFAULT_EXPRESSION_ATTR, handler.getDefaultExpression());
            entry.put(WorkflowConstants.FORM_PROPERTY_VARIABLE_EXPRESSION_ATTR, handler.getVariableExpression());
            entry.put(WorkflowConstants.FORM_PROPERTY_VARIABLE_NAME_ATTR, handler.getVariableName());
            entry.put(WorkflowConstants.NAME_ATTR, handler.getName());
            Map<String, Object> type = new HashMap<>(3);
            if (handler.getType() != null) {
                type.put(WorkflowConstants.NAME_ATTR, handler.getType().getName());
                type.put(WorkflowConstants.FORM_PROPERTY_ENUM_VALUES_ATTR, handler.getType().getInformation("values"));
                type.put(WorkflowConstants.FORM_PROPERTY_DATE_PATTERN_ATTR, handler.getType().getInformation("datePattern"));
            }
            entry.put(WorkflowConstants.FORM_PROPERTY_TYPE_ATTR, type);
            entry.put(WorkflowConstants.FORM_PROPERTY_READABLE_ATTR, handler.isReadable());
            entry.put(WorkflowConstants.FORM_PROPERTY_REQUIRED_ATTR, handler.isRequired());
            entry.put(WorkflowConstants.FORM_PROPERTY_WRITABLE_ATTR, handler.isWritable());
            result.add(entry);
        }
        return result;
    }

}
