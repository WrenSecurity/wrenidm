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
package com.forgerock.openidm.web.model.impl;

import com.forgerock.openidm.web.consumer.ModelService;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.diff.DiffException;
import com.forgerock.openidm.web.dto.GuiResourceDto;
import com.forgerock.openidm.web.model.AccountShadowDto;
import com.forgerock.openidm.web.model.ObjectStage;
import com.forgerock.openidm.web.model.PagingDto;
import com.forgerock.openidm.web.model.PropertyAvailableValues;
import com.forgerock.openidm.web.model.PropertyChange;
import com.forgerock.openidm.web.model.ResourceDto;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.web.model.UserManager;
import com.forgerock.openidm.web.model.WebModelException;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType.Attributes;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Element;

/**
 * End user entity.
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class UserTypeManager implements UserManager, Serializable {

    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserTypeManager.class);
    private Class constructUserType;

    public UserTypeManager(Class constructUserType) {
        this.constructUserType = constructUserType;
    }

    @Override
    public void delete(String oid) throws WebModelException {
        logger.info("oid = {}", new Object[]{oid});
        Validate.notNull(oid);

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            port.deleteObject(oid);
        } catch (FaultMessage ex) {
            logger.error("Delete user failed for oid = {}", oid);
            logger.error("Exception was: ", ex);
            throw new WebModelException(ex.getFaultInfo().getMessage(), "[Web Service Error] Delete user failed for oid " + oid);
        }

    }

    @Override
    public String add(UserDto newObject) throws WebModelException {
        Validate.notNull(newObject);

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            ObjectContainerType userContainer = new ObjectContainerType();
            userContainer.setObject((UserType) newObject.getStage().getObject());
            java.lang.String result = port.addObject(userContainer);
            return result;
        } catch (FaultMessage fault) {
            throw new WebModelException(fault.getFaultInfo().getMessage(), "Web Service Error");
        }

    }

    @Override
    public Set<PropertyChange> submit(UserDto changedObject) throws WebModelException {
        Validate.notNull(changedObject);

        UserDto oldUser = get(changedObject.getOid(), Utils.getResolveResourceList());

        try { // Call Web Service Operation
            ObjectModificationType changes = CalculateXmlDiff.calculateChanges(oldUser.getXmlObject(), changedObject.getXmlObject());
            if (changes != null && changes.getOid() != null && changes.getPropertyModification().size() > 0) {
                ModelService service = new ModelService();
                ModelPortType port = service.getModelPort();
                port.modifyObject(changes);
            }

            //TODO: finish this
            Set<PropertyChange> set = new HashSet<PropertyChange>();
            List<PropertyModificationType> modifications = changes.getPropertyModification();
            for (PropertyModificationType modification : modifications) {
                Set<Object> values = new HashSet<Object>();
                if (modification.getValue() != null) {
                    values.addAll(modification.getValue().getAny());
                }
                set.add(new PropertyChange(createQName(modification.getPath()), getChangeType(modification.getModificationType()), values));
            }

            return set;
        } catch (FaultMessage fault) {
            throw new WebModelException(fault.getFaultInfo().getMessage(), "[Web Service Error] Submit user failed.");
        } catch (DiffException ex) {
            throw new WebModelException(ex.getMessage(), "[Diff Error] Submit user failed.");
        }
    }

    private QName createQName(Element element) {
        String namespace = element.getNamespaceURI();
        if (namespace == null) {
            namespace = element.getBaseURI();
        }
        return new QName(namespace, element.getLocalName(), element.getPrefix());
    }

    private PropertyChange.ChangeType getChangeType(PropertyModificationTypeType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case add:
                return PropertyChange.ChangeType.ADD;
            case delete:
                return PropertyChange.ChangeType.DELETE;
            case replace:
                return PropertyChange.ChangeType.REPLACE;
            default:
                throw new IllegalArgumentException("Unknown change type '" + type + "'.");
        }
    }

    @Override
    public List<PropertyAvailableValues> getPropertyAvailableValues(String oid, List<String> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<UserDto> list() throws WebModelException {
        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            // TODO: more reasonable handling of paging info
            PagingType paging = new PagingType();
            ObjectListType result = port.listObjects(Utils.getObjectType("UserType"), paging);

            List<ObjectType> users = result.getObject();
            List<UserDto> guiUsers = new ArrayList<UserDto>();
            for (ObjectType userType : users) {
                ObjectStage stage = new ObjectStage();
                stage.setObject((UserType) userType);
                UserDto userDto = (UserDto) constructUserType.newInstance();
                userDto.setStage(stage);
                guiUsers.add(userDto);
            }

            return guiUsers;
        } catch (FaultMessage ex) {
            throw new WebModelException(ex.getFaultInfo().getMessage(), "[Web Service Error] list user failed");
        } catch (InstantiationException ex) {

            throw new WebModelException(ex.getMessage(), "Instatiation failed.");
        } catch (IllegalAccessException ex) {

            throw new WebModelException(ex.getMessage(), "Class or its nullary constructor is not accessible.");
        }

    }

    @Override
    public UserDto get(
            String oid, PropertyReferenceListType resolve) throws WebModelException {
        logger.info("oid = {}", new Object[]{oid});
        Validate.notNull(oid);

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            ObjectContainerType result = port.getObject(oid, resolve);
            //ObjectStage stage = new ObjectStage();
            //stage.setObject((UserType) result.getObject());

            UserDto userDto = (UserDto) constructUserType.newInstance();
            userDto.setXmlObject((UserType) result.getObject());
            //userDto.setStage(stage);
            return userDto;
        } catch (FaultMessage ex) {
            logger.error("User lookup for oid = {}", oid);
            logger.error("Exception was: ", ex);
            throw new WebModelException(ex.getFaultInfo().getMessage(), "Failed to get user with oid " + oid, ex);
        } catch (IllegalAccessException ex) {
            logger.error("Class or its nullary constructor is not accessible: {}", ex);
            throw new WebModelException("Class or its nullary constructor is not accessible", "Internal Error", ex);
        } catch (InstantiationException ex) {
            logger.error("Instantiation failed: {}", ex);
            throw new WebModelException("Instantiation failed", "Internal Error", ex);
        } catch (RuntimeException ex) {
            // We want to catch also runtime exceptions here. These are severe
            // internal errors (bugs) or system errors (out of memory). But
            // we want at least to let user know that something bad happened here
            logger.error("Runtime exception: {}", ex);
            throw new WebModelException(ex.getMessage(), "Internal Error", ex);
        }

    }

    @Override
    public AccountShadowDto addAccount(UserDto userDto, String resourceOid) throws WebModelException {
        AccountShadowDto accountShadowDto = new AccountShadowDto();
        AccountShadowType accountShadowType = new AccountShadowType();
        accountShadowType.setAttributes(new Attributes());
        //TODO: workaround, till we switch to staging
        ResourceTypeManager rtm = new ResourceTypeManager(
                GuiResourceDto.class);
        ResourceDto resourceDto;
        try {
            resourceDto = rtm.get(resourceOid, new PropertyReferenceListType());
        } catch (WebModelException ex) {
            throw new WebModelException(ex.getMessage(), "User - add account failed.");
        }
        accountShadowType.setResource(
                (ResourceType) resourceDto.getXmlObject());

        accountShadowDto.setXmlObject(accountShadowType);
        //TODO: account is set to user not here, but in method where we are going to persist it from GUI,
        //because actual account is retrivede from form generator
        //userDto.getAccount().add(accountShadowDto);

        return accountShadowDto;
    }

    @Override
    public UserDto create() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<UserDto> list(PagingDto pagingDto) throws WebModelException {
        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            // TODO: more reasonable handling of paging info
            PagingType paging = new PagingType();

            PropertyReferenceType propertyReferenceType = Utils.fillPropertyReference(pagingDto.getOrderBy());
            paging.setOrderBy(propertyReferenceType);
            paging.setOffset(BigInteger.valueOf(pagingDto.getOffset()));
            paging.setMaxSize(BigInteger.valueOf(pagingDto.getMaxSize()));
            paging.setOrderDirection(paging.getOrderDirection());
            ObjectListType result = port.listObjects(Utils.getObjectType("UserType"), paging);

            List<ObjectType> users = result.getObject();
            List<UserDto> guiUsers = new ArrayList<UserDto>();
            for (ObjectType userType : users) {
                ObjectStage stage = new ObjectStage();
                stage.setObject((UserType) userType);
                UserDto userDto = (UserDto) constructUserType.newInstance();
                userDto.setStage(stage);
                guiUsers.add(userDto);
            }

            return guiUsers;
        } catch (FaultMessage ex) {

            throw new WebModelException(ex.getMessage(), "[Web Service Error] list user failed");
        } catch (InstantiationException ex) {

            throw new WebModelException(ex.getMessage(), "Instatiation failed.");
        } catch (IllegalAccessException ex) {

            throw new WebModelException(ex.getMessage(), "Class or its nullary constructor is not accessible.");
        }

    }
}
