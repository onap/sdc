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

package org.openecomp.sdc.be.model.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

class FilterConstraintDtoTest {

    @Test
    void isCapabilityPropertyFilter() {
        var filterConstraintDto = new FilterConstraintDto();
        assertFalse(filterConstraintDto.isCapabilityPropertyFilter());
        filterConstraintDto.setCapabilityName("aCapability");
        assertTrue(filterConstraintDto.isCapabilityPropertyFilter());
    }

    @Test
    void readGetFunctionWithToscaGetFunctionInstanceAsValue() {
        final var filterConstraintDto = new FilterConstraintDto();
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
        filterConstraintDto.setValue(toscaGetFunction);
        final Optional<ToscaGetFunctionDataDefinition> readGetFunctionOpt = filterConstraintDto.getAsToscaGetFunction();
        assertTrue(readGetFunctionOpt.isPresent());
        assertEquals(toscaGetFunction, readGetFunctionOpt.get());
    }

    @Test
    void readGetFunctionWithInvalidGetFunctionValue() {
        final var filterConstraintDto = new FilterConstraintDto();
        filterConstraintDto.setValue("not a ToscaGetFunctionDataDefinition");
        final Optional<ToscaGetFunctionDataDefinition> readGetFunctionOpt = filterConstraintDto.getAsToscaGetFunction();
        assertTrue(readGetFunctionOpt.isEmpty());
    }

    @Test
    void readGetFunctionWithGetFunctionValueAsMap() {
        //given
        final List<String> propertyPathFromSource = List.of("input", "path");
        final String propertyUniqueId = "propertyUniqueIdValue";
        final String propertyName = "propertyNameValue";
        final String sourceUniqueId = "sourceUniqueIdValue";
        final String sourceName = "sourceNameValue";
        final Map<String, Object> toscaGetFunctionAsMap = Map.of(
            "propertyUniqueId", propertyUniqueId,
            "propertyName", propertyName,
            "propertySource", PropertySource.SELF.getName(),
            "sourceUniqueId", sourceUniqueId,
            "sourceName", sourceName,
            "functionType", ToscaGetFunctionType.GET_INPUT.getFunctionName(),
            "propertyPathFromSource", propertyPathFromSource
        );

        final var filterConstraintDto = new FilterConstraintDto();
        filterConstraintDto.setValue(toscaGetFunctionAsMap);
        //when
        final Optional<ToscaGetFunctionDataDefinition> readGetFunctionOpt = filterConstraintDto.getAsToscaGetFunction();
        //then
        assertTrue(readGetFunctionOpt.isPresent());
        final ToscaGetFunctionDataDefinition toscaGetFunctionDataDefinition = readGetFunctionOpt.get();
        assertEquals(toscaGetFunctionDataDefinition.getPropertyUniqueId(), propertyUniqueId);
        assertEquals(toscaGetFunctionDataDefinition.getPropertyName(), propertyName);
        assertEquals(PropertySource.SELF, toscaGetFunctionDataDefinition.getPropertySource());
        assertEquals(toscaGetFunctionDataDefinition.getSourceUniqueId(), sourceUniqueId);
        assertEquals(toscaGetFunctionDataDefinition.getSourceName(), sourceName);
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunctionDataDefinition.getFunctionType());
        assertEquals(2, toscaGetFunctionDataDefinition.getPropertyPathFromSource().size());
        assertEquals(toscaGetFunctionDataDefinition.getPropertyPathFromSource().get(0), propertyPathFromSource.get(0));
        assertEquals(toscaGetFunctionDataDefinition.getPropertyPathFromSource().get(1), propertyPathFromSource.get(1));
    }

    @Test
    void readGetFunctionWithGetFunctionAsListValueAsMap() {
        //given
        final List<String> propertyPathFromSource1 = List.of("input1", "path1");
        final String propertyUniqueId1 = "propertyUniqueIdValue1";
        final String propertyName1 = "propertyNameValue1";
        final String sourceUniqueId1 = "sourceUniqueIdValue1";
        final String sourceName1 = "sourceNameValue1";
        final Map<String, Object> toscaGetFunctionAsMap1 = Map.of(
            "propertyUniqueId", propertyUniqueId1,
            "propertyName", propertyName1,
            "propertySource", PropertySource.SELF.getName(),
            "sourceUniqueId", sourceUniqueId1,
            "sourceName", sourceName1,
            "functionType", ToscaGetFunctionType.GET_INPUT.getFunctionName(),
            "propertyPathFromSource", propertyPathFromSource1
        );
        final List<String> propertyPathFromSource2 = List.of("input2", "path2");
        final String propertyUniqueId2 = "propertyUniqueIdValue2";
        final String propertyName2 = "propertyNameValue2";
        final String sourceUniqueId2 = "sourceUniqueIdValue2";
        final String sourceName2 = "sourceNameValue2";
        final Map<String, Object> toscaGetFunctionAsMap2 = Map.of(
            "propertyUniqueId", propertyUniqueId2,
            "propertyName", propertyName2,
            "propertySource", PropertySource.SELF.getName(),
            "sourceUniqueId", sourceUniqueId2,
            "sourceName", sourceName2,
            "functionType", ToscaGetFunctionType.GET_INPUT.getFunctionName(),
            "propertyPathFromSource", propertyPathFromSource2
        );

        List<Object> toscaGetFunctionDataDefinitionList = new ArrayList<>();
        toscaGetFunctionDataDefinitionList.add(toscaGetFunctionAsMap1);
        toscaGetFunctionDataDefinitionList.add(toscaGetFunctionAsMap2);

        final var filterConstraintDto = new FilterConstraintDto();
        filterConstraintDto.setValue(toscaGetFunctionDataDefinitionList);
        //when
        final Optional<List<ToscaGetFunctionDataDefinition>> readListGetFunctionOpt = filterConstraintDto.getAsListToscaGetFunction();
        //then
        assertTrue(readListGetFunctionOpt.isPresent());
        final List<ToscaGetFunctionDataDefinition> toscaGetFunctionDataDefinition = readListGetFunctionOpt.get();
        assertEquals(toscaGetFunctionDataDefinition.get(0).getPropertyUniqueId(), propertyUniqueId1);
        assertEquals(toscaGetFunctionDataDefinition.get(1).getPropertyUniqueId(), propertyUniqueId2);
        assertEquals(toscaGetFunctionDataDefinition.get(0).getPropertyName(), propertyName1);
        assertEquals(toscaGetFunctionDataDefinition.get(1).getPropertyName(), propertyName2);
        assertEquals(PropertySource.SELF, toscaGetFunctionDataDefinition.get(0).getPropertySource());
        assertEquals(PropertySource.SELF, toscaGetFunctionDataDefinition.get(1).getPropertySource());
        assertEquals(toscaGetFunctionDataDefinition.get(0).getSourceUniqueId(), sourceUniqueId1);
        assertEquals(toscaGetFunctionDataDefinition.get(1).getSourceUniqueId(), sourceUniqueId2);
        assertEquals(toscaGetFunctionDataDefinition.get(0).getSourceName(), sourceName1);
        assertEquals(toscaGetFunctionDataDefinition.get(1).getSourceName(), sourceName2);
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunctionDataDefinition.get(0).getFunctionType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunctionDataDefinition.get(1).getFunctionType());
        assertEquals(2, toscaGetFunctionDataDefinition.get(0).getPropertyPathFromSource().size());
        assertEquals(2, toscaGetFunctionDataDefinition.get(1).getPropertyPathFromSource().size());
        assertEquals(toscaGetFunctionDataDefinition.get(0).getPropertyPathFromSource().get(0), propertyPathFromSource1.get(0));
        assertEquals(toscaGetFunctionDataDefinition.get(1).getPropertyPathFromSource().get(0), propertyPathFromSource2.get(0));
        assertEquals(toscaGetFunctionDataDefinition.get(0).getPropertyPathFromSource().get(1), propertyPathFromSource1.get(1));
        assertEquals(toscaGetFunctionDataDefinition.get(1).getPropertyPathFromSource().get(1), propertyPathFromSource2.get(1));
    }
}