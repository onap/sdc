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
import org.onap.config.test.CliFallbackAndLookupTest;
import org.onap.config.test.CliTest;
import org.onap.config.test.ConfigSourceLocationTest;
import org.onap.config.test.FallbackConfigTest;
import org.onap.config.test.FallbackToGlobalNamespaceTest;
import org.onap.config.test.GlobalAndNamespaceConfigTest;
import org.onap.config.test.JavaPropertiesConfigTest;
import org.onap.config.test.JsonConfigTest;
import org.onap.config.test.LoadOrderMergeAndOverrideTest;
import org.onap.config.test.ModeAsConfigPropTest;
import org.onap.config.test.MultiTenancyConfigTest;
import org.onap.config.test.NodeSpecificCliTest;
import org.onap.config.test.ValidateDefaultModeTest;
import org.onap.config.test.XmlConfigTest;
import org.onap.config.test.YamlConfigTest;

/**
 * Created by sheetalm on 10/25/2016.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {ConfigurationUtilsTest.class, JavaPropertiesConfigTest.class, JsonConfigTest.class, XmlConfigTest.class,
                YamlConfigTest.class, CliFallbackAndLookupTest.class, CliTest.class, ConfigSourceLocationTest.class,
                FallbackConfigTest.class, FallbackToGlobalNamespaceTest.class, GlobalAndNamespaceConfigTest.class,
                ModeAsConfigPropTest.class, MultiTenancyConfigTest.class, NodeSpecificCliTest.class,
                ValidateDefaultModeTest.class, LoadOrderMergeAndOverrideTest.class})
public class TestCMSuite extends junit.framework.TestSuite {

    private TestCMSuite() {
        // prevent instantiation
    }
}
