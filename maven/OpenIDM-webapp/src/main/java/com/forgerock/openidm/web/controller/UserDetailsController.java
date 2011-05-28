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

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.common.web.FacesUtils;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.provisioning.schema.util.SchemaParserException;
import com.forgerock.openidm.util.DebugUtil;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.web.component.formgenerator.SchemaFormBean;
import com.forgerock.openidm.web.dto.GuiAccountShadowDto;
import com.forgerock.openidm.web.dto.GuiResourceDto;
import com.forgerock.openidm.web.dto.GuiUserDto;
import com.forgerock.openidm.web.model.AccountShadowDto;
import com.forgerock.openidm.web.model.AccountShadowManager;
import com.forgerock.openidm.web.model.ObjectManager;
import com.forgerock.openidm.web.model.ObjectTypeCatalog;
import com.forgerock.openidm.web.model.PropertyChange;
import com.forgerock.openidm.web.model.ResourceDto;
import com.forgerock.openidm.web.model.ResourceManager;
import com.forgerock.openidm.web.model.UserDto;
import com.forgerock.openidm.web.model.UserManager;
import com.forgerock.openidm.web.model.WebModelException;
import com.forgerock.openidm.xml.ns._public.common.common_1.AccountShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectReferenceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vilo Repan
 */
@Controller("userDetailsController")
@Scope("session")
public class UserDetailsController implements Serializable {

    private static transient final Trace logger = TraceManager.getTrace(UserDetailsController.class);
    @Autowired(required = true)
    private ObjectTypeCatalog objectTypeCatalog;
    private boolean editMode = false;
    private GuiUserDto user;
    private List<FormBean> accountList;
    private List<FormBean> accountListDeleted = new ArrayList<FormBean>();
    private List<SelectItem> availableResourceList;
    private List<String> selectedResourceList;

    public boolean isEditMode() {
        return editMode;
    }

    public List<FormBean> getAccountList() {
        if (accountList == null) {
            accountList = new ArrayList<FormBean>();
        }

        return accountList;
    }

    public List<String> getSelectedResourceList() {
        if (selectedResourceList == null) {
            selectedResourceList = new ArrayList<String>();
        }

        return selectedResourceList;
    }

    public void setSelectedResourceList(List<String> selectedResourceList) {
        this.selectedResourceList = selectedResourceList;
    }

    public List<SelectItem> getAvailableResourceList() {
        if (availableResourceList == null) {
            availableResourceList = new ArrayList<SelectItem>();
        }

        return availableResourceList;
    }

    public GuiUserDto getUser() {
        return user;
    }

    public void setUser(GuiUserDto user) {
        editMode = false;

        //if we are going to work with user details, we will get it's fresh version from model
        //Requirement: we will need resolved accountRefs to accounts
        if (null != user) {
            ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
            UserManager userManager = (UserManager) (objectManager);
            try {
                this.user = (GuiUserDto) userManager.get(user.getOid(), Utils.getResolveResourceList());
                accountList = createFormBeanList(this.user.getAccount(), false);
                getAvailableResourceList().clear();
                availableResourceList = createResourceList(this.user.getAccount());
            } catch (WebModelException ex) {
                StringBuilder message = new StringBuilder();
                message.append("Get user failed. Reason: ");
                message.append(ex.getTitle());
                message.append(" (");
                message.append(ex.getMessage());
                message.append(").");
                FacesUtils.addErrorMessage(message.toString());
            }
        } else {
            this.user = user;
        }
    }

//    public List<AccountShadowDto> listUserAccounts(String userDtoOid) {
//        logger.info("listUserAccounts start");
//        ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
//        UserManager userManager = (UserManager) (objectManager);
//
//        List<AccountShadowDto> userAccounts = new ArrayList<AccountShadowDto>();
//
//        UserDto guiUser = null;
//        try {
//            guiUser = (UserDto) userManager.get(userDtoOid, Utils.getResolveResourceList());
//        } catch (WebModelException ex) {
//            StringBuilder message = new StringBuilder();
//            message.append("Get user failed. Reason: ");
//            message.append(ex.getTitle());
//            message.append(" (");
//            message.append(ex.getMessage());
//            message.append(").");
//            FacesUtils.addErrorMessage(message.toString());
//            return userAccounts;
//        }
//
//        if (!guiUser.getAccount().isEmpty()) {
//            userAccounts.addAll(guiUser.getAccount());
//            logger.info("listUserAccounts account name {} ", guiUser.getAccount().get(0).getName());
//            logger.info("listUserAccounts resource name {} ", guiUser.getAccount().get(0).getResource().getName());
//        }
//        logger.info("listUserAccounts end");
//
//        return userAccounts;
//    }
    public void startEditMode(ActionEvent evt) {
        editMode = true;
    }

    private void clearController() {
        editMode = false;
        user = null;

        getAccountList().clear();
        if (accountListDeleted != null) {
            accountListDeleted.clear();
        }
        getAvailableResourceList().clear();
        getSelectedResourceList().clear();
    }

    public String backPerformed() {
        clearController();

        return "/listUser";
    }

    /**
     * TODO:
     * remove account from user which are in accountListDeleted
     * save accounts from accountList
     * save user attributes from form
     */
    public void savePerformed(ActionEvent evt) {
        try {
            //for add account we have to call method modify for User Object
            ObjectManager<UserDto> usrManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
            UserManager userManager = (UserManager) (usrManager);
            ObjectManager<AccountShadowDto> accManager = objectTypeCatalog.getObjectManager(AccountShadowDto.class, GuiAccountShadowDto.class);
            AccountShadowManager accountManager = (AccountShadowManager) (accManager);

            //new accounts are processed as modification of user in one operation
            logger.debug("Start processing of new accounts");
            for (FormBean formBean : accountList) {
                if (formBean.isNew()) {
                    //Note: we have to add new account directly to xmlObject
                    // add and delete of accounts will be process later by call to userManager.submit(user);
                    AccountShadowType newAccountShadowType = (AccountShadowType) formBean.getBean().updateAccountAttributes().getXmlObject();
                    logger.debug("Found new account in GUI: {}", DebugUtil.prettyPrint(newAccountShadowType));
                    ((UserType) user.getXmlObject()).getAccount().add(newAccountShadowType);
                }
            }
            logger.debug("Finished processing of new accounts");

            //delete accounts are also processed as modification of user in one operation
            logger.debug("Start processing of deleted accounts");
            for (FormBean formBean : accountListDeleted) {
                String oidToDelete = formBean.getBean().getAccount().getOid();
                logger.debug("Following account is marked as candidate for delete in GUI: {}", DebugUtil.prettyPrint(formBean.getBean().getAccount().getXmlObject()));
                List<ObjectReferenceType> accountsRef = ((UserType) user.getXmlObject()).getAccountRef();
                logger.debug("account reference list size {}", accountsRef.size());
                for (ObjectReferenceType account : accountsRef) {
                    if (StringUtils.equals(oidToDelete, account.getOid())) {
                        accountsRef.remove(account);
                        accountManager.delete(account.getOid());
                        ((UserType) user.getXmlObject()).getAccountRef().remove(account);
                        break;
                    }
                }
            }
            logger.debug("Finished processing of deleted accounts");

            logger.debug("Submit user modified in GUI");
            Set<PropertyChange> userChanges = userManager.submit(user);
            logger.debug("Modified user in GUI submitted ");
            if (userChanges.isEmpty()) {
                //account changes are processed as modification of account, every account is processed separately
                logger.debug("Start processing of modified accounts");
                for (FormBean formBean : accountList) {
                    if (!formBean.isNew()) {
                        AccountShadowDto modifiedAccountShadowDto = (AccountShadowDto) formBean.getBean().updateAccountAttributes();
                        logger.debug("Found modified account in GUI: {}", DebugUtil.prettyPrint(modifiedAccountShadowDto.getXmlObject()));
                        logger.debug("Submit account modified in GUI");
                        accountManager.submit(modifiedAccountShadowDto);
                        logger.debug("Modified account in GUI submitted");
                    }
                }
                logger.debug("Finished processing of modified accounts");
            } else {
                updateAccounts(accountList);
            }

            //action is done in clearController
            //accountListDeleted.clear();
            clearController();

            FacesUtils.addSuccessMessage("Save changes successfully.");

        } catch (SchemaParserException ex) {
            logger.error("Dynamic form generator error", ex);
            //TODO: What action should we fire in GUI if error occurs ???
            String loginFailedMessage = FacesUtils.getBundleKey("msg", "save.failed");
            FacesUtils.addErrorMessage(loginFailedMessage + " " + ex.toString());

            return;
        } catch (WebModelException ex) {
            logger.error("Web error {} : {}", ex.getTitle(), ex.getMessage());
            //TODO: What action should we fire in GUI if error occurs ???
            StringBuilder message = new StringBuilder();
            message.append(FacesUtils.getBundleKey("msg", "save.failed"));
            message.append(" Reason: ");
            message.append(ex.getTitle());
            message.append(" (");
            message.append(ex.getMessage());
            message.append(").");
            FacesUtils.addErrorMessage(message.toString());

            return;
        } catch (Exception ex) {
            //should not be here, it's only because bad error handling
            logger.error("Unknown error occured during save operation, reason: {}.", ex.getMessage());
            logger.trace("Unknown error occured during save operation.", ex);
            FacesUtils.addErrorMessage("Unknown error occured during save operation, reason: " + ex.getMessage());
        }

        return;
    }

    //XXX: fix me, commented xpath utils just for minute
    private void updateAccounts(List<FormBean> accountBeans) throws WebModelException {
        logger.debug("Start processing accounts with outbound schema handling");
        for (FormBean bean : accountBeans) {
            if (bean.isNew()) {
                continue;
            }

            ResourceObjectShadowType resourceObjectShadow = null;
            AccountShadowDto account = null;
            try {
                account = bean.getBean().updateAccountAttributes();
//                resourceObjectShadow = XPathUtil.applyOutboundSchemaHandlingOnAccount((UserType) user.getXmlObject(), (AccountShadowType) account.getXmlObject());
//            } catch (JAXBException ex) {
//                logger.error("Failed to parse outbound schema hanling for account '{}' reason: {}.",
//                        account.getName(), ex.getMessage());
//                throw new WebModelException("Failed to parse outbound schema hanling for account '" +
//                        account.getName() + "' reason: " + ex.getMessage() + ".",
//                        "Failed to apply outbound schema handling.", ex);
            } catch (SchemaParserException ex) {
                throw new WebModelException("Failed to update account attributes, reason: " +
                        ex.getMessage() + ".", "Failed to update account attributes.", ex);
            }

//            account = new AccountShadowDto((AccountShadowType) resourceObjectShadow);
            ObjectManager<AccountShadowDto> accManager = objectTypeCatalog.getObjectManager(AccountShadowDto.class, GuiAccountShadowDto.class);
            AccountShadowManager accountManager = (AccountShadowManager) (accManager);
            accountManager.submit(account);
        }
        logger.debug("Finished processing accounts with outbound schema handling");
    }

    public void addResourcePerformed(ActionEvent evt) {
        if (selectedResourceList == null) {
            return;
        }

        List<ResourceDto> selectedResources = new ArrayList<ResourceDto>();

        //TODO: handle exception
        List<ResourceDto> resources = listResources();
        for (String resName : selectedResourceList) {
            for (ResourceDto resource : resources) {
                if (resource.getName().equals(resName)) {
                    selectedResources.add(resource);
                    break;
                }
            }
        }
        selectedResourceList.clear();

        if (selectedResources.isEmpty()) {
            return;
        }

        List<AccountShadowDto> newAccounts = new ArrayList<AccountShadowDto>();
        for (ResourceDto resource : selectedResources) {
            ObjectManager<UserDto> objectManager = objectTypeCatalog.getObjectManager(UserDto.class, GuiUserDto.class);
            UserManager userManager = (UserManager) (objectManager);
            AccountShadowDto account = null;
            try {
                account = userManager.addAccount(user, resource.getOid());
            } catch (WebModelException ex) {
                StringBuilder message = new StringBuilder();
                message.append("Error occured. Reason: ");
                message.append(ex.getTitle());
                message.append(" (");
                message.append(ex.getMessage());
                message.append(").");
                FacesUtils.addErrorMessage(message.toString());
            }

            //TODO: HACK
            try {
                setAccountDetails(account);
            } catch (Exception ex) {
                FacesUtils.addErrorMessage("Unknown error: Can't update account attributes: " + ex.getMessage());
            }
            newAccounts.add(account);
        }
        getAccountList().addAll(createFormBeanList(newAccounts, true));

        //update available resource list
        getAvailableResourceList().clear();
        List<AccountShadowDto> existingAccounts = new ArrayList<AccountShadowDto>();
        for (FormBean form : accountList) {
            existingAccounts.add(form.getBean().getAccount());
        }

        availableResourceList = createResourceList(existingAccounts);
    }

    private AccountShadowDto setAccountDetails(AccountShadowDto accountShadow) throws ParserConfigurationException {
        String resourceNamespace = accountShadow.getResource().getNamespace();
        //TODO: hack - setting default values for account and some account's attributes
        accountShadow.setName(accountShadow.getResource().getName() + "-" + user.getName());
        List<Element> attributes = new ArrayList<Element>();

        logger.info("starting DocumnetBuilderFactory");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        //attributes/uid = user.name
        Document doc = db.newDocument();
        Element element = doc.createElementNS(resourceNamespace, "uid");
        element.setPrefix("ns76");
        element.setTextContent(user.getName());
        attributes.add(element);
        //attributes/name = "uid="+user.name+",ou=people,ou=example,ou=com"
        //!!! another namespace
        doc = db.newDocument();
        element = doc.createElementNS("http://openidm.forgerock.com/xml/ns/public/resource/idconnector/resource-schema-1.xsd", "__NAME__");
        element.setPrefix("ns75");
//     
//        element.setTextContent("uid=" + user.getName() + ",ou=people,dc=example,dc=com");
//        attributes.add(element);
        //attributes/cn = user.fullName
        doc = db.newDocument();
        element = doc.createElementNS(resourceNamespace, "cn");
        element.setPrefix("ns76");
        element.setTextContent(user.getFullName());
        attributes.add(element);
        //attributes/sn = user.familyName
        doc = db.newDocument();
        element = doc.createElementNS(resourceNamespace, "sn");
        element.setPrefix("ns76");
        element.setTextContent(user.getFamilyName());
        attributes.add(element);
        //attributes/givenName = user.givenName
        doc = db.newDocument();
        element = doc.createElementNS(resourceNamespace, "givenName");
        element.setPrefix("ns76");
        element.setTextContent(user.getGivenName());
        attributes.add(element);
        logger.info("ending Documnet BuilderFactory");

        accountShadow.setAttributes(attributes);
        return accountShadow;
    }

    public void removeResourcePerformed(ActionEvent evt) {
        Integer formBeanId = (Integer) evt.getComponent().getAttributes().get("beanId");
        if (formBeanId == null) {
            return;
        }

        FormBean formBean = null;
        for (FormBean bean : accountList) {
            if (formBeanId == bean.getId()) {
                formBean = bean;
                break;
            }
        }

        if (formBean == null) {
            return;
        }

        accountList.remove(formBean);
        accountListDeleted.add(formBean);
    }

    private List<SelectItem> createResourceList(List<AccountShadowDto> existingAccounts) {
        List<SelectItem> list = new ArrayList<SelectItem>();

        List<ResourceDto> resources = listAvailableResources(listResources(), existingAccounts);
        for (ResourceDto resourceDto : resources) {
            SelectItem si = new SelectItem((GuiResourceDto) resourceDto);
            list.add(si);
        }

        return list;
    }

    private List<ResourceDto> listResources() {
        ObjectManager<ResourceDto> manager = objectTypeCatalog.getObjectManager(ResourceDto.class, GuiResourceDto.class);
        ResourceManager resManager = (ResourceManager) (manager);

        List<ResourceDto> resources = new ArrayList<ResourceDto>();
        try {
            Collection<ResourceDto> list = resManager.list();
            if (list != null) {
                resources.addAll(list);
            }
        } catch (WebModelException ex) {
            StringBuilder message = new StringBuilder();
            message.append(FacesUtils.getBundleKey("msg", "resource.list.failed"));
            message.append(" Reason: ");
            message.append(ex.getTitle());
            message.append(" (");
            message.append(ex.getMessage());
            message.append(").");
            FacesUtils.addErrorMessage(message.toString());
        }

        return resources;
    }

    private List<ResourceDto> listAvailableResources(List<ResourceDto> resources, List<AccountShadowDto> accounts) {
        if (resources == null) {
            return null;
        }
        if (accounts == null || accounts.isEmpty()) {
            return resources;
        }

        List<ResourceDto> list = new ArrayList<ResourceDto>();
        list.addAll(resources);

        for (int i = 0; i < list.size(); i++) {
            for (AccountShadowDto accountDto : accounts) {
                if (list.get(i).getOid().equals(accountDto.getResource().getOid())) {
                    list.remove(list.get(i));
                }
            }
        }

        return list;
    }

    private int getFormBeanNextId() {
        int maxId = getMaxId(accountList, 0);
        maxId = getMaxId(accountListDeleted, maxId);
        maxId++;

        return maxId;
    }

    private int getMaxId(List<FormBean> list, int maxId) {
        if (list != null) {
            for (FormBean bean : list) {
                if (maxId < bean.getId()) {
                    maxId = bean.getId();
                }
            }
        }

        return maxId;
    }

    private List<FormBean> createFormBeanList(List<AccountShadowDto> accounts, boolean createNew) {
        int maxId = getFormBeanNextId();

        List<FormBean> list = new ArrayList<FormBean>();
        if (accounts != null) {
            for (AccountShadowDto account : accounts) {
                try {
                    SchemaFormBean bean = new SchemaFormBean();
                    if (createNew) {
                        bean.generateForm(account);
                    } else {
                        bean.generateForm(account, account.getObjectClass());
                    }

                    list.add(new FormBean(maxId++, bean, createNew));
                } catch (SchemaParserException ex) {
                    logger.error("Can't parse schema for account '{}': {}", new Object[]{account.getName(),ex.getMessage(), ex});
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Can't parse schema for account '" + account.getName() + "'.",
                            "Can't parse schema for account '" + account.getName() + "': " + ex.getMessage()));
                }
            }
        }

        return list;
    }

    public class FormBean {

        private int id;
        private SchemaFormBean bean;
        private boolean expanded = true;
        private boolean isNew;
        private boolean enabled;

        FormBean(int id, SchemaFormBean bean, boolean isNew) {
            this.id = id;
            this.bean = bean;
            this.isNew = isNew;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isNew() {
            return isNew;
        }

        public int getId() {
            return id;
        }

        public SchemaFormBean getBean() {
            return bean;
        }

        public void setBean(SchemaFormBean bean) {
            this.bean = bean;
        }

        public String getName() {
            return bean.getDisplayName();
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }
}
