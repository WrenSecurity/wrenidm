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
package org.wrensecurity.wrenidm.workflow.flowable.impl.mixin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import java.util.Map;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Jackson's MixIn class for {@link TaskEntityImpl}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public abstract class TaskEntityMixIn {

    @JsonProperty(WorkflowConstants.RESOURCE_ID)
    protected String id;

    @JsonProperty(WorkflowConstants.RESOURCE_REVISION)
    protected int revision;

    @JsonGetter
    protected abstract boolean isCanceled();

    @JsonProperty(WorkflowConstants.CATEGORY_ATTR)
    protected String category;

    @JsonProperty(WorkflowConstants.CLAIM_TIME_ATTR)
    @JsonSerialize(using = DateSerializer.class)
    protected Date claimTime;

    @JsonProperty(WorkflowConstants.CREATE_TIME_ATTR)
    @JsonSerialize(using = DateSerializer.class)
    protected Date createTime;

    @JsonGetter
    protected abstract boolean isDeleted();

    @JsonProperty(WorkflowConstants.DESCRIPTION_ATTR)
    protected String description;

    @JsonProperty(WorkflowConstants.DUE_DATE_ATTR)
    @JsonSerialize(using = DateSerializer.class)
    protected Date dueDate;

    @JsonProperty(WorkflowConstants.EXECUTION_ID_ATTR)
    protected String executionId;

    @JsonProperty
    protected String formKey;

    @JsonProperty(WorkflowConstants.NAME_ATTR)
    protected String name;

    @JsonProperty(WorkflowConstants.OWNER_ATTR)
    protected String owner;

    @JsonProperty
    protected String parentTaskId;

    @JsonProperty(WorkflowConstants.PRIORITY_ATTR)
    protected int priority;

    @JsonGetter
    protected abstract Map<String, Object> getProcessVariables();

    @JsonProperty(WorkflowConstants.PROCESS_DEFINITION_ID_ATTR)
    protected String processDefinitionId;

    @JsonProperty(WorkflowConstants.PROCESS_INSTANCE_ID_ATTR)
    protected String processInstanceId;

    @JsonProperty(WorkflowConstants.TASK_DEFINITION_KEY_ATTR)
    protected String taskDefinitionKey;

    @JsonGetter
    protected abstract Map<String, Object> getTaskLocalVariables();

    @JsonGetter
    protected abstract boolean isUpdated();

}
