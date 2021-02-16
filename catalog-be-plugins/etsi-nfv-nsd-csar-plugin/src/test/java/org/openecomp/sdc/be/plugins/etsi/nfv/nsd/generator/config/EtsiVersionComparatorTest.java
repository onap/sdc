
/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion.VERSION_2_5_1;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion.VERSION_2_7_1;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion.VERSION_3_3_1;

import org.junit.jupiter.api.Test;

class EtsiVersionComparatorTest {

    @Test
    void compareTest() {
        final NsDescriptorVersionComparator comparator = new NsDescriptorVersionComparator();
        assertEquals(0, comparator.compare(VERSION_2_5_1, VERSION_2_5_1));
        assertEquals(0, comparator.compare(VERSION_2_7_1, VERSION_2_7_1));
        assertEquals(0, comparator.compare(VERSION_3_3_1, VERSION_3_3_1));
        assertEquals(-1, comparator.compare(VERSION_2_5_1, VERSION_2_7_1));
        assertEquals(1, comparator.compare(VERSION_2_7_1, VERSION_2_5_1));
        assertEquals(1, comparator.compare(VERSION_3_3_1, VERSION_2_7_1));
        assertEquals(-1, comparator.compare(VERSION_2_7_1, VERSION_3_3_1));
    }
}
