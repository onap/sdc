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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.onap.sdc.tosca.datatypes.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CapabilityDefinitionTest {


    CapabilityDefinition capabilityDefinition;

    @Before
    public void initialize() {
        capabilityDefinition = new CapabilityDefinition();
    }

    @Test
    public void testClone() {
        CapabilityDefinition cap2 = capabilityDefinition.clone();
        assertIfObjectsAreSame(cap2, capabilityDefinition);
    }

    private void assertIfObjectsAreSame(CapabilityDefinition expected, CapabilityDefinition actual) {
        assertEquals(expected.getAttributes(), actual.getAttributes());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getOccurrences(), actual.getOccurrences());
        assertEquals(expected.getProperties(), actual.getProperties());
        assertEquals(expected.getType(), actual.getType());
    }
}
