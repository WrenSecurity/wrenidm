package com.forgerock.openidm.test.ldap;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;




import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author elek
 */
public class OpenDJUnitTestAdapterTest extends OpenDJUnitTestAdapter {

    public OpenDJUnitTestAdapterTest() {
    }

    @BeforeClass
    public static void init() throws Exception {
        dbTemplateDir = new File("src/main/resources/test-data/opendj.template");
        startACleanDJ();
    }

    @AfterClass
    public static void stop() throws Exception {
        stopDJ();
    }

    @Test
    public void testSomeMethod() {
        //comment it out if you would like to connect to the ldap
        //please attention that it's only a copy of the template so if you would like
        //to modify the ldap you should copy the resource back to src/main/resource/...
//       try {
//            Thread.sleep(100000000);
//        } catch (InterruptedException ex) {
//            System.out.println("OpenDJ server will be stopped. ");
//        }

    }
}
