/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Utility class to ease map manipulation.
 */
public final class MapUtil {
	private MapUtil() {
	}

	/**
	 * Try to get a value following a path in the map. For example :
	 * MapUtil.get(map, "a.b.c") correspond to: map.get(a).get(b).get(c)
	 * 
	 * @param map
	 *            the map to search for path
	 * @param path
	 *            keys in the map separated by '.'
	 */
	public static Object get(Map<String, ? extends Object> map, String path) {
		String[] tokens = path.split("\\.");
		if (tokens.length == 0) {
			return null;
		} else {
			Object value = map;
			for (String token : tokens) {
				if (!(value instanceof Map)) {
					return null;
				} else {
					@SuppressWarnings("unchecked")
					Map<String, Object> nested = (Map<String, Object>) value;
					if (nested.containsKey(token)) {
						value = nested.get(token);
					} else {
						return null;
					}
				}
			}
			return value;
		}
	}

	/**
	 *
	 * @param valuesToMap the list of values to group
	 * @param groupingFunction the function to group the list values by
	 * @return a map of list of values grouped by a key, as specified in the {@code groupingFunction}
	 */
	public static <K,V> Map<K,List<V>> groupListBy(Collection<V> valuesToMap, Function<V,K> groupingFunction) {
		return valuesToMap.stream().collect(Collectors.groupingBy(groupingFunction));
	}

	/**
	 *
	 * @param valuesToMap list of values to map
	 * @param mappingFunction a function which specifies how to map each element on the list
	 * @return a map created by mapping each element from the {@code valuesToMap} as specified in the {@code mappingFunction}
	 */
	public static <K,V> Map<K,V> toMap(Collection<V> valuesToMap, Function<V,K> mappingFunction) {
		return Optional.ofNullable(valuesToMap).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(mappingFunction, Function.identity()));
	}

	/**
	 *
	 * @param map the map of which it keys to convert
	 * @param keyMappingFunction a function which converts the key object
	 * @return a map with converted keys.
	 */
	public static <K,U, V> Map<U, List<V>> convertMapKeys(Map<K, List<V>> map, Function<K,U> keyMappingFunction) {
		return map.entrySet().stream()
				.collect(Collectors.toMap(entry -> keyMappingFunction.apply(entry.getKey()),
										  Map.Entry::getValue));
	}

	/**
	 * Create a new hash map and fills it from the given keys and values
	 * (keys[index] -> values[index].
	 * 
	 * @param keys
	 *            The array of keys.
	 * @param values
	 *            The array of values.
	 * @return A map that contains for each key element in the keys array a
	 *         value from the values array at the same index.
	 */
	public static <K, V> Map<K, V> newHashMap(K[] keys, V[] values) {
		Map<K, V> map = new HashMap<K, V>();
		if (keys == null || values == null || keys.length != values.length) {
			throw new IllegalArgumentException("keys and values must be non-null and have the same size.");
		}
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		return map;
	}
}
