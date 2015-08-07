/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*global define */

define("org/forgerock/openidm/ui/admin/delegates/ScriptDelegate", [
    "underscore",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/AbstractDelegate"
], function(_, constants, AbstractDelegate) {

    var obj = new AbstractDelegate(constants.host + "/openidm/script");

    obj.evalScript = function(script, additionalGlobals) {

        if(_.isUndefined(script.globals) || _.isNull(script.globals)) {
            script.globals = {};
        }

        if(additionalGlobals) {
            script.globals = _.extend(script.globals, additionalGlobals);
        }

        return obj.serviceCall({
            url: "?_action=eval",
            type: "POST",
            data: JSON.stringify(script),
            errorsHandlers : {
                "error": {
                    status: "500"
                }
            }
        });
    };

    obj.evalLinkQualifierScript = function(script) {
        script.globals = script.globals || {};
        script.globals.returnAll = true;

        return obj.evalScript(script);
    };

    return obj;
});