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
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.form.AbstractFormType;
import org.flowable.engine.impl.form.FormPropertyHandler;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;

/**
 * Jackson's MixIn class for {@link FormPropertyHandler}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public abstract class FormPropertyHandlerMixIn {

    @JsonProperty(WorkflowConstants.RESOURCE_ID)
    protected String id;

    @JsonProperty
    protected String name;

    @JsonProperty
    protected AbstractFormType type;

    @JsonGetter
    protected abstract boolean isReadable();

    @JsonGetter
    protected abstract boolean isWritable();

    @JsonGetter
    protected abstract boolean isRequired();

    @JsonProperty
    protected String variableName;

    @JsonProperty
    protected Expression variableExpression;

    @JsonProperty
    protected Expression defaultExpression;

}
