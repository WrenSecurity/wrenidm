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
package com.forgerock.openidm.staging.test.mock;

import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Note: This mock is not generic mock for IdmModelPortType. It is hardcoded for junit tests in class StagingServiceTest
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
class IdmModelPortTypeMock implements ModelPortType {

    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IdmModelPortTypeMock.class);
    private UserType modifiedUser;

    public IdmModelPortTypeMock() {
    }

    public String addObject(ObjectContainerType objectContainer) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ObjectContainerType getObject(String oid, PropertyReferenceListType resolve) throws FaultMessage {
        if ("32769".equals(oid)) {
            ObjectContainerType objectContainer = new ObjectContainerType();
            ObjectType objectType = new UserType();
            objectType.setOid(oid);
            objectContainer.setObject(objectType);
            return objectContainer;
        }
        if ("333".equals(oid)) {
            ObjectContainerType objectContainer = new ObjectContainerType();
            ObjectType objectType = new ResourceType();
            objectType.setOid(oid);
            objectContainer.setObject(objectType);
            return objectContainer;
        }

        if ("12345".equals(oid)) {
            ObjectContainerType objectContainer = new ObjectContainerType();
            ObjectType objectType = this.modifiedUser;
            objectContainer.setObject(objectType);
            return objectContainer;
        }

        return null;

    }

    @Override
    public ObjectListType listObjects(String objectType, PagingType paging) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ObjectListType searchObjects(QueryType query, PagingType paging) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void modifyObject(ObjectModificationType objectChange) throws FaultMessage {
        modifyUser(objectChange);
    }

    public void deleteObject(String oid) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PropertyAvailableValuesListType getPropertyAvailableValues(String oid, PropertyReferenceListType properties) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UserContainerType listAccountShadowOwner(String accountOid) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceObjectShadowListType listResourceObjectShadows(String resourceOid, String resourceObjectShadowType) throws FaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String addUser(UserContainerType userContainer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UserContainerType getUser(String oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UserListType listUsers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void mergeChanges(Object object, ObjectModificationType objectChange) {
        logger.info("[ModelMock] mergeChanges begin");
        Validate.notNull(objectChange);

        List<PropertyModificationType> changes = objectChange.getPropertyModification();

        Map changedProperties = new HashMap();
        for (PropertyModificationType change : changes) {
            if ((0 == PropertyModificationTypeType.replace.compareTo(change.getModificationType())) ||
                    (0 == PropertyModificationTypeType.add.compareTo(change.getModificationType()))) {
                ElementImpl element = (ElementImpl) change.getValue().getAny().get(0);
                changedProperties.put(change.getPath(), (String) element.getTextContent());
            }
            if (0 == PropertyModificationTypeType.delete.compareTo(change.getModificationType())) {
                changedProperties.put(change.getPath(), null);
            }
        }

        try {
            BeanUtils.populate(object, changedProperties);
        } catch (IllegalAccessException ex) {
            logger.warn("Failed to populated properties. Object was {}, Properties were {}. Error message was {}", new Object[]{object, changedProperties, ex.getMessage()});
        } catch (InvocationTargetException ex) {
            logger.warn("Failed to populated properties. Object was {}, Properties were {}. Error message was {}", new Object[]{object, changedProperties, ex.getMessage()});
        }

        logger.info("[ModelMock] mergeChanges end");
    }

    public void modifyUser(ObjectModificationType objectChange) {
        UserType oldUserType = new UserType();
        oldUserType.setFamilyName("Hrasko");
        oldUserType.setFullName("Janko Mrkvicka");
        oldUserType.setGivenName("Janko");
        oldUserType.setName("name");
        oldUserType.setOid("12345");
        oldUserType.setVersion("1.0");

        mergeChanges(oldUserType, objectChange);

        this.modifiedUser = oldUserType;

    }

    public void deleteUser(String oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
