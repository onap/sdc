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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;

/**
 * This class was generated.
 */
public class MapItemToVspDetailsDtoTest {

    @Test()
    public void testConversion() {

        final Item source = new Item();

        final String id = "1377a613-7f39-43f7-a930-bec3156dcc51";
        source.setId(id);

        final String name = "5a4e8b51-f661-4a47-a920-378aac80aad3";
        source.setName(name);

        final String owner = "021fe90b-e51c-4683-bc0d-9d2456a07896";
        source.setOwner(owner);

        final ItemStatus status = ItemStatus.ACTIVE;
        source.setStatus(status);

        final String description = "7514fb1a-2f2b-4d44-a7ab-971813ee8413";
        source.setDescription(description);

        final VspDetailsDto target = new VspDetailsDto();
        final MapItemToVspDetailsDto mapper = new MapItemToVspDetailsDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(name, target.getName());
        assertEquals(owner, target.getOwner());
        assertEquals(status.name(), target.getStatus());
        assertEquals(description, target.getDescription());
    }
}
