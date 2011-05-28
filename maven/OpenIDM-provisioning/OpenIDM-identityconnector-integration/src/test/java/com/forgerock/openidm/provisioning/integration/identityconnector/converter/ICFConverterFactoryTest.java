package com.forgerock.openidm.provisioning.integration.identityconnector.converter;


import com.forgerock.openidm.provisioning.conversion.Converter;
import com.forgerock.openidm.provisioning.integration.identityconnector.converter.ICFConverterFactory;
import org.identityconnectors.common.security.GuardedString;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;




import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author elek
 */
public class ICFConverterFactoryTest {

    public ICFConverterFactoryTest() {
    }

    @Test
    public void convert() {
        assertEquals(new GuardedString("test".toCharArray()),ICFConverterFactory.getInstance().getConverter(GuardedString.class, "test").convert("test"));
    }
}
