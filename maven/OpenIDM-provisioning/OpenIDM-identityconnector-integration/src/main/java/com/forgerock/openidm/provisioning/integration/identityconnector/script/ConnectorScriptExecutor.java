/*
 * 
 * Copyright (c) 2010 ForgeRock Inc. All Rights Reserved
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1.php or
 * OpenIDM/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at OpenIDM/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2010 [name of copyright owner]"
 * 
 * $Id$
 */
package com.forgerock.openidm.provisioning.integration.identityconnector.script;

import com.forgerock.openidm.api.exceptions.OpenIDMException;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.objects.ScriptContextBuilder;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class ConnectorScriptExecutor {

    public static final String code_id = "$Id$";

    public Object execute(ConnectorFacade connector, ConnectorScript script)
            throws OpenIDMException {

        String method = "execute";

        ScriptContextBuilder builder = script.getScriptContextBuilder();
        String scriptLanguage = builder.getScriptLanguage();
        String actionName = script.getActionName();
        String execMode = script.getExecMode();
        //log("execute", "Executing " + scriptLanguage + " resource action '" + actionName + "'");

        ScriptOnResourceApiOp scriptOnResource = (ScriptOnResourceApiOp) connector.getOperation(ScriptOnResourceApiOp.class);
        ScriptOnConnectorApiOp scriptOnConnector = (ScriptOnConnectorApiOp) connector.getOperation(ScriptOnConnectorApiOp.class);       

        ScriptContext scriptContext = builder.build();
        OperationOptions opOptions = script.getOperationOptionsBuilder().build();

        Object scriptResult = null;

        if (execMode.equals("connector")) {
            if (scriptOnConnector == null) {
                //(Severity.ERROR, "ERR_UNSUPPORTED_CONN_OP", "ScriptOnConnector");
                throw new OpenIDMException();
            }
            try {
                scriptResult = scriptOnConnector.runScriptOnConnector(scriptContext, opOptions);
            } catch (Exception e) {
                //(Severity.ERROR, "ERR_CONN_SCRIPT_EXEC_FAILED", new Object[]{actionName, script.getResourceName()});
                throw new OpenIDMException();
            }

        }

        if (execMode.equals("resource")) {
            if (scriptOnResource == null) {
                //(Severity.ERROR, "ERR_UNSUPPORTED_CONN_OP", "ScriptOnResource");
                throw new OpenIDMException();
            }
            try {
                scriptResult = scriptOnResource.runScriptOnResource(scriptContext, opOptions);
            } catch (Exception e) {
                //(Severity.ERROR, "ERR_CONN_SCRIPT_EXEC_FAILED", new Object[]{actionName, script.getResourceName()});
                throw new OpenIDMException();
            }

        }

        return scriptResult;
    }
}
