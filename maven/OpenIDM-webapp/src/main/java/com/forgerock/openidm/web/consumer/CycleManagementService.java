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
package com.forgerock.openidm.web.consumer;

import com.forgerock.openidm.catalog.OASISCatalogManager;
import com.forgerock.openidm.ws.WSClientTool;
import com.forgerock.openidm.xml.ns._public.model.cycle_management_1.CycleManagementPortType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@WebServiceClient(name = "cycleManagementService", targetNamespace = "http://openidm.forgerock.com/xml/ns/public/model/cycle-management-1.wsdl", wsdlLocation = CycleManagementService.WSDL_LOCATION)
public class CycleManagementService
        extends Service {

    public static final String code_id = "$Id$";

    public static final String WSDL_LOCATION = "http://localhost:8080/cycleManagementService/CycleManagementService?wsdl";

    private static final Logger logger = Logger.getLogger(CycleManagementService.class.getName());

    private static final ResourceBundle properties = ResourceBundle.getBundle("openidm");

    private static String webEndpoint = "cycleManagementPortWebappClient";

    private static final URL CYCLE_MANAGEMENT_SERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            if (null != properties && "false".equalsIgnoreCase(properties.getString("JBI_ENVIRONMENT"))) {
                webEndpoint = "cycleManagementPort";
                if (null != properties.getString("OPENIDM_CYCLE_MANAGEMENT_SERVICE")) {
                    url = new URL(properties.getString("OPENIDM_CYCLE_MANAGEMENT_SERVICE"));
                }
            } else {
                try {
                    OASISCatalogManager cm = OASISCatalogManager.getContextCatalog();
                    String urlString = cm.getCatalog().resolveSystem(WSDL_LOCATION);
                    if (null != urlString) {
                        url = new URL(urlString);
                    } else {
                        // TODO: Fix NPE on baseUrl
                        URL baseUrl = CycleManagementService.class.getResource(".");
                        url = new URL(baseUrl, "../../../../../wsdl/cycleManagementWrapperWebapp.wsdl");
                    }
                } catch (IOException ex) {
                    URL baseUrl = CycleManagementService.class.getResource(".");
                    url = new URL(baseUrl, WSDL_LOCATION);
                }
            }
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: '" + WSDL_LOCATION + "', retrying as a local file");
            logger.warning(e.getMessage());
        }
        CYCLE_MANAGEMENT_SERVICE_WSDL_LOCATION = url;
    }

    public CycleManagementService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public CycleManagementService() {
        super(CYCLE_MANAGEMENT_SERVICE_WSDL_LOCATION, new QName("http://openidm.forgerock.com/xml/ns/public/model/cycle-management-1.wsdl", "cycleManagementService"));
    }

    /**
     *
     * @return
     *     returns CycleManagementPortType
     */
    @WebEndpoint(name = "cycleManagementPortWebappClient")
    public CycleManagementPortType getCycleManagementPort() {
        CycleManagementPortType port = super.getPort(new QName("http://openidm.forgerock.com/xml/ns/public/model/cycle-management-1.wsdl", webEndpoint), CycleManagementPortType.class);
        port = (CycleManagementPortType) WSClientTool.getInstance().fixWebServicePort(port);
        return port;
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CycleManagementPortType
     */
    @WebEndpoint(name = "cycleManagementPortWebappClient")
    public CycleManagementPortType getCycleManagementPort(WebServiceFeature... features) {
        CycleManagementPortType port = super.getPort(new QName("http://openidm.forgerock.com/xml/ns/public/model/cycle-management-1.wsdl", webEndpoint), CycleManagementPortType.class, features);
        port = (CycleManagementPortType) WSClientTool.getInstance().fixWebServicePort(port);
        return port;
    }
}
