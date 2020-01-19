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

package org.openecomp.sdc.be.components.property;

import org.junit.Before;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openecomp.sdc.common.api.Constants.GET_INPUT;

public class PropertyDeclaratorTestBase {

    static final String INNER_PROP1 = "ecomp_generated_naming";
    static final String INNER_PROP2 = "naming_policy";
    static final String RESOURCE_ID = "resourceId";
    static final String INPUT_ID = "inputId";
    static final String INSTANCE_ID = "inst1";
    static final String ORIGIN_INSTANCE_ID = "originInst1";
    PropertyDataDefinition prop1, prop2, complexProperty;
    Resource resource;

    @Before
    public void setUp() throws Exception {
        prop1 = new PropertyDataDefinitionBuilder()
                .setUniqueId("prop1")
                .setType("string")
                .setName("prop1")
                .setValue("value1")
                .build();

        prop2 = new PropertyDataDefinitionBuilder()
                .setUniqueId("prop2")
                .setType("string")
                .setSchemaType("string")
                .setName("prop2")
                .setValue("[\"a\", \"b\"]")
                .build();

        complexProperty = new PropertyDataDefinitionBuilder()
                .setUniqueId("prop3")
                .setType("org.openecomp.type1")
                .setName("prop3")
                .setValue("{\"ecomp_generated_naming\":true\",\"naming_policy\":\"abc\"}")
                .build();

        ComponentInstance inst1 = new ComponentInstanceBuilder()
                .setComponentUid(ORIGIN_INSTANCE_ID)
                .setId(INSTANCE_ID)
                .setNormalizedName(INSTANCE_ID)
                .build();

        resource = new ResourceBuilder()
                .setUniqueId(RESOURCE_ID)
                .addComponentInstance(inst1)
                .build();

    }

    List<ComponentInstancePropInput> createInstancePropInputList(List<PropertyDataDefinition> properties) {
        return properties.stream().map(prop -> new ComponentInstancePropInput(new ComponentInstanceProperty(prop)))
                .collect(Collectors.toList());
    }

    void verifyInputPropertiesList(List<InputDefinition> createdInputs, List<PropertyDataDefinition> capturedUpdatedProperties) {
        Map<String, InputDefinition> propertyIdToCreatedInput = MapUtil.toMap(createdInputs, InputDefinition::getPropertyId);
        capturedUpdatedProperties.forEach(updatedProperty -> verifyInputPropertiesList(updatedProperty, propertyIdToCreatedInput.get(updatedProperty.getUniqueId())));
    }

    String generateGetInputValue(String value) {
        return String.format("{\"%s\":\"%s\"}", GET_INPUT, value);
    }

    String generateGetInputValueAsListInput(String inputName, String inputProperty) {
        return String.format("{\"%s\":[\"%s\",\"INDEX\",\"%s\"]}", GET_INPUT, inputName, inputProperty);
    }

    private void verifyInputPropertiesList(PropertyDataDefinition updatedProperty, InputDefinition input) {
        assertThat(input.getProperties()).hasSize(1);
        assertThat(new ComponentInstanceProperty(updatedProperty)).isEqualTo(input.getProperties().get(0));
    }

}
