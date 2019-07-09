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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessEntityDto;

/**
 * This class was generated.
 */
public class MapProcessEntityToProcessEntityDtoTest {

    @Test()
    public void testConversion() {
        final ProcessEntity source = new ProcessEntity();

        final String id = "0ac97ed6-b35e-4568-80c2-71b5db9e3261";
        source.setId(id);

        final String name = "c449c97f-330d-4864-aa7b-a4569520f880";
        source.setName(name);

        final String description = "11040b64-24eb-4fca-b1d8-373a3727d535";
        source.setDescription(description);

        final ProcessType type = ProcessType.Lifecycle_Operations;
        source.setType(type);

        final String artifactName = "11773228-4e11-4f29-b5e4-733089b99d76";
        source.setArtifactName(artifactName);

        final ProcessEntityDto target = new ProcessEntityDto();
        final MapProcessEntityToProcessEntityDto mapper = new MapProcessEntityToProcessEntityDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(type, target.getType());
        assertEquals(artifactName, target.getArtifactName());
    }
}
