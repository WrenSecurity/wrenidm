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

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * An abstract implementation of {@link Identifiable}.
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class IdentifiableBase implements Identifiable, Serializable {

    public static final String code_id = "$Id$";
    /**
     * The ID.
     */
    private UUID oid;    

    /**
     * {@inheritDoc}
     */
    @Id
    @GenericGenerator(name = "IdGenerator", strategy = "com.forgerock.openidm.model.UUIDGenerator")
    @GeneratedValue(generator = "IdGenerator")
    @Type(type = "com.forgerock.openidm.hibernate.usertype.UUIDType")
    @Columns(columns = {@Column(name = "uuid", length = 36)})
    @Override
    public UUID getOid() {
        return oid;
    }

    /**
     * {@inheritDoc}
     */
    public void setOid(final UUID oid) {
        this.oid = oid;
    }
}
