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
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;

/**
 * This class was generated.
 */
public class MapProcessRequestDtoToProcessEntityTest {

    @Test()
    public void testConversion() {

        final ProcessRequestDto source = new ProcessRequestDto();

        final String name = "bab6bcf0-7c1e-44e8-9730-55638c58027a";
        source.setName(name);

        final String description = "725019d0-2501-4c70-ade7-5deca94b40e3";
        source.setDescription(description);

        final ProcessType type = ProcessType.Lifecycle_Operations;
        source.setType(type);

        final ProcessEntity target = new ProcessEntity();
        final MapProcessRequestDtoToProcessEntity mapper = new MapProcessRequestDtoToProcessEntity();
        mapper.doMapping(source, target);

        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertSame(type, target.getType());
    }
}
