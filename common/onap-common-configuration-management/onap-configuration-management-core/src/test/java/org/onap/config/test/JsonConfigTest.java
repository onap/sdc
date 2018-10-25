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

import static org.onap.config.util.TestUtil.validateConfiguration;
import static org.onap.config.util.TestUtil.writeFile;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.util.TestUtil;

/**
 * Created by ARR on 10/14/2016.
 * Scenario 3 Validate configuration with JSON file format with mode.
 */
public class JsonConfigTest {

    private static final String NAMESPACE = "JSONConfig";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        writeFile(data);
    }

    @Test
    public void testConfigurationWithJsonFileFormat() {
        validateConfiguration(NAMESPACE);
    }


    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
