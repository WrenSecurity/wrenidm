package com.forgerock.openidm.provisioning.conversion;




import javax.xml.namespace.QName;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author elek
 */
public class StringConverterTest {

    public StringConverterTest() {
    }

   
    @Test
    public void convert() {
        StringConverter convert = new StringConverter();
        assertEquals("test", convert.convertToJava(convert.convertToXML(new QName("ns", "tag"),"test")));
    }

}