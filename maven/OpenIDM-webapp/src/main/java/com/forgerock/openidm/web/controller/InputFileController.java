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

import com.forgerock.openidm.util.jaxb.JAXBUtil;
import com.forgerock.openidm.common.web.FacesUtils;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.validator.ObjectHandler;
import com.forgerock.openidm.validator.ValidationMessage;
import com.forgerock.openidm.validator.Validator;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import javax.xml.bind.JAXBElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.icefaces.component.fileentry.FileEntry;
import org.icefaces.component.fileentry.FileEntryEvent;
import org.icefaces.component.fileentry.FileEntryResults;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author katuska
 */
@Controller
@Scope("session")
public class InputFileController {

    private int fileProgress;
    private String xmlObject;
    private boolean overwrite = false;
    @Autowired(required = true)
    RepositoryPortType repositoryService;
    private static transient final org.slf4j.Logger logger = TraceManager.getTrace(InputFileController.class);

    public InputFileController() {
    }

    public String setImportPage() {
        xmlObject = "";

        return "/importPage";
    }

    private void addObjectsToRepository(List<ObjectType> objects) {
        if (objects == null) {
            return;
        }

        setImportPage();

        for (ObjectType object : objects) {
            ObjectContainerType objectContainer = new ObjectContainerType();
            objectContainer.setObject(object);
            try {
                if (overwrite) {
                    if (repositoryService.getObject(object.getOid(), new PropertyReferenceListType()) != null) {
                        repositoryService.deleteObject(object.getOid());
                    }
                }
                String result = repositoryService.addObject(objectContainer);
                FacesUtils.addSuccessMessage("Added object: " + object.getName());
            } catch (FaultMessage ex) {
                String message = ex.getFaultInfo() != null ? ex.getFaultInfo().getMessage() : ex.getMessage();
                String failureMesage = FacesUtils.getBundleKey("msg", "import.jaxb.failed");
                FacesUtils.addErrorMessage(failureMesage + " " + message);
                FacesUtils.addErrorMessage("Failed to add object " + object.getName());
                logger.error("Exception was: {}", ex, ex);
            } catch (Exception ex) {
                String failureMessage = FacesUtils.getBundleKey("msg", "import.jaxb.failed");
                FacesUtils.addErrorMessage(failureMessage + ":" + ex.getMessage());
                FacesUtils.addErrorMessage("Failed to add object " + object.getName());
                logger.error("Add object failed");
                logger.error("Exception was: {}", ex, ex);
            }

        }
        xmlObject = "";
    }

    public String addObjects() {
        if (StringUtils.isEmpty(xmlObject)) {
            String loginFailedMessage = FacesUtils.getBundleKey("msg", "import.null.failed");
            FacesUtils.addErrorMessage(loginFailedMessage);
            return "";
        }

        try {
            InputStream stream = IOUtils.toInputStream(xmlObject, "utf-8");
            uploadObjects(stream);
            stream.close();
        } catch (IOException ex) {
            FacesUtils.addErrorMessage("Couldn't load object from xml, reason: " + ex.getMessage());
        }

        return "/importPage";
    }

    public void uploadFile(FileEntryEvent event) {
        logger.info("uploadFile start");
        FileEntry fileEntry = (FileEntry) event.getSource();
        FileEntryResults results = fileEntry.getResults();
        for (FileEntryResults.FileInfo fi : results.getFiles()) {
            logger.info("file name {}", fi.getFileName());

            File file = fi.getFile();
            if (file == null || !file.exists() || !file.canRead()) {
                FacesUtils.addErrorMessage("Can't read file '" + fi.getFileName() + "'.");
                return;
            }

            try {
                InputStream stream = new BufferedInputStream(new FileInputStream(file));
                uploadObjects(stream);
                stream.close();
            } catch (IOException ex) {
                FacesUtils.addErrorMessage("Couldn't load object from file '" + file.getName() +
                        "', reason: " + ex.getMessage());
            }
        }

        logger.info("uploadFile end");

    }

    private void uploadObjects(InputStream input) {
        final List<ObjectType> objects = new ArrayList<ObjectType>();
        Validator validator = new Validator(new ObjectHandler() {

            @Override
            public void handleObject(ObjectType object, List<ValidationMessage> objectErrors) {
                objects.add(object);
            }
        });
        List<ValidationMessage> messages = validator.validate(input);

        if (messages != null && !messages.isEmpty()) {
            StringBuilder builder;
            for (ValidationMessage message : messages) {
                builder = new StringBuilder();
                builder.append(message.getType());
                builder.append(": Object with oid '");
                builder.append(message.getOid());
                builder.append("' is not valid, reason: ");
                builder.append(message.getMessage());
                builder.append(".");
                if (!StringUtils.isEmpty(message.getProperty())) {
                    builder.append(" Property: ");
                    builder.append(message.getProperty());
                }
                FacesUtils.addErrorMessage(builder.toString());
            }
            return;
        } else {
            addObjectsToRepository(objects);
        }
    }

    public void importDataFromFile() {
        try {
            //TODO: finish
            JAXBElement jaxb = (JAXBElement) JAXBUtil.unmarshal(new File(""));
            logger.info("jaxb {}", jaxb.getValue().getClass().getSimpleName());

        } catch (JAXBException ex) {
            logger.error("Unmarshaler failed");
            logger.error("Exception was; {}", ex);
        }

    }

    public int getFileProgress() {
        return fileProgress;
    }

    public void setFileProgress(int fileProgress) {
        this.fileProgress = fileProgress;
    }

    public String getXmlObject() {
        return xmlObject;
    }

    public void setXmlObject(String xmlObject) {
        this.xmlObject = xmlObject;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
