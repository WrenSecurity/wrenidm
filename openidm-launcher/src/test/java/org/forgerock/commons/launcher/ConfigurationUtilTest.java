/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock AS. All Rights Reserved
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
 */

package org.forgerock.commons.launcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import org.testng.annotations.Test;

/**
 * A NAME does ...
 *
 * @author Laszlo Hordos
 */
public class ConfigurationUtilTest {
    @Test
    public void testGetZipFileListing() throws Exception {
        URL zip = ConfigurationUtilTest.class.getResource("/test2/bundles.zip");
        Vector<URL> result = ConfigurationUtil.getZipFileListing(zip, null, null);
        assertEquals(result.size(), 3, "Find all files");
        for (URL file : result) {
            InputStream is = null;
            try {
                is = file.openConnection().getInputStream();
                if (is != null) {
                    assertTrue(is.available() > 0, "Stream is empty");
                } else {
                    fail("Can not read from " + file);
                }
            } catch (Exception e) {
                fail(e.getMessage());
            } finally {
                if (null != is) {
                    try {
                        is.close();
                    } catch (Exception e) {/* ignore */
                    }
                }
            }
        }
        result = ConfigurationUtil.getZipFileListing(zip, List.of("**/*.jar"), null);
        assertEquals(result.size(), 2, "Find all jar files");
        result = ConfigurationUtil.getZipFileListing(zip, List.of("*.jar"), null);
        assertEquals(result.size(), 1, "Find jar file in the root");
        result = ConfigurationUtil.getZipFileListing(zip, List.of("bundle/*.jar"), null);
        assertEquals(result.size(), 1, "Find jar file in the bundle");
        result = ConfigurationUtil.getZipFileListing(zip, List.of("*.jar", "**/*.jar"), List.of("bundle/*.jar"));
        assertEquals(result.size(), 1, "Find jar file in the root exclude the bundle");
    }

}
