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
package com.forgerock.openidm.validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Class with main method for command-line invocation of validator.
 * 
 * @author semancik
 */
public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            usage();
            return;
        }

        String filename = args[0];

        if (filename == null || filename.isEmpty()) {
            usage();
            return;
        }

        FileInputStream fis = null;

        try {

            File file = new File(args[0]);
            fis = new FileInputStream(file);
            Validator validator = new Validator();
            validator.setVerbose(true);
            List<ValidationMessage> errors = validator.validate(fis);

            if (!errors.isEmpty()) {
                for (ValidationMessage error : errors) {
                    System.out.println("ERROR: " + error);
                }
            } else {
                System.out.println("No errors");
            }

        } catch (FileNotFoundException ex) {
            System.out.println("File not found " + ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                System.out.println("Error closing the file " + ex);
            }
        }

    }

    static void usage() {
        System.out.println("Usage: TODO");
    }
}
