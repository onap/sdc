/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.vendorlicense.rest.mapping;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;

/**
 * Any change to ChoiceOrOther easily break reconstruction of objects of this type.
 * This test protects from such accidental changes.
 *
 * @author EVITALIY
 * @since 28 Dec 17
 */
class ChoiceOrOtherMappingTest {

    private static final String UNKNOWN = "Unknown";

    public enum TestEnum {
        Yes
    }

    @Test
    void testApplyMappingFromDto() {

        ChoiceOrOtherDto<TestEnum> source = new ChoiceOrOtherDto<>();
        source.setChoice(TestEnum.Yes);
        source.setOther(UNKNOWN);

        ChoiceOrOther<TestEnum> expected = new ChoiceOrOther<>(TestEnum.Yes, UNKNOWN);

        ChoiceOrOther result = new MapChoiceOrOtherDtoToChoiceOrOther().applyMapping(source, ChoiceOrOther.class);
        assertEquals(result, expected);
    }

    @Test
    void testApplyMappingToDto() {

        ChoiceOrOther<TestEnum> source = new ChoiceOrOther<>(TestEnum.Yes, UNKNOWN);

        ChoiceOrOtherDto<TestEnum> expected = new ChoiceOrOtherDto<>();
        expected.setChoice(TestEnum.Yes);
        expected.setOther(UNKNOWN);

        ChoiceOrOtherDto result = new MapChoiceOrOtherToChoiceOrOtherDto().applyMapping(source, ChoiceOrOtherDto.class);
        assertEquals(result, expected);
    }
}
