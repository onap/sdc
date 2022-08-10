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

package org.openecomp.sdc.be.components.validation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.FilterConstraintExceptionSupplier;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.validation.FilterConstraintValidator;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class FilterConstraintValidatorImplTest {

    private final FilterConstraintValidator filterConstraintValidator = new FilterConstraintValidatorImpl();

    @BeforeAll
    static void setUp() {
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        new ConfigurationManager(configurationSource);
    }

    @Test
    void validatePropertyFilterSuccess() {
        final FilterConstraintDto filterConstraintDto = buildFilterConstraintDto("aProperty", null, ConstraintType.EQUAL, FilterValueType.STATIC,
            PropertyFilterTargetType.PROPERTY, "any");
        assertDoesNotThrow(() -> filterConstraintValidator.validate(filterConstraintDto));
    }

    @Test
    void validateCapabilityFilterSuccess() {
        final var filterConstraintDto = new FilterConstraintDto();
        filterConstraintDto.setCapabilityName("aCapability");
        filterConstraintDto.setPropertyName("aProperty");
        filterConstraintDto.setTargetType(PropertyFilterTargetType.CAPABILITY);
        filterConstraintDto.setOperator(ConstraintType.EQUAL);
        filterConstraintDto.setValueType(FilterValueType.STATIC);
        filterConstraintDto.setValue("any");
        assertDoesNotThrow(() -> filterConstraintValidator.validate(filterConstraintDto));
    }

    @ParameterizedTest
    @MethodSource("provideMissingFieldParameters")
    void missingFieldError(FilterConstraintDto filterConstraintDto, ComponentException expectedException) {
        final ByActionStatusComponentException actualException =
            assertThrows(ByActionStatusComponentException.class, () -> filterConstraintValidator.validate(filterConstraintDto));
        assertEquals(actualException.getActionStatus(), expectedException.getActionStatus());
        assertArrayEquals(actualException.getParams(), expectedException.getParams());
    }

    public static Stream<Arguments> provideMissingFieldParameters() {
        final var filterConstraintMissingValue = buildFilterConstraintDto("aProperty", null, ConstraintType.EQUAL, FilterValueType.STATIC, PropertyFilterTargetType.PROPERTY, null);
        final var filterConstraintMissingValueType = buildFilterConstraintDto("aProperty", null, ConstraintType.EQUAL, null, PropertyFilterTargetType.PROPERTY, "any");
        final var filterConstraintMissingPropertyName = buildFilterConstraintDto(null, null, ConstraintType.EQUAL, FilterValueType.STATIC, PropertyFilterTargetType.PROPERTY, "any");
        final var filterConstraintMissingCapabilityName = buildFilterConstraintDto("aProperty", null, ConstraintType.EQUAL, FilterValueType.STATIC, PropertyFilterTargetType.CAPABILITY, "any");
        final var filterConstraintMissingOperator = buildFilterConstraintDto("aProperty", null, null, FilterValueType.STATIC, PropertyFilterTargetType.PROPERTY, "any");
        final var filterConstraintMissingTargetType = buildFilterConstraintDto("aProperty", null, ConstraintType.EQUAL, FilterValueType.STATIC, null, "any");

        return Stream.of(
            Arguments.of(filterConstraintMissingValue, FilterConstraintExceptionSupplier.missingField("value").get()),
            Arguments.of(filterConstraintMissingValueType, FilterConstraintExceptionSupplier.missingField("valueType").get()),
            Arguments.of(filterConstraintMissingPropertyName, FilterConstraintExceptionSupplier.missingField("propertyName").get()),
            Arguments.of(filterConstraintMissingCapabilityName, FilterConstraintExceptionSupplier.missingField("capabilityName").get()),
            Arguments.of(filterConstraintMissingOperator, FilterConstraintExceptionSupplier.missingField("operator").get()),
            Arguments.of(filterConstraintMissingTargetType, FilterConstraintExceptionSupplier.missingField("targetType").get())
        );
    }

    @Test
    void validateNullFilterConstraint() {
        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> filterConstraintValidator.validate(null));
        final ByActionStatusComponentException expectedException = FilterConstraintExceptionSupplier.filterConstraintNotProvided().get();
        assertEquals(actualException.getActionStatus(), expectedException.getActionStatus());
        assertArrayEquals(actualException.getParams(), expectedException.getParams());
    }

    private static FilterConstraintDto buildFilterConstraintDto(String propertyName, String capabilityName, ConstraintType constraintType,
                                                                FilterValueType filterValueType, PropertyFilterTargetType targetType, Object value) {
        final var filterConstraintMissingValueType = new FilterConstraintDto();
        filterConstraintMissingValueType.setPropertyName(propertyName);
        filterConstraintMissingValueType.setCapabilityName(capabilityName);
        filterConstraintMissingValueType.setTargetType(targetType);
        filterConstraintMissingValueType.setOperator(constraintType);
        filterConstraintMissingValueType.setValueType(filterValueType);
        filterConstraintMissingValueType.setValue(value);
        return filterConstraintMissingValueType;
    }
}