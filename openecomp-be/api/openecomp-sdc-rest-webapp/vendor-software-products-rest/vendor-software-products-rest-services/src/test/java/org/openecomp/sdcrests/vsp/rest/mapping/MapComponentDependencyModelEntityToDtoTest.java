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

public class MapComponentDependencyModelEntityToDtoTest {

    private static final String TEST_VALUE = "some_test_id";

    @Test
    public void testSourceId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyModel target = new ComponentDependencyModel();
        MapComponentDependencyModelEntityToDto mapper = new  MapComponentDependencyModelEntityToDto();
        source.setSourceComponentId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getSourceId(), TEST_VALUE);
    }
    @Test
    public void testTargetId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyModel target = new ComponentDependencyModel();
        MapComponentDependencyModelEntityToDto mapper = new  MapComponentDependencyModelEntityToDto();
        source.setTargetComponentId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getTargetId(), TEST_VALUE);
    }

    @Test
    public void testRelationType() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyModel target = new ComponentDependencyModel();
        MapComponentDependencyModelEntityToDto mapper = new  MapComponentDependencyModelEntityToDto();
        source.setRelation(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getRelationType(), TEST_VALUE);
    }
}
