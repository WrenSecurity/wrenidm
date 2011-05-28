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
 * "Portions Copyrighted 2011 [name of copyright owner]"
 * 
 * $Id$
 */
package com.forgerock.openidm.validator.test;

import com.forgerock.openidm.validator.ObjectHandler;
import com.forgerock.openidm.validator.ValidationMessage;
import com.forgerock.openidm.validator.Validator;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author semancik
 */
public class BasicValidatorTest {

    public static final String BASE_PATH = "src/test/resources/validator/";

    public BasicValidatorTest() {
    }

    @Test
    public void resource1Valid() throws Exception {

        List<ValidationMessage> errors = validateFile("resource-1-valid.xml");
        
        assertTrue(errors.isEmpty());

    }

    @Test
    public void handlerTest() throws Exception {

        final List<String> handledOids = new ArrayList<String>();

        ObjectHandler handler = new ObjectHandler() {

            @Override
            public void handleObject(ObjectType object, List<ValidationMessage> objectErrors) {
                handledOids.add(object.getOid());
            }
        };

        List<ValidationMessage> errors = validateFile("three-objects.xml",handler);

        assertTrue(errors.isEmpty());
        assertTrue(handledOids.contains("c0c010c0-d34d-b33f-f00d-111111111111"));
        assertTrue(handledOids.contains("c0c010c0-d34d-b33f-f00d-111111111112"));
        assertTrue(handledOids.contains("c0c010c0-d34d-b33f-f00d-111111111113"));
    }

    @Test
    public void notWellFormed() throws Exception {

        List<ValidationMessage> errors = validateFile("not-well-formed.xml");
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("terminated by the matching"));
        // Check if line number is in the error
        assertTrue(errors.get(0).toString().contains("line 52"));

    }

    @Test
    public void undeclaredPrefix() throws Exception {

        List<ValidationMessage> errors = validateFile("undeclared-prefix.xml");
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("not bound"));
        // Check if line number is in the error
        assertTrue(errors.get(0).toString().contains("line 48"));

    }


    @Test
    public void noName() throws Exception {

        List<ValidationMessage> errors = validateFile("no-name.xml");

        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Empty property"));
        assertTrue(errors.get(0).toString().contains("name"));

    }

    private List<ValidationMessage> validateFile(String filename) throws FileNotFoundException {
        return validateFile(filename,null);
    }

    private List<ValidationMessage> validateFile(String filename,ObjectHandler handler) throws FileNotFoundException {

        String filepath = BASE_PATH + filename;

        System.out.println("Validating " + filename);

        FileInputStream fis = null;

        File file = new File(filepath);
        fis = new FileInputStream(file);

        Validator validator = new Validator();
        if (handler!=null) {
            validator.setHandler(handler);
        }
        validator.setVerbose(false);

        List<ValidationMessage> errors = validator.validate(fis);

        if (!errors.isEmpty()) {
            for (ValidationMessage error : errors) {
                System.out.println(error);
            }
        } else {
            System.out.println("No errors");
        }

        return errors;

    }

}
