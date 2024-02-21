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
import org.flowable.engine.impl.form.TaskFormHandler;

/**
 * Command for retrieving {@link TaskFormHandler} instance.
 */
public class GetTaskFormHandlerCommand implements Command<TaskFormHandler> {

    private final String processDefinitionId;
    private final String taskId;

    public GetTaskFormHandlerCommand(String processDefinitionId, String taskId) {
        this.processDefinitionId = processDefinitionId;
        this.taskId = taskId;
    }

    @Override
    public TaskFormHandler execute(CommandContext context) {
        return new FormHandlerHelper().getTaskFormHandlder(processDefinitionId, taskId);
    }

}
