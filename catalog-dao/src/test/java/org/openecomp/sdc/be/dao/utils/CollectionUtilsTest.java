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

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CollectionUtilsTest {

	@Test
	public void testMerge() throws Exception {
		Set<T> source = new HashSet<>();
		Set<T> target = new HashSet<>();
		assertNull(CollectionUtils.merge(source, target));

		T t = null;
		source.add(t);
		assertNotNull(CollectionUtils.merge(source, target));
	}

	@Test
	public void testMerge_1() throws Exception {
		Map<String, String> source = new HashMap();
		Map<String, String> target = new HashMap();

		source.put("key", "value");
		target.put("key", "value2");
		assertEquals("value2", CollectionUtils.merge(source, target, false).get("key"));
		assertEquals("value", CollectionUtils.merge(source, target, true).get("key"));
	}

	@Test
	public void testMerge_2() throws Exception {
		List<String> source = null;
		List<String> target = null;
		assertEquals(0, CollectionUtils.merge(source, target).size());

		source = new LinkedList<>();
		target = new LinkedList<>();
		assertEquals(0, CollectionUtils.merge(source, target).size());

		source.add("test1");
		target.add("test2");
		assertEquals("test2", CollectionUtils.merge(source, target).get(0));
		assertEquals("test1", CollectionUtils.merge(source, target).get(1));
	}

	@Test
	public void testUnion() throws Exception {
		List<String> source = new LinkedList<>();
		List<String> target = new LinkedList<>();

		source.add("test1");
		target.add("test2");
		assertEquals("test1", CollectionUtils.union(source, target).get(0));
		assertEquals("test2", CollectionUtils.union(source, target).get(1));
	}
}
