package org.openecomp.config.test;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.io.OutputStream;
import java.io.FileOutputStream;

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
