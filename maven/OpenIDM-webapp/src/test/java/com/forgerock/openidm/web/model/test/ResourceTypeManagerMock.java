/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.forgerock.openidm.web.model.test;

import com.forgerock.openidm.web.model.PagingDto;
import com.forgerock.openidm.web.model.PropertyAvailableValues;
import com.forgerock.openidm.web.model.PropertyChange;
import com.forgerock.openidm.web.model.ResourceDto;
import com.forgerock.openidm.web.model.ResourceManager;
import com.forgerock.openidm.web.model.ResourceObjectShadowDto;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.web.model.WebModelException;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author katuska
 */
public class ResourceTypeManagerMock implements ResourceManager{

    Map<String, ResourceDto> resourceTypeList = new HashMap<String, ResourceDto>();

    private final Class constructResourceType;

    public ResourceTypeManagerMock(Class constructResourceType) {
        this.constructResourceType = constructResourceType;
    }

    @Override
    public List<ResourceObjectShadowDto> listObjectShadows(String oid, Class resourceObjectShadowType) {
        return new ArrayList<ResourceObjectShadowDto>();
    }

    @Override
    public Collection<ResourceDto> list() {
         return resourceTypeList.values();
    }

    @Override
    public ResourceDto get(String oid, PropertyReferenceListType resolve) {
        for (ResourceDto resource : resourceTypeList.values()){
            if (resource.getOid().equals(oid)){
                return resource;
            }
        }
        return null;
    }

    @Override
    public ResourceDto create() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String add(ResourceDto newObject) {
        resourceTypeList.clear();
        if (newObject.getOid() == null){
            newObject.setOid(UUID.randomUUID().toString());
        }
        resourceTypeList.put(newObject.getOid(), newObject);
        return newObject.getOid();
    }

    @Override
    public Set<PropertyChange> submit(ResourceDto changedObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(String oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PropertyAvailableValues> getPropertyAvailableValues(String oid, List<String> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<UserDto> list(PagingDto pagingDto) throws WebModelException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
