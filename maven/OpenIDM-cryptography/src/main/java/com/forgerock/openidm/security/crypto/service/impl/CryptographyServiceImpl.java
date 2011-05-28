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
package com.forgerock.openidm.security.crypto.service.impl;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.security.crypto.service.CryptographyService;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.keyvalues.RSAKeyValue;
import org.apache.xml.security.utils.EncryptionConstants;
import org.apache.xml.security.utils.JavaUtils;
import org.apache.xml.security.utils.XMLUtils;
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
public class CryptographyServiceImpl implements CryptographyService {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(CryptographyServiceImpl.class);

    static {
        org.apache.xml.security.Init.init();
    }

    public CryptographyServiceImpl() {
//        try {
//            /**
//             * Java Key Store (JKS)
//             * Java Cryptography Extension Key Store (JCEKS)
//             * Personal Information Exchange Syntax Standard (PKCS12 Public Key Cryptography Standards #12)
//             *
//             */
//            // Load KeyStore and get the encryption key.
//            KeyStore ks = KeyStore.getInstance("JCEKS");
//            char[] password = "xmlsecurity".toCharArray();
//            InputStream keyFile = CryptographyServiceImpl.class.getResourceAsStream("cekeystore.jks");
//            if (null != keyFile) {
//                KeyStore.SecretKeyEntry keyEntry = (KeyStore.SecretKeyEntry) ks.getEntry("skey", new KeyStore.PasswordProtection(password));
//                ks.load(keyFile, password);
//            }
//
//
//            ks = KeyStore.getInstance("JKS");
//            keyFile = CryptographyServiceImpl.class.getResourceAsStream("/keysample/keystore.jks");
//            ks.load(keyFile, password);
//
//            javax.xml.parsers.DocumentBuilderFactory dbf =
//                    javax.xml.parsers.DocumentBuilderFactory.newInstance();
//
//            dbf.setNamespaceAware(true);
//
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            org.w3c.dom.Document doc = db.newDocument();
//            KeyInfo ki = new KeyInfo(doc);
//
//            doc.appendChild(ki.getElement());
//            ki.setId("myKI");
//            ki.addKeyName("A simple key");
//
//            X509Certificate cert = (X509Certificate) ks.getCertificate("test");
//
//            ki.addKeyValue(cert.getPublicKey());
//
//            X509Data x509Data = new X509Data(doc);
//
//            ki.add(x509Data);
//            x509Data.addCertificate(cert);
//            x509Data.addSubjectName("Subject name");
//            x509Data.addIssuerSerial("Subject nfsdfhs", 6786);
//            ki.add(new RSAKeyValue(doc, new BigInteger("678"),
//                    new BigInteger("6870")));
//            XMLUtils.outputDOMc14nWithComments(doc, System.out);
//
//
//
//        } catch (XMLSecurityException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (UnrecoverableEntryException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (KeyStoreException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (CertificateException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private static SecretKey GenerateAndStoreKeyEncryptionKey() throws NoSuchAlgorithmException {
        FileOutputStream f = null;
        String jceAlgorithmName = "DESede";
        KeyGenerator keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
        SecretKey kek = keyGenerator.generateKey();
//        try {
//            byte[] keyBytes = kek.getEncoded();
//            File kekFile = new File("/keyEncryption.key");
//            f = new FileOutputStream(kekFile);
//            f.write(keyBytes);
//            f.close();
//            System.out.println("Key encryption key stored in " + kekFile.toURL().toString());
//
//        } catch (IOException ex) {
//            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                f.close();
//            } catch (IOException ex) {
//                Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        return kek;
    }

    private static SecretKey loadKeyEncryptionKey() throws Exception {
        SecretKey key = null;
        String fileName = "/keysample/keyEncryption.key";
        String jceAlgorithmName = "DESede";
        InputStream is = CryptographyServiceImpl.class.getResourceAsStream(fileName);
        if (null != is) {
            DESedeKeySpec keySpec =
                    new DESedeKeySpec(JavaUtils.getBytesFromStream(is));
            SecretKeyFactory skf =
                    SecretKeyFactory.getInstance(jceAlgorithmName);
            key = skf.generateSecret(keySpec);
        }
        return key;
    }

    private static SecretKey GenerateDataEncryptionKey() throws NoSuchAlgorithmException {

        String jceAlgorithmName = "AES";
        KeyGenerator keyGenerator =
                KeyGenerator.getInstance(jceAlgorithmName);
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    @Override
    public Document encryptDocument(Document document) {
        try {

            /*
             * Get a key to be used for encrypting the element.
             * Here we are generating an AES key.
             */
            Key symmetricKey = GenerateDataEncryptionKey();

            /*
             * Get a key to be used for encrypting the symmetric key.
             * Here we are generating a DESede key.
             */
            //Key kek = GenerateAndStoreKeyEncryptionKey();
            Key kek = loadKeyEncryptionKey();

            String algorithmURI = XMLCipher.TRIPLEDES_KeyWrap;

            XMLCipher keyCipher =
                    XMLCipher.getInstance(algorithmURI);
            keyCipher.init(XMLCipher.WRAP_MODE, kek);
            EncryptedKey encryptedKey =
                    keyCipher.encryptKey(document, symmetricKey);

            /*
             * Let us encrypt the contents of the document element.
             */
            Element rootElement = document.getDocumentElement();

            algorithmURI = XMLCipher.AES_128;

            XMLCipher xmlCipher =
                    XMLCipher.getInstance(algorithmURI);
            xmlCipher.init(XMLCipher.ENCRYPT_MODE, symmetricKey);

            /*
             * Setting keyinfo inside the encrypted data being prepared.
             */
            EncryptedData encryptedData = xmlCipher.getEncryptedData();
            KeyInfo keyInfo = new KeyInfo(document);
            keyInfo.add(encryptedKey);
            encryptedData.setKeyInfo(keyInfo);

            /*
             * doFinal -
             * "true" below indicates that we want to encrypt element's content
             * and not the element itself. Also, the doFinal method would
             * modify the document by replacing the EncrypteData element
             * for the data to be encrypted.
             */
            xmlCipher.doFinal(document, rootElement, true);

            /*
             * Output the document containing the encrypted information into
             * a file.
             */

        } catch (XMLEncryptionException ex) {
            Logger.getLogger(CryptographyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.security.InvalidAlgorithmParameterException iape) {
            System.out.println("IAPE: " + iape);
        } catch (java.security.InvalidKeyException ike) {
            System.out.println("IKE: " + ike);
        } catch (java.security.NoSuchAlgorithmException nsae) {
            System.out.println("NSAE: " + nsae);
        } catch (javax.crypto.NoSuchPaddingException nspe) {
            System.out.println("NSPE: " + nspe);
        } catch (java.security.NoSuchProviderException snspe) {
            System.out.println("SNSPE: " + snspe);
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
        } catch (org.xml.sax.SAXException se) {
            System.out.println("SAXException: " + se);
        } catch (Exception e) {
        }
        return document;

    }

    @Override
    public Document decryptDocument(Document document) {
        try {

            Element encryptedDataElement =
                    (Element) document.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);

            /*
             * Load the key to be used for decrypting the xml data
             * encryption key.
             */
            //Key kek = GenerateAndStoreKeyEncryptionKey();
            Key kek = loadKeyEncryptionKey();

            String providerName = "BC";

            XMLCipher xmlCipher =
                    XMLCipher.getInstance();
            /*
             * The key to be used for decrypting xml data would be obtained
             * from the keyinfo of the EncrypteData using the kek.
             */
            xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
            xmlCipher.setKEK(kek);
            /*
             * The following doFinal call replaces the encrypted data with
             * decrypted contents in the document.
             */
            xmlCipher.doFinal(document, encryptedDataElement);

        } catch (Exception e) {
        }
        return document;
    }

    @Override
    public Document encryptElement(Element data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XNodeSet encryptNode(NodeList nl, String passPhrase, String keyStore, String keyName, String encryptionTemplate) {
        XNodeSet encryptedResult = null;
//        XEncryption xenc = new XEncryption();
//        Document doc;
//        Element ee = null, encrypted = null;
//
//        try {
//            DOMParser parser = new DOMParser();
//            parser.setIncludeIgnorableWhitespace(false);
//            parser.parse(encryptionTemplate);
//            doc = parser.getDocument();
//            ee = doc.getDocumentElement();
//            Element ek = (Element) (ee.getElementsByTagName("EncryptedKey").item(0));
//
//            KeyStore ks = KeyStore.getInstance("JKS");
//            //ks.load(new FileInputStream(keyStore), passPhrase.toCharArray());
//            Key k = null;
//            if (ks.isKeyEntry(keyName)) {
//                k = (ks.getCertificate(keyName)).getPublicKey();
//            }
//
//            encrypted = xenc.encrypt((Element) nl.item(0), false, ee, k, ek);
//            encryptedResult = new XNodeSet(encrypted);
//        } catch (java.security.InvalidAlgorithmParameterException iape) {
//            System.out.println("IAPE: " + iape);
//        } catch (java.security.InvalidKeyException ike) {
//            System.out.println("IKE: " + ike);
//        } catch (java.security.NoSuchAlgorithmException nsae) {
//            System.out.println("NSAE: " + nsae);
//        } catch (javax.crypto.NoSuchPaddingException nspe) {
//            System.out.println("NSPE: " + nspe);
//        } catch (java.security.NoSuchProviderException snspe) {
//            System.out.println("SNSPE: " + snspe);
//        } catch (IOException ioe) {
//            System.out.println("IOException: " + ioe);
//        } catch (org.xml.sax.SAXException se) {
//            System.out.println("SAXException: " + se);
//        }
        return encryptedResult;
    }
}
