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
 * Portions Copyright 2021-2024 Wren Security
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.identity;

import org.flowable.idm.api.User;
import org.forgerock.json.JsonValue;

/**
 * Component representing IdM user.
 */
public class IdmUser extends JsonValue implements User {

    public static final long serialVersionUID = 1L;

    public IdmUser(JsonValue value) {
        super(value);
    }

    @Override
    public String getId() {
        return get(IdmIdentityService.USERNAME_ATTR).required().asString();
    }

    @Override
    public void setId(String id) {
    }

    @Override
    public String getFirstName() {
        return get(IdmIdentityService.GIVEN_NAME_ATTR).asString();
    }

    @Override
    public void setFirstName(String firstName) {
    }

    @Override
    public String getLastName() {
        return get(IdmIdentityService.SURNAME_ATTR).asString();
    }

    @Override
    public void setLastName(String lastName) {
    }

    @Override
    public String getDisplayName() {
        return getFirstName() + " " + getLastName();
    }

    @Override
    public void setDisplayName(String displayName) {
    }

    @Override
    public String getEmail() {
        return get(IdmIdentityService.MAIL_ATTR).asString();
    }

    @Override
    public void setEmail(String email) {
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setPassword(String password) {
    }

    @Override
    public String getTenantId() {
        return null;
    }

    @Override
    public void setTenantId(String tenantId) {
    }

    @Override
    public boolean isPictureSet() {
        return false;
    }

}
