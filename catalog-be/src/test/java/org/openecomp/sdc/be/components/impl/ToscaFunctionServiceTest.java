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

package org.openecomp.sdc.be.components.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;

class ToscaFunctionServiceTest {

    private final ToscaFunctionService toscaFunctionService = new ToscaFunctionService();


    @Test
    void updateFunctionWithDataFromSelfComponentTest() {
        //given a component with one property, one attribute, one instance. The instance have one property and one attribute.
        final Component component = new Service();
        component.setUniqueId("componentId");
        component.setName("componentName");
        final var componentInput1 = new InputDefinition();
        componentInput1.setUniqueId("input1Id");
        componentInput1.setName("input1Name");
        component.setInputs(List.of(componentInput1));

        final var componentAttribute1 = new AttributeDefinition();
        componentAttribute1.setUniqueId("componentAttribute1Id");
        componentAttribute1.setName("componentAttribute1Name");
        component.setAttributes(List.of(componentAttribute1));

        final var componentProperty1 = new PropertyDefinition();
        componentProperty1.setUniqueId("componentProperty1Id");
        componentProperty1.setName("componentProperty1Name");
        component.setProperties(List.of(componentProperty1));

        final var componentInstance1 = new ComponentInstance();
        componentInstance1.setName("componentInstance1Name");
        componentInstance1.setUniqueId("componentInstance1Id");
        component.setComponentInstances(List.of(componentInstance1));

        final Map<String, List<ComponentInstanceProperty>> instancePropertyMap = new HashMap<>();
        final var componentInstanceProperty = new ComponentInstanceProperty();
        final String instancePropertyId1 = "instancePropertyId1";
        componentInstanceProperty.setUniqueId(instancePropertyId1);
        final String instancePropertyName1 = "instancePropertyName1";
        componentInstanceProperty.setName(instancePropertyName1);
        instancePropertyMap.put(componentInstance1.getUniqueId(), List.of(componentInstanceProperty));

        final Map<String, List<AttributeDefinition>> instanceAttributeMap = new HashMap<>();
        final AttributeDefinition instanceAttribute1 = new ComponentInstanceAttribute();
        instanceAttribute1.setUniqueId("instanceAttribute1Id");
        instanceAttribute1.setName("instanceAttribute1Name");
        instanceAttributeMap.put(componentInstance1.getUniqueId(), List.of(instanceAttribute1));

        final ToscaConcatFunction toscaConcatFunction = new ToscaConcatFunction();

        final ToscaGetFunctionDataDefinition toscaGetInput = new ToscaGetFunctionDataDefinition();
        toscaGetInput.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaGetInput.setPropertyName(componentInput1.getName());
        toscaGetInput.setPropertySource(PropertySource.SELF);
        toscaConcatFunction.setParameters(List.of(toscaGetInput));

        final ToscaGetFunctionDataDefinition toscaGetPropertyFromInstance = new ToscaGetFunctionDataDefinition();
        toscaGetPropertyFromInstance.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
        toscaGetPropertyFromInstance.setPropertyName(instancePropertyName1);
        toscaGetPropertyFromInstance.setSourceName(componentInstance1.getName());
        toscaGetPropertyFromInstance.setPropertySource(PropertySource.INSTANCE);
        toscaGetPropertyFromInstance.setPropertyPathFromSource(List.of(instancePropertyName1));

        final ToscaGetFunctionDataDefinition toscaGetPropertyFromSelf = new ToscaGetFunctionDataDefinition();
        toscaGetPropertyFromSelf.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
        toscaGetPropertyFromSelf.setPropertyName(componentProperty1.getName());
        toscaGetPropertyFromSelf.setPropertySource(PropertySource.SELF);
        toscaGetPropertyFromSelf.setPropertyPathFromSource(List.of(componentProperty1.getName()));

        final ToscaGetFunctionDataDefinition toscaGetAttributeFromInstance = new ToscaGetFunctionDataDefinition();
        toscaGetAttributeFromInstance.setFunctionType(ToscaGetFunctionType.GET_ATTRIBUTE);
        toscaGetAttributeFromInstance.setPropertyName(instanceAttribute1.getUniqueId());
        toscaGetAttributeFromInstance.setSourceName(componentInstance1.getName());
        toscaGetAttributeFromInstance.setPropertySource(PropertySource.INSTANCE);
        toscaGetAttributeFromInstance.setPropertyPathFromSource(List.of(instanceAttribute1.getName()));

        final ToscaGetFunctionDataDefinition toscaGetAttributeFromSelf = new ToscaGetFunctionDataDefinition();
        toscaGetAttributeFromSelf.setFunctionType(ToscaGetFunctionType.GET_ATTRIBUTE);
        toscaGetAttributeFromSelf.setPropertyName(componentAttribute1.getName());
        toscaGetAttributeFromSelf.setPropertySource(PropertySource.SELF);
        toscaGetAttributeFromSelf.setPropertyPathFromSource(List.of(componentAttribute1.getName()));

        toscaConcatFunction.setParameters(
            List.of(toscaGetInput, toscaGetPropertyFromSelf, toscaGetPropertyFromInstance, toscaGetAttributeFromSelf, toscaGetAttributeFromInstance)
        );

        //when
        toscaFunctionService.updateFunctionWithDataFromSelfComponent(toscaConcatFunction, component, instancePropertyMap, instanceAttributeMap);

        //then
        assertEquals(componentInput1.getUniqueId(), toscaGetInput.getPropertyUniqueId());
        assertEquals(component.getUniqueId(), toscaGetInput.getSourceUniqueId());
        assertEquals(component.getName(), toscaGetInput.getSourceName());

        assertEquals(instancePropertyId1, toscaGetPropertyFromInstance.getPropertyUniqueId());
        assertEquals(componentInstance1.getUniqueId(), toscaGetPropertyFromInstance.getSourceUniqueId());

        assertEquals(instanceAttribute1.getUniqueId(), toscaGetAttributeFromInstance.getPropertyUniqueId());
        assertEquals(componentInstance1.getUniqueId(), toscaGetAttributeFromInstance.getSourceUniqueId());

        assertEquals(componentAttribute1.getUniqueId(), toscaGetAttributeFromSelf.getPropertyUniqueId());
        assertEquals(component.getUniqueId(), toscaGetAttributeFromSelf.getSourceUniqueId());
        assertEquals(component.getName(), toscaGetAttributeFromSelf.getSourceName());

        assertEquals(componentProperty1.getUniqueId(), toscaGetPropertyFromSelf.getPropertyUniqueId());
        assertEquals(component.getUniqueId(), toscaGetPropertyFromSelf.getSourceUniqueId());
        assertEquals(component.getName(), toscaGetPropertyFromSelf.getSourceName());
    }
}