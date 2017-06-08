package org.openecomp.config.test;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.openecomp.config.util.TestUtil.validateConfiguraton;
import static org.openecomp.config.util.TestUtil.writeFile;

/**
 * Created by ARR on 10/14/2016.
 *
 * Scenario 2
 * Validate configuration with XML file format with mode
 */
public class XMLConfigTest {

    public static final String NAMESPACE = "XMLConfig";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithXMLFileFormat(){
        validateConfiguraton(NAMESPACE);
    }



    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
