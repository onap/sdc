/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
package org.openecomp.sdc.be.resources.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class ModelDataTest {

    private final String modelName = "ETSI-SDC-MODEL-TEST";
    private final String modelId = UUID.randomUUID().toString();
    ModelData modelData;

    @BeforeAll
    void initTestData() {
        modelData = new ModelData(modelName, modelId);
    }

    @Test
    void modelDataTest() {
        assertThat(modelData).isNotNull();
        assertThat(modelData.getUniqueId()).isEqualTo(modelId);
        assertThat(modelData.getName()).isEqualTo(modelName);
    }

    @Test
    void modelDataFromPropertiesMapTest() {
        final Map<String, Object> properties = new HashMap();
        properties.put("name", modelData.getName());
        properties.put("uid", modelData.getUniqueId());
        final ModelData modelDataFromPropertiesMap = new ModelData(properties);
        assertThat(modelDataFromPropertiesMap).isNotNull();
        assertThat(modelDataFromPropertiesMap.getUniqueId()).isEqualTo(modelId);
        assertThat(modelDataFromPropertiesMap.getName()).isEqualTo(modelName);
    }

    @Test
    void modelDataToGraphTest() {
        final var result = modelData.toGraphMap();
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.values()).contains(modelId);
        assertThat(result.values()).contains(modelName);
    }

}
