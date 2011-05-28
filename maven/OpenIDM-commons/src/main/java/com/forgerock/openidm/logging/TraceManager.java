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
package com.forgerock.openidm.logging;

import com.forgerock.openidm.api.logging.*;
import com.forgerock.openidm.logging.impl.TraceImpl;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;
import org.slf4j.Logger;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class TraceManager {

    public static final String code_id = "$Id$";
    private static Logger LOG = org.slf4j.LoggerFactory.getLogger(TraceManager.class);

    public static Trace getTrace(Class clazz) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);
        Manifest manifest = null;
        try {
            String className = clazz.getSimpleName();
            String classFileName = className + ".class";
            String classFilePath = clazz.getPackage().toString().replace('.', '/') + "/" + className;
            String pathToThisClass = clazz.getResource(classFileName).toString();
            String pathToManifest = pathToThisClass.substring(0, pathToThisClass.length() + 2 - ("/" + classFilePath).length()) + "/META-INF/MANIFEST.MF";
            URL manifestUrl = new URL(pathToManifest);
            manifest = new Manifest(manifestUrl.openStream());

        } catch (IOException ex) {
            LOG.error("MANIFEST.MF Location is indeterminable."+ex.getMessage());
        }

        return new TraceImpl(logger, manifest);
    }
}
