package org.openecomp.config.test;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Scenario 15
 * Update and Verify Change Notifications for any change in the registered key for node specific configuration
 * Pre-requisite - set -Dnode.config.location=${"user.home"}/TestResources/ while running test
 * Created by sheetalm on 10/17/2016.
 */
public class NotificationForNodeConfigTest {
    public final static String NAMESPACE = "NotificationForNodeConfig";

    private String updatedValue = null;

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testNotificationForNode() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();

        System.out.println(config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "30");
        props.setProperty("_config.namespace",NAMESPACE);
        File f = new File(TestUtil.jsonSchemaLoc + "config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Node Config Property");
        }

        Thread.sleep(35000);

        //Verify property from node specific configuration
        Assert.assertEquals("30", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        config.addConfigurationChangeListener(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, new NodePropValListener());

        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "80");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Updated Node Config Property");
        }

        Thread.sleep(35000);

        //Verify change listenere is invoked when node specific configuration is changed.
        Assert.assertEquals("80", updatedValue);

    }

    private class NodePropValListener implements ConfigurationChangeListener {
        @Override
        public void notify(String key, Object oldValue, Object newValue) {
            System.out.println("received notification::oldValue=="+oldValue+" newValue=="+newValue);
            updatedValue = newValue.toString();
        }
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
        File f = new File(TestUtil.jsonSchemaLoc+"config.properties");
        if(f.exists()) {
            f.delete();
        }
    }
}
