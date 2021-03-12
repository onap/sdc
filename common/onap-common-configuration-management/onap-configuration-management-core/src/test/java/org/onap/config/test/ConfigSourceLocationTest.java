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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Created by sheetalm on 10/14/2016.
 * Scenario 11
 * Validate conventional and configurational source location.
 * Pre-requisite - set -Dconfig.location=${"user.home"}/TestResources/ while running test
 */
class ConfigSourceLocationTest {

    private static final String NAMESPACE = "SourceLocation";

    @BeforeEach
    public void setUp() throws Exception {
        TestUtil.cleanUp();

        Properties props = new Properties();
        props.setProperty("maxCachedBufferSize", "1024");
        props.setProperty("artifact.maxsize", "1024");
        File f = new File(TestUtil.jsonSchemaLoc + "config.properties");
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Config Property at Conventional Resource");
        }
    }

    @Test
    void testMergeStrategyInConfig() {
        Configuration config = ConfigurationManager.lookup();
        assertEquals("a-zA-Z_0-9", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER));
        assertEquals("1024", config.getAsString(ConfigTestConstant.ARTIFACT_MAXSIZE));
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
