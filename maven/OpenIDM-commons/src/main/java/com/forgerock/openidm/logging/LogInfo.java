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
package com.forgerock.openidm.logging;

import java.beans.ConstructorProperties;

/**
 *
 * @author Vilo Repan
 */
public class LogInfo {

    private String packageName;
    private int level;

    @ConstructorProperties({"packageName", "level"})
    public LogInfo(String packageName, int level) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name can't be null nor empty.");
        }
        this.packageName = packageName;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public int hashCode() {
       return packageName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LogInfo)) {
            return false;
        }

        LogInfo info = (LogInfo) obj;
        return packageName == null ? info.getPackageName() == null
                : packageName.equals(info.getPackageName());
    }
}
