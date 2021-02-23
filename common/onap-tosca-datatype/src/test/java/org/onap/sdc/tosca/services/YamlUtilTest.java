/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.onap.sdc.tosca.services;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class YamlUtilTest {

    private YamlUtil yamlUtil = new YamlUtil();

    @Test
    public void testYamlToMap() {
        InputStream is = yamlUtil.loadYamlFileIs("/yamlMap.yaml");
        Map<String, LinkedHashMap<String, Object>> res = yamlUtil.yamlToMap(is);
        Assert.assertNotNull(res);
        Assert.assertEquals(res.size(), 3);
        Assert.assertEquals(res.get("parameter1"), "value1");
        Assert.assertEquals(res.get("parameter2"), "value2");
        Assert.assertEquals(res.get("parameter3"), "value3");
    }

    @Test
    public void testYamlToList() {
        InputStream is = yamlUtil.loadYamlFileIs("/yamlList.yaml");
        Optional<List<Object>> res = yamlUtil.yamlToList(is);
        Assert.assertEquals(res.get().size(), 3);
        Assert.assertEquals(res.get().get(0), "value1");

        InputStream is2 = yamlUtil.loadYamlFileIs("/yamlListError.yaml");
        Optional<List<Object>> res2 = yamlUtil.yamlToList(is2);
        Assert.assertTrue(res2.isEmpty());
    }

    @Test
    public void testIsYamlFileContentValid() {
        String yamlString = "tosca_definitions_version: tosca_simple_yaml_1_1\n" +
                "imports: []\n" +
                "node_types:\n" +
                "  tosca.nodes.Root:\n" +
                "    description: The TOSCA Node Type all other TOSCA base Node Types derive from";
        boolean res = yamlUtil.isYamlFileContentValid(yamlString);
        Assert.assertTrue(res);

        String yamlString2 = "{tosca_definitions_version: tosca_simple_yaml_1_1\n" +
                "node_types:\n" +
                "  tosca.nodes.Root:\n" +
                "    description: The TOSCA Node Type all other TOSCA base Node Types derive from}";
        boolean res2 = yamlUtil.isYamlFileContentValid(yamlString2);
        Assert.assertFalse(res2);
    }
}