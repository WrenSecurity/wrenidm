/*
 *
 * Copyright (c) 2011 ForgeRock Inc. All Rights Reserved
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
 * "Portions Copyrighted 2011 [name of copyright owner]"
 *
 * $Id$
 */
package com.forgerock.openidm.model.filter;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Igor Farinic
 */
public class FilterManagerImpl<T extends Filter> implements FilterManager {

    private static transient Trace trace = TraceManager.getTrace(FilterManagerImpl.class);
    private Map<String, Class<T>> filterMap;

    @Override
    public void setFilterMapping(Map filterMap) {
        this.filterMap = filterMap;
    }

    @Override
    public Filter getFilterInstance(String uri) {
        return getFilterInstance(uri, null);
    }

    @Override
    public Filter getFilterInstance(String uri, List parameters) {
        Class clazz = filterMap.get(uri);
        if (clazz == null) {
            return null;
        }

        Filter filter = null;
        try {
            filter = (Filter) clazz.newInstance();
            filter.setParameters(parameters);
        } catch (InstantiationException ex) {
            trace.error("Couln't create filter instance, reason: {}.", ex.getMessage());
            trace.debug("Couln't create filter instance.", ex);
        } catch (IllegalAccessException ex) {
            trace.error("Couln't create filter instance, reason: {}.", ex.getMessage());
            trace.debug("Couln't create filter instance.", ex);
        }

        return filter;
    }

}
