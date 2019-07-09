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
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;

public class MapComponentDataToComponentDtoTest {

    @Test
    public void testDisplayName() {
        ComponentData source = new ComponentData();
        ComponentDto target = new ComponentDto();
        MapComponentDataToComponentDto mapper = new MapComponentDataToComponentDto();
        String displayName = "some_test_name";
        source.setDisplayName(displayName);
        mapper.doMapping(source, target);
        assertEquals(target.getDisplayName(), displayName);
    }

    @Test
    public void testName() {
        ComponentData source = new ComponentData();
        ComponentDto target = new ComponentDto();
        MapComponentDataToComponentDto mapper = new MapComponentDataToComponentDto();
        String name = "component_name_1";
        source.setName(name);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), name);
    }

    @Test
    public void testDescription() {
        ComponentData source = new ComponentData();
        ComponentDto target = new ComponentDto();
        MapComponentDataToComponentDto mapper = new MapComponentDataToComponentDto();
        String description = "my_test_component";
        source.setDescription(description);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), description);
    }
}
