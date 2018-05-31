package org.openecomp.sdc.be.dao.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

public class MapUtilTest {

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
		String[] keys = new String[] { "mock" };
		String[] values = new String[] { "mock" };
		Map<String, String> result;

		// test 1
		result = MapUtil.newHashMap(keys, values);
		//Assert.assertEquals(null, result);

		// test 2
		keys = new String[] { "mock" };
		values = null;
		try {
			result = MapUtil.newHashMap(keys, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// test 3
		values = null;
		keys = null;
		try {
			result = MapUtil.newHashMap(keys, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Assert.assertEquals(null, result);

		// test 4
		values = new String[] { "mock" };
		keys = null;
		try {
			result = MapUtil.newHashMap(keys, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}