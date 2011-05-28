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

package com.forgerock.openidm.web.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Vilo Repan
 */
@Controller
@Scope("session")
public class ResourceListController {

    private List<ResourceListItem> resourceList = new ArrayList<ResourceListItem>();

    public ResourceListController() {
        ResourceListItem item;

        item = new ResourceListItem();
        item.setDisplayName("Directory Server v7 EE");
        item.setDescription("Server used for testing purposes.");
        item.setStatus("Connected, used, whatever");
        resourceList.add(item);

        item = new ResourceListItem();
        item.setDisplayName("Active Directory");
        item.setDescription("AD DS on Windows 2008.");
        item.setStatus("Disconnected");
        resourceList.add(item);
    }

    public List<ResourceListItem> getResourceList() {
        return resourceList;
    }

    private class ResourceListItem {

        private boolean selected;
        private String displayName;
        private String description;
        private String status;

        public String getDescription() {
            return description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getStatus() {
            return status;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
