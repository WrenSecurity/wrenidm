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
 * Copyright 2011-2015 ForgeRock AS.
 * Portions Copyright 2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.scripting;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.script.Bindings;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.impl.javax.el.ELContext;
import org.flowable.common.engine.impl.javax.el.ELResolver;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;
import org.forgerock.json.JsonValue;
import org.forgerock.script.ScriptEntry;
import org.forgerock.script.ScriptName;
import org.forgerock.script.ScriptRegistry;
import org.forgerock.script.groovy.FunctionClosure;
import org.forgerock.util.LazyMap;
import org.wrensecurity.wrenidm.workflow.flowable.WorkflowConstants;
import org.wrensecurity.wrenidm.workflow.flowable.impl.FlowableContext;
import org.wrensecurity.wrenidm.workflow.flowable.impl.session.IdmSession;

/**
 * Component for resolving 'openidm' variable in expressions.
 */
public class IdmELResolver extends ELResolver {

    private Map<String, JavaDelegate> delegateMap = new HashMap<String, JavaDelegate>();

    public IdmELResolver(Map<String, JavaDelegate> delegateMap) {
        this.delegateMap = delegateMap;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        // Retrieve script registry to get script to execute
        IdmSession session = Context.getCommandContext().getSession(IdmSession.class);
        ScriptRegistry scriptRegistry = session.getIdmScriptRegistry();
        // Prepare script bindings
        Bindings bindings = null;
        String key = (String) property;
        try {
            JsonValue idmContext = (JsonValue) context.getELResolver().getValue(context, null, WorkflowConstants.OPENIDM_CONTEXT);
            org.forgerock.services.context.Context workflowContext = new FlowableContext(idmContext, this.getClass().getClassLoader());
            ScriptEntry script = scriptRegistry.takeScript(new ScriptName("FlowableScript", "groovy"));
            if (script == null) {
                Map<String, String> properties = new HashMap<>(3);
                properties.put("source", "");
                properties.put("type", "groovy");
                properties.put("name", "FlowableScript");
                script = scriptRegistry.takeScript(new JsonValue(properties));
            }
            bindings = script.getScriptBindings(workflowContext, null);
        } catch (Exception e) {
            throw new FlowableException("Failed to prepare script bindings.", e);
        }
        // Get property value
        if (base == null) {
            if (bindings.containsKey(key)) {
                context.setPropertyResolved(true);
                return bindings.get(key);
            } else {
                for (String name : delegateMap.keySet()) {
                    if (name.equalsIgnoreCase(key)) {
                        context.setPropertyResolved(true);
                        return delegateMap.get(name);
                    }
                }
            }
        }
        // Fetching of the openidmcontext sets it to true, we need to set it to false again if the property was not found
        context.setPropertyResolved(false);
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) { /* NO-OP */}

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object arg) {
        return Object.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object arg1, Object arg2) {
        return Object.class;
    }

    /**
     * Invoked when openidm.xxx() function is called from an {@link Expression}.
     * @return result of the function call.
     */
    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        if (base instanceof LazyMap && ((LazyMap<?, ?>) base).containsKey(method)) {
            context.setPropertyResolved(true);
            FunctionClosure function = (FunctionClosure) ((LazyMap<?, ?>) base).get(method);
            return function.doCall(params);
        }
        return null;
    }
}
