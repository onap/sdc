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
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdcrests.item.types.ActivityLogDto;

/**
 * This class was generated.
 */
public class MapActivityLogEntityToDtoTest {

    @Test()
    public void testConversion() {

        final ActivityLogEntity source = new ActivityLogEntity();

        final String id = "e8ad437b-8d86-4819-954f-57fb9161a912";
        source.setId(id);

        final ActivityType type = ActivityType.Upload_Artifact;
        source.setType(type);

        final String user = "278dbb70-469d-4a5f-bea2-bc62fa9d4671";
        source.setUser(user);

        final Date timestamp = new Date();
        source.setTimestamp(timestamp);

        final String comment = "1cf631a9-016b-4914-9ced-ded9d5ba9469";
        source.setComment(comment);

        final boolean success = true;
        source.setSuccess(true);

        final String message = "9b5e29e9-b47a-4ece-b8a0-b68f4b346e3b";
        source.setMessage(message);

        final ActivityLogDto target = new ActivityLogDto();
        final MapActivityLogEntityToDto mapper = new MapActivityLogEntityToDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(timestamp, target.getTimestamp());
        assertEquals(type.name(), target.getType());
        assertEquals(comment, target.getComment());
        assertEquals(user, target.getUser());
        assertEquals(success, target.getStatus().isSuccess());
        assertEquals(message, target.getStatus().getMessage());
    }
}
