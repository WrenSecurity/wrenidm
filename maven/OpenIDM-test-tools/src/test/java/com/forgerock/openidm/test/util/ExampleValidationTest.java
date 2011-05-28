package com.forgerock.openidm.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXParseException;

/**
 *Validate example objects again XML schemas.
 * 
 * @author elek
 */
public class ExampleValidationTest {

    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    @Test
    public void validateReposioryObjects() throws Exception {
        List<File> inputFiles = new ArrayList();
        findFile(".*\\.xml", new File("src/main/resources/test-data/repository"), inputFiles);

        //for enable this an independent opendj.xsd file should be created
        //findFile(".*\\.xml", new File("src/main/resources/test-data/sample-requests"), inputFiles);

        List<File> schemaFiles = new ArrayList<File>();
        schemaFiles.add(new File("target/test-classes/standard/XMLSchema.xsd"));
        schemaFiles.add(new File("target/META-INF/wsdl/xml/ns/public/common/common-1.xsd"));
        schemaFiles.add(new File("target/META-INF/wsdl/xml/ns/public/common/exception-1.xsd"));
        schemaFiles.add(new File("target/META-INF/wsdl/xml/ns/public/resource/resource-schema-1.xsd"));
        schemaFiles.add(new File("target/META-INF/wsdl/xml/ns/public/resource/idconnector/configuration-1.xsd"));
        schemaFiles.add(new File("target/META-INF/wsdl/xml/ns/public/resource/idconnector/resource-schema-1.xsd"));

        Source[] schemaSources = new Source[schemaFiles.size()];
        int i = 0;
        for (File schema : schemaFiles) {
            System.out.println("Adding schema to validation: " + schema.getAbsolutePath());
            schemaSources[i] = new StreamSource(new FileInputStream(schema));
            i++;

        }

        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(schemaSources);
        Validator validation = schema.newValidator();
        for (File inputFile : inputFiles) {
            System.out.println("Validating " + inputFile.getAbsolutePath());
            Source source = new StreamSource(new FileInputStream(inputFile));
            try {
                validation.validate(source);
            } catch (SAXParseException ex) {
                System.out.println("ERROR on " + inputFile.getName() + " atline " + ex.getLineNumber());
                throw ex;
            }
        }






    }

    private void findFile(String pattern, File baseDir, List<File> result) {
        for (File file : baseDir.listFiles()) {
            if (file.isDirectory()) {
                findFile(pattern, file, result);
            } else if (file.getName().matches(pattern)) {
                result.add(file);
            }
        }

    }
}
