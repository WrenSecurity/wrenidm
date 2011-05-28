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

import org.apache.xpath.objects.XNodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public interface CryptographyService {

    public static final String code_id = "$Id$";

    public Document encryptDocument(Document data);

    public Document decryptDocument(Document data);

    public Document encryptElement(Element data);

    /*
    <?xml version="1.0"?>
    <xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:encrypt="EncryptionExtension"
    extension-element-prefixes="encrypt">

    <xsl:output method="xml"/>

    <xsl:template match="/">
    <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="*">
    <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates select="*|text()"/>
    </xsl:copy>
    </xsl:template>

    <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

    <xsl:template match="credit_payment">
    <xsl:copy-of select="encrypt:encryptNode(., 'storepass',
    'keystore', 'key', 'crypto-details.xml')"/>
    </xsl:template>

    </xsl:stylesheet>
     */

    /**
     *
     * @param nl
     * @param passPhrase
     * @param keyStore
     * @param keyName
     * @param encryptionTemplate
     * @return
     */
    public XNodeSet encryptNode(NodeList nl, String passPhrase,
            String keyStore, String keyName,
            String encryptionTemplate);
}
