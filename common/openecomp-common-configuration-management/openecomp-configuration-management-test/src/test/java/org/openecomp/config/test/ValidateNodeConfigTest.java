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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Scenario 13
 * Validate node specific configuration
 * Pre-requisite - set -Dnode.config.location=${"user.home"}/TestResources/ while running test
 *
 * Created by sheetalm on 10/14/2016.
 */
public class ValidateNodeConfigTest {

    public final static String NAMESPACE = "ValidateNodeConfig";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testValidateNodeConfig() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();

        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "56");
        props.setProperty("_config.namespace","ValidateNodeConfig");
        File f = new File(TestUtil.jsonSchemaLoc+"config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Node Config Property");
        }

        System.out.println(System.getProperty("node.config.location"));

        Thread.sleep(35000);

        //Verify property from node specific configuration
        Assert.assertEquals("56", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        //Verify if property is not in node specific then fetch from namespace
        //Assert.assertEquals("a-zA-Z",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER));

        //Verify if property is not in node specific and namespace then fetch from global
        Assert.assertEquals("1024", config.getAsString(NAMESPACE, "maxCachedBufferSize"));

        //Deleting node configurations to test property is fetched from namespace configuration when node configuration is not present
        if(f.exists()) {
            boolean isDeleted = f.delete();
            System.out.println(isDeleted);
        }

        Thread.sleep(35000);

        Assert.assertEquals(config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH), "14");
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
