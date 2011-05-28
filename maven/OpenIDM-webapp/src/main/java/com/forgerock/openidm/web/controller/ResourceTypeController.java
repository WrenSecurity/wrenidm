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
import com.forgerock.openidm.web.consumer.ModelService;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.web.dto.GuiResourceDto;
import com.forgerock.openidm.web.dto.GuiTestResultDto;
import com.forgerock.openidm.web.model.AccountShadowDto;
import com.forgerock.openidm.web.model.DiagnosticMessageDto;
import com.forgerock.openidm.web.model.ObjectStage;
import com.forgerock.openidm.web.model.ObjectTypeCatalog;
import com.forgerock.openidm.web.model.ResourceDto;
import com.forgerock.openidm.web.model.TaskStatusDto;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import com.icesoft.faces.component.ext.RowSelectorEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.Holder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author Katuska
 */
@Controller
@Scope("session")
public class ResourceTypeController {

    private ObjectTypeCatalog objectTypeCatalog;
    private List<GuiResourceDto> resources;
    private GuiResourceDto resourceDto;
    private List<AccountShadowDto> accounts;
    private GuiTestResultDto guiTestResult;
    private TaskStatusDto status;

    private static transient final org.slf4j.Logger logger = TraceManager.getTrace(ResourceTypeController.class);

    public ResourceTypeController() {
    }

    public String backPerformed() {
        for (GuiResourceDto res : resources) {
            res.setSelected(false);
        }
        resourceDto = null;
        accounts = null;
        return "/listResources";
    }

    private void parseForUsedConnector(GuiResourceDto guiResource) {
//        String connectorUsed = "";
        if (guiResource.getConfiguration().size() > 0) {
            for (Element element : guiResource.getConfiguration()) {
                NamedNodeMap attributes = element.getFirstChild().getAttributes();
                if (attributes.getNamedItem("connectorName") != null) {
                    guiResource.setConnectorUsed(attributes.getNamedItem("connectorName").getTextContent());
                }
                if (attributes.getNamedItem("bundleVersion")!=null) {
                    guiResource.setConnectorVersion(attributes.getNamedItem("bundleVersion").getTextContent());
                }
            }
        }
//        return connectorUsed;
    }

    public String listAccounts() {

        if (resourceDto == null) {
            FacesUtils.addErrorMessage("Resource must be selected");
            return "";
        }

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            String objectType = Utils.getObjectType("AccountType");

            OperationalResultType operationalResult = new OperationalResultType();
            Holder<OperationalResultType> holder = new Holder<OperationalResultType>(operationalResult);
            ObjectListType result = port.listResourceObjects(resourceDto.getOid(), objectType, new PagingType(), holder);
            List<ObjectType> objects = result.getObject();
            accounts = new ArrayList<AccountShadowDto>();

            for (ObjectType o : objects) {
                ObjectStage stage = new ObjectStage();
                stage.setObject(o);
                AccountShadowDto account = new AccountShadowDto();
                account.setStage(stage);
                accounts.add(account);
            }
            for (AccountShadowDto account : accounts) {
                logger.debug("account oid {}", account.getOid());
                logger.debug("account name {}", account.getName());
                logger.debug("account version {}", account.getVersion());
            }

        } catch (FaultMessage ex) {
            String message = (ex.getFaultInfo().getMessage() != null) ? ex.getFaultInfo().getMessage() : ex.getMessage();
            FacesUtils.addErrorMessage("List accounts failed.");
            FacesUtils.addErrorMessage("Exception was: " + message);
            logger.error("List accounts failed");
            logger.error("Exception was: ", ex);
        }
        return "/listResourcesAccounts";
    }

    private String getResults(boolean test) {
        //accept == test passed, cancel == test faild
        String result = (test) ? "accept" : "cancel";
        logger.debug("test result: {}", result);
        return result;
    }

    private String getEachResult(TestResultType testResultType) {
        //error == test not tested
        String result = (testResultType != null) ? getResults(testResultType.isSuccess()) : "error";
        return result;


    }

    public String testConnection() {

        if (resourceDto == null) {
            FacesUtils.addErrorMessage("Resource must be selected");
            return "";
        }

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            ResourceTestResultType result = port.testResource(resourceDto.getOid());
            guiTestResult = new GuiTestResultDto();
            guiTestResult.setConfigurationValidation(getEachResult(result.getConfigurationValidation()));
            guiTestResult.setConnectorConnection(getEachResult(result.getConnectorConnection()));
            guiTestResult.setConnectionInitialization(getEachResult(result.getConnectorInitialization()));
            guiTestResult.setConnectionSanity(getEachResult(result.getConnectorSanity()));
            guiTestResult.setConnectionSchema(getEachResult(result.getConnectorSchema()));
            if (result.getExtraTest() != null) {
                guiTestResult.setExtraTestResult(getEachResult(result.getExtraTest().getResult()));
                guiTestResult.setExtraTestName(result.getExtraTest().getName());
            } else {
                guiTestResult.setExtraTestResult("error");
                guiTestResult.setExtraTestName("error");
            }


        } catch (FaultMessage ex) {
            String message = (ex.getFaultInfo().getMessage() != null) ? ex.getFaultInfo().getMessage() : ex.getMessage();
            FacesUtils.addErrorMessage("Test resource failed.");
            FacesUtils.addErrorMessage("Exception was: " + message);
            logger.error("Test resources failed");
            logger.error("Exception was: ", ex);
        }

        return "/testResource";
    }

    public void importFromResource() {

        if (resourceDto == null) {
            FacesUtils.addErrorMessage("Resource must be selected");
            return;
        }

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();

            // TODO: HACK: this should be determined from the resource schema.
            // But ICF always generates the name for __ACCOUNT__ like this.
            String objectClass = "Account";

            logger.debug("Calling launchImportFromResource({})", resourceDto.getOid());
            EmptyType result = port.launchImportFromResource(resourceDto.getOid(), objectClass);

        } catch (FaultMessage ex) {
            String message = (ex.getFaultInfo().getMessage() != null) ? ex.getFaultInfo().getMessage() : ex.getMessage();
            FacesUtils.addErrorMessage("Launching import from resource failed: " + message);
            logger.error("Launching import from resources failed.", ex);
        } catch (RuntimeException ex) {
            // Due to insane preferrence of runtime exception in "modern" Java
            // we need to catch all of them. These may happen in JBI layer and
            // we are not even sure which exceptions to cacht.
            // To a rough hole a rough patch.
            FacesUtils.addErrorMessage("Launching import from resource failed. Runtime error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            logger.error("Launching import from resources failed. Runtime error.", ex);
        }

        // TODO switch to a page that will show progress
        return;
    }

    private TaskStatusDto convertTaskStatusResults(TaskStatusType taskStatusType) {
        TaskStatusDto taskStatus = new TaskStatusDto();
        if (taskStatusType.getFinishTime() != null) {
            taskStatus.setFinishTime(taskStatusType.getFinishTime().toXMLFormat());
        }
        DiagnosticsMessageType lastError = null;
        if ((lastError = taskStatusType.getLastError()) != null) {
            DiagnosticMessageDto diagnosticMessage = new DiagnosticMessageDto();

            if (lastError.getDetails() != null) {
                diagnosticMessage.setDetails(lastError.getDetails());
            }
            if (lastError.getMessage() != null) {
                diagnosticMessage.setMessage(lastError.getMessage());
            }
            if (lastError.getTimestamp() != null) {
                diagnosticMessage.setTimestamp(lastError.getTimestamp().toXMLFormat());
            }
            taskStatus.setLastError(diagnosticMessage);
        }

        if (taskStatusType.getLaunchTime() != null) {
            taskStatus.setLaunchTime(taskStatusType.getLaunchTime().toXMLFormat());
        }
        if (taskStatusType.getNumberOfErrors() != null) {
            taskStatus.setNumberOfErrors(String.valueOf(taskStatusType.getNumberOfErrors()));
        }
        if (taskStatusType.getProgress() != null) {
            taskStatus.setProgress(String.valueOf(taskStatusType.getProgress()));
        }
        taskStatus.setLastStatus(taskStatusType.getLastStatus());
        taskStatus.setName(taskStatusType.getName());
        String running = (taskStatusType.isRunning()) ? "YES" : "NO";
        taskStatus.setRunning(running);

        return taskStatus;
    }

    public String getImportStatus() {

         if (resourceDto == null) {
            FacesUtils.addErrorMessage("Resource must be selected");
            return "";
        }


        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            TaskStatusType taskStatus = port.getImportStatus(resourceDto.getOid());
            status = convertTaskStatusResults(taskStatus);
        } catch (FaultMessage ex) {
            String message = (ex.getFaultInfo().getMessage() != null) ? ex.getFaultInfo().getMessage() : ex.getMessage();
            FacesUtils.addErrorMessage("Getting import status failed: " + message);
            logger.error("Getting import status failed.", ex);
        } catch (RuntimeException ex) {
            // Due to insane preferrence of runtime exception in "modern" Java
            // we need to catch all of them. These may happen in JBI layer and
            // we are not even sure which exceptions to cacht.
            // To a rough hole a rough patch.
            FacesUtils.addErrorMessage("Getting import status failed:. Runtime error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            logger.error("Getting import status failed:. Runtime error.", ex);
        }


        return "/resourceImportStatus";
    }

    public void resourceSelectionListener(RowSelectorEvent event) {
        String resourceId = resources.get(event.getRow()).getOid();

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();

            ObjectContainerType result = port.getObject(resourceId, new PropertyReferenceListType());
            resourceDto = new GuiResourceDto((ResourceType) result.getObject());
            parseForUsedConnector(resourceDto);
        } catch (Exception ex) {
            logger.error("Select resource failed");
            logger.error("Exception was: ", ex);
        }
    }

    public String listResources() {

        try { // Call Web Service Operation
            ModelService service = new ModelService();
            ModelPortType port = service.getModelPort();
            String objectType = Utils.getObjectType("ResourceType");
            // TODO: more reasonable handling of paging info
            PagingType paging = new PagingType();
            ObjectListType result = port.listObjects(objectType, paging);
            List<ObjectType> objects = result.getObject();
            resources = new ArrayList<GuiResourceDto>();

            for (ObjectType o : objects) {
                ObjectStage stage = new ObjectStage();
                stage.setObject(o);
                ResourceDto resource = new ResourceDto();
                resource.setStage(stage);
                GuiResourceDto guiResource = new GuiResourceDto((ResourceType) resource.getXmlObject());
                parseForUsedConnector(guiResource);
                resources.add(guiResource);

            }


        } catch (FaultMessage ex) {
            String message = (ex.getFaultInfo().getMessage() != null) ? ex.getFaultInfo().getMessage() : ex.getMessage();
            FacesUtils.addErrorMessage("List resources failed.");
            FacesUtils.addErrorMessage("Exception was: " + message);
            logger.error("List resources failed");
            logger.error("Exception was: ", ex);
            return null;
        }

        return "/listResources";

    }

    public ObjectTypeCatalog getObjectTypeCatalog() {
        return objectTypeCatalog;
    }

    public void setObjectTypeCatalog(ObjectTypeCatalog objectTypeCatalog) {
        this.objectTypeCatalog = objectTypeCatalog;
    }

    public List<GuiResourceDto> getResources() {
        return resources;
    }

    public void setResources(List<GuiResourceDto> resources) {
        this.resources = resources;
    }

    public List<AccountShadowDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountShadowDto> accounts) {
        this.accounts = accounts;
    }

    public GuiResourceDto getResourceDto() {
        return resourceDto;
    }

    public void setResourceDto(GuiResourceDto resourceDto) {
        this.resourceDto = resourceDto;
    }

    public GuiTestResultDto getGuiTestResult() {
        return guiTestResult;
    }

    public void setGuiTestResult(GuiTestResultDto guiTestResult) {
        this.guiTestResult = guiTestResult;
    }

    public TaskStatusDto getStatus() {
        return status;
    }

    public void setStatus(TaskStatusDto status) {
        this.status = status;
    }

    
}
