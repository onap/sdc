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
package org.openecomp.sdc.common.util;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class PairUtilsTest {

    final ImmutablePair<String, String> immutablePair01 = new ImmutablePair<>("leftValue01", "rightValue01");
    final ImmutablePair<String, String> immutablePair02 = new ImmutablePair<>("leftValue02", "rightValue02");
    final ImmutablePair<String, String> immutablePair03 = new ImmutablePair<>("leftValue03", "rightValue03");

    @Test
    public void validateLeftSequenceReturnsListOfLeftElements() {
        List<String> result = PairUtils.leftSequence(generateImmutableListOfPairs());
        assertTrue(result.containsAll(Lists.newArrayList(immutablePair01.left, immutablePair02.left, immutablePair03.left)));
    }

    @Test
    public void validateRightSequenceReturnsListOfRightElements() {
        List<String> result = PairUtils.rightSequence(generateListOfPairs());
        assertTrue(result.containsAll(Lists.newArrayList(immutablePair01.right, immutablePair02.right, immutablePair03.right)));
    }

    private List<Pair<String, String>> generateListOfPairs() {
        return Lists.newArrayList(immutablePair01, immutablePair02, immutablePair03);
    }

    private List<ImmutablePair<String, String>> generateImmutableListOfPairs() {
        return Lists.newArrayList(immutablePair01, immutablePair02, immutablePair03);
    }
}
