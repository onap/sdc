package org.openecomp.config.test;

import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.openecomp.config.util.TestUtil.validateConfiguraton;
import static org.openecomp.config.util.TestUtil.writeFile;

/**
 * Created by ARR on 10/13/2016.
 *
 * Scenario 1
 * Validate configuration with Java Properties file format with mode
 */
public class JAVAPropertiesConfigTest {

    public static final String NAMESPACE = "javaProperties";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithPropertiesFileFormat(){
        validateConfiguraton(NAMESPACE);
    }



    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
