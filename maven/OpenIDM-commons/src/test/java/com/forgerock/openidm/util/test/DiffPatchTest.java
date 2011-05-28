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
package com.forgerock.openidm.util.test;

import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.diff.OidQualifier;
import com.forgerock.openidm.util.diff.OpenIdmDifferenceListener;
import com.forgerock.openidm.util.patch.PatchXml;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringBufferInputStream;
import java.util.List;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.XMLUnit;
import static junit.framework.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 *
 * @author Igor Farinic
 */
public class DiffPatchTest {

    public DiffPatchTest() {
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

    public static void assertPatch(File fileNewXml, String patchedXml) throws Exception {
        setupXmlUnitForTest();
        Diff d = new Diff(new InputSource(new FileInputStream(fileNewXml)), new InputSource(new StringBufferInputStream(patchedXml)));
        DetailedDiff dd = new DetailedDiff(d);
        dd.overrideElementQualifier(new OidQualifier());
        dd.overrideDifferenceListener(new OpenIdmDifferenceListener());
        List<Difference> differences = dd.getAllDifferences();

        for (Difference diff : differences) {
            switch (diff.getId()) {
//                case DifferenceConstants.NAMESPACE_PREFIX_ID:
//                    //ignore namespaces
//                    //TODO: ^^^
//                    break;
                case DifferenceConstants.ATTR_VALUE_ID:
                    if (diff.getControlNodeDetail().getNode().getNodeName().contains("type")) {
                        //ignore attribute values for xsi type, because of namespaces
                        //TODO: ^^^
                        break;
                    }
                case DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID:
                case DifferenceConstants.SCHEMA_LOCATION_ID:
                    break;
                default:
                    fail(diff.toString());
            }
        }

    }

    private static void setupXmlUnitForTest() {
        //XmlUnit setup
        //Note: compareUnmatched has to be set to false to calculate diff properly, to avoid matching of nodes that are not comparable
        XMLUnit.setCompareUnmatched(false);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void testDiffPatchAccount() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/account-old.xml"), new File("src/test/resources/account-new.xml"));
        assertNotNull(changes);
        assertEquals(5, changes.getPropertyModification().size());
        assertEquals("12345", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/account-old.xml"));
        assertPatch(new File("src/test/resources/account-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchAccountWithResourceSchemaHandlingConfiguration() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/account-full-old.xml"), new File("src/test/resources/account-full-new.xml"));
        assertNotNull(changes);
        assertEquals(5, changes.getPropertyModification().size());
        assertEquals("12345", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/account-full-old.xml"));
        assertPatch(new File("src/test/resources/account-full-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchUser() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/user-old.xml"), new File("src/test/resources/user-new.xml"));
        assertNotNull(changes);
        assertEquals(6, changes.getPropertyModification().size());
        assertEquals("007", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/user-old.xml"));
        assertPatch(new File("src/test/resources/user-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchUserExtension() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/user-extension-old.xml"), new File("src/test/resources/user-extension-new.xml"));
        assertNotNull(changes);
        assertEquals(1, changes.getPropertyModification().size());
        assertEquals("007", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/user-extension-old.xml"));
        assertPatch(new File("src/test/resources/user-extension-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchResource() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/resource-old.xml"), new File("src/test/resources/resource-new.xml"));
        assertNotNull(changes);
        assertEquals(1, changes.getPropertyModification().size());
        assertEquals("ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/resource-old.xml"));
        assertPatch(new File("src/test/resources/resource-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchAdvancedResource() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/resource-advanced-old.xml"), new File("src/test/resources/resource-advanced-new.xml"));
        assertNotNull(changes);
        assertEquals(1, changes.getPropertyModification().size());
        assertEquals("ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/resource-advanced-old.xml"));
        assertPatch(new File("src/test/resources/resource-advanced-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchResourceSchemaHandling() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/resource-schemahandling-old.xml"), new File("src/test/resources/resource-schemahandling-new.xml"));
        assertNotNull(changes);
        assertEquals(1, changes.getPropertyModification().size());
        assertEquals("ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/resource-schemahandling-old.xml"));
        assertPatch(new File("src/test/resources/resource-schemahandling-new.xml"), patchedXml);
    }

    @Test
    public void testDiffPatchUserCredentials() throws Exception {
        ObjectModificationType changes = CalculateXmlDiff.calculateChanges(new File("src/test/resources/user-credentials-old.xml"), new File("src/test/resources/user-credentials-new.xml"));
        assertNotNull(changes);
        assertEquals(2, changes.getPropertyModification().size());
        assertEquals("d7f1f990-b1fc-4001-9003-2106bd289c5b", changes.getOid());

        String patchedXml = (new PatchXml()).applyDifferences(changes, new File("src/test/resources/user-credentials-old.xml"));
        assertPatch(new File("src/test/resources/user-credentials-new.xml"), patchedXml);
    }
}
