/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.onap.config.test.CLIFallbackAndLookupTest;
import org.onap.config.test.CliTest;
import org.onap.config.test.ConfigSourceLocationTest;
import org.onap.config.test.FallbackConfigTest;
import org.onap.config.test.FallbackToGlobalNSTest;
import org.onap.config.test.GlobalAndNSConfigTest;
import org.onap.config.test.JAVAPropertiesConfigTest;
import org.onap.config.test.JSONConfigTest;
import org.onap.config.test.LoadOrderMergeAndOverrideTest;
import org.onap.config.test.ModeAsConfigPropTest;
import org.onap.config.test.MultiTenancyConfigTest;
import org.onap.config.test.NodeSpecificCliTest;
import org.onap.config.test.ValidateDefaultModeTest;
import org.onap.config.test.XMLConfigTest;
import org.onap.config.test.YAMLConfigTest;

/**
 * Created by sheetalm on 10/25/2016.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConfigurationUtilsTest.class,
        JAVAPropertiesConfigTest.class,
        JSONConfigTest.class,
        XMLConfigTest.class,
        YAMLConfigTest.class,
        CLIFallbackAndLookupTest.class,
        CliTest.class,
        ConfigSourceLocationTest.class,
        FallbackConfigTest.class,
        FallbackToGlobalNSTest.class,
        GlobalAndNSConfigTest.class,
        ModeAsConfigPropTest.class,
        MultiTenancyConfigTest.class,
        NodeSpecificCliTest.class,
        ValidateDefaultModeTest.class,
        LoadOrderMergeAndOverrideTest.class})
public class TestCMSuite extends junit.framework.TestSuite {

    private TestCMSuite() {
        // prevent instantiation
    }
}
