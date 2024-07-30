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
package org.wrensecurity.wrenidm.workflow.flowable.impl.mixin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import java.util.Map;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Jackson's MixIn class for {@link HistoricProcessInstanceEntityImpl}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public abstract class HistoricProcessInstanceMixIn {

    @JsonProperty(WorkflowConstants.RESOURCE_ID)
    protected String id;

    @JsonProperty(WorkflowConstants.BUSINESS_KEY_ATTR)
    protected String businessKey;

    @JsonProperty(WorkflowConstants.BUSINESS_STATUS_ATTR)
    protected String businessStatus;

    @JsonProperty(WorkflowConstants.DELETE_REASON_ATTR)
    protected String deleteReason;

    @JsonGetter
    protected abstract boolean isDeleted();

    @JsonProperty
    protected String deploymentId;

    @JsonProperty
    protected String description;

    @JsonProperty(WorkflowConstants.DURATION_IN_MILLIS_ATTR)
    protected Long durationInMillis;

    @JsonProperty(WorkflowConstants.END_TIME_ATTR)
    @JsonSerialize(using = DateSerializer.class)
    protected Date endTime;

    @JsonProperty
    protected String name;

    @JsonProperty(WorkflowConstants.PROCESS_DEFINITION_ID_ATTR)
    protected String processDefinitionId;

    @JsonProperty
    protected String processDefinitionName;

    @JsonProperty(WorkflowConstants.START_TIME_ATTR)
    @JsonSerialize(using = DateSerializer.class)
    protected Date startTime;

    @JsonProperty(WorkflowConstants.START_USER_ID_ATTR)
    protected String startUserId;

    @JsonProperty(WorkflowConstants.SUPER_PROCESS_INSTANCE_ID_ATTR)
    protected String superProcessInstanceId;

    @JsonProperty(WorkflowConstants.PROCESS_DEFINITION_RESOURCE_NAME_ATTR)
    protected String processDefinitionResourceName;

    @JsonGetter
    protected abstract Map<String, Object> getProcessVariables();

}
