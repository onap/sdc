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
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;


public class MapMultiChoiceOrOtherDtoToMultiChoiceOrOtherTest {

    @Test
    public void testChoices() {
        MultiChoiceOrOtherDto source = new MultiChoiceOrOtherDto();
        MultiChoiceOrOther target = new MultiChoiceOrOther();
        MapMultiChoiceOrOtherDtoToMultiChoiceOrOther mapper = new MapMultiChoiceOrOtherDtoToMultiChoiceOrOther();
        Set set = new HashSet();
        set.add(TestEnum.Yes);
        set.add(TestEnum.No);
        source.setChoices(set);
        mapper.doMapping(source, target);
        assertEquals(target.getChoices(), set);
    }

    @Test
    public void testOther() {
        MultiChoiceOrOtherDto source = new MultiChoiceOrOtherDto();
        MultiChoiceOrOther target = new MultiChoiceOrOther();
        MapMultiChoiceOrOtherDtoToMultiChoiceOrOther mapper = new MapMultiChoiceOrOtherDtoToMultiChoiceOrOther();
        String param = "930a30ce-72a5-43e3-b845-807a3e34228f";
        source.setOther(param);
        mapper.doMapping(source, target);
        assertEquals(target.getOther(), param);
    }

    enum TestEnum {
        Yes, No
    }
}
