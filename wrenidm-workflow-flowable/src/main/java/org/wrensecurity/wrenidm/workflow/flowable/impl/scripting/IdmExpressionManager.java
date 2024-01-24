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
package org.wrensecurity.wrenidm.workflow.flowable.impl.scripting;

import java.util.HashMap;
import java.util.Map;
import org.flowable.common.engine.impl.el.DefaultExpressionManager;
import org.flowable.engine.delegate.JavaDelegate;
import org.osgi.service.component.ComponentConstants;

/**
 * Component for registering custom EL resolver.
 */
public class IdmExpressionManager extends DefaultExpressionManager {

    private Map<String, JavaDelegate> delegateMap = new HashMap<>();

    public IdmExpressionManager() {
        this(null);
    }

    public IdmExpressionManager(Map<Object, Object> beans) {
        super(beans);
        super.addPreDefaultResolver(new IdmELResolver(delegateMap));
    }

    public void bindService(JavaDelegate delegate, Map<String, Object> props) {
        String name = (String) props.get(ComponentConstants.COMPONENT_NAME);
        if (name == null) { // Handle blueprint services as well
            name = (String) props.get("osgi.service.blueprint.compname");
        }
        if (name != null) {
            delegateMap.put(name, delegate);
        }
    }

    public void unbindService(JavaDelegate delegate, Map<String, Object> props) {
        String name = (String) props.get(ComponentConstants.COMPONENT_NAME);
        if (delegateMap.containsKey(name)) {
            delegateMap.remove(name);
        }
    }
}
