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

package org.openecomp.sdc.be.datatypes.enums;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FilterValueTypeTest {

    @Test
    void findByEmptyNameTest() {
        assertTrue(FilterValueType.findByName(null).isEmpty());
        assertTrue(FilterValueType.findByName("").isEmpty());
    }

    @Test
    void findByNameNotFoundTest() {
        assertTrue(FilterValueType.findByName("thisNameDoesNotExist").isEmpty());
    }

    @ParameterizedTest(name = "{index}: {0} should be {1}")
    @MethodSource("getValueTypeForFindByName")
    void test(final String nameToFind, final FilterValueType filterValueType) {
        final Optional<FilterValueType> actualFilterValueType = FilterValueType.findByName(nameToFind);
        assertTrue(actualFilterValueType.isPresent());
        assertEquals(actualFilterValueType.get(), filterValueType);
    }

    private static Stream<Arguments> getValueTypeForFindByName() {
        final Stream<Arguments> allFilterValueTypeNameArguments = Arrays.stream(FilterValueType.values())
            .map(filterValueType -> Arguments.of(filterValueType.getName(), filterValueType));
        final Stream<Arguments> allFilterValueTypeNameIgnoreCaseArguments = Arrays.stream(FilterValueType.values())
            .map(filterValueType -> Arguments.of(filterValueType.getName().toUpperCase(), filterValueType));

        final Stream<Arguments> legacyArguments = Stream.of(
            Arguments.of(FilterValueType.GET_INPUT.getLegacyName(), FilterValueType.GET_INPUT),
            Arguments.of(FilterValueType.GET_INPUT.getLegacyName().toUpperCase(), FilterValueType.GET_INPUT),
            Arguments.of(FilterValueType.GET_PROPERTY.getLegacyName(), FilterValueType.GET_PROPERTY),
            Arguments.of(FilterValueType.GET_PROPERTY.getLegacyName().toUpperCase(), FilterValueType.GET_PROPERTY)
        );

        return Stream.of(allFilterValueTypeNameIgnoreCaseArguments, allFilterValueTypeNameArguments, legacyArguments)
            .reduce(Stream::concat)
            .orElseGet(Stream::empty);
    }

}