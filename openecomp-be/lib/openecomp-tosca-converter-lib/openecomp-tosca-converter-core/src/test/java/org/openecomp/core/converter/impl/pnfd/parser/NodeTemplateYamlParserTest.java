/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.core.converter.impl.pnfd.parser;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.core.converter.impl.pnfd.parser.NodeTemplateYamlParser;
import org.openecomp.core.util.YamlTestUtil;

public class NodeTemplateYamlParserTest {

    @Test
    public void shouldParseNodeTemplate() {
        final Map<String, Object> nodeTemplateYaml = (Map<String, Object>) YamlTestUtil.readOrFail("transformation/nodeTemplate/nodeTemplate.yaml");
        final NodeTemplate nodeTemplate = NodeTemplateYamlParser.parse(nodeTemplateYaml);
        assertThat("Should have the same type", nodeTemplate.getType(), equalTo(nodeTemplateYaml.get("type")));
        assertThat("Should have a not null properties map", nodeTemplate.getProperties(), notNullValue());
        assertThat("Should have a not empty properties map", nodeTemplate.getProperties(), is(not(aMapWithSize(0))));
        assertThat("Should have the same properties", nodeTemplate.getProperties(), equalTo(nodeTemplateYaml.get("properties")));
    }
}