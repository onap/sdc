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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.impl.CliConfigurationImpl;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Created by sheetalm on 10/19/2016. Scenario 19 Pre-requisite - set -Dnode.config.location=${"user.home"}/TestResources/ while running test Verify
 * node specific override using CLI
 */
class NodeSpecificCliTest {

    private static final String NAMESPACE = "NodeCLI";
    private static final File FILE = new File(TestUtil.jsonSchemaLoc + "config.properties");

    @AfterAll
    public static void tearDown() throws Exception {
        TestUtil.cleanUp();
        if (FILE.exists()) {
            assertTrue(FILE.delete());
        }
    }

    @Test
    void testCliApi() throws Exception {
        //Verify without fallback
        final Map<String, Object> input = new HashMap<>();
        input.put("ImplClass", "org.onap.config.type.ConfigurationQuery");
        input.put("namespace", NAMESPACE);
        input.put("key", ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH);

        final ConfigurationManager conf = new CliConfigurationImpl();
        final String maxLength = conf.getConfigurationValue(input);

        //Verify Property from Namespace configurations
        assertEquals("30", maxLength);

        //Add node specific configurations
        final Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH, "50");
        props.setProperty("_config.namespace", NAMESPACE);

        try (final OutputStream out = new FileOutputStream(FILE)) {
            props.store(out, "Node Config Property");
        }

        //Verify property from node specific configuration
        input.put("nodeSpecific", true);
        final String nodeVal = conf.getConfigurationValue(input);
        assertEquals("30", nodeVal);

        if (FILE.exists()) {
            assertTrue(FILE.delete());
        }
    }
}
