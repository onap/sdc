package org.openecomp.config.test;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.api.DynamicConfiguration;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Created by sheetalm on 10/17/2016.
 * Pre-requisite - set -Dconfig.location=${"user.home"}/TestResources/ while running test
 * Scenario 20
 * Update the central configuration and fetch the Dynamic Configuration
 */
public class DynamicConfigurationTest {

    public final static String NAMESPACE = "DynamicConfiguration";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testDynamicConfig() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();
        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "20");
        props.setProperty("_config.namespace",NAMESPACE);
        props.setProperty("_config.mergeStrategy","override");
        File f = new File(TestUtil.jsonSchemaLoc + "config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Override Config Property at Conventional Resource");
        }

        //Verify configuration with Configuration without wait. This should fetch cached value
        Assert.assertEquals("14" , config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        Thread.sleep(10000);

        DynamicConfiguration dynaConfig = config.getDynamicConfiguration(NAMESPACE,ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH,String.class,"14");
        //Verify configuration with DynamicConfiguration This should fetch values from DB
        Assert.assertEquals("20" , dynaConfig.get());

    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
        File f = new File(TestUtil.jsonSchemaLoc+"config.properties");
        if(f.exists()) {
            f.delete();
        }
    }
}
