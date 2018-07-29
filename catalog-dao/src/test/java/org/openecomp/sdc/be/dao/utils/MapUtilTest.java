package org.openecomp.sdc.be.dao.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
		Map<String, ? extends Object> map = null;
		String path = "";
		Object result;

		// default test
		result = MapUtil.get(map, path);
		path = "\\mock\\mock";
		result = MapUtil.get(map, path);
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