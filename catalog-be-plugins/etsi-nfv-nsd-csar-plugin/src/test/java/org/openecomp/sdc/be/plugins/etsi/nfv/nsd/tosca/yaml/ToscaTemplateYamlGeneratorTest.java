/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.tosca.yaml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintValidValues;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;

class ToscaTemplateYamlGeneratorTest {

    @Test
    void testGenerateYamlWithImportsKey() {
        //given
        final ToscaTemplate toscaTemplate = new ToscaTemplate("tosca_simple_yaml_1_1");
        final List<Map<String, Map<String, String>>> importList =
            ImmutableList.of(
                ImmutableMap.of("etsi_nfv_sol001_nsd_2_7_1_types",
                    ImmutableMap.of("file", "etsi_nfv_sol001_nsd_2_7_1_types.yaml")
                ),
                ImmutableMap.of("anotherImport",
                    ImmutableMap.of("file", "anotherImport.yaml")
                )
            );
        toscaTemplate.setImports(importList);
        final ToscaTemplateYamlGenerator toscaTemplateYamlGenerator = new ToscaTemplateYamlGenerator(toscaTemplate);
        //when
        final String toscaTemplateYamlString = toscaTemplateYamlGenerator.parseToYamlString();

        //then
        final String expectedImports = "imports:\n"
            + "- file: etsi_nfv_sol001_nsd_2_7_1_types.yaml\n"
            + "- file: anotherImport.yaml";
        assertThat("Imports format should be as expected", toscaTemplateYamlString.contains(expectedImports), is(true));
    }

    @Test
    void testGenerateYamlWithToscaProperty() {
        //given
        final ToscaTemplate toscaTemplate = new ToscaTemplate("tosca_simple_yaml_1_1");

        final Map<String, ToscaProperty> toscaPropertyMap = new HashMap<>();
        final ToscaProperty toscaProperty = new ToscaProperty();
        final String defaultpValue = "defaultpValue";
        toscaProperty.setDefaultp(defaultpValue);
        ToscaPropertyConstraintValidValues toscaPropertyConstraintValidValues =
            new ToscaPropertyConstraintValidValues(Collections.singletonList(defaultpValue));
        toscaProperty.setConstraints(Collections.singletonList(toscaPropertyConstraintValidValues));
        final String propertyName = "aProperty";
        toscaPropertyMap.put(propertyName, toscaProperty);

        final Map<String, ToscaNodeType> toscaNodeMap = new HashMap<>();
        final ToscaNodeType toscaNodeType = new ToscaNodeType();
        toscaNodeType.setProperties(toscaPropertyMap);
        toscaNodeMap.put("aNode", toscaNodeType);
        toscaTemplate.setNode_types(toscaNodeMap);
        final ToscaTemplateYamlGenerator toscaTemplateYamlGenerator = new ToscaTemplateYamlGenerator(toscaTemplate);
        //when
        final String toscaTemplateYamlString = toscaTemplateYamlGenerator.parseToYamlString();

        final String expectedProperty = String.format("%s:\n"
                + "        default: %s\n"
                + "        constraints:\n"
                + "        - valid_values:\n"
                + "          - %s",
            propertyName, defaultpValue, defaultpValue);
        //then
        assertThat("Property format should be as expected",
            toscaTemplateYamlString.contains(expectedProperty), is(true));
    }
}