package com.forgerock.openidm.annotations;
import org.junit.Assert;



import org.junit.Test;

/**
 *
 * @author elek
 */
public class VersionTest {

    public VersionTest() {
    }

   
    @Test
    public void initialization() {
        Assert.assertFalse("UNKNOWN".equals(Version.VERSION));
        Assert.assertTrue(Version.VERSION.contains("."));
    }

}