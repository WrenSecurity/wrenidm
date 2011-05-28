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
import com.forgerock.openidm.web.model.ObjectStage;
import com.forgerock.openidm.web.model.PagingDto;
import com.forgerock.openidm.web.model.PropertyAvailableValues;
import com.forgerock.openidm.web.model.PropertyChange;
import com.forgerock.openidm.web.model.ResourceDto;
import com.forgerock.openidm.web.model.ResourceManager;
import com.forgerock.openidm.web.model.ResourceObjectShadowDto;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.web.model.WebModelException;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.Validate;

/**
 *
 * @author katuska
 */
public class ResourceTypeManager implements ResourceManager, Serializable {

    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ResourceTypeManager.class);
    private Class constructResourceType;

    public ResourceTypeManager(Class constructResourceType) {
        this.constructResourceType = constructResourceType;
    }

    @Override
    public Collection<ResourceDto> list() {


        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            String objectType = Utils.getObjectType("ResourceType");
            // TODO: more reasonable handling of paging info
            PagingType paging = new PagingType();
            ObjectListType result = port.listObjects(objectType,paging);
            List<ObjectType> objects = result.getObject();
            Collection<ResourceDto> items = new ArrayList<ResourceDto>(objects.size());

            for (ObjectType o : objects) {
                ObjectStage stage = new ObjectStage();
                stage.setObject(o);
                ResourceDto resourceDto = (ResourceDto) constructResourceType.newInstance();
                resourceDto.setStage(stage);
                items.add(resourceDto);
            }

            return items;
        } catch (Exception ex) {
            logger.error("List resources failed");
            logger.error("Exception was: ", ex);
            return null;
        }

    }

    @Override
    public ResourceDto get(String oid, PropertyReferenceListType resolve) throws WebModelException {
       logger.info("oid = {}", new Object[]{oid});
       Validate.notNull(oid);
        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            ObjectContainerType result = port.getObject(oid, resolve);
            ObjectStage stage = new ObjectStage();
            stage.setObject(result.getObject());

            ResourceDto resourceDto = (ResourceDto) constructResourceType.newInstance();
            resourceDto.setStage(stage);

            return resourceDto;
        } catch (FaultMessage ex) {
            throw new WebModelException(ex.getMessage(), "Failed to get resource with oid "+oid);
        } catch (InstantiationException ex) {
            logger.error("Instantiation failed: {}", ex);
            return null;
//            throw new WebModelException(ex.getMessage(), "Instatiation failed.");
        } catch (IllegalAccessException ex) {
            logger.error("Class or its nullary constructor is not accessible: {}", ex);
            return null;
//            throw new WebModelException(ex.getMessage(), "Class or its nullary constructor is not accessible.");
        }
    }

    @Override
    public ResourceDto create() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String add(ResourceDto newObject) throws WebModelException {
        Validate.notNull(newObject);

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            ObjectContainerType objectContainerType = new ObjectContainerType();
            objectContainerType.setObject(newObject.getStage().getObject());
            String result = port.addObject(objectContainerType);
            return result;
        } catch (FaultMessage ex) {
           throw new WebModelException(ex.getMessage(), "[Web Service Error] Add resource failed");
            
        }

    }

    @Override
    public Set<PropertyChange> submit(ResourceDto changedObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(String oid) throws WebModelException {
        Validate.notNull(oid);
        try {
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            port.deleteObject(oid);
        } catch (FaultMessage ex) {
            throw new WebModelException(ex.getMessage(), "[Web Service Error] Failed to delete resource with oid "+oid);
        }

    }

    @Override
    public List<PropertyAvailableValues> getPropertyAvailableValues(String oid, List<String> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ResourceObjectShadowDto> listObjectShadows(String oid, Class resourceObjectShadowType) {

        Validate.notNull(oid);
        try {
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            ResourceObjectShadowListType resourceObjectShadowListType = port.listResourceObjectShadows(oid, resourceObjectShadowType.getName());
            List<ResourceObjectShadowDto> resourceObjectShadowDtoList = new ArrayList<ResourceObjectShadowDto>();
            for (ResourceObjectShadowType resourceObjectShadow : resourceObjectShadowListType.getObject()){
                ResourceObjectShadowDto resourceObjectShadowDto = new ResourceObjectShadowDto(resourceObjectShadow);
                resourceObjectShadowDtoList.add(resourceObjectShadowDto);
            }
            return resourceObjectShadowDtoList;
        } catch (Exception ex) {
            logger.error("Delete user failed for oid = {}", oid);
            logger.error("Exception was: ", ex);
            return null;
        }

    }

    @Override
    public Collection<UserDto> list(PagingDto pagingDto) throws WebModelException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
