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
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;

import static org.testng.Assert.assertEquals;


public class MapLimitEntityToLimitDtoTest {

    @Test
    public void testDescription() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "7315b1fa-f797-438a-b0d1-8b652899ecec";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testAggregationFunction() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        AggregationFunction aggregationFunction = AggregationFunction.Average;
        source.setAggregationFunction(aggregationFunction);
        mapper.doMapping(source, target);
        assertEquals(target.getAggregationFunction(), aggregationFunction.toString());
    }

    @Test
    public void testType() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        LimitType limitType = LimitType.Vendor;
        source.setType(limitType);
        mapper.doMapping(source, target);
        assertEquals(target.getType(), limitType.toString());
    }

    @Test
    public void testUnit() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "94ab5e65-f777-49b7-9328-14af712a3767";
        source.setUnit(param);
        mapper.doMapping(source, target);
        assertEquals(target.getUnit(), param);
    }

    @Test
    public void testMetric() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "1fb615b9-8108-485d-83c9-5dbfb14cf953";
        source.setMetric(param);
        mapper.doMapping(source, target);
        assertEquals(target.getMetric(), param);
    }

    @Test
    public void testName() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "ab5d6967-0020-4cd2-aeba-ff3e39e52385";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testId() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "52d4d919-015a-4a46-af04-4d0dec17e88d";
        source.setId(param);
        mapper.doMapping(source, target);
        assertEquals(target.getId(), param);
    }

    @Test
    public void testTime() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "4e19d619-5004-423c-abf2-0d6e69a47a5c";
        source.setTime(param);
        mapper.doMapping(source, target);
        assertEquals(target.getTime(), param);
    }

    @Test
    public void testValue() {
        LimitEntity source = new LimitEntity();
        LimitEntityDto target = new LimitEntityDto();
        MapLimitEntityToLimitDto mapper = new MapLimitEntityToLimitDto();
        String param = "13d67707-b02f-40b2-8b19-60fd6405a7af";
        source.setValue(param);
        mapper.doMapping(source, target);
        assertEquals(target.getValue(), param);
    }
}
