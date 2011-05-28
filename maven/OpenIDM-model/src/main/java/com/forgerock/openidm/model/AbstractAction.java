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
package com.forgerock.openidm.model;

import com.forgerock.openidm.model.xpath.SchemaHandling;
import com.forgerock.openidm.provisioning.service.ProvisioningService;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ScriptsType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vilo Repan
 */
public abstract class AbstractAction implements Action {

    private ModelService model;
    private ProvisioningService provisioning;
    private SchemaHandling schemaHandling;
    private List<Object> parameters;

    @Override
    public List<Object> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Object>();
        }
        return parameters;
    }

    @Override
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    protected UserType getUser(String oid) throws SynchronizationException {
        if (oid == null) {
            return null;
        }
        return SynchronizationUtils.getUser(oid, model);
    }

    public void setModel(ModelService model) {
        this.model = model;
    }

    protected ModelService getModel() {
        return model;
    }

    public ProvisioningService getProvisioning() {
        return provisioning;
    }

    public void setProvisioning(ProvisioningService provisioning) {
        this.provisioning = provisioning;
    }

    public SchemaHandling getSchemaHandling() {
        return schemaHandling;
    }

    public void setSchemaHandling(SchemaHandling schemaHandling) {
        this.schemaHandling = schemaHandling;
    }

    protected ScriptsType getScripts(ResourceType resource) {
        ScriptsType scripts = resource.getScripts();
        if (scripts == null) {
            scripts = new ScriptsType();
        }

        return scripts;
    }
}
