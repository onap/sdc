package org.openecomp.config.test;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openecomp.config.util.ConfigTestConstant.*;
import static org.openecomp.config.util.ConfigTestConstant.ARTIFACT_ENCODED;
import static org.openecomp.config.util.ConfigTestConstant.ARTIFACT_MANDATORY_NAME;
import static org.openecomp.config.util.TestUtil.validateConfiguraton;
import static org.openecomp.config.util.TestUtil.writeFile;

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
        writeFile(data);
    }

    @Test
    public void testConfigurationWithMultiTenancyFileFormat(){
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals(config.getAsString("OPENECOMP",NAMESPACE, ARTIFACT_NAME_MAXLENGTH ), "20");

        Assert.assertEquals(config.getAsString("Telefonica",NAMESPACE, ARTIFACT_STATUS ), "Deleted");

        Assert.assertEquals(config.getAsString("TID",NAMESPACE, ARTIFACT_NAME_MAXLENGTH ), "14");

    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
