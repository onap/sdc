package org.onap.config;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.onap.config.test.*;

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
