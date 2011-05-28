/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.forgerock.openidm.web.model.test;

import com.forgerock.openidm.web.model.AccountShadowDto;
import com.forgerock.openidm.web.model.AccountShadowManager;
import com.forgerock.openidm.web.model.PagingDto;
import com.forgerock.openidm.web.model.PropertyAvailableValues;
import com.forgerock.openidm.web.model.PropertyChange;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.web.model.WebModelException;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
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
public class AccountShadowTypeManagerMock implements AccountShadowManager{

    Map<String, AccountShadowDto> accountTypeList = new HashMap<String, AccountShadowDto>();

    private final Class constructAccountShadowType;

    public AccountShadowTypeManagerMock(Class constructAccountShadowType) {
        this.constructAccountShadowType = constructAccountShadowType;
    }

    @Override
    public UserType listOwner(String oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<AccountShadowDto> list() {
        return accountTypeList.values();
    }

    @Override
    public AccountShadowDto get(String oid, PropertyReferenceListType resolve) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AccountShadowDto create() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String add(AccountShadowDto newObject) {
        accountTypeList.clear();
       newObject.setOid(UUID.randomUUID().toString());
        accountTypeList.put(newObject.getOid(), newObject);
        return newObject.getOid();
    }

    @Override
    public Set<PropertyChange> submit(AccountShadowDto changedObject) {
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
