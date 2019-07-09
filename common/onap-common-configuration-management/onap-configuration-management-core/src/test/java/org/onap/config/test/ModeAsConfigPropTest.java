/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

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
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Scenario 8 Validate configuration with mode specified as a configuration property.
 */
public class ModeAsConfigPropTest {

    private static final String NAMESPACE = "ModeAsConfigProp";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testMergeStrategyInConfig() {
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals("14", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        Assert.assertEquals("1048", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_MAXSIZE));

        List<String> expectedExtList = new ArrayList<>();
        expectedExtList.add("pdf");
        expectedExtList.add("zip");
        expectedExtList.add("xml");
        expectedExtList.add("pdf");
        expectedExtList.add("tgz");
        expectedExtList.add("xls");
        List<String> extList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_EXT);
        Assert.assertEquals(expectedExtList, extList);

        List<String> expectedEncList = new ArrayList<>();
        expectedEncList.add("Base64");
        expectedEncList.add("MD5");
        List<String> encList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_ENC);
        Assert.assertEquals(expectedEncList, encList);

        Assert.assertEquals("{name:\"SCM\"}", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_JSON_SCHEMA));

        Assert.assertEquals("a-zA-Z_0-9", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER));

        Assert.assertEquals("Deleted", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_STATUS));

        List<String> expectedLocList = new ArrayList<>();
        expectedLocList.add("/opt/spool");
        expectedLocList.add(System.getProperty("user.home") + "/asdc");
        List<String> locList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_LOC);
        Assert.assertEquals(expectedLocList, locList);

        Assert.assertEquals("@" + TestUtil.getenv(ConfigTestConstant.PATH) + "/myschema.json",
                config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_XML_SCHEMA));

        List<String> artifactConsumer = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER);
        Assert.assertEquals(config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER_APPC),
                artifactConsumer);

        Assert.assertEquals(config.getAsBooleanValue(NAMESPACE, ConfigTestConstant.ARTIFACT_MANDATORY_NAME), true);

        Assert.assertEquals(config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MINLENGTH), "6");

        Assert.assertEquals(config.getAsBooleanValue(NAMESPACE, ConfigTestConstant.ARTIFACT_ENCODED), true);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }

}
