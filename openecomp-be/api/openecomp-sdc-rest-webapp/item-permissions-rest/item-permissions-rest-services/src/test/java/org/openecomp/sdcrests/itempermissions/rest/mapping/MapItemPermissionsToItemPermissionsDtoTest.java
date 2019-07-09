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

package org.openecomp.sdcrests.itempermissions.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;

/**
 * This class was generated.
 */
public class MapItemPermissionsToItemPermissionsDtoTest {

    @Test
    public void testConversion() {

        final ItemPermissionsEntity source = new ItemPermissionsEntity();
        final String userId = "23b32755-a837-41bb-9216-aeb8243fd470";
        source.setUserId(userId);
        final String permission = "5069e4f8-ae49-4dfd-8b78-58291ec85b2b";
        source.setPermission(permission);

        final ItemPermissionsDto target = new ItemPermissionsDto();
        final MapItemPermissionsToItemPermissionsDto mapper = new MapItemPermissionsToItemPermissionsDto();
        mapper.doMapping(source, target);

        assertEquals(userId, target.getUserId());
        assertEquals(permission, target.getPermission());
    }
}
