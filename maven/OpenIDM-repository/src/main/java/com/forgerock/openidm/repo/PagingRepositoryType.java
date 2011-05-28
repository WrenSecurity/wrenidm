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
package com.forgerock.openidm.repo;

import com.forgerock.openidm.xml.ns._public.common.common_1.PagingType;
import com.forgerock.openidm.xml.schema.XPathSegment;
import com.forgerock.openidm.xml.schema.XPathType;
import java.util.List;

/**
 *
 * @author Katuska
 */
public class PagingRepositoryType {

    private String orderBy;
    private int offset;
    private int maxSize;
    private String orderDirection;

    public void fillPagingProperties(PagingType paging) {

        XPathType xpath = new XPathType(paging.getOrderBy().getProperty());
        List<XPathSegment> segments = xpath.toSegments();
        if (segments.size() > 0) {
            orderBy = segments.get(segments.size() - 1).getQName().getLocalPart();
        }
        String direction = paging.getOrderDirection() == null ? null : paging.getOrderDirection().value();
        if (direction != null) {
            orderDirection = (direction.equals("ascending")) ? "asc" : "desc";
        } else {
            orderDirection = "asc";
        }
        offset = paging.getOffset().intValue();
        maxSize = paging.getMaxSize().intValue();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }
}
