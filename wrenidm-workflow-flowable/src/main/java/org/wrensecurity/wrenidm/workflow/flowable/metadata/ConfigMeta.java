/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License
 * for the specific language governing permission and limitations under the
 * License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each
 * file and include the License file at legal/CDDLv1.0.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2015 ForgeRock AS.
 * Portions Copyright 2018-2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.metadata.MetaDataProvider;
import org.forgerock.openidm.metadata.MetaDataProviderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrensecurity.wrenidm.workflow.flowable.impl.FlowableServiceImpl;

/**
 * Metadata provider describing requirements of the bundle.
 */
public class ConfigMeta implements MetaDataProvider {

    protected static final Logger logger = LoggerFactory.getLogger(ConfigMeta.class);

    private Map<String, List<JsonPointer>> propertiesToEncrypt;

    public ConfigMeta() {
        propertiesToEncrypt = new HashMap<>();
        List<JsonPointer> props = new ArrayList<>();
        props.add(new JsonPointer("engine/password"));
        props.add(new JsonPointer("mail/password"));
        propertiesToEncrypt.put(FlowableServiceImpl.PID, props);
    }

    @Override
    public List<JsonPointer> getPropertiesToEncrypt(String pidOrFactory, String instanceAlias, JsonValue config) {
        if (propertiesToEncrypt.containsKey(pidOrFactory)) {
            return propertiesToEncrypt.get(pidOrFactory);
        }
        return null;
    }

    @Override
    public void setCallback(MetaDataProviderCallback callback) { /* NO-OP */}
}
