package org.openecomp.config.test;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;

/**
 * Scenario 12
 * Verify configuration value fallback to the global namespace if the configuraton property doesnot exist in the namespace configuration
 * Created by sheetalm on 10/14/2016.
 */
public class FallbackToGlobalNSTest {

    public final static String NAMESPACE = "FallbackToGlobalNS";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testFallbackToGlobalNS() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();
        Assert.assertEquals("14",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));
        Assert.assertEquals("1024",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_MAXSIZE));
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }


}
