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
package org.wrensecurity.wrenidm.workflow.flowable.impl.session;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.Session;
import org.flowable.common.engine.impl.interceptor.SessionFactory;
import org.forgerock.script.ScriptRegistry;

/**
 * A session factory producing a {@link IdmSession} instance.
 */
public class IdmSessionFactory implements SessionFactory {

    private ScriptRegistry scriptRegistry;

    public IdmSessionFactory() {
    }

    public IdmSessionFactory(ScriptRegistry scriptRegistry) {
        this.scriptRegistry = scriptRegistry;
    }

    public void setScriptRegistry(ScriptRegistry scriptRegistry) {
        this.scriptRegistry = scriptRegistry;
    }

    @Override
    public Class<?> getSessionType() {
        return IdmSession.class;
    }

    @Override
    public Session openSession(CommandContext commandContext) {
        return new IdmSessionImpl(scriptRegistry);
    }
}
