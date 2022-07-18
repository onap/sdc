/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class CustomYamlFunctionTest {

    @Test
    void getTypeTest() {
        final var yamlFunction = new CustomYamlFunction();
        assertEquals(ToscaFunctionType.YAML, yamlFunction.getType());
    }

    @Test
    void getValue() {
        final var yamlFunction = new CustomYamlFunction();
        assertNull(yamlFunction.getValue());
        final String yamlValue1 = "my value";
        yamlFunction.setYamlValue(yamlValue1);
        assertEquals(yamlValue1, yamlFunction.getValue());
        final Map<String, Map<String, String>> yamlValue2 = Map.of("entry", Map.of("property1", "value1"));
        yamlFunction.setYamlValue(yamlValue2);
        assertEquals(new Yaml().dump(yamlValue2), yamlFunction.getValue());
    }
}