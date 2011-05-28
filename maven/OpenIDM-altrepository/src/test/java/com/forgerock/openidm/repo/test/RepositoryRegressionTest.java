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
package com.forgerock.openidm.repo.test;

import com.forgerock.openidm.repository.RepositoryService;
import com.forgerock.openidm.repository.spring.GenericDao;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.Objects;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectNotFoundFaultType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.FaultMessage;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 *
 * @author sleepwalker
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../../../../application-context-repository.xml", "../../../../../application-context-repository-test.xml"})
public class RepositoryRegressionTest {

    private JAXBContext context = null;
    @Autowired(required = true)
    GenericDao genericDao;

    public GenericDao getGenericDao() {
        return genericDao;
    }

    public void setGenericDao(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    public RepositoryRegressionTest() throws Exception {
        context = JAXBContext.newInstance("com.forgerock.openidm.xml.ns._public.common.common_1");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
            
    }

    /**
     * Test simulates creation of object with specified ID - import of object and
     * remove of object with properties and attributes, to simulate cascade delete
     *
     * @throws Exception
     */
    @Test
    public void importResource() throws Exception {
        RepositoryService repositoryService = new RepositoryService();
        repositoryService.setGenericDao(genericDao);

        InputStream in = getClass().getResourceAsStream("/import-resource-regression.xml");

        Objects o = (Objects) context.createUnmarshaller().unmarshal(in);
        //test data ensure that we will have first object the one we expect
        JAXBElement<? extends ObjectType> firstObject = o.getObject().get(0);
        ResourceType resource = (ResourceType) firstObject.getValue();
        ObjectContainerType container = new ObjectContainerType();
        container.setObject(resource);

        try {
            repositoryService.deleteObject(resource.getOid());
        } catch(FaultMessage fm) {
            if (!(fm.getFaultInfo() instanceof ObjectNotFoundFaultType)) {
                fail();
            } else {
                //remove object is part of cleanup, if the object is not in DB everything is ok
            }
        }

        repositoryService.addObject(container);
    }
}
