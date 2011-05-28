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

package com.forgerock.openidm.schema.test;

import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.ValueConstructionType;
import com.forgerock.openidm.xml.schema.ExpressionHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

/**
 *
 * @author semancik
 */
public class ExpressionHolderTest {

    private static final String FILENAME_EXPRESSION_1 = "src/test/resources/examples/expression-1.xml";
    private static final String FILENAME_EXPRESSION_EXPLICIT_NS = "src/test/resources/examples/expression-explicit-ns.xml";

    public ExpressionHolderTest() {
    }

    @Test
    public void basicExpressionHolderTest() throws FileNotFoundException, JAXBException {

        File file = new File(FILENAME_EXPRESSION_1);
        FileInputStream fis = new FileInputStream(file);

        Unmarshaller u = null;

        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        u = jc.createUnmarshaller();

        Object object = u.unmarshal(fis);

        ValueConstructionType valueConstruction = (ValueConstructionType) ((JAXBElement) object).getValue();

        Element element = valueConstruction.getValueExpression();

        ExpressionHolder ex = new ExpressionHolder(element);

        assertEquals("$c:user/c:extension/foo:something/bar:somethingElse", ex.getExpressionAsString().trim());
        
        Map<String, String> namespaceMap = ex.getNamespaceMap();

        for(String key : namespaceMap.keySet()) {
            String uri = namespaceMap.get(key);
            System.out.println(key+" : "+uri);
        }

        assertEquals("http://openidm.forgerock.com/xml/ns/samples/piracy", namespaceMap.get("piracy"));
        assertEquals("http://default.com/whatever", namespaceMap.get(""));

    }

    @Test
    public void explicitNsTest() throws FileNotFoundException, JAXBException {

        File file = new File(FILENAME_EXPRESSION_EXPLICIT_NS);
        FileInputStream fis = new FileInputStream(file);

        Unmarshaller u = null;

        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        u = jc.createUnmarshaller();

        Object object = u.unmarshal(fis);

        ValueConstructionType valueConstruction = (ValueConstructionType) ((JAXBElement) object).getValue();

        Element element = valueConstruction.getValueExpression();

        ExpressionHolder ex = new ExpressionHolder(element);

        assertEquals("$c:user/c:extension/foo:something/bar:somethingElse", ex.getExpressionAsString().trim());

        Map<String, String> namespaceMap = ex.getNamespaceMap();

        for(String key : namespaceMap.keySet()) {
            String uri = namespaceMap.get(key);
            System.out.println(key+" : "+uri);
        }

        assertEquals("http://openidm.forgerock.com/xml/ns/samples/piracy", namespaceMap.get("piracy"));
        assertEquals("http://openidm.forgerock.com/xml/ns/samples/bar", namespaceMap.get("bar"));
        assertEquals("http://default.com/whatever", namespaceMap.get(""));

    }


}