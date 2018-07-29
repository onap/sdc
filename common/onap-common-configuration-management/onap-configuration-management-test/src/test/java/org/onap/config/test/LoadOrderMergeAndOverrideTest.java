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

import static org.onap.config.util.TestUtil.writeFile;

/**
 * Scenario
 * Check loadorder for merge and overide. Higher loadorder takes precedence for override
 * LoWer loadorder takes precedence for merge.
 */
public class LoadOrderMergeAndOverrideTest {

    public static final String NAMESPACE = "LoadOrderConfiguration";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithPropertiesFileFormat(){
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals(config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH ), "14");
        Assert.assertEquals("5", config.getAsString(NAMESPACE, "artifact.length"));
        Assert.assertEquals("56", config.getAsString(NAMESPACE, "artifact.size"));
    }



    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
