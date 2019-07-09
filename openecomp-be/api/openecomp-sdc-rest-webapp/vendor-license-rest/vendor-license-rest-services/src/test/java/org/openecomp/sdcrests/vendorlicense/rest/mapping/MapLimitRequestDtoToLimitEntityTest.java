/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;

/**
 * This class was generated.
 */
public class MapLimitRequestDtoToLimitEntityTest {

    @Test()
    public void testConversion() {

        final LimitRequestDto source = new LimitRequestDto();

        final String name = "d35387ba-b2da-4b96-b4bb-3fcc947729b6";
        source.setName(name);

        final String description = "b58fb468-023f-4336-86de-3ef588f60d75";
        source.setDescription(description);

        final LimitType type = LimitType.Vendor;
        source.setType(type.name());

        final String metric = "139d3366-fd6b-4167-83cc-ac78de1d08ab";
        source.setMetric(metric);

        final String value = "eb4a1266-92ea-4c9d-82aa-12c2cf8b3d63";
        source.setValue(value);

        final String unit = "86c66de7-a02c-461b-88ce-6875cf9b1225";
        source.setUnit(unit);

        final String time = "7352b6b5-2d47-4bfc-a7d1-5fe2b1fba4e0";
        source.setTime(time);

        final LimitEntity target = new LimitEntity();
        final MapLimitRequestDtoToLimitEntity mapper = new MapLimitRequestDtoToLimitEntity();
        mapper.doMapping(source, target);

        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(type, target.getType());
        assertEquals(metric, target.getMetric());
        assertEquals(value, target.getValue());
        assertEquals(unit, target.getUnit());
        assertEquals(time, target.getTime());
    }
}
