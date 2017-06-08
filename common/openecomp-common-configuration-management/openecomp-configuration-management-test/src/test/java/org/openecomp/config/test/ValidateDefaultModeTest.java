package org.openecomp.config.test;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openecomp.config.util.ConfigTestConstant.*;
import static org.openecomp.config.util.TestUtil.writeFile;

/**
 * Created by ARR on 10/17/2016.
 *
 * Scenario 22
 * Validate the default mode if the mode is not set
 */
public class ValidateDefaultModeTest {

    public static final String NAMESPACE = "defaultmode";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithValidateDefaultMode(){
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals(config.getAsString(NAMESPACE, ARTIFACT_NAME_MAXLENGTH ), "14");

        Assert.assertEquals(config.getAsString(NAMESPACE, ARTIFACT_MAXSIZE ), "1048");

        List<String> expectedExtList = new ArrayList<String>();
        expectedExtList.add("pdf");
        expectedExtList.add("tgz");
        expectedExtList.add("xls");
        List<String> extList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_EXT);
        Assert.assertEquals(expectedExtList, extList);

        Assert.assertEquals(config.getAsString(NAMESPACE, ARTIFACT_NAME_MINLENGTH ), "6");

    }

    @After
    public void tearDown() throws Exception {
        String data = "{name:\"SCM\"}";
        writeFile(data);
       // ConfigurationUtils.executeDDLSQL("truncate dox.configuration");
    }
}
