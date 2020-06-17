/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T. All rights reserved.
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

package org.onap.sdc.tosca.datatypes.model.heatextend;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyTypeExtTest {

    @Test
    public void initializeProperMapping() {
        Map<String, PropertyTypeExt> map = PropertyTypeExt.initializeMapping();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(PropertyTypeExt.JSON, map.get("json"));
    }

    @Test
    public void getDisplayName() {
        assertEquals("json", PropertyTypeExt.JSON.getDisplayName());
    }

}
