/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2015 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openidm.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directs OSGi logging service entries into the slf4j logger
 *
 */
public class OsgiLogHandler {

    private static final Logger logger = LoggerFactory.getLogger(OsgiLogHandler.class);

    ServiceTracker<LogReaderService, LogReaderService> tracker;

    public OsgiLogHandler(final BundleContext context) {
        tracker = new LogServiceTracker<>(context, LogReaderService.class.getName(), null);
        tracker.open();
        logger.debug("Log service tracker opened");
    }
}

/**
 * Attach log listeners to log reader services to re-direct to slf4j
 */
class LogServiceTracker<S, T extends LogReaderService> extends ServiceTracker<S, T> {

    private static final Logger logger = LoggerFactory.getLogger(LogServiceTracker.class);

    // Logger to use for OSGi log entries
    private static final Logger defaultEntryLogger = LoggerFactory.getLogger("org.forgerock.openidm.Framework");

    private Map<T, LogListener> logReaderServices = Collections.synchronizedMap(new HashMap<T, LogListener>());

    public LogServiceTracker(BundleContext context, String clazz, ServiceTrackerCustomizer<S, T> customizer) {
        super(context, clazz, customizer);
    }

    @Override
    public T addingService(ServiceReference<S> reference) {
        @SuppressWarnings("unchecked")
        T svc = (T) context.getService(reference);
        addLogReaderService(svc);
        return svc;
    }

    @Override
    public void removedService(ServiceReference<S> reference, T service) {
        @SuppressWarnings("unchecked")
        T svc = (T) context.getService(reference);
        removeLogReaderService(svc);
    }

    private void addLogReaderService(T reader) {
        logger.trace("Adding log reader service");
        LogListener existing = logReaderServices.get(reader);
        if (existing == null) {
            LogListener logListener = new LogListener() {
                @Override
                public void logged(LogEntry entry) {
                    logEntry(entry);
                }
            };
            reader.addLogListener(logListener);

            // This dumps the existing log entries.
            // These entries may appear out of chronological order
            // if services already started logging to slf4j directly
            @SuppressWarnings("rawtypes")
            java.util.Enumeration entries = reader.getLog();
            while (entries.hasMoreElements()) {
                logEntry((LogEntry) entries.nextElement());
            }

            // Redirects all new log entries
            logReaderServices.put(reader, logListener);
        }
    }

    /**
     * Log the OSGi entry using the slf4j logger
     * @param entry log entry
     */
    private void logEntry(LogEntry entry) {
        StringBuilder logMessage = new StringBuilder("Bundle: ");
        logMessage.append(entry.getBundle());
        if (entry.getServiceReference() != null) {
            logMessage.append(" - ");
            logMessage.append(entry.getServiceReference());
        }
        logMessage.append(" ");
        logMessage.append(entry.getMessage());

        Throwable ex = entry.getException();

        switch (entry.getLogLevel()) {
            case ERROR: {
                defaultEntryLogger.error(logMessage.toString(), ex);
                break;
            }
            case WARN: {
                defaultEntryLogger.warn(logMessage.toString(), ex);
                break;
            }
            case INFO: {
                defaultEntryLogger.info(logMessage.toString(), ex);
                break;
            }
            case DEBUG: {
                defaultEntryLogger.debug(logMessage.toString(), ex);
                break;
            }
            default: {
                defaultEntryLogger.warn("Unknown OSGi log level [" + entry.getLogLevel() + "] for" + logMessage.toString(), ex);
            }
        }
    }

    private void removeLogReaderService(T reader) {
        logger.trace("Removing log reader service");
        LogListener logListener = logReaderServices.remove(reader);
        if (logListener != null) {
            reader.removeLogListener(logListener);
        }
    }
}