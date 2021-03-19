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

package org.openecomp.sdc.be.dao.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;


import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openecomp.sdc.be.dao.utils.MapUtil.mergeMaps;

public class MapUtilTest {

    @Test
    public void mergeMaps_whenBothMapsAreNull_returnEmptyMap() {
        assertThat(mergeMaps(null, null)).isEmpty();
    }

    @Test
    public void mergeMaps_whenFirstMapIsNull_returnSecondMap() {
        ImmutableMap<String, String> second = ImmutableMap.of("a", "b", "c", "d");
        assertThat(mergeMaps(null, second))
                .isNotSameAs(second)
                .containsAllEntriesOf(second);
    }

    @Test
    public void mergeMaps_whenSecondMapsIsNull_returnFirstMap() {
        ImmutableMap<String, String> first = ImmutableMap.of("a", "b", "c", "d");
        assertThat(mergeMaps(first, null))
                .isNotSameAs(first)
                .containsAllEntriesOf(first);
    }

    @Test
    public void mergeMaps_avoidDuplications_takeValFromFirstMap() {
        ImmutableMap<String, String> first = ImmutableMap.of("key1", "val1", "key2", "val2");
        ImmutableMap<String, String> second = ImmutableMap.of("key1", "val11", "key3", "val3");
        assertThat(mergeMaps(first, second))
                .containsEntry("key1", "val1")
                .containsEntry("key2", "val2")
                .containsEntry("key3", "val3");
     }
     	@Test
	public void testGet() throws Exception {
		Map<String, ? extends Object> mapWildcard = null;
		String path = "";
		Object result;

		result = MapUtil.get(mapWildcard, path);
		assertNull(result);

		path = "mock1.mock2";

		Map<String, Object> map = new HashMap<>();
		map.put("mock1", "test");
		mapWildcard = map;
		result = MapUtil.get(mapWildcard, path);
		assertNull(result);

		Map<String, Integer> subMap = new HashMap<>();
		subMap.put("mock2", 1);
		Map<String, ? extends Object> subMapWildcard = subMap;
		map.put("mock1", subMapWildcard);
		mapWildcard = map;
		result = MapUtil.get(mapWildcard, path);
		assertEquals(1, result);
	}

	@Test
	public void testFlattenMapValues() throws Exception {
		assertNotNull(MapUtil.flattenMapValues(null));

		Map<String, List<String>> map = new HashMap<>();
		List<String> list1 = new LinkedList<>();
		list1.add("test1");
		List<String> list2 = new LinkedList<>();
		list2.add("test2");
		map.put("key1", list1);
		map.put("key2", list2);
		List<String> result = MapUtil.flattenMapValues(map);
		assertEquals(2, result.size());
		assertEquals("test1", result.get(0));
		assertEquals("test2", result.get(1));
	}

	@Test
	public void testStreamOfNullable() throws Exception {
		assertEquals(0, MapUtil.streamOfNullable(null).count());

		Collection collectionTest = new LinkedList<String>();
		collectionTest.add("test");
		assertEquals(1, MapUtil.streamOfNullable(collectionTest).count());
	}

	@Test
	public void testGroupListBy() throws Exception {
		Collection valuesToMap = new LinkedList<String>();
		Function<String, String> groupingFunction = new Function<String, String>() {
			
			@Override
			public String apply(String t) {
				return t;
			}
		};
		Map<String, List<String>> result;

		// default test
		result = MapUtil.groupListBy(valuesToMap, groupingFunction);
	}

	@Test
	public void testToMap() throws Exception {
		Collection<String> valuesToMap = null;
		Function<String, String> mappingFunction = null;
		Map<String, String> result;

		// default test
		result = MapUtil.toMap(valuesToMap, mappingFunction);
	}

	@Test
	public void testConvertMapKeys() throws Exception {
		Map<String, List<String>> map = new HashMap<>();
		Function<String, String> keyMappingFunction = new Function<String, String>() {
			
			@Override
			public String apply(String t) {
				return t;
			}
		};
		Map<String, List<String>> result;

		// default test
		result = MapUtil.convertMapKeys(map, keyMappingFunction);
	}

	@Test
	public void testNewHashMap() throws Exception {
        final String[] keys1 = new String[] { "mock" };
        final String[] values1 = new String[] { "mock" };
		Map<String, String> result;

		// test 1
		result = MapUtil.newHashMap(keys1, values1);

		// test 2
        final String[] keys2 = new String[] { "mock" };
        final String[] values2 = null;
        assertThatThrownBy(() -> MapUtil.newHashMap(keys2, values2))
                    .isInstanceOf(IllegalArgumentException.class);

		// test 3
        final String[] keys3 = null;
        final String[] values3 = null;
        assertThatThrownBy(() -> MapUtil.newHashMap(keys3, values3))
                .isInstanceOf(IllegalArgumentException.class);

		// test 4
		final String[] values4 = new String[] { "mock" };
		final String[] keys4 = null;
		assertThatThrownBy(() -> MapUtil.newHashMap(keys4, values4))
					.isInstanceOf(IllegalArgumentException.class);

	}
}
