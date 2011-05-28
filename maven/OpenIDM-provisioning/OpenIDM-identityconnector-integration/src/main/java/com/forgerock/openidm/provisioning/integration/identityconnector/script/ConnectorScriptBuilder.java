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
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class ConnectorScriptBuilder {

    public static final String code_id = "$Id$";
    private ResourceType _connector;
    private String _defExecMode;
    //private ResourceAction _resourceAction;

    public ConnectorScriptBuilder(ResourceType connector, String defExecMode) {
        this._connector = connector;
        this._defExecMode = defExecMode;
    }

    public ConnectorScript build()
            throws OpenIDMException {
        checkState();

//        String resourceTypeString = this._connector.getConfiguration().getTypeString();
//
//        String scriptText = this._resourceAction.getAction(resourceTypeString);
//        String scriptLanguage = this._resourceAction.getActionType(resourceTypeString);
//        String execMode = this._resourceAction.getExecMode(resourceTypeString);
//        String actionName = this._resourceAction.getName();
//
//        if (scriptLanguage == null) {
//            scriptLanguage = this._connector.getConfiguration().getDefaultActionType();
//        }
//
//        if (execMode == null) {
//            execMode = this._defExecMode;
//        }
//
//        if (execMode == null) {
////            ActionExecModes actionExecModes = this._connector.getConfiguration().getActionExecModes();
////            if (actionExecModes != null) {
////                execMode = actionExecModes.getExecMode(scriptLanguage);
////            }
//        }
//
//        if (execMode == null) {
//            //e(Severity.ERROR, "ERR_MISSING_ACTION_EXECMODE", actionName);
//            throw new OpenIDMException();
//        }

        ConnectorScript connScriptContext = new ConnectorScript();
//        connScriptContext.getScriptContextBuilder().setScriptText(scriptText);
//        connScriptContext.getScriptContextBuilder().setScriptLanguage(scriptLanguage);
//        connScriptContext.setActionName(actionName);
//        connScriptContext.setExecMode(execMode);
        connScriptContext.setResourceName(this._connector.getName());

        return connScriptContext;
    }

//    public static ConnectorScript build(ResourceType connector, ResourceAction resAction, String defExecMode)
//            throws OpenIDMException {
//        ConnectorScript connScript = null;
//
//        if (resAction != null) {
//            ConnectorScriptBuilder connCtxBuilder = new ConnectorScriptBuilder(connector, defExecMode);
//
//            connCtxBuilder.setResourceAction(resAction);
//            connScript = connCtxBuilder.build();
//        }
//
//        return connScript;
//    }


//    private List<ResourceAction> findResourceAction(ResourceObjectShadowType shadow, String operation, String timing) {
//        return new ArrayList<ResourceAction>(0);
//    }

    public static List<ConnectorScript> buildAll(ResourceType connector, ResourceObjectShadowType shadow, String defExecMode, String operation, String timing)
            throws OpenIDMException {
        ConnectorScriptBuilder connCtxBuilder = new ConnectorScriptBuilder(connector, defExecMode);
        List connScripts = new ArrayList();
//        for (ResourceAction resAction: findResourceAction(shadow,operation,timing)) {
//            connCtxBuilder.setResourceAction(resAction);
//            ConnectorScript connScript = connCtxBuilder.build();
//
//            if (connScript.getScriptContextBuilder().getScriptLanguage().equalsIgnoreCase("SHELL")) {
//                connScript.getOperationOptionsBuilder().setOption("variablePrefix", "EXEC_");
//            }
//
//            connScripts.add(connScript);
//        }

        return connScripts;
    }

    public ResourceType getconnector() {
        return this._connector;
    }

//    public ResourceAction getResourceAction() {
//        return this._resourceAction;
//    }
//
//    public void setResourceAction(ResourceAction resourceAction) {
//        this._resourceAction = resourceAction;
//    }

    private void checkState() throws IllegalStateException {
        if (this._connector == null) {
            throw new IllegalStateException("Value 'resource' cannot be null.");
        }

//        if (this._resourceAction == null) {
//            throw new IllegalStateException("Value 'resourceAction' cannot be null.");
//        }
    }
}
