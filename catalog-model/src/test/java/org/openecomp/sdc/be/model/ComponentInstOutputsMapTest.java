/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentInstOutputsMapTest {

    private final ComponentInstOutputsMap testInstance = new ComponentInstOutputsMap();

    @Test
    void test_resolveAttributesToDeclare_isEmpty() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            final Pair<String, List<ComponentInstanceAttribOutput>> result = testInstance.resolveAttributesToDeclare();
        });
    }

    @Test
    void test_resolveAttributesToDeclare_success() {
        Map<String, List<ComponentInstanceAttribOutput>> map = new HashMap<>();
        map.put("mock", Arrays.asList(new ComponentInstanceAttribOutput()));
        testInstance.setComponentInstanceOutputsMap(map);
        testInstance.setComponentInstanceAttributes(null);
        Pair<String, List<ComponentInstanceAttribOutput>> result = testInstance.resolveAttributesToDeclare();
        Assertions.assertNotNull(result);

        testInstance.setComponentInstanceOutputsMap(null);
        testInstance.setComponentInstanceAttributes(map);
        result = testInstance.resolveAttributesToDeclare();
        Assertions.assertNotNull(result);
    }

    @Test
    void test_getComponentInstanceOutputsMap() {
        final Map<String, List<ComponentInstanceAttribOutput>> result = testInstance.getComponentInstanceOutputsMap();
        Assertions.assertNotNull(result);
    }

    @Test
    void test_getComponentInstanceAttributes() {
        final Map<String, List<ComponentInstanceAttribOutput>> result = testInstance.getComponentInstanceAttributes();
        Assertions.assertNotNull(result);
    }

}
