package com.forgerock.openidm.ws;

import com.forgerock.openidm.api.exceptions.OpenIDMException;
import com.forgerock.openidm.logging.TraceManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;

/**
 * SOAPHandler to log outgoing and ingoing request content.
 *
 * A HandlerChain végére érdemes rakni.
 * 
 * @author elek
 */
public class MessageLoggerHandler implements SOAPHandler<SOAPMessageContext> {

    private boolean writeToFile = true;

    Logger logger = TraceManager.getTrace(MessageLoggerHandler.class);

    /**
     * Headers to handle.
     *
     * @return
     */
    @Override
    public Set<QName> getHeaders() {
        return new HashSet();
    }

    /**
     * Print out the SOAP MESSAGES.
     * @param messageContext
     * @return
     */
    @Override
    public boolean handleMessage(SOAPMessageContext messageContext) {
        SOAPMessage msg = messageContext.getMessage();
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            msg.writeTo(bas);
            logger.info(bas.toString());
            if (writeToFile){
                Date d = new Date();
                File outDir = new File(System.getProperties().getProperty("java.io.tmpdir"),"openidm.log");
                if (!outDir.exists()){
                    outDir.mkdirs();
                }
                File outFile = new File(outDir,""+d.getTime()+".xml");
                FileOutputStream out = new FileOutputStream(outFile);
                out.write(bas.toByteArray());
                out.close();
            }
        } catch (Exception ex) {
            throw new OpenIDMException("Error on logging WS message", ex);
        }
        return true;
    }

    /**
     * Handle fault.
     *
     * @param context
     * @return
     */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    /**
     * NOOP deconstructor.
     * 
     * @param context
     */
    @Override
    public void close(MessageContext context) {
    }
}
