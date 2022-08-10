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
        assertEquals(toscaGetFunctionDataDefinition.getPropertySource(), PropertySource.SELF);
        assertEquals(toscaGetFunctionDataDefinition.getSourceUniqueId(), sourceUniqueId);
        assertEquals(toscaGetFunctionDataDefinition.getSourceName(), sourceName);
        assertEquals(toscaGetFunctionDataDefinition.getFunctionType(), ToscaGetFunctionType.GET_INPUT);
        assertEquals(toscaGetFunctionDataDefinition.getPropertyPathFromSource().size(), 2);
        assertEquals(toscaGetFunctionDataDefinition.getPropertyPathFromSource().get(0), propertyPathFromSource.get(0));
        assertEquals(toscaGetFunctionDataDefinition.getPropertyPathFromSource().get(1), propertyPathFromSource.get(1));
    }

}