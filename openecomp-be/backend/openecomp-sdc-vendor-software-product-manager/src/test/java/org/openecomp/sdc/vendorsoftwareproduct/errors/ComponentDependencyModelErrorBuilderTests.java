/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class ComponentDependencyModelErrorBuilderTests {

    @Test
    public void testGetCyclicDependencyComponentErrorBuilder() {
        //when
        ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getCyclicDependencyComponentErrorBuilder();

        //then
        assertEquals("CYCLIC_DEPENDENCY_IN_COMPONENTS", errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Cyclic dependency exists between components.", errorCode.message());
    }

    @Test
    public void testGetInvalidRelationTypeErrorBuilder() {
        //when
        ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getInvalidRelationTypeErrorBuilder();

        //then
        assertEquals("INVALID_COMPONENT_RELATION_TYPE", errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Invalid relation type for components.", errorCode.message());
    }

    @Test
    public void testGetNoSourceComponentErrorBuilder() {
        //when
        ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder();

        //then
        assertEquals("NO_SOURCE_COMPONENT", errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Source component is mandatory.", errorCode.message());
    }

    @Test
    public void testGetSourceTargetComponentEqualErrorBuilder() {
        //when
        ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder();

        //then
        assertEquals("SAME_SOURCE_TARGET_COMPONENT", errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Source and target components are same.", errorCode.message());
    }
}
