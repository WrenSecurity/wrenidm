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
import com.forgerock.openidm.logging.LogInfo;
import com.forgerock.openidm.logging.LoggerMXBean;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.web.bean.TraceItem;
import com.forgerock.openidm.web.bean.TraceModule;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Vilo Repan
 */
@Controller
@Scope("session")
public class TraceController implements Serializable {

    private static transient final Trace trace = TraceManager.getTrace(TraceController.class);
    private static final int[] levels = {
        Level.ALL_INT, Level.DEBUG_INT, Level.ERROR_INT, Level.FATAL_INT,
        Level.INFO_INT, Level.OFF_INT, Level.TRACE_INT, Level.WARN_INT
    };
    private List<TraceModule> modules;
    @Autowired(required = true)
    private transient org.springframework.jmx.export.MBeanExporter exporterWeb;

    public String refreshPerformed() {
        trace.debug("debug");
        trace.error("error");
        trace.info("info");
        trace.trace("trace");
        trace.warn("warn");
        
        getModules().clear();
        try {
            MBeanServer mbs = exporterWeb.getServer();
            List<ObjectName> names = new ArrayList<ObjectName>(mbs.queryNames(new ObjectName("openidm:name=*"), null));

            int id = 0;
            for (ObjectName name : names) {
                trace.debug("Available logger MXBean: " + name);

                LoggerMXBean bean = JMX.newMXBeanProxy(mbs, name, LoggerMXBean.class);
                TraceModule module = new TraceModule(getModules().size(), bean.getModuleLogLevel(), bean.getLogPattern(), bean.getDisplayName());
                getModules().add(module);

                List<LogInfo> infoList = bean.getLogInfoList();
                for (LogInfo info : infoList) {
                    module.getItems().add(new TraceItem(id, info.getPackageName(), info.getLevel()));
                    id++;
                }

                if (module.getItemsSize() == 0) {
                    module.getItems().add(new TraceItem(id));
                    id++;
                }
            }
        } catch (Exception ex) {
            trace.error("Couldn't refresh logging settings.", ex);
            FacesUtils.addErrorMessage("Couldn't refresh logging settings, reason: " + ex.getMessage());
        }

        return null;
    }

    public void setExporterWeb(MBeanExporter exporter) {
        this.exporterWeb = exporter;

        refreshPerformed();
    }

    public List<TraceModule> getModules() {
        if (modules == null) {
            modules = new ArrayList<TraceModule>();
        }
        return modules;
    }

    public List<SelectItem> getLogLevels() {
        List<SelectItem> list = new ArrayList<SelectItem>();

        for (Integer level : levels) {
            list.add(new SelectItem(level, Level.toLevel(level).toString()));
        }

        return list;
    }

    public int getValue(String value) {
        int id = -1;
        if (value != null && value.matches("[0-9]*")) {
            id = Integer.parseInt(value);
        }

        return id;
    }

    public void deleteTrace(ActionEvent evt) {
        trace.debug("Deleting package");
        int moduleId = getValue(FacesUtils.getRequestParameter("moduleId"));
        int itemId = getValue(FacesUtils.getRequestParameter("itemId"));

        if (moduleId == -1 || itemId == -1) {
            return;
        }

        List<TraceItem> itemList = null;
        TraceItem toBeRemoved = null;

        TraceModule module = getModule(moduleId);
        if (module == null) {
            return;
        }
        List<TraceItem> items = module.getItems();
        for (TraceItem item : items) {
            if (item.getId() == itemId) {
                itemList = items;
                toBeRemoved = item;
                break;
            }
        }

        if (itemList != null && toBeRemoved != null) {
            itemList.remove(toBeRemoved);
        }
    }

    public void addTrace(ActionEvent evt) {
        trace.debug("Adding package");
        int moduleId = getValue(FacesUtils.getRequestParameter("moduleId"));
        if (moduleId == -1) {
            return;
        }

        TraceModule module = getModule(moduleId);
        if (module == null) {
            return;
        }

        module.getItems().add(new TraceItem(getNewItemId()));
    }

    private TraceModule getModule(int moduleId) {
        TraceModule module = null;
        List<TraceModule> moduleList = getModules();
        for (TraceModule mod : moduleList) {
            if (mod.getId() == moduleId) {
                module = mod;
                break;
            }
        }

        return module;
    }

    private int getNewItemId() {
        int id = 0;
        List<TraceModule> moduleList = getModules();
        for (TraceModule module : moduleList) {
            List<TraceItem> items = module.getItems();
            for (TraceItem item : items) {
                if (item.getId() > id) {
                    id = item.getId();
                }
            }
        }
        id++;

        return id;
    }

    private void removeEmptyTraceItems() {
        List<TraceModule> moduleList = getModules();
        for (TraceModule module : moduleList) {
            List<TraceItem> toBeDeleted = new ArrayList<TraceItem>();
            List<TraceItem> items = module.getItems();
            for (TraceItem item : items) {
                if (item.getPackageName() == null || item.getPackageName().isEmpty()) {
                    toBeDeleted.add(item);
                }
            }

            module.getItems().removeAll(toBeDeleted);
        }
    }

    public String savePerformed() {
        try {
            saveLoggingConfiguration(getModules());

            MBeanServer mbs = exporterWeb.getServer();
            List<ObjectName> names = new ArrayList<ObjectName>(mbs.queryNames(new ObjectName("openidm:name=*"), null));
            Map<String, LoggerMXBean> beanMap = new HashMap<String, LoggerMXBean>();
            for (ObjectName name : names) {
                LoggerMXBean bean = JMX.newMXBeanProxy(mbs, name, LoggerMXBean.class);
                beanMap.put(bean.getName(), bean);
            }
            saveChangesToJMX(beanMap, getModules());

            removeEmptyTraceItems();

            for (TraceModule module : modules) {
                if (module.getItemsSize() == 0) {
                    module.getItems().add(new TraceItem(getNewItemId()));
                }
            }
        } catch (Exception ex) {
            trace.error("Couldn't save logging settings.", ex);
            FacesUtils.addErrorMessage("Couldn't save logging settings, reason: " + ex.getMessage());
        }

        return null;
    }

    //TODO: implement saving to idm configuration xml
    private void saveLoggingConfiguration(List<TraceModule> moduleList) {
        
    }

    private void saveChangesToJMX(Map<String, LoggerMXBean> beanMap, List<TraceModule> moduleList) {
        for (TraceModule module : moduleList) {
            if (!beanMap.containsKey(module.getName())) {
                FacesUtils.addErrorMessage("Couldn't save logger settings for module '" +
                        module.getName() + "'.");
                continue;

            }

            List<LogInfo> infoList = new ArrayList<LogInfo>();
            List<TraceItem> items = module.getItems();
            for (TraceItem item : items) {
                if (item.getPackageName() == null || item.getPackageName().isEmpty()) {
                    continue;
                }
                infoList.add(new LogInfo(item.getPackageName(), item.getLevel()));
            }

            LoggerMXBean bean = beanMap.get(module.getName());
            bean.setModuleLogLevel(module.getLevel());
            bean.setLogPattern(module.getLogPattern());
            bean.setLogInfoList(infoList);
        }
    }
}
