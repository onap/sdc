package org.onap.config.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.util.TestUtil;

import java.io.IOException;

import static org.onap.config.util.TestUtil.validateConfiguraton;
import static org.onap.config.util.TestUtil.writeFile;

/**
 * Created by ARR on 10/14/2016.
 *
 * Scenario 4
 * Validate configuration with YAML file format with mode
 */
public class YAMLConfigTest {

    public static final String NAMESPACE = "YAMLConfig";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithYAMLFileFormat(){
        validateConfiguraton(NAMESPACE);
    }



    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
