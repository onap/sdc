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

import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;

import static org.testng.Assert.assertEquals;

public class MapChoiceOrOtherDtoToChoiceOrOtherTest {

    @Test
    public void testChoice() {
        ChoiceOrOtherDto<TestEnum> source = new ChoiceOrOtherDto<>();
        ChoiceOrOther target = new ChoiceOrOther();
        MapChoiceOrOtherDtoToChoiceOrOther mapper = new MapChoiceOrOtherDtoToChoiceOrOther();
        TestEnum param = TestEnum.Yes;
        source.setChoice(param);
        mapper.doMapping(source, target);
        assertEquals(target.getChoice(), param);
    }

    @Test
    public void testOther() {
        ChoiceOrOtherDto source = new ChoiceOrOtherDto();
        ChoiceOrOther target = new ChoiceOrOther();
        MapChoiceOrOtherDtoToChoiceOrOther mapper = new MapChoiceOrOtherDtoToChoiceOrOther();
        String param = "47e3f467-1700-498f-b445-2ba8910253b2";
        source.setOther(param);
        mapper.doMapping(source, target);
        assertEquals(target.getOther(), param);
    }

    enum TestEnum {
        Yes
    }
}
