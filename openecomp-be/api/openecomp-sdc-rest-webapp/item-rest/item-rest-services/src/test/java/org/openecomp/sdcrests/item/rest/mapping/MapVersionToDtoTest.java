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
import java.util.Date;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionState;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdcrests.item.types.VersionDto;

/**
 * This class was generated.
 */
public class MapVersionToDtoTest {

    @Test()
    public void testConversion() {

        final Version source = new Version();

        final String id = "2cb11529-5b9c-4561-a6d6-9188d3df84d6";
        source.setId(id);

        final String name = "c9798995-750d-4759-99ac-7942b24e356a";
        source.setName(name);

        final String description = "269d595d-e818-41d0-a991-bf6dccab8125";
        source.setDescription(description);

        final String baseId = "d8efa2d7-8def-4646-8b9d-363ae102cde0";
        source.setBaseId(baseId);

        final Date creationTime = new Date();
        source.setCreationTime(creationTime);

        final Date modificationTime = new Date();
        source.setModificationTime(modificationTime);

        final VersionStatus status = VersionStatus.Deleted;
        source.setStatus(status);

        final VersionState state = new VersionState();
        source.setState(state);

        final Map<String, Object> additionalInfo = Collections.emptyMap();
        source.setAdditionalInfo(additionalInfo);

        final VersionDto target = new VersionDto();
        final MapVersionToDto mapper = new MapVersionToDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(baseId, target.getBaseId());
        assertSame(status, target.getStatus());
        assertSame(state, target.getState());
        assertEquals(creationTime, target.getCreationTime());
        assertEquals(modificationTime, target.getModificationTime());
        assertSame(additionalInfo, target.getAdditionalInfo());
    }
}
