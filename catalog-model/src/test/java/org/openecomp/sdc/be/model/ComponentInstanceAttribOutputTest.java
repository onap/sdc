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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentInstanceAttribOutputTest {

    private final ComponentInstanceAttribOutput testSubject = new ComponentInstanceAttribOutput();

    @Test
    void testCtor() throws Exception {
        Assertions.assertThat(testSubject).isNotNull().isInstanceOf(ComponentInstanceAttribOutput.class);
        Assertions.assertThat(new ComponentInstanceAttribOutput(testSubject)).isNotNull().isInstanceOf(ComponentInstanceAttribOutput.class);
    }

    @Test
    void test_getParsedAttribNames() {
        String[] result;

        testSubject.setAttributesName("");
        result = testSubject.getParsedAttribNames();
        assertNull(result);

        testSubject.setAttributesName("ZZZ");
        result = testSubject.getParsedAttribNames();
        assertNotNull(result);
        assertEquals(1, result.length);

        testSubject.setAttributesName("AAA#BBB#CCC");
        result = testSubject.getParsedAttribNames();
        assertNotNull(result);
        assertEquals(3, result.length);
    }
}
