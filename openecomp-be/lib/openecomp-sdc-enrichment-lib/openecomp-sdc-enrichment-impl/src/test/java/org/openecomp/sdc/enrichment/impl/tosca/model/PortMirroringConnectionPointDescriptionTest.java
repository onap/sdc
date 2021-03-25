/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.enrichment.impl.tosca.model;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PortMirroringConnectionPointDescriptionTest {

    @Test
    public void testIsEmpty() {
        PortMirroringConnectionPointDescription testSuite = new PortMirroringConnectionPointDescription();
        assertFalse(testSuite.isEmpty());

        testSuite.setNf_type(null);
        assertFalse(testSuite.isEmpty());

        testSuite.setNfc_type(null);
        assertFalse(testSuite.isEmpty());

        testSuite.setNf_naming_code(null);
        assertFalse(testSuite.isEmpty());

        testSuite.setNfc_naming_code(null);
        assertFalse(testSuite.isEmpty());

        testSuite.setNetwork_role(null);
        assertFalse(testSuite.isEmpty());

        testSuite.setPps_capacity(null);
        assertTrue(testSuite.isEmpty());
    }
}
