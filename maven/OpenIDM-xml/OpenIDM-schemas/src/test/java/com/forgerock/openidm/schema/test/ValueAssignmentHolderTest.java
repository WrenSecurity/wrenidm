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

import com.forgerock.openidm.xml.ns._public.common.common_1.AttributeDescriptionType;
import com.forgerock.openidm.xml.ns._public.common.common_1.FilterType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.ValueAssignmentType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ValueFilterType;
import com.forgerock.openidm.xml.schema.ValueAssignmentHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
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
public class ValueAssignmentHolderTest {

    private static final String FILENAME_ATTRIBUTE_DESCRIPTION_1 = "src/test/resources/testdata/attribute-description-1.xml";
    private static final String FILENAME_ATTRIBUTE_DESCRIPTION_2 = "src/test/resources/testdata/attribute-description-2.xml";

    public ValueAssignmentHolderTest() {
    }

        @Test
    public void basicValueAssignmentHolderTest() throws FileNotFoundException, JAXBException {

        File file = new File(FILENAME_ATTRIBUTE_DESCRIPTION_1);
        FileInputStream fis = new FileInputStream(file);

        Unmarshaller u = null;

        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        u = jc.createUnmarshaller();

        Object object = u.unmarshal(fis);

        AttributeDescriptionType attrDesc = (AttributeDescriptionType) ((JAXBElement) object).getValue();

        List<Element> inbounds = attrDesc.getInbound();
        Element inboundElement = inbounds.get(0);

        ValueAssignmentHolder inbound = new ValueAssignmentHolder(inboundElement);

        assertNotNull(inbound.getSource());
        assertEquals("$i:account/i:attributes/foo:vessel", inbound.getSource().getExpressionAsString().trim());
        assertEquals("$c:user/c:extension/piracy:ship", inbound.getTarget().getXPath().trim());

        // -----

        inboundElement = inbounds.get(1);
        inbound = new ValueAssignmentHolder(inboundElement);

        assertNull(inbound.getSource());
        assertEquals("$c:user/c:extension/foo:whatever", inbound.getTarget().getXPath().trim());


    }


    @Test
    public void filterValueAssignmentHolderTest() throws FileNotFoundException, JAXBException {

        File file = new File(FILENAME_ATTRIBUTE_DESCRIPTION_2);
        FileInputStream fis = new FileInputStream(file);

        Unmarshaller u = null;

        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        u = jc.createUnmarshaller();

        Object object = u.unmarshal(fis);

        AttributeDescriptionType attrDesc = (AttributeDescriptionType) ((JAXBElement) object).getValue();

        List<Element> inbounds = attrDesc.getInbound();
        Element inboundElement = inbounds.get(0);

        ValueAssignmentHolder inbound = new ValueAssignmentHolder(inboundElement);

        assertNotNull(inbound.getSource());
        assertEquals("$i:account/i:attributes/foo:vessel", inbound.getSource().getExpressionAsString().trim());
        assertEquals("$c:user/c:extension/piracy:ship", inbound.getTarget().getXPath().trim());
        
        List<ValueFilterType> filter = inbound.getFilter();

        System.out.println("Filters: "+filter);

        assertNotNull(filter);
        assertFalse(filter.isEmpty());

        assertEquals("http://whatever.com/filter#firstOne", filter.get(0).getType());

    }


}