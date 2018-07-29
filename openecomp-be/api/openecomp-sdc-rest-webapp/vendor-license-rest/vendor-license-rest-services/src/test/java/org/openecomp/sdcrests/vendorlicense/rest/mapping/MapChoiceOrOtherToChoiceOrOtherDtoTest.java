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
import static org.testng.Assert.assertNotNull;


public class MapChoiceOrOtherToChoiceOrOtherDtoTest {

    @Test
    public void testOther() {
        ChoiceOrOther source = new ChoiceOrOther();
        ChoiceOrOtherDto target = new ChoiceOrOtherDto();
        MapChoiceOrOtherToChoiceOrOtherDto mapper = new MapChoiceOrOtherToChoiceOrOtherDto();
        String param = "768f875f-af54-4cd2-aaa7-75ee7a176031";
        source.setOther(param);
        mapper.doMapping(source, target);
        assertEquals(target.getOther(), param);
    }

    @Test
    public void testChoice() {
        ChoiceOrOther source = new ChoiceOrOther();
        ChoiceOrOtherDto target = new ChoiceOrOtherDto();
        MapChoiceOrOtherToChoiceOrOtherDto mapper = new MapChoiceOrOtherToChoiceOrOtherDto();
        TestEnum param = TestEnum.Yes;
        source.setChoice(param);
        mapper.doMapping(source, target);
        assertNotNull(source.getChoice());
    }

    enum TestEnum {
        Yes
    }
}
