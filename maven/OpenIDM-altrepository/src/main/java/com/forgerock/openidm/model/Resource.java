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

import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.UniqueConstraint;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@Entity
@SecondaryTable(catalog = SimpleDomainObject.DDL_CATALOG, name = Resource.DDL_TABLE_RESOURCE,
pkJoinColumns = {@PrimaryKeyJoinColumn(name = "uuid", referencedColumnName = "uuid")},
uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@NamedQueries({
    @NamedQuery(name = Resource.QUERY_RESOURCE_FIND_ALL, query = "SELECT m FROM Resource m"),
    @NamedQuery(name = Resource.QUERY_RESOURCE_FIND_BY_NAME, query = "SELECT m FROM Resource m WHERE m.name = ?0")
})
public class Resource extends SimpleDomainObject<ResourceType> {

    public static final String code_id = "$Id$";
    public static final String DDL_TABLE_RESOURCE = "Resources";
    // queries
    public final static String QUERY_RESOURCE_FIND_BY_NAME = "Resource.findByName";
    public final static String QUERY_RESOURCE_FIND_ALL = "Resource.findAll";

    @Column(table=Resource.DDL_TABLE_RESOURCE, name = "name", unique = true, nullable = false, length = 128)
    public String getName() {
        return name;
    }

    private String type;
    private String namespace;

    @Column(table=Resource.DDL_TABLE_RESOURCE, name = "type", unique = false, nullable = false, length = 128)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(table=Resource.DDL_TABLE_RESOURCE, name = "namespace", unique = false, nullable = false, length = 128)
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }



}
