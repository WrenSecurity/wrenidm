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
package com.forgerock.openidm.web.dto;

import com.forgerock.openidm.util.Base64;
import com.forgerock.openidm.util.DOMUtil;
import com.forgerock.openidm.web.model.ObjectStage;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.xml.ns._public.common.common_1.ActivationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.CredentialsType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author katuska
 */
public class GuiUserDto extends UserDto {

    private boolean selected;
    private transient org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GuiUserDto.class);
    private boolean enabled = false;
    private String password1;
    private String password2;

    public GuiUserDto(ObjectStage stage) {
        super(stage);
    }

    public GuiUserDto(UserType object) {
        super(object);
    }

    public GuiUserDto(GuiUserDto user) {
        this.setFamilyName(user.getFamilyName());
        this.setFullName(user.getFullName());
        this.setGivenName(user.getGivenName());
        this.setHonorificPrefix(user.getHonorificPrefix());
        this.setHonorificSuffix(user.getHonorificSuffix());
        this.setName(user.getName());
        this.setOid(user.getOid());
        this.setVersion(user.getVersion());
        this.setEmail(user.getEmail());
        this.getAccount().addAll(user.getAccount());
        this.getAccountRef().addAll(user.getAccountRef());
    }

    public GuiUserDto() {
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        logger.info("setting user.selected with value {}", selected);
        this.selected = selected;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;

        if (password1 != null) {
            UserType user = (UserType) this.getXmlObject();
            CredentialsType credentials = user.getCredentials();
            if (credentials == null) {
                credentials = new CredentialsType();
                user.setCredentials(credentials);
            }
            CredentialsType.Password password = credentials.getPassword();
            if (password == null) {
                password = new CredentialsType.Password();
                credentials.setPassword(password);
            }

            Document document = DOMUtil.getDocument();
            Element hash = document.createElementNS(SchemaConstants.NS_C, "c:base64");
            hash.setTextContent(Base64.encode(password1));
            password.setAny(hash);
        }
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public boolean isEnabled() {
        UserType user = (UserType) this.getXmlObject();
        ActivationType activation = user.getActivation();
        if (activation != null) {
            enabled = activation.isEnabled();
        }

        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        UserType user = (UserType) this.getXmlObject();
        ActivationType activation = user.getActivation();
        if (activation == null) {
            activation = new ActivationType();
            user.setActivation(activation);
        }
        activation.setEnabled(enabled);
    }

    public void setWebAccessEnabled(boolean webAccessEnabled) {
        UserType user = (UserType) this.getXmlObject();
        CredentialsType credentials = user.getCredentials();
        if (credentials == null) {
            credentials = new CredentialsType();
            user.setCredentials(credentials);
        }
        credentials.setAllowedIdmGuiAccess(webAccessEnabled);
    }

    public boolean isWebAccessEnabled() {
        UserType user = (UserType) this.getXmlObject();
        CredentialsType credentials = user.getCredentials();

        if (credentials == null || credentials.isAllowedIdmGuiAccess() == null) {
            return false;
        }

        return credentials.isAllowedIdmGuiAccess();
    }
}
