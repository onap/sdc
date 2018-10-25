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
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.Constants;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;


/**
 * Created by sheetalm on 10/18/2016.
 * Scenario 21, Scenario 23
 * 21 - Verify the CLI fetches only the current value unless the fallback option is specified
 * 23 - Fetch value using CLI for a key with underlying resource
 */
public class CliFallbackAndLookupTest {

    private static final String NAMESPACE = "CLIFallback";
    private static final String TENANT = "OPENECOMP";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testCliFallbackAndLookup() throws Exception {

        //Verify without fallback
        Map<String, Object> input = new HashMap<>();
        input.put("ImplClass", "org.onap.config.type.ConfigurationQuery");
        input.put("tenant", TENANT);
        input.put("namespace", NAMESPACE);
        input.put("key", ConfigTestConstant.ARTIFACT_MAXSIZE);

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName(Constants.MBEAN_NAME);
        ConfigurationManager conf = JMX.newMBeanProxy(mbsc, mbeanName, ConfigurationManager.class, true);
        String maxSizeWithNoFallback = conf.getConfigurationValue(input);
        Assert.assertEquals("", maxSizeWithNoFallback);

        //Verify underlying resource without lookup switch
        input.put("key", ConfigTestConstant.ARTIFACT_JSON_SCHEMA);
        String jsonSchema = conf.getConfigurationValue(input);
        System.out.println("jsonSchema==" + jsonSchema);
        Assert.assertEquals("@" + System.getProperty("user.home") + "/TestResources/GeneratorsList.json", jsonSchema);

        //Verify underlying resource with lookup switch
        input.put("externalLookup", true);
        jsonSchema = conf.getConfigurationValue(input);
        System.out.println("jsonSchema==" + jsonSchema);
        Assert.assertEquals("{name:\"SCM\"}", jsonSchema);

        //Verify with fallback
        Map<String, Object> fallbackInput = new HashMap<>();
        fallbackInput.put("ImplClass", "org.onap.config.type.ConfigurationQuery");
        fallbackInput.put("fallback", true);
        fallbackInput.put("tenant", TENANT);
        fallbackInput.put("namespace", NAMESPACE);
        fallbackInput.put("key", ConfigTestConstant.ARTIFACT_MAXSIZE);

        String maxSizeWithFallback = conf.getConfigurationValue(fallbackInput);
        Assert.assertEquals("1024", maxSizeWithFallback);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
