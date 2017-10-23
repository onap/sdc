package org.openecomp.config.test;

import org.openecomp.config.Constants;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sheetalm on 10/19/2016.
 * Scenario 19
 * Pre-requisite - set -Dnode.config.location=${"user.home"}/TestResources/ while running test
 * Verify node specific override using CLI
 */
public class NodeSpecificCLITest {

    public final static String NAMESPACE = "NodeCLI";
    private String updatedValue = "";

    @Test
    public void testCLIApi() throws Exception{
        //Verify without fallback
        Map<String, Object> input = new HashMap<>();
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        input.put("namespace", NAMESPACE);
        input.put("key", ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH);

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName(Constants.MBEAN_NAME);
        ConfigurationManager conf = JMX.newMBeanProxy(mbsc, mbeanName, org.openecomp.config.api.ConfigurationManager.class, true);
        String maxLength = conf.getConfigurationValue(input);

        //Verify Property from Namespace configurations
        Assert.assertEquals("30",maxLength);

        //Add node specific configurations
        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "50");
        props.setProperty("_config.namespace",NAMESPACE);
        File f = new File(TestUtil.jsonSchemaLoc + "config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Node Config Property");
        }

        Thread.sleep(35000);

        //Verify property from node specific configuration
        input.put("nodeSpecific", true);
        String nodeVal = conf.getConfigurationValue(input);
        Assert.assertEquals("50", nodeVal);

        //Add Change Listener
        conf.addConfigurationChangeListener(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, new CLINodeListener());

        //Update maxlength
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationUpdate");
        input.put("nodeOverride", true);
        input.put("nodeSpecific", false);
        input.put("value", "60");
        conf.updateConfigurationValue(input);

        Thread.sleep(35000);

        Assert.assertEquals("60",updatedValue);

        //Fetch the updated nodespecific value
        input.put("nodeOverride", false);
        input.put("nodeSpecific", true);
        input.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        String updatedMaxLength = conf.getConfigurationValue(input);
        Assert.assertEquals("60",updatedMaxLength);

        //Verify maxlength on other nodes by deleting node specific configuration
        if(f.exists()) {
            f.delete();
        }

        Thread.sleep(35000);

        input.put("ImplClass", "org.openecomp.config.type.ConfigurationQuery");
        input.put("nodeOverride", false);
        input.put("nodeSpecific", false);
        System.out.println("val on other node is::"+conf.getConfigurationValue(input));
        Assert.assertEquals("30",conf.getConfigurationValue(input));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TestUtil.cleanUp();
        File f = new File(TestUtil.jsonSchemaLoc+"config.properties");
        if(f.exists()) {
            f.delete();
        }
    }

    private class CLINodeListener implements ConfigurationChangeListener {
        @Override
        public void notify(String key, Object oldValue, Object newValue) {
            System.out.println("received notification::oldValue=="+oldValue+" newValue=="+newValue);
            updatedValue = newValue.toString();
        }
    }
}
