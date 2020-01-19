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
package org.openecomp.sdc.be.components.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MapUtilsTest {

    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";

    @Test
    public void shouldCompareMapsOfNulls() {
        assertTrue(MapUtils.compareMaps(null, null));
    }

    @Test
    public void shouldCompareMapIsTrue() {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        map1.put(KEY_1, VALUE_1);
        map2.put(KEY_1, VALUE_1);
        assertTrue(MapUtils.compareMaps(map1, map2));
    }

    @Test
    public void shouldCompareMapWithNull() {
        assertFalse(MapUtils.compareMaps(null, Collections.emptyMap()));
    }

    @Test
    public void shouldCompareMapsIsFalse() {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        map1.put(KEY_1, VALUE_1);
        map2.put(KEY_2, VALUE_1);
        assertFalse(MapUtils.compareMaps(map1, map2));
    }

    @Test
    public void shouldHandleSourceAndTargetObjectsOfDifferentValues() {
        assertFalse(MapUtils.handleSourceAndTargetObjects(VALUE_1, VALUE_2));
    }

    @Test
    public void shouldHandleSourceAndTargetObjectsOfSameValues() {
        assertTrue(MapUtils.handleSourceAndTargetObjects(VALUE_1, VALUE_1));
    }

    @Test
    public void shouldHandleSourceAndTargetObjectsOfNullValues() {
        assertTrue(MapUtils.handleSourceAndTargetObjects(null, null));
    }

    @Test
    public void shouldHandleSourceAndTargetObjectsOfNullValueAndNotNullValue() {
        assertFalse(MapUtils.handleSourceAndTargetObjects(null, ""));
    }

    @Test
    public void shouldCompareListsOfNulls() {
        assertTrue(MapUtils.compareLists(null, null));
    }

    @Test
    public void shouldCompareListsIsTrue() {
        List<Object> list1 = new ArrayList<>();
        List<Object> list2 = new ArrayList<>();
        list1.add(VALUE_1);
        list2.add(VALUE_1);
        assertTrue(MapUtils.compareLists(list1, list2));
    }

    @Test
    public void shouldCompareListWithNull() {
        assertFalse(MapUtils.compareLists(null, Collections.emptyList()));
    }

    @Test
    public void shouldCompareListsIsFalse() {
        List<Object> list1 = new ArrayList<>();
        List<Object> list2 = new ArrayList<>();
        list1.add(VALUE_1);
        list2.add(VALUE_2);
        assertFalse(MapUtils.compareLists(list1, list2));
    }
}