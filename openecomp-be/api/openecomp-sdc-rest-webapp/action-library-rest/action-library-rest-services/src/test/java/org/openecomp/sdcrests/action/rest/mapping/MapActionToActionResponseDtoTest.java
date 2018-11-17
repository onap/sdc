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

package org.openecomp.sdcrests.action.rest.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionStatus;
import org.openecomp.sdcrests.action.types.ActionResponseDto;

/**
 * This class was generated.
 */
public class MapActionToActionResponseDtoTest {

    @Test
    public void testConversion() {

        final Action source = new Action();

        final String actionUuId = "40c23c22-bdf3-4c01-88aa-d6f218d408c1";
        source.setActionUuId(actionUuId);

        final String actionInvariantUuId = "3f82fc78-1c78-4df0-9a87-f81c09fc38b7";
        source.setActionInvariantUuId(actionInvariantUuId);

        final String version = "36ca7fe9-fc71-4a8c-9acc-e1d8e379527c";
        source.setVersion(version);

        final ActionStatus status = ActionStatus.Deleted;
        source.setStatus(status);

        final ActionResponseDto target = new ActionResponseDto();

        final MapActionToActionResponseDto mapper = new MapActionToActionResponseDto();
        mapper.doMapping(source, target);

        assertEquals(actionUuId, target.getActionUuId());
        assertEquals(actionInvariantUuId, target.getActionInvariantUuId());
        assertEquals(version, target.getVersion());
        assertEquals(status.name(), target.getStatus());
    }

    @Test
    public void testConversionWhenStatusNull() {

        final Action source = new Action();
        final ActionResponseDto target = new ActionResponseDto();
        final MapActionToActionResponseDto mapper = new MapActionToActionResponseDto();
        mapper.doMapping(source, target);
        assertNull(target.getStatus());
    }
}
