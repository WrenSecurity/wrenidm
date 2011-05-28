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
package com.forgerock.openidm.security.crypto.service;

import com.forgerock.openidm.security.crypto.service.impl.CryptographyServiceImpl;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xml.security.utils.Constants;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author laszlohordos
 */
public class CryptographyServiceTest {

    public CryptographyServiceTest() {
    }

    /**
     * Test of encryptDocument method, of class CryptographyService.
     */
    @Test
    public void testEncryptDocument() throws Exception {
        System.out.println("encryptDocument");
        Document data = createSampleDocument();
        CryptographyService instance = new CryptographyServiceImpl();
        Document result = instance.encryptDocument(data);
        assertNotNull(result);
        outputDocToConsole(result);
        Document clearResult = instance.decryptDocument(result);
        outputDocToConsole(clearResult);
        assertEquals(data, clearResult);
    }


    private static Document createSampleDocument() throws Exception {

        javax.xml.parsers.DocumentBuilderFactory dbf =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.newDocument();

        /**
         * Build a sample document. It will look something like:
         *
         * <apache:RootElement xmlns:apache="http://www.apache.org/ns/#app1">
         * <apache:foo>Some simple text</apache:foo>
         * </apache:RootElement>
         */
        Element root =
                document.createElementNS(
                "http://www.apache.org/ns/#app1", "apache:RootElement");
        root.setAttributeNS(
                Constants.NamespaceSpecNS,
                "xmlns:apache",
                "http://www.apache.org/ns/#app1");
        document.appendChild(root);

        root.appendChild(document.createTextNode("\n"));

        Element childElement =
                document.createElementNS(
                "http://www.apache.org/ns/#app1", "apache:foo");
        childElement.appendChild(
                document.createTextNode("Some simple text"));
        root.appendChild(childElement);

        root.appendChild(document.createTextNode("\n"));

        outputDocToConsole(document);
        return document;
    }

    private static void outputDocToConsole(Document doc)
            throws Exception {

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        System.out.println("Result:");
        StreamResult result = new StreamResult(System.out);
        transformer.transform(source, result);
    }
}
