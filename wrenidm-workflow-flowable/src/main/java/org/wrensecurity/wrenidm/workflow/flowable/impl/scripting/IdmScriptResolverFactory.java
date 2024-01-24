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
 * Portions copyright 2017-2024 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.scripting;

import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ScriptTask;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.scripting.Resolver;
import org.flowable.common.engine.impl.scripting.ResolverFactory;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.variable.api.delegate.VariableScope;
import org.forgerock.json.JsonValue;
import org.forgerock.script.ScriptEntry;
import org.forgerock.script.ScriptName;
import org.forgerock.script.ScriptRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.FlowableContext;
import org.wrensecurity.wrenidm.workflow.flowable.impl.session.IdmSession;

/**
 * Component for resolving 'openidm' variable in scripts.
 */
public class IdmScriptResolverFactory implements ResolverFactory {

    protected static final Logger logger = LoggerFactory.getLogger(IdmScriptResolverFactory.class);

    @Override
    public Resolver createResolver(AbstractEngineConfiguration engineConfiguration, VariableScope variableScope) {
        // Retrieve script registry to get script to execute
        IdmSession session = Context.getCommandContext().getSession(IdmSession.class);
        ScriptRegistry scriptRegistry = session.getIdmScriptRegistry();
        // Prepare IdM context
        JsonValue idmContext = (JsonValue) variableScope.getVariable(WorkflowConstants.OPENIDM_CONTEXT);
        if (idmContext == null && variableScope instanceof ExecutionEntity) {
            // Copy context from the parent execution entity
            ExecutionEntity parentEntity = ((ExecutionEntity) variableScope).getSuperExecution();
            String entityId = ((ExecutionEntity) variableScope).getId();
            String parentEntityId = parentEntity.getId();
            if (entityId != null && parentEntityId != null && !entityId.equals(parentEntity.getId())) {
                JsonValue parentContext = (JsonValue) parentEntity.getVariable(WorkflowConstants.OPENIDM_CONTEXT);
                if (parentContext != null) {
                    variableScope.setVariable(WorkflowConstants.OPENIDM_CONTEXT, parentContext);
                    idmContext = parentContext;
                } else {
                    throw new FlowableException("Unable to find idmcontext in parent execution activity.");
                }
            }
        }
        // Resolve script language
        String language = resolveScriptLanguage(variableScope);
        // Prepare script bindings
        Bindings bindings = null;
        try {
            org.forgerock.services.context.Context workflowContext = new FlowableContext(idmContext, this.getClass().getClassLoader());
            ScriptEntry script = scriptRegistry.takeScript(new ScriptName("FlowableScript", language));
            if (script == null) {
                Map<String, String> properties = new HashMap<>(3);
                properties.put("source", "");
                properties.put("type", language);
                properties.put("name", "FlowableScript");
                script = scriptRegistry.takeScript(new JsonValue(properties));
            }
            bindings = script.getScriptBindings(workflowContext, null);
        } catch (Exception e) {
            throw new FlowableException("Failed to prepare script bindings.", e);
        }
        return new IdmScriptResolver(bindings);
    }

    /**
     * Resolve language (i.e. groovy or javascript) for the specified entity.
     * Groovy language is returned if no language can be resolved.
     */
    private String resolveScriptLanguage(VariableScope variableScope) {
        String resolved = null;
        try {
            if (variableScope instanceof ExecutionEntity) {
                resolved = resolveScriptLanguage((ExecutionEntity) variableScope);
            } else if (variableScope instanceof TaskEntity) {
                resolved = resolveScriptLanguage((TaskEntity) variableScope);
            } else {
                logger.info("Script language could not be determined, using default groovy instead.");
            }
        } catch (Exception e) {
            logger.error("Failed to resolve script language.", e);
        }
        return resolved != null ? resolved : "groovy";
    }

    /**
     * See {@link #resolveScriptLanguage(VariableScope)}
     */
    private String resolveScriptLanguage(ExecutionEntity entity) {
        if (entity.getCurrentFlowElement() != null && entity.getCurrentFlowElement() instanceof ScriptTask) {
            return ((ScriptTask) entity.getCurrentFlowElement()).getScriptFormat();
        }
        if (entity.getCurrentFlowableListener() != null) {
            return resolveScriptLanguage(entity.getCurrentFlowableListener());
        }
        return null;
    }

    /**
     * See {@link #resolveScriptLanguage(VariableScope)}
     */
    private String resolveScriptLanguage(TaskEntity entity) {
        Process process = ProcessDefinitionUtil.getProcess(entity.getProcessDefinitionId());
        FlowElement task = process.getFlowElement(entity.getName(), true);
        if (task instanceof UserTask && ((UserTask) task).getTaskListeners() != null) {
            for (FlowableListener listener : ((UserTask) task).getTaskListeners()) {
                if (entity.getEventHandlerId().equals(listener.getId())) {
                    resolveScriptLanguage(listener);
                }
            }
        }
        return null;
    }

    /**
     * See {@link #resolveScriptLanguage(VariableScope)}
     */
    private String resolveScriptLanguage(FlowableListener listener) {
        if (listener.getFieldExtensions() == null) {
            return null;
        }
        for (FieldExtension field : listener.getFieldExtensions()) {
            if ("language".equalsIgnoreCase(field.getFieldName())) {
                return field.getStringValue();
            }
        }
        return null;
    }

}
