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
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.impl.CliConfigurationImpl;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Created by sheetalm on 10/18/2016.
 * Scenario 17
 * Verify Configuration Management System - Command Line Interface for query, update and list operations
 */
public class CliTest {

    private static final String NAMESPACE = "CLI";
    private static final String TENANT = "OPENECOMP";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testCliApi() throws Exception {
        //Verify without fallback
        Map<String, Object> input = new HashMap<>();
        input.put("ImplClass", "org.onap.config.type.ConfigurationQuery");
        input.put("tenant", TENANT);
        input.put("namespace", NAMESPACE);
        input.put("key", ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH);

        ConfigurationManager conf = new CliConfigurationImpl();
        String maxLength = conf.getConfigurationValue(input);
        Assert.assertEquals("14", maxLength);

        Map<String, String> outputMap = conf.listConfiguration(input);
        validateCliListConfig(outputMap);
    }

    private void validateCliListConfig(Map<String, String> outputMap) {

        Assert.assertEquals("@" + System.getProperty("user.home") + "/TestResources/GeneratorsList.json",
                outputMap.get(ConfigTestConstant.ARTIFACT_JSON_SCHEMA));
        Assert.assertEquals("appc,catalog", outputMap.get(ConfigTestConstant.ARTIFACT_CONSUMER));
        Assert.assertEquals("6", outputMap.get(ConfigTestConstant.ARTIFACT_NAME_MINLENGTH));
        Assert.assertEquals("true", outputMap.get(ConfigTestConstant.ARTIFACT_ENCODED));
        Assert.assertEquals("14", outputMap.get(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));
        Assert.assertEquals("pdf,zip,xml,pdf,tgz,xls", outputMap.get(ConfigTestConstant.ARTIFACT_EXT));
        Assert.assertEquals("Base64,MD5", outputMap.get(ConfigTestConstant.ARTIFACT_ENC));
        Assert.assertEquals("@" + TestUtil.getenv(ConfigTestConstant.PATH) + "/myschema.json",
                outputMap.get(ConfigTestConstant.ARTIFACT_XML_SCHEMA));
        Assert.assertEquals("a-zA-Z_0-9", outputMap.get(ConfigTestConstant.ARTIFACT_NAME_UPPER));
        Assert.assertEquals("/opt/spool," + System.getProperty("user.home") + "/asdc",
                outputMap.get(ConfigTestConstant.ARTIFACT_LOC));
        Assert.assertEquals("deleted,Deleted", outputMap.get(ConfigTestConstant.ARTIFACT_STATUS));
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
