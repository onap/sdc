/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;

class AttributeDefinitionTest {

    private static final String VALUE = "mock";
    private final AttributeDefinition testSubject = new AttributeDefinition();

    @Test
    void testCtor() throws Exception {
        assertNotNull(new AttributeDefinition());
        assertNotNull(new AttributeDefinition(new AttributeDefinition()));
        assertNotNull(new AttributeDefinition(new AttributeDataDefinition()));
    }

    @Test
    void test_DefaultValue() {
        testSubject.setDefaultValue(VALUE);
        final String result = testSubject.getDefaultValue();
        assertEquals(VALUE, result);
    }

    @Test
    void test_isDefinition() {
        assertFalse(testSubject.isDefinition());
    }
}