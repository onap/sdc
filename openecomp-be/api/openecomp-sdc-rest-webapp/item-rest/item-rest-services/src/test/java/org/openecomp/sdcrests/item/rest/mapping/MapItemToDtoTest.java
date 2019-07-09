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

package org.openecomp.sdcrests.item.rest.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.item.types.ItemDto;

/**
 * This class was generated.
 */
public class MapItemToDtoTest {

    @Test
    public void testConversion() {

        final Item source = new Item();

        final String id = "8c3cf5c1-956d-4701-9805-41ee7496a4b0";
        source.setId(id);

        final String type = "0df6dc63-30a9-453c-9d20-ccd839b30e55";
        source.setType(type);

        final String name = "fa425317-c53a-4ad2-94f5-9a33f2b76e67";
        source.setName(name);

        final String owner = "db08f579-7f4c-4d1a-ae92-a1c1c27c8cc6";
        source.setOwner(owner);

        final ItemStatus status = ItemStatus.ARCHIVED;
        source.setStatus(status);

        final String description = "9b5e29e9-b47a-4ece-b8a0-b68f4b346e3b";
        source.setDescription(description);

        final Map<String, Object> properties = Collections.emptyMap();
        source.setProperties(properties);

        final ItemDto target = new ItemDto();
        final MapItemToDto mapper = new MapItemToDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(type, target.getType());
        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(owner, target.getOwner());
        assertEquals(status.name(), target.getStatus());
        assertSame(properties, target.getProperties());
    }
}
