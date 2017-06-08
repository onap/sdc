package org.openecomp.config.test;

import org.openecomp.config.Constants;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import static org.openecomp.config.util.ConfigTestConstant.*;

/**
 * Created by sheetalm on 10/18/2016.
 * Scenario 17
 * Verify Configuration Management System - Command Line Interface for query, update and list operations
 */
public class CLITest {

        public final static String NAMESPACE = "CLI";
        public final static String TENANT = "OPENECOMP";
        private String updatedValue = "";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
        public void testCLIApi() throws Exception{
        //Verify without fallback
        Map<String, Object> input = new HashMap<>();
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        input.put("tenant", TENANT);
        input.put("namespace", NAMESPACE);
        input.put("key", ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH);

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName(Constants.MBEAN_NAME);
        ConfigurationManager conf = JMX.newMBeanProxy(mbsc, mbeanName, org.openecomp.config.api.ConfigurationManager.class, true);
        String maxLength = conf.getConfigurationValue(input);
        Assert.assertEquals("14",maxLength);

        conf.addConfigurationChangeListener(TENANT,NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, new CLIListener());


        //Update maxlength
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationUpdate");
        input.put("value", "24");
        conf.updateConfigurationValue(input);

        Thread.sleep(35000);

        Assert.assertEquals("24",updatedValue);

        //Reset value and fetch updated value again
        input.put("value", "");
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        String updatedMaxLength = conf.getConfigurationValue(input);
        Assert.assertEquals("24",updatedMaxLength);

        Map<String, String> outputMap = conf.listConfiguration(input);
        for(Map.Entry<String, String> entry : outputMap.entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue());
            validateCLIListConfig(outputMap);
        }
    }

    private class CLIListener implements ConfigurationChangeListener {
        @Override
        public void notify(String key, Object oldValue, Object newValue) {
            System.out.println("received notification::oldValue=="+oldValue+" newValue=="+newValue);
            updatedValue = newValue.toString();
        }
    }

    private void validateCLIListConfig(Map<String, String> outputMap ) {

        Assert.assertEquals("@"+System.getProperty("user.home")+"/TestResources/GeneratorsList.json" , outputMap.get(ARTIFACT_JSON_SCHEMA));
        Assert.assertEquals("appc,catalog", outputMap.get(ARTIFACT_CONSUMER));
        Assert.assertEquals("6", outputMap.get(ARTIFACT_NAME_MINLENGTH));
        Assert.assertEquals("true", outputMap.get(ARTIFACT_ENCODED));
        Assert.assertEquals("24", outputMap.get(ARTIFACT_NAME_MAXLENGTH));
        Assert.assertEquals("pdf,zip,xml,pdf,tgz,xls", outputMap.get(ARTIFACT_EXT));
        Assert.assertEquals("Base64,MD5", outputMap.get(ARTIFACT_ENC));
        Assert.assertEquals("@"+System.getenv("Path")+"/myschema.json", outputMap.get(ARTIFACT_XML_SCHEMA));
        Assert.assertEquals("a-zA-Z_0-9", outputMap.get(ARTIFACT_NAME_UPPER));
        Assert.assertEquals("/opt/spool,"+System.getProperty("user.home")+"/asdc", outputMap.get(ARTIFACT_LOC));
        Assert.assertEquals("deleted,Deleted", outputMap.get(ARTIFACT_STATUS));
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
