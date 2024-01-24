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
 * Copyright 2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.command;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.form.FormHandlerHelper;
import org.flowable.engine.impl.form.StartFormHandler;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * Command for retrieving {@link StartFormHandler} instance.
 */
public class GetStartFormHandlerCommand implements Command<StartFormHandler> {

    private final ProcessDefinition processDefinition;

    public GetStartFormHandlerCommand(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    public StartFormHandler execute(CommandContext context) {
        return new FormHandlerHelper().getStartFormHandler(context, processDefinition);
    }

}
