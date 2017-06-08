package org.openecomp.config.test;

import org.openecomp.config.ConfigurationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.openecomp.config.util.TestUtil.validateConfiguraton;
import static org.openecomp.config.util.TestUtil.writeFile;

/**
 * Created by ARR on 10/14/2016.
 *
 * Validate configuration with properties,xml,json,yaml file format with mode
 */
public class FallbackConfigTest {

    public static final String NAMESPACE = "fallback";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithFallbackFileFormat(){
        validateConfiguraton(NAMESPACE);
    }

    @After
    public void tearDown() throws Exception {
        String data = "{name:\"SCM\"}";
        writeFile(data);
        //ConfigurationUtils.executeDDLSQL("truncate dox.configuration");
    }
}
