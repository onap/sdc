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
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;

import java.util.Collections;
import java.util.HashSet;

import static org.testng.Assert.assertEquals;


public class MapEntitlementPoolRequestDtoToEntitlementPoolEntityTest {

    @Test
    public void testExpiryDate() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        String param = "2d9bd40d-9b86-4621-a0b3-015cc65ed29f";
        source.setExpiryDate(param);
        mapper.doMapping(source, target);
        assertEquals(target.getExpiryDate(), param);
    }

    @Test
    public void testName() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        String param = "08c72f48-2cb6-4509-9983-5564c74c1bc8";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testDescription() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        String param = "b5161f7b-b5bc-4e98-b242-a8dc99aeab01";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testThresholdValue() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        Integer param = -767970235;
        source.setThresholdValue(param);
        mapper.doMapping(source, target);
        assertEquals(target.getThresholdValue(), param);
    }

    @Test
    public void testIncrements() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        String param = "3b760eff-6407-44b7-a1f2-2a87b432f094";
        source.setIncrements(param);
        mapper.doMapping(source, target);
        assertEquals(target.getIncrements(), param);
    }

    @Test
    public void testOperationalScope() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        MultiChoiceOrOtherDto<OperationalScope> param = new MultiChoiceOrOtherDto<>();
        param.setChoices(new HashSet(Collections.singletonList(TestEnum.Yes)));
        source.setOperationalScope(param);
        mapper.doMapping(source, target);
        assertEquals(target.getOperationalScope().getChoices(), param.getChoices());
    }

    @Test
    public void testStartDate() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper =
                new MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        String param = "654f3536-9346-45fb-97f5-6dc24e517e31";
        source.setStartDate(param);
        mapper.doMapping(source, target);
        assertEquals(target.getStartDate(), param);
    }

    @Test
    public void testManufacturerReferenceNumber() {
        EntitlementPoolRequestDto source = new EntitlementPoolRequestDto();
        EntitlementPoolEntity target = new EntitlementPoolEntity();
        MapEntitlementPoolRequestDtoToEntitlementPoolEntity mapper = new
            MapEntitlementPoolRequestDtoToEntitlementPoolEntity();
        String param = "02402e1e-7092-485a-9574-46e2d49cca97";
        source.setManufacturerReferenceNumber(param);
        mapper.doMapping(source, target);
        assertEquals(target.getManufacturerReferenceNumber(), param);
    }

    enum TestEnum {
        Yes
    }
}
