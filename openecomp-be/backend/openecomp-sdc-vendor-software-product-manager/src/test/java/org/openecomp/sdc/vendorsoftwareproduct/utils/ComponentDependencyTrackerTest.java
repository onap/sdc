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
package org.openecomp.sdc.vendorsoftwareproduct.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComponentDependencyTrackerTest {

    @Test
    public void shouldAddNonCyclicDependency() {
        ComponentDependencyTracker componentDependencyTracker = new ComponentDependencyTracker();
        componentDependencyTracker.addDependency("TEST1", "TEST2");
        assertFalse(componentDependencyTracker.isCyclicDependencyPresent());
    }

    @Test
    public void shouldAddCyclicDependency() {
        ComponentDependencyTracker componentDependencyTracker = new ComponentDependencyTracker();
        componentDependencyTracker.addDependency("TEST1", "TEST2");
        componentDependencyTracker.addDependency("TEST2", "TEST1");
        assertTrue(componentDependencyTracker.isCyclicDependencyPresent());
    }
}