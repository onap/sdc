package org.onap.config;

import org.onap.config.test.CLIFallbackAndLookupTest;
import org.onap.config.test.CLITest;
import org.onap.config.test.ConfigSourceLocationTest;
import org.onap.config.test.DynamicConfigurationTest;
import org.onap.config.test.FallbackConfigTest;
import org.onap.config.test.FallbackToGlobalNSTest;
import org.onap.config.test.GlobalAndNSConfigTest;
import org.onap.config.test.JAVAPropertiesConfigTest;
import org.onap.config.test.JSONConfigTest;
import org.onap.config.test.LoadOrderMergeAndOverrideTest;
import org.onap.config.test.ModeAsConfigPropTest;
import org.onap.config.test.MultiTenancyConfigTest;
import org.onap.config.test.NodeSpecificCLITest;
import org.onap.config.test.NotificationForNodeConfigTest;
import org.onap.config.test.NotificationOnPropValTest;
import org.onap.config.test.ResourceChangeNotificationTest;
import org.onap.config.test.UnregisterNotificationTest;
import org.onap.config.test.ValidateDefaultModeTest;
import org.onap.config.test.ValidateNodeConfigTest;
import org.onap.config.test.XMLConfigTest;
import org.onap.config.test.YAMLConfigTest;
import org.onap.config.test.*;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by sheetalm on 10/25/2016.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        JAVAPropertiesConfigTest.class,
        JSONConfigTest.class,
        XMLConfigTest.class,
        YAMLConfigTest.class,
        CLIFallbackAndLookupTest.class,
        CLITest.class,
        ConfigSourceLocationTest.class,
        DynamicConfigurationTest.class,
        FallbackConfigTest.class,
        FallbackToGlobalNSTest.class,
        GlobalAndNSConfigTest.class,
        ModeAsConfigPropTest.class,
        MultiTenancyConfigTest.class,
        NodeSpecificCLITest.class,
        NotificationForNodeConfigTest.class,
        NotificationOnPropValTest.class,
        ResourceChangeNotificationTest.class,
        UnregisterNotificationTest.class,
        ValidateDefaultModeTest.class,
        ValidateNodeConfigTest.class,
        LoadOrderMergeAndOverrideTest.class


})

public class TestCMSuite extends junit.framework.TestSuite {

    private TestCMSuite() {

    }

    @AfterClass
    public static void tearDown(){
        try {
            ConfigurationUtils.executeDdlSql("truncate dox.configuration_change");
            ConfigurationUtils.executeDdlSql("truncate dox.configuration");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
