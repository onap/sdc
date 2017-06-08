package org.openecomp.config.test;

import org.openecomp.config.Constants;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;



/**
 * Created by sheetalm on 10/18/2016.
 * Scenario 21, Scenario 23
 * 21 - Verify the CLI fetches only the current value unless the fallback option is specified
 * 23 - Fetch value using CLI for a key with underlying resource
 */
public class CLIFallbackAndLookupTest {

    public final static String NAMESPACE = "CLIFallback";
    public final static String TENANT = "OPENECOMP";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testCLIFallbackAndLookup() throws Exception{

        //Verify without fallback
        Map<String, Object> input = new HashMap<>();
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        input.put("tenant", TENANT);
        input.put("namespace", NAMESPACE);
        input.put("key", ConfigTestConstant.ARTIFACT_MAXSIZE);

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName(Constants.MBEAN_NAME);
        ConfigurationManager conf = JMX.newMBeanProxy(mbsc, mbeanName, org.openecomp.config.api.ConfigurationManager.class, true);
        String maxSizeWithNoFallback = conf.getConfigurationValue(input);
        Assert.assertEquals("",maxSizeWithNoFallback);

        //Verify underlying resource without lookup switch
        input.put("key", ConfigTestConstant.ARTIFACT_JSON_SCHEMA);
        String jsonSchema = conf.getConfigurationValue(input);
        System.out.println("jsonSchema=="+jsonSchema);
        Assert.assertEquals("@"+System.getProperty("user.home")+"/TestResources/GeneratorsList.json" , jsonSchema);

        //Verify underlying resource with lookup switch
        input.put("externalLookup", true);
        jsonSchema = conf.getConfigurationValue(input);
        System.out.println("jsonSchema=="+jsonSchema);
        Assert.assertEquals("{name:\"SCM\"}" , jsonSchema);

        //Verify with fallback
        Map<String, Object> fallbackInput = new HashMap<>();
        fallbackInput.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        fallbackInput.put("fallback", true);
        fallbackInput.put("tenant", TENANT);
        fallbackInput.put("namespace", NAMESPACE);
        fallbackInput.put("key", ConfigTestConstant.ARTIFACT_MAXSIZE);

        String maxSizeWithFallback = conf.getConfigurationValue(fallbackInput);
        Assert.assertEquals("1024",maxSizeWithFallback);
    }

    @After
    public void tearDown() throws Exception {
      TestUtil.cleanUp();
    }
}
