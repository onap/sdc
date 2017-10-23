package org.openecomp.config.test;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Pre-requisite - set -Dconfig.location=${"user.home"}/TestResources/ while running test
 * Scenario 14 - Verify Change Notifications for any change in the registered key
 * Created by sheetalm on 10/14/2016.
 */
public class NotificationOnPropValTest {

    public final static String NAMESPACE = "NotificationOnPropVal";

    private String updatedValue = null;

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testNotification() throws IOException, InterruptedException {
        Configuration config = ConfigurationManager.lookup();

        System.out.println(config.getAsString(NAMESPACE,ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        config.addConfigurationChangeListener(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, new PropValListener());

        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "20");
        props.setProperty("_config.namespace",NAMESPACE);
        props.setProperty("_config.mergeStrategy","override");
        File f = new File(TestUtil.jsonSchemaLoc + "config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Override Config Property at Conventional Resource");
        }

        Thread.sleep(35000);

        System.out.println(config.getAsString(NAMESPACE,ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        Assert.assertEquals("20" , updatedValue);
    }

     private class PropValListener implements ConfigurationChangeListener {
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
