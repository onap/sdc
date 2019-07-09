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

package org.openecomp.sdcrests.conflict.rest.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.openecomp.conflicts.types.ConflictInfo;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdcrests.conflict.types.ConflictInfoDto;

/**
 * This class was generated.
 */
public class MapConflictInfoToDtoTest {

    @Test
    public void testConversion() {
        final String id = "d10f2016-4c5c-4999-a36a-46bbdb53a1d9";
        final ElementType type = ElementType.Artifact;
        final String name = "8259ae71-0114-4a99-9605-2af801f5e3e3";
        final ConflictInfo source = new ConflictInfo(id, type, name);

        final ConflictInfoDto target = new ConflictInfoDto();
        final MapConflictInfoToDto mapper = new MapConflictInfoToDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertSame(type, target.getType());
        assertEquals(name, target.getName());
    }
}
