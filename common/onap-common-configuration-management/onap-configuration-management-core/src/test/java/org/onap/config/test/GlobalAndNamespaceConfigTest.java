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

package org.onap.config.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Created by sheetalm on 10/13/2016.
 * Scenario 10 Verify configuration present in both global and defined namespace
 */
public class GlobalAndNamespaceConfigTest {

    private static final String NAMESPACE = "GlobalAndNSConfig";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testNamespaceInConfig() {
        Configuration config = ConfigurationManager.lookup();
        Assert.assertEquals("a-zA-Z", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER));
        Assert.assertEquals("a-zA-Z_0-9", config.getAsString(ConfigTestConstant.ARTIFACT_NAME_UPPER));
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }


}
