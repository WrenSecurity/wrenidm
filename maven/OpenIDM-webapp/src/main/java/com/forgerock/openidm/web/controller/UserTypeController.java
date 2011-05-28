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
package com.forgerock.openidm.web.controller;

import com.forgerock.openidm.common.web.FacesUtils;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.web.UserTypeSortableList;
import com.forgerock.openidm.web.dto.GuiUserDto;
import com.forgerock.openidm.web.model.*;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.icesoft.faces.component.ext.RowSelectorEvent;
import java.io.Serializable;
import java.util.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 *
 * @author katuska
 */
@Controller("userTypeController")
@Scope("session")
public class UserTypeController extends UserTypeSortableList implements Serializable {

    private List<GuiUserDto> userData;
    private GuiUserDto user;
    private String searchOid;
    private boolean selectAll = false;
    @Autowired(required = true)
    private UserDetailsController userDetailsController;
    @Autowired(required = true)
    private ObjectTypeCatalog objectTypeCatalog;
    private int offset = 0;
    private int rowsCount = 20;
    private static transient final org.slf4j.Logger logger = TraceManager.getTrace(UserTypeController.class);
    private boolean showPopup = false;

    public UserTypeController() {
        super("name");

    }

    public String createUser() {
        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
        UserManager userManager = (UserManager) (objectManager);
        String oid = null;
        try {
            oid = userManager.add(user);
        } catch (WebModelException ex) {
            // TODO: Update the message content from thr exception
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Failed to create user:" + ex.getTitle()));
            logger.error("Failed to create user {}, exception {}", user, ex);
            return "";
        }
        if (oid == null) {
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Failed to create user"));
            logger.error("Failed to create user {}", user);
            return "";
        }
//        if (oid == null) {
//            FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Failed to create user"));
//            logger.error("Failed to create user {}", user);
//            return "";
//        }
        logger.info("Created user with oid {}", oid);
        FacesUtils.addSuccessMessage("User created successfully");
        listUsers();
        return "/listUser";
    }

    public void list(){
        offset = 0;
        listUsers();
    }

    public void listUsers() {
        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
        UserManager userManager = (UserManager) (objectManager);
        userData = new ArrayList<GuiUserDto>();
        Collection<UserDto> userDtoList = null;
        OrderDirectionType direction = OrderDirectionType.ASCENDING;
        if (!ascending){
           direction = OrderDirectionType.DESCENDING;
        }
        logger.debug("sortColumn name : {}", sortColumnName);
        try {
            userDtoList = (Collection<UserDto>) userManager.list(new PagingDto(sortColumnName, offset, rowsCount, direction));
            for (UserDto userDto : userDtoList) {
                GuiUserDto guiUserDto = (GuiUserDto) userDto;
                userData.add(guiUserDto);
            }
        } catch (WebModelException ex) {
            logger.error("List users failed: ", ex);
            return;
        }
    }

    public void listLast(){
        offset = -1;
        listUsers();
    }

    public void listNext(){
        offset += rowsCount;
        listUsers();
    }

    public void listFirst(){
        offset = 0;
        listUsers();
    }

    public void listPrevious(){
        if (offset < rowsCount ){
            return;
        }
        offset -= rowsCount;
        listUsers();
    }

    public void userSelectionListener(RowSelectorEvent event) throws WebModelException {
        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
        UserManager userManager = (UserManager) (objectManager);
        logger.info("userSelectionListener start");
        String userId = userData.get(event.getRow()).getOid();
        user = (GuiUserDto) userManager.get(userId, new PropertyReferenceListType());
        logger.info("userSelectionListener end");

        //TODO: handle exception
        userDetailsController.setUser(user);
    }

    public void deleteUsers() {
        showPopup = false;
        
        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
        UserManager userManager = (UserManager) (objectManager);
        for (GuiUserDto guiUserDto : userData) {
            logger.info("delete user {} is selected {}", guiUserDto.getFullName(), guiUserDto.isSelected());

            if (guiUserDto.isSelected()) {
                try {
                    userManager.delete(guiUserDto.getOid());
                } catch (WebModelException ex) {
                    logger.error("Delete user failed: {}", ex);
                    FacesUtils.addErrorMessage("Delete user failed: " + ex.getMessage());
                }
            }

        }
        listUsers();

    }

    public void searchUser(ActionEvent evt) {
        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
        UserManager userManager = (UserManager) (objectManager);
        listUsers();
        if (null != searchOid) {
            GuiUserDto guiUserDto = null;
            try {
                guiUserDto = (GuiUserDto) (userManager.get(searchOid, new PropertyReferenceListType()));
            } catch (WebModelException ex) {
                logger.error("Get user with oid {} failed : {}", searchOid, ex);
                FacesUtils.addErrorMessage("Get user failed: " + ex.getMessage());
                FacesUtils.addErrorMessage(ex.getTitle());
                return;
            }
            userData.clear();
            userData.add(guiUserDto);
        }
    }

    public String selectAll() {
        if (selectAll) {
            selectAll = false;
        } else {
            selectAll = true;
        }
        for (GuiUserDto guiUser : userData) {
            guiUser.setSelected(selectAll);
        }
        logger.info("setSelectedAll value");
        return "/deleteUser";
    }

    public void sortItem(ActionEvent e) {
        sort();
    }

    public String createAction() {
        user = new GuiUserDto();
        UserType object = new UserType();
        user.setStage(new ObjectStage());
        user.getStage().setObject(object);
        user.setVersion("1.0");
        return "/createUser";
    }

    public String deleteAction() {
        listUsers();
        return "/deleteUser";
    }

    public String fillTableList() {
        listUsers();
        return "/listUser";
    }

    @Override
    public void sort() {
        Comparator<GuiUserDto> comparator = new Comparator<GuiUserDto>() {

            @Override
            public int compare(GuiUserDto u1, GuiUserDto u2) {

                if (sortColumnName == null) {
                    return 0;
                }
                if (sortColumnName.equals("fullName")) {
                    return ascending ? String.CASE_INSENSITIVE_ORDER.compare(u1.getFullName(), u2.getFullName()) : String.CASE_INSENSITIVE_ORDER.compare(u2.getFullName(), u1.getFullName());
                } else if (sortColumnName.equals("givenName")) {
                    return ascending ? String.CASE_INSENSITIVE_ORDER.compare(u1.getGivenName(), u2.getGivenName()) : String.CASE_INSENSITIVE_ORDER.compare(u2.getGivenName(), u1.getGivenName());
                } else if (sortColumnName.equals("familyName")) {
                    return ascending ? String.CASE_INSENSITIVE_ORDER.compare(u1.getFamilyName(), u2.getFamilyName()) : String.CASE_INSENSITIVE_ORDER.compare(u2.getFamilyName(), u1.getFamilyName());
                } else if (sortColumnName.equals("oid")) {
                    return ascending ? String.CASE_INSENSITIVE_ORDER.compare(u1.getOid(), u2.getOid()) : String.CASE_INSENSITIVE_ORDER.compare(u2.getOid(), u1.getOid());
                } else if (sortColumnName.equals("name")) {
                    return ascending ? String.CASE_INSENSITIVE_ORDER.compare(u1.getName(), u2.getName()) : String.CASE_INSENSITIVE_ORDER.compare(u2.getName(), u1.getName());
                } else {
                    return 0;
                }
            }
        };
        Collections.sort(userData, comparator);

    }

    @Override
    protected boolean isDefaultAscending(String sortColumn) {
        return true;
    }

    public ObjectTypeCatalog getObjectTypeCatalog() {
        return objectTypeCatalog;
    }

    public void setObjectTypeCatalog(ObjectTypeCatalog objectTypeCatalog) {
        this.objectTypeCatalog = objectTypeCatalog;
    }

    public List<GuiUserDto> getUserData() {
        return userData;
    }

    public void setUserData(List<GuiUserDto> userData) {
        this.userData = userData;
    }

    public GuiUserDto getUser() {
        return user;
    }

    public void setUser(GuiUserDto user) {
        this.user = user;
    }

    public String getSearchOid() {
        return searchOid;
    }

    public void setSearchOid(String searchOid) {
        this.searchOid = searchOid;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
  
    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public boolean isShowPopup() {
        return showPopup;
    }

    public void hideConfirmDelete() {
        showPopup = false;
    }

    public void showConfirmDelete() {
        showPopup = true;
    }
}
