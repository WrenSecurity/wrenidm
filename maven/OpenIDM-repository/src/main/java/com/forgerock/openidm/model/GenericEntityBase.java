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

import javax.persistence.Column;
import javax.persistence.Entity;
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
@SecondaryTable(catalog = SimpleDomainObject.DDL_CATALOG, name = GenericEntityBase.DDL_TABLE_GENERICENTITIES,
pkJoinColumns = {@PrimaryKeyJoinColumn(name = "uuid", referencedColumnName = "uuid")},
uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class GenericEntityBase extends SimpleDomainObject {

    public static final String code_id = "$Id$";
    public static final String DDL_TABLE_GENERICENTITIES = "GenericEntities";

    private String objectType;

    // Fields
//    private T _info;
//    private Map<String, String> _attributes = new HashMap<String, String>(0);

    // Constructors
    // Property accessors
//    @Type(type = "org.forgerock.openidm.hibernate.usertype.XMLType")
//    @Column(name = "xml_data", length = 1073741823)
//    public T getInfo() {
//        if (null == _info) {
//            try {
//                _info = (T) ((Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
//            } catch (InstantiationException e) {
//                System.out.println(e);
//            } catch (IllegalAccessException e) {
//                System.out.println(e);
//            }
//        }
//        return (T) _info;
//    }
//
//    public void setInfo(T info) {
//        this._info = info;
//    }
    @Column(table = GenericEntityBase.DDL_TABLE_GENERICENTITIES, name = "name", unique = true, nullable = false, length = 128)
    public String getName() {
        return name;
    }

    @Column
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    
//    @CollectionOfElements(fetch = FetchType.EAGER)
//    @JoinTable(name = "ResourceAttributes",
//    joinColumns = @JoinColumn(name = "uuid"))
//    @Column(name = "attrvalue",
//    nullable = false)
//    @org.hibernate.annotations.MapKey(columns = {
//        @Column(name = "attrname")
//    })
//    public Map<String, String> getAttributes() {
//        return this._attributes;
//    }
//
//    public void setAttributes(
//            Map<String, String> resourceAttributeses) {
//        this._attributes = resourceAttributeses;
//    }
}
