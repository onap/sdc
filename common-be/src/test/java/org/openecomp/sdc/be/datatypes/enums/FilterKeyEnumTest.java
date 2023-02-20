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

package org.openecomp.sdc.be.datatypes.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum.CATEGORY;
import static org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum.RESOURCE_TYPE;
import static org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum.VERSION;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterKeyEnumTest {

    @Test
    void testGetValidFiltersByAssetType() {
        List<String> result;

        result = FilterKeyEnum.getValidFiltersByAssetType(ComponentTypeEnum.RESOURCE);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(CATEGORY.getName()));
        assertFalse(result.contains(VERSION.getName()));
        result = FilterKeyEnum.getValidFiltersByAssetType(ComponentTypeEnum.SERVICE);
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(CATEGORY.getName()));
        assertFalse(result.contains(RESOURCE_TYPE.getName()));
        result = FilterKeyEnum.getValidFiltersByAssetType(ComponentTypeEnum.SERVICE_INSTANCE);
        assertNotNull(result);
        assertEquals(0, result.size());
        result = FilterKeyEnum.getValidFiltersByAssetType(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
