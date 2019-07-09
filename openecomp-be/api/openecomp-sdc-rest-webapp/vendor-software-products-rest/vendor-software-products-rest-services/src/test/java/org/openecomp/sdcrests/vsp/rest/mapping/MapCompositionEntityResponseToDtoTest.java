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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;

public class MapCompositionEntityResponseToDtoTest {

    @Test
    public void testId() {
        CompositionEntityResponse source = new CompositionEntityResponse();
        CompositionEntityResponseDto target = new CompositionEntityResponseDto();
        String testId = "some_test_id";
        source.setId(testId);
        MapCompositionEntityResponseToDto mapper =
                new MapCompositionEntityResponseToDto(new MapComponentDataToComponentDto(), ComponentDto.class);
        mapper.doMapping(source, target);
        assertEquals(target.getId(), testId);
    }

    @Test
    public void testSchema() {
        CompositionEntityResponse source = new CompositionEntityResponse();
        CompositionEntityResponseDto target = new CompositionEntityResponseDto();
        MapCompositionEntityResponseToDto mapper =
                new MapCompositionEntityResponseToDto(new MapComponentDataToComponentDto(), ComponentDto.class);
        String schema = "component_name_1";
        source.setSchema(schema);
        mapper.doMapping(source, target);
        assertEquals(target.getSchema(), schema);
    }

}
