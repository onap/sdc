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

import java.util.Date;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdcrests.item.types.RevisionDto;

/**
 * This class was generated.
 */
public class MapRevisionToDtoTest {

    @Test()
    public void testConversion() {

        final Revision source = new Revision();

        final String id = "ecbf1a77-a420-4655-b2fc-e403a703036d";
        source.setId(id);

        final String message = "2f2a55c8-90b6-413c-bf31-168108e75cb3";
        source.setMessage(message);

        final Date time = new Date();
        source.setTime(time);

        final String user = "503cf667-028b-4d53-bf76-68ad85edf51d";
        source.setUser(user);

        final RevisionDto target = new RevisionDto();
        final MapRevisionToDto mapper = new MapRevisionToDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(message, target.getMessage());
        assertEquals(time, target.getTime());
        assertEquals(user, target.getUser());
    }
}
