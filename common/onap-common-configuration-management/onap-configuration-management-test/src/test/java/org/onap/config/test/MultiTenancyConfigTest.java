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
 * Created by ARR on 10/14/2016.
 *
 * Scenario 17
 * Verify Configuration management System - Support for Multi-Tenancy
 */
public class MultiTenancyConfigTest {

    public static final String NAMESPACE = "tenancy";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testConfigurationWithMultiTenancyFileFormat(){
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals(config.getAsString("OPENECOMP",NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH ), "20");

        Assert.assertEquals(config.getAsString("Telefonica",NAMESPACE, ConfigTestConstant.ARTIFACT_STATUS ), "Deleted");

        Assert.assertEquals(config.getAsString("TID",NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH ), "14");

    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
