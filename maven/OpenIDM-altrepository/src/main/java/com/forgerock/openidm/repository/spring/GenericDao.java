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
package com.forgerock.openidm.repository.spring;

import com.forgerock.openidm.model.SimpleDomainObject;
import java.util.List;
import java.util.UUID;

/**
 * TODO
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public interface GenericDao {

    List<SimpleDomainObject> findAll();

    List<SimpleDomainObject> findAllOfType(final String dtype);

    SimpleDomainObject findById(UUID id);

    SimpleDomainObject flush(SimpleDomainObject entity);

    SimpleDomainObject merge(SimpleDomainObject entity);

    void persist(SimpleDomainObject entity);

    void refresh(SimpleDomainObject entity);

    void remove(SimpleDomainObject entity);

    //void remove(String oid);

    Integer removeAll();

}
