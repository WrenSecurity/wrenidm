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
import java.util.List;
import org.flowable.bpmn.model.UserTask;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Jackson's MixIn class for {@link UserTask}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public abstract class UserTaskMixIn {

    @JsonProperty(WorkflowConstants.RESOURCE_ID)
    protected String id;

    @JsonProperty(WorkflowConstants.ASSIGNEE_ATTR)
    protected String assignee;

    @JsonProperty(WorkflowConstants.CATEGORY_ATTR)
    protected String category;

    @JsonProperty(WorkflowConstants.CANDIDATE_GROUP_ATTR)
    protected List<String> candidateGroups;

    @JsonProperty(WorkflowConstants.CANDIDATE_USER_ATTR)
    protected List<String> candidateUsers;

    @JsonProperty(WorkflowConstants.DUE_DATE_ATTR)
    protected String dueDate;

    @JsonGetter
    protected abstract boolean isExclusive();

    @JsonProperty(WorkflowConstants.NAME_ATTR)
    protected String name;

    @JsonProperty(WorkflowConstants.PRIORITY_ATTR)
    protected String priority;

}
