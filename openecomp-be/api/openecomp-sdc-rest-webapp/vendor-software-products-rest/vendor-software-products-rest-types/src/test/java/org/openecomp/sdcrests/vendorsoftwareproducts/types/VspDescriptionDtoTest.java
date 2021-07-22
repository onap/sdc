/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nokia. All rights reserved.
 *  Modifications Copyright (C) 2021 Nordix Foundation.
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
package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class VspDescriptionDtoTest {

    @Test
    void shouldHaveValidGettersAndSetters() {
        assertThat(VspDescriptionDto.class, hasValidGettersAndSettersExcluding("selectedModelList"));
    }

    @Test
    void shouldHaveValidToString() {
        assertThat(VspDescriptionDto.class, hasValidBeanToStringExcluding("selectedModelList"));
    }

    @Test
    void shouldHaveEquals() {
        assertThat(VspDescriptionDto.class, hasValidBeanEqualsExcluding("selectedModelList"));
    }

    @Test
    void shouldHaveHashCode() {
        assertThat(VspDescriptionDto.class, hasValidBeanHashCodeExcluding("selectedModelList"));
    }
}