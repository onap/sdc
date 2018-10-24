package org.onap.config.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Created by sheetalm on 10/14/2016.
 * Scenario 11
 * Validate conventional and configurational source location
 *
 * Pre-requisite - set -Dconfig.location=${"user.home"}/TestResources/ while running test
 */
public class ConfigSourceLocationTest {
    public final static String NAMESPACE = "SourceLocation";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);

        Properties props = new Properties();
        props.setProperty("maxCachedBufferSize", "1024");
        props.setProperty("artifact.maxsize", "1024");
        File f = new File(TestUtil.jsonSchemaLoc + "config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Config Property at Conventional Resource");
        }
    }

    @Test
    public void testMergeStrategyInConfig() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();
        Assert.assertEquals("a-zA-Z_0-9", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER));
        Assert.assertEquals("1024", config.getAsString(ConfigTestConstant.ARTIFACT_MAXSIZE));
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
