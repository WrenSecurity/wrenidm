/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2025 Wren Security. All rights reserved.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.resource;

import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.resource.Responses.newActionResponse;
import static org.forgerock.json.resource.Responses.newQueryResponse;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnCollection;
import static org.forgerock.openidm.util.ResourceUtil.notSupportedOnInstance;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.flowable.engine.ProcessEngine;
import org.flowable.job.api.DeadLetterJobQuery;
import org.flowable.job.api.Job;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntity;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntityImpl;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.json.resource.CountPolicy;
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
import org.forgerock.util.promise.Promise;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.mixin.DeadLetterJobEntityMixIn;

/**
 * Resource handling queries related to the {@link DeadLetterJobEntity} objects.
 */
public class DeadLetterJobEntityResource implements CollectionResourceProvider {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(DeadLetterJobEntityImpl.class, DeadLetterJobEntityMixIn.class)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private final ProcessEngine processEngine;

    public DeadLetterJobEntityResource(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionCollection(Context context, ActionRequest request) {
        return notSupportedOnCollection(request).asPromise();
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionInstance(Context context, String resourceId, ActionRequest request) {
        if ("retry".equals(request.getAction())) {
            Job job = processEngine.getManagementService().createDeadLetterJobQuery().jobId(resourceId).singleResult();
            if (job == null) {
                return new NotFoundException().asPromise();
            }
            try {
                processEngine.getManagementService().moveDeadLetterJobToExecutableJob(job.getId(), 1);
            } catch (Exception e) {
                return new InternalServerErrorException("Failed to retry dead letter job '" + job.getId() + "'.").asPromise();
            }
            return newActionResponse(new JsonValue(Map.of("Successfully retried dead letter job", job.getId()))).asPromise();
        }
        return new BadRequestException("Unknown action '" + request.getAction() + "'.").asPromise();
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
        if (!WorkflowConstants.QUERY_ALL_IDS.equals(request.getQueryId())
                && !WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
            return new BadRequestException("Unknown query '" + request.getQueryId() + "' to get dead letter jobs.").asPromise();
        }
        try {
            DeadLetterJobQuery query = processEngine.getManagementService().createDeadLetterJobQuery();
            if (WorkflowConstants.QUERY_FILTERED.equals(request.getQueryId())) {
                applyRequestParams(query, request);
                applySortKeys(query, request);
            }
            List<Job> results = request.getPageSize() == 0 ? query.list() : query.listPage(
                    request.getPagedResultsOffset(), request.getPageSize());
            for (Job job : results) {
                JsonValue content = json(mapper.convertValue(job, Map.class));
                handler.handleResource(newResourceResponse(job.getId(), null, content));
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
        } catch (NotSupportedException e) {
            return e.asPromise();
        } catch (Exception e) {
            return new InternalServerErrorException(e.getMessage(), e).asPromise();
        }
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, String resourceId, ReadRequest request) {
        try {
            Job job = processEngine.getManagementService().createDeadLetterJobQuery().jobId(resourceId).singleResult();
            if (job == null) {
                return new NotFoundException().asPromise();
            }
            JsonValue content = json(mapper.convertValue(job, Map.class));
            return newResourceResponse(job.getId(), null, content).asPromise();
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
    private void applyRequestParams(DeadLetterJobQuery query, QueryRequest request) {
        for (Entry<String, String> param : request.getAdditionalParameters().entrySet()) {
            switch (param.getKey()) {
                case WorkflowConstants.EXECUTION_ID_ATTR:
                    query.executionId(param.getValue());
                    break;
                case WorkflowConstants.JOB_ID_ATTR:
                    query.jobId(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_DEFINITION_ID_ATTR:
                    query.processDefinitionId(param.getValue());
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                    query.processInstanceId(param.getValue());
                    break;
            }
        }
    }

    /**
     * Apply sort keys from the specified request.
     *
     * @param query query to apply sort keys
     * @param request request to get sort keys
     */
    private void applySortKeys(DeadLetterJobQuery query, QueryRequest request) throws NotSupportedException {
        for (SortKey key : request.getSortKeys()) {
            if (key.getField() == null || key.getField().isEmpty()) {
                continue;
            }
            switch (key.getField().toString().substring(1)) { // Remove leading JsonPointer slash
                case WorkflowConstants.CREATE_TIME_ATTR:
                    query.orderByJobCreateTime();
                    break;
                case WorkflowConstants.EXECUTION_ID_ATTR:
                    query.orderByExecutionId();
                    break;
                case WorkflowConstants.RESOURCE_ID:
                    query.orderByJobId();
                    break;
                case WorkflowConstants.PROCESS_INSTANCE_ID_ATTR:
                    query.orderByProcessInstanceId();
                    break;
                case WorkflowConstants.RETRIES_ATTR:
                    query.orderByJobRetries();
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
