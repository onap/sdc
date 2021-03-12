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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Created by ARR on 10/17/2016.
 * Scenario 22 - Validate the default mode if the mode is not set
 */
class ValidateDefaultModeTest {

    private static final String NAMESPACE = "defaultmode";

    @BeforeEach
    public void setUp() throws Exception {
        TestUtil.cleanUp();
    }

    @Test
    void testConfigurationWithValidateDefaultMode() {
        Configuration config = ConfigurationManager.lookup();

        assertEquals("14", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        assertEquals("1048", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_MAXSIZE));

        List<String> expectedExtList = new ArrayList<>();
        expectedExtList.add("pdf");
        expectedExtList.add("tgz");
        expectedExtList.add("xls");
        List<String> extList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_EXT);
        assertEquals(expectedExtList, extList);

        assertEquals("6", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MINLENGTH));

    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
