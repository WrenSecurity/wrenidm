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

import com.forgerock.openidm.web.model.ObjectStage;
import com.forgerock.openidm.web.model.ResourceDto;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;

/**
 *
 * @author katuska
 */
public class GuiResourceDto extends ResourceDto{

   private String connectorUsed;
   private String connectorVersion;
   private boolean selected;

    public GuiResourceDto(ResourceType object) {
        super(object);
    }

    public GuiResourceDto(ObjectStage stage) {
        super(stage);
    }

    public GuiResourceDto() {
    }

  


   public String getConnectorUsed() {
        return connectorUsed;
    }

    public void setConnectorUsed(String connectorUsed) {
        this.connectorUsed = connectorUsed;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public void setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
    }

   


    @Override
    public String toString() {
        return super.getName();
    }



}
