/*
 *
 * CDDL HEADER START
 *
 * Parts of this file are based on OpenDS project. Therefore following
 * statement may also apply:
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2006-2010 Sun Microsystems, Inc.
 *      Copyright 2010 Forgerock
 *
 */
package com.forgerock.openidm.test.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opends.messages.Message;
import org.opends.server.config.ConfigException;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.types.Attribute;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.types.InitializationException;
import org.opends.server.util.EmbeddedUtils;

/**
 * This class controls embedded OpenDS instance.
 * 
 * It is used in Unit tests. It configures and starts and stops the instance.
 * It can even manage a "template" configuration of OpenDS and copy it to
 * working instance configuration.
 *
 * @author semancik
 */
public class OpenDJController {

    protected File serverRoot;

    protected File configFile;

    protected File templateServerRoot;


    protected InternalClientConnection internalConnection;

    public OpenDJController() {
        init(new File("opends"));
    }


    public OpenDJController(File ldapDataDir) {
        init(ldapDataDir);
    }

    public OpenDJController(File ldapDataDir,File templateDir) {
        init(ldapDataDir,templateDir);
    }

    public OpenDJController(String ldapDataDir) {
        init(new File(ldapDataDir));
    }

    private void init(File serverRoot) {
        init(serverRoot,null);
    }

    private void init(File serverRoot,File templateDir) {
        this.serverRoot = serverRoot;
        if (!serverRoot.exists()){
            serverRoot.mkdirs();
        }
        this.configFile = new File(serverRoot, "config/config.ldif");
        if (templateDir==null){
        this.templateServerRoot = new File(serverRoot.getParentFile(), "opendj.template");
        } else {
            this.templateServerRoot = templateDir;
        }
    }



    /**
     * Get the value of serverRoot.
     *
     * The top directory of working OpenDS installation.
     * The OpenDS placed in this directory will be used during
     * the tests.
     *
     * @return the value of serverRoot
     */
     public File getServerRoot() {
        return this.serverRoot;
    }

    /**
     * Set the value of serverRoot
     *
     * The top directory of working OpenDS installation.
     * The OpenDS placed in this directory will be used during
     * the tests.
     *
     * @param serverRoot new value of serverRoot
     */
    public void setServerRoot(File serverRoot) {
        this.serverRoot = serverRoot;
    }

    /**
     * Get the value of configFile
     *
     * File name of primary OpenDS configuration file.
     * Normally <serverRoot>/config/config.ldif
     *
     * @return the value of configFile
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Set the value of configFile
     *
     * File name of primary OpenDS configuration file.
     * Normally <serverRoot>/config/config.ldif
     *
     * @param configFile new value of configFile
     */
    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    /**
     * Get the value of templateServerRoot
     *
     * The top directory of template OpenDS installation.
     * All the files from this directory will be copied to the working
     * OpenDS directory (serverRoot). This usually happens before the tests.
     *
     * @return the value of templateServerRoot
     */
    public File getTemplateServerRoot() {
        return templateServerRoot;
    }

    /**
     * Set the value of templateServerRoot
     *
     * The top directory of template OpenDS installation.
     * All the files from this directory will be copied to the working
     * OpenDS directory (serverRoot). This usually happens before the tests.
     * 
     * @param templateServerRoot new value of templateServerRoot
     */
    public void setTemplateServerRoot(File templateServerRoot) {
        this.templateServerRoot = templateServerRoot;
    }

    /**
     * Get the value of internalConnection
     *
     * The connection to the OpenDS instance. It can be used to fetch and
     * manipulate the data.
     *
     * @return the value of internelConnection
     */
    public InternalClientConnection getInternalConnection() {
        return internalConnection;
    }

    /**
     * Set the value of internalConnection
     *
     * The connection to the OpenDS instance. It can be used to fetch and
     * manipulate the data.
     *
     * @param internelConnection new value of internelConnection
     */
    public void setInternalConnection(InternalClientConnection internalConnection) {
        this.internalConnection = internalConnection;
    }

    /**
     * Refresh working OpenDS installation from the template.
     *
     * The existing working OpenDS installation (in serverRoot) will be
     * discarded and replaced by a fresh known-state setup (from templateServerRoot).
     *
     * @throws IOException
     */
    public void refreshFromTemplate() throws IOException {
        deleteDirectory(serverRoot);
        copyDirectory(templateServerRoot,serverRoot);
    }

    /**
     * Start the embedded OpenDS directory server.
     *
     * Configuration and databases from serverRoot location will be used.
     * 
     * @return
     */
    public InternalClientConnection start() {

        DirectoryEnvironmentConfig envConfig = new DirectoryEnvironmentConfig();
        try {
            envConfig.setServerRoot(serverRoot);
            envConfig.setConfigFile(configFile);
            //envConfig.setDisableConnectionHandlers(true);
        } catch (InitializationException ex) {
            ex.printStackTrace();
            throw new RuntimeException("OpenDS initialization failed", ex);
        }

        // Check if the server is already running
        if (EmbeddedUtils.isRunning()) {
            throw new RuntimeException("Server already running");
        } else {
            try {

                EmbeddedUtils.startServer(envConfig);

            } catch (ConfigException ex) {
                ex.printStackTrace();
                throw new RuntimeException("OpenDS startup failed", ex);
            } catch (InitializationException ex) {
                ex.printStackTrace();
                throw new RuntimeException("OpenDS startup failed", ex);
            }
        }

        internalConnection = InternalClientConnection.getRootConnection();
        if (internalConnection == null) {
            throw new RuntimeException("OpenDS cannot get internal connection (null)");
        }

        return internalConnection;
    }

    /**
     * Stop the embedded OpenDS server.
     *
     */
    public void stop() {
        if (EmbeddedUtils.isRunning()){
            System.out.println("Stopping OpenDJ server");
            EmbeddedUtils.stopServer(this.getClass().getName(), Message.EMPTY);
            System.out.println("OpenDJ server is stopped");
        } else {

            System.err.println("Warrning: OpenDJ server is already stopped.");
        }
    }

    /**
     * Copy a directory and its contents.
     *
     * @param src
     *          The name of the directory to copy.
     * @param dst
     *          The name of the destination directory.
     * @throws IOException
     *           If the directory could not be copied.
     */
    public static void copyDirectory(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            // Create the destination directory if it does not exist.
            if (!dst.exists()) {
                dst.mkdirs();
            }

            // Recursively copy sub-directories and files.
            for (String child : src.list()) {
                copyDirectory(new File(src, child), new File(dst, child));
            }
        } else {
            copyFile(src, dst);
        }
    }

    /**
     * Delete a directory and its contents.
     *
     * @param dir
     *          The name of the directory to delete.
     * @throws IOException
     *           If the directory could not be deleted.
     */
    public static void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            // Recursively delete sub-directories and files.
            for (String child : dir.list()) {
                deleteDirectory(new File(dir, child));
            }
        }

        dir.delete();
    }

    /**
     * Copy a file.
     *
     * @param src
     *          The name of the source file.
     * @param dst
     *          The name of the destination file.
     * @throws IOException
     *           If the file could not be copied.
     */
    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public Set<String> asSet(List<Attribute> attributes) {
        // Just blindly get the fist one now.
        // There is most likely just one anyway.
        // TODO: improve that later

        Attribute attr = attributes.get(0);
        Set<String> result = new HashSet<String>();


        //TODO find newer OpenDS jar
//        Iterator<AttributeValue> iterator = attr.iterator();
//        while (iterator.hasNext()) {
//            result.add(iterator.next().toString());
//        }

        return result;
    }
}
