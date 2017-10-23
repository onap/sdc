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
 * Pre-requisite - set -Dconfig.location=${"user.home"}/TestResources/ while running test
 * Created by sheetalm on 10/19/2016.
 * Scenario 24
 * Unregister notification and verify listener
 */
public class UnregisterNotificationTest {
    public final static String NAMESPACE = "UnregisterNotification";

    private String updatedValue = null;

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testNotification() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();

        System.out.println(config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        PropertyListener propListener = new PropertyListener();
        config.addConfigurationChangeListener(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH,propListener);

        updateValue("20");

        Thread.sleep(35000);

        System.out.println(config.getAsString(NAMESPACE,ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        //Verify listener is invoked and updated value to 20
        Assert.assertEquals("20" , updatedValue);

        config.removeConfigurationChangeListener(NAMESPACE,ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH,propListener);

        updateValue("22");

        Thread.sleep(35000);

        //When listener is unregistered updating value does not invoke any listener and  value from listener should remain unchanged
        Assert.assertEquals("20" , updatedValue);

        //Verify value is updated even if listener is unregistered
        Assert.assertEquals("22" , config.getAsString(NAMESPACE,ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));
    }

    private void updateValue(String newValue) throws IOException {
        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, newValue);
        props.setProperty("_config.namespace",NAMESPACE);
        props.setProperty("_config.mergeStrategy","override");
        File f = new File(TestUtil.jsonSchemaLoc+"config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Override Config Property at Conventional Resource");
        }
    }

    private class PropertyListener implements ConfigurationChangeListener {
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
