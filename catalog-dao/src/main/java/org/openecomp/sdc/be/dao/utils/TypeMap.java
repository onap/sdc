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

import java.util.HashMap;
import java.util.Map;

public class TypeMap {
	private Map<Class<? extends Object>, Map<String, Object>> cacheMap = new HashMap<Class<? extends Object>, Map<String, Object>>();

	private Map<String, Object> getMap(Class<? extends Object> clazz) {
		Map<String, Object> map = cacheMap.get(clazz);
		if (map == null) {
			cacheMap.put(clazz, new HashMap<String, Object>());
		}
		return cacheMap.get(clazz);
	}

	/**
	 * put an object (value) in it's type map using the given key.
	 * 
	 * @param key
	 *            The key inside the type map.
	 * @param value
	 *            The object to insert (based on it's type and the given key).
	 */
	public void put(String key, Object value) {
		getMap(value.getClass()).put(key, value);
	}

	/**
	 * Get the cached object based on it's type and key.
	 * 
	 * @param clazz
	 *            The object's type.
	 * @param key
	 *            The object key.
	 * @return The object that match the given type and key or null if none
	 *         matches.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz, String key) {
		return (T) (cacheMap.get(clazz) == null ? null : cacheMap.get(clazz).get(key));
	}
}
