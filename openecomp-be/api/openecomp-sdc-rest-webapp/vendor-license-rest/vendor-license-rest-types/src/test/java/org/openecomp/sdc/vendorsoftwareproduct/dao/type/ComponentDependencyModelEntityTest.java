/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class ComponentDependencyModelEntityTest {
    @Test
    public void accessorsTest() {
        assertThat(ComponentDependencyModelEntity.class,
                hasValidGettersAndSettersExcluding("entityType", "firstClassCitizenId"));
    }


    @Test
    public void equalsTest() {
        String vspId = "1";
        Version version = new Version("2");
        String dependencyId = "3";
        ComponentDependencyModelEntity obj = null;
        ComponentDependencyModelEntity componentDependencyModelEntity =
                new ComponentDependencyModelEntity(vspId, version, dependencyId);
        assertNotEquals(componentDependencyModelEntity, obj);
        obj = new ComponentDependencyModelEntity(vspId, version, dependencyId);
        assertEquals(componentDependencyModelEntity, obj);
    }

    @Test
    public void hashCodeTest() {
        String vspId = "1";
        Version version = new Version("2");
        String dependencyId = "3";
        ComponentDependencyModelEntity obj = new ComponentDependencyModelEntity(vspId, version, dependencyId);
        ComponentDependencyModelEntity componentDependencyModelEntity =
                new ComponentDependencyModelEntity(vspId, version, dependencyId);

        assertEquals(obj.hashCode(), componentDependencyModelEntity.hashCode());
        assertNotEquals(obj.hashCode(), null);
        assertNotEquals(componentDependencyModelEntity.hashCode(), 0);
    }
}