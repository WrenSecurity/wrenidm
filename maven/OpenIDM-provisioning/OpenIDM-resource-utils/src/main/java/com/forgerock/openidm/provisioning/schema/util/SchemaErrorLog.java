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
package com.forgerock.openidm.provisioning.schema.util;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import java.text.MessageFormat;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ErrorHandler that reports sax errors to log.
 *
 * @author Vilo Repan
 */
public class SchemaErrorLog implements ErrorHandler {

    private static final Trace logger = TraceManager.getTrace(SchemaErrorLog.class);

    @Override
    public void warning(SAXParseException e) throws SAXException {
        print("[Warning]", e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        print("[Error]", e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        print("[Fatal]", e);
    }

    private void print(String header, SAXParseException e) {
        final String message = "Error occured during schema parsing: " + header + " " + MessageFormat.format("on line {0} at {1}",
                new Object[]{Integer.toString(e.getLineNumber()), e.getSystemId()}) + ": " + e.getMessage();

        logger.error(message);
        logger.trace(message, e);
    }
}
