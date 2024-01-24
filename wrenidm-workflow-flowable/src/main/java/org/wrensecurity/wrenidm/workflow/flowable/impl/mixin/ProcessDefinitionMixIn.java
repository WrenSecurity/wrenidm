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
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Jackson's MixIn class for {@link ProcessDefinitionEntityImpl}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public abstract class ProcessDefinitionMixIn {

    @JsonProperty(WorkflowConstants.RESOURCE_ID)
    protected String id;

    @JsonProperty(WorkflowConstants.RESOURCE_REVISION)
    protected int revision;

    @JsonProperty(WorkflowConstants.KEY_ATTR)
    protected String key;

    @JsonProperty(WorkflowConstants.NAME_ATTR)
    protected String name;

    @JsonProperty(WorkflowConstants.CATEGORY_ATTR)
    protected String category;

    @JsonGetter
    protected abstract boolean isDeleted();

    @JsonProperty(WorkflowConstants.DEPLOYMENT_ID_ATTR)
    protected String deploymentId;

    @JsonProperty(WorkflowConstants.DESCRIPTION_ATTR)
    protected String description;

    @JsonProperty(WorkflowConstants.DIAGRAM_RESOURCE_NAME_ATTR)
    protected String diagramResourceName;

    @JsonProperty
    protected String resourceName;

    @JsonGetter
    protected abstract boolean isSuspended();

    @JsonGetter
    protected abstract boolean isUpdated();

}
