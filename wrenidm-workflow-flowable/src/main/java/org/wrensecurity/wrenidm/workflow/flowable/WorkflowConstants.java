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
 * Copyright 2013-2015 ForgeRock AS.
 * Portions Copyright 2017-2024 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable;

import org.forgerock.json.JsonPointer;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.openidm.core.ServerConstants;

/**
 * Workflow-related constants
 */
public class WorkflowConstants {

    //~ Resource related constants
    public static final String RESOURCE_ID = ResourceResponse.FIELD_CONTENT_ID;
    public static final String RESOURCE_REVISION = ResourceResponse.FIELD_CONTENT_REVISION;
    public static final String QUERY_ALL_IDS = ServerConstants.QUERY_ALL_IDS;
    public static final String QUERY_FILTERED = "filtered-query";
    public static final String VARIABLE_QUERY_PREFIX = "var-";
    public static final String QUERY_LIKE_CONDITION = "Like";
    public static final String OPENIDM_CONTEXT = "openidmcontext";
    public static final String PROCESS_VARIABLES_ATTR = "processVariables";

    //~ Flowable related attributes
    public static final String PROCESS_DEFINITION_ID_ATTR = "processDefinitionId";
    public static final String PROCESS_DEFINITION_KEY_ATTR = "processDefinitionKey";
    public static final String PROCESS_DEFINITION_RESOURCE_NAME_ATTR = "processDefinitionResourceName";
    public static final String PROCESS_INSTANCE_BUSINESS_KEY_ATTR = "processInstanceBusinessKey";
    public static final String PROCESS_INSTANCE_ID_ATTR = "processInstanceId";
    public static final String DIAGRAM_RESOURCE_NAME_ATTR = "processDiagramResourceName";
    public static final String FORM_RESOURCE_KEY_ATTR = "formResourceKey";
    public static final String DEPLOYMENT_ID_ATTR = "deploymentId";
    public static final String KEY_ATTR = "key";
    public static final String START_TIME_ATTR = "startTime";
    public static final String END_TIME_ATTR = "endTime";
    public static final String STATUS_ATTR = "status";
    public static final String BUSINESS_KEY_ATTR = "businessKey";
    public static final String DELETE_REASON_ATTR = "deleteReason";
    public static final String DURATION_IN_MILLIS_ATTR = "durationInMillis";
    public static final String TASK_NAME_ATTR = "taskName";
    public static final String ASSIGNEE_ATTR = "assignee";
    public static final String DESCRIPTION_ATTR = "description";
    public static final String NAME_ATTR = "name";
    public static final String OWNER_ATTR = "owner";
    public static final String CREATE_TIME_ATTR = "createTime";
    public static final String DUE_DATE_ATTR = "dueDate";
    public static final String EXECUTION_ID_ATTR = "executionId";
    public static final String CANDIDATE_GROUP_ATTR = "taskCandidateGroup";
    public static final String CANDIDATE_USER_ATTR = "taskCandidateUser";
    public static final String START_USER_ID_ATTR = "startUserId";
    public static final String SUPER_PROCESS_INSTANCE_ID_ATTR = "superProcessInstanceId";
    public static final String TASK_ID_ATTR = "taskId";
    public static final String PRIORITY_ATTR = "priority";
    public static final String TASK_DEFINITION_KEY_ATTR = "taskDefinitionKey";
    public static final String VARIABLES_ATTR = "variables";
    public static final String DELEGATE_ATTR = "delegate";
    public static final String VERSION_ATTR = "version";
    public static final String CATEGORY_ATTR = "category";
    public static final String CLAIM_TIME_ATTR = "claimTime";
    public static final String INVOLVED_USER_ID_ATTR = "involvedUserId";
    public static final String TENANT_ID_ATTR = "tenantId";
    public static final String UNASSIGNED_ATTR = "unassigned";
    public static final String FINISHED_ATTR = "finished";
    public static final String UNFINISHED_ATTR = "unfinished";
    public static final String PROCESS_FINISHED_ATTR = "processFinished";
    public static final String PROCESS_UNFINISHED_ATTR = "processUnfinished";
    public static final String STARTED_AFTER_ATTR = "startedAfter";
    public static final String STARTED_BEFORE_ATTR = "startedBefore";
    public static final String FINISHED_AFTER_ATTR = "finishedAfter";
    public static final String FINISHED_BEFORE_ATTR = "finishedBefore";
    public static final JsonPointer DIAGRAM_ATTR = new JsonPointer("/diagram");
    public static final String FORM_PROPERTIES_ATTR = "formProperties";
    public static final String FORM_PROPERTY_ID_ATTR = "id";
    public static final String FORM_PROPERTY_TYPE_ATTR = "type";
    public static final String FORM_PROPERTY_VALUE_ATTR = "value";
    public static final String FORM_PROPERTY_READABLE_ATTR = "readable";
    public static final String FORM_PROPERTY_REQUIRED_ATTR = "required";
    public static final String FORM_PROPERTY_WRITABLE_ATTR = "writable";
    public static final String FORM_PROPERTY_VARIABLE_NAME_ATTR = "variableName";
    public static final String FORM_PROPERTY_DEFAULT_EXPRESSION_ATTR = "defaultExpression";
    public static final String FORM_PROPERTY_VARIABLE_EXPRESSION_ATTR = "variableExpression";
    public static final String FORM_PROPERTY_ENUM_VALUES_ATTR = "values";
    public static final String FORM_PROPERTY_DATE_PATTERN_ATTR = "datePattern";
    public static final String FORM_GENERATION_TEMPLATE_ATTR = "formGenerationTemplate";
}
