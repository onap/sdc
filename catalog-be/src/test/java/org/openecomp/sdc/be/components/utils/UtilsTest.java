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

package org.openecomp.sdc.be.components.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void testThatEmptyListReturns0() {
        List<String> existingResourceNames = new ArrayList<>();
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isZero();
    }

    @Test
    void testListWithValidValue() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..0");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(1);
    }

    @Test
    void testListWithInvalidSingleValue() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(1);
    }

    @Test
    void testListWithValidValues() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..0",
            "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..10",
            "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..15",
            "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..2");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(16);
    }

    @Test
    void testListWithInvalidValue() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..0",
            "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..10",
            "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..15",
            "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(16);
    }
}
