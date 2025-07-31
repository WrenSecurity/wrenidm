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
package org.wrensecurity.wrenidm.workflow.flowable.impl.mixin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntity;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Jackson's MixIn class for {@link DeadLetterJobEntity}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public class DeadLetterJobEntityMixIn {

    @JsonProperty(WorkflowConstants.RESOURCE_ID)
    protected String id;

    @JsonProperty(WorkflowConstants.RESOURCE_REVISION)
    protected int revision;

    @JsonProperty(WorkflowConstants.CREATE_TIME_ATTR)
    @JsonSerialize(using = DateSerializer.class)
    protected Date createTime;

    @JsonProperty(WorkflowConstants.EXECUTION_ID_ATTR)
    protected String executionId;

    @JsonProperty(WorkflowConstants.PROCESS_DEFINITION_ID_ATTR)
    protected String processDefinitionId;

    @JsonProperty(WorkflowConstants.PROCESS_INSTANCE_ID_ATTR)
    protected String processInstanceId;

    @JsonProperty(WorkflowConstants.RETRIES_ATTR)
    protected int retries;

}
