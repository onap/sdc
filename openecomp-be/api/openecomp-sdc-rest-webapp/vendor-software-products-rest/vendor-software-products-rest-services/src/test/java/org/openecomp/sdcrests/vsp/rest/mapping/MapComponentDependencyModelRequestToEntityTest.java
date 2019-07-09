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
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;

public class MapComponentDependencyModelRequestToEntityTest {

    private static final String TEST_VALUE = "some_test_id";
    private static final String RELATION_TYPE = "dependsOn";

    @Test
    public void testSourceId() {
        ComponentDependencyModel source = new ComponentDependencyModel();
        ComponentDependencyModelEntity target = new ComponentDependencyModelEntity();
        MapComponentDependencyModelRequestToEntity mapper = new   MapComponentDependencyModelRequestToEntity();
        source.setSourceId(TEST_VALUE);
        source.setRelationType(RELATION_TYPE);
        mapper.doMapping(source, target);
        assertEquals(target.getSourceComponentId(), TEST_VALUE);
    }
    @Test
    public void testTargetId() {
        ComponentDependencyModel source = new ComponentDependencyModel();
        ComponentDependencyModelEntity target = new ComponentDependencyModelEntity();
        MapComponentDependencyModelRequestToEntity mapper = new   MapComponentDependencyModelRequestToEntity();
        source.setTargetId(TEST_VALUE);
        source.setRelationType(RELATION_TYPE);
        mapper.doMapping(source, target);
        assertEquals(target.getTargetComponentId(), TEST_VALUE);
    }

    @Test
    public void testRelationType() {
        ComponentDependencyModel source = new ComponentDependencyModel();
        ComponentDependencyModelEntity target = new ComponentDependencyModelEntity();
        MapComponentDependencyModelRequestToEntity mapper = new   MapComponentDependencyModelRequestToEntity();
        source.setRelationType(RELATION_TYPE);
        mapper.doMapping(source, target);
        assertEquals(target.getRelation(), RELATION_TYPE);
    }
}
