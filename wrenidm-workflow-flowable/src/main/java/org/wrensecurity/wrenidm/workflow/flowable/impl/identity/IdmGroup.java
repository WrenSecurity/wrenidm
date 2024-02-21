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
package org.wrensecurity.wrenidm.workflow.flowable.impl.identity;

import org.flowable.idm.api.Group;
import org.forgerock.json.JsonValue;

/**
 * Component representing IdM role.
 */
public class IdmGroup extends JsonValue implements Group {

    private static final long serialVersionUID = 1L;

    public IdmGroup(JsonValue value) {
        super(value);
    }

    @Override
    public String getId() {
        return get(IdmIdentityService.ID_ATTR).asString();
    }

    @Override
    public void setId(String id) {
    }

    @Override
    public String getName() {
        return get(IdmIdentityService.NAME_ATTR).asString();
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void setType(String string) {
    }
}
