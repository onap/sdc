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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Scenario 8 Validate configuration with mode specified as a configuration property.
 */
class ModeAsConfigPropTest {

    private static final String NAMESPACE = "ModeAsConfigProp";

    @BeforeEach
    public void setUp() throws Exception {
        TestUtil.cleanUp();
    }

    @Test
    void testMergeStrategyInConfig() {
        Configuration config = ConfigurationManager.lookup();

        assertEquals("14", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        assertEquals("1048", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_MAXSIZE));

        List<String> expectedExtList = new ArrayList<>();
        expectedExtList.add("pdf");
        expectedExtList.add("zip");
        expectedExtList.add("xml");
        expectedExtList.add("pdf");
        expectedExtList.add("tgz");
        expectedExtList.add("xls");
        List<String> extList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_EXT);
        assertEquals(expectedExtList, extList);

        List<String> expectedEncList = new ArrayList<>();
        expectedEncList.add("Base64");
        expectedEncList.add("MD5");
        List<String> encList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_ENC);
        assertEquals(expectedEncList, encList);

        assertEquals("{name:\"SCM\"}", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_JSON_SCHEMA));

        assertEquals("a-zA-Z_0-9", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER));

        assertEquals("Deleted", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_STATUS));

        List<String> expectedLocList = new ArrayList<>();
        expectedLocList.add("/opt/spool");
        expectedLocList.add(System.getProperty("user.home") + "/asdc");
        List<String> locList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_LOC);
        assertEquals(expectedLocList, locList);

        assertEquals("@" + TestUtil.getenv(ConfigTestConstant.PATH) + "/myschema.json",
            config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_XML_SCHEMA));

        List<String> artifactConsumer = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER);
        assertEquals(config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER_APPC),
            artifactConsumer);

        assertTrue(config.getAsBooleanValue(NAMESPACE, ConfigTestConstant.ARTIFACT_MANDATORY_NAME));

        assertEquals("6", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MINLENGTH));

        assertTrue(config.getAsBooleanValue(NAMESPACE, ConfigTestConstant.ARTIFACT_ENCODED));
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }

}
