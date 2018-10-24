package org.onap.config.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

import java.io.IOException;

/**
 * Created by sheetalm on 10/13/2016.
 * Scenario 10 Verify configuration present in both global and defined namespace
 */
public class GlobalAndNSConfigTest {

    public final static String NAMESPACE = "GlobalAndNSConfig";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testNamespaceInConfig() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();
        Assert.assertEquals("a-zA-Z",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER ));
        Assert.assertEquals("a-zA-Z_0-9",config.getAsString(ConfigTestConstant.ARTIFACT_NAME_UPPER ));
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }




}
