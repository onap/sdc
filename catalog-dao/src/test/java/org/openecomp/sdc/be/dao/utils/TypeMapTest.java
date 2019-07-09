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

import org.junit.Test;

public class TypeMapTest {

	private TypeMap createTestSubject() {
		return new TypeMap();
	}

	@Test
	public void testPut() throws Exception {
		TypeMap testSubject;
		String key = "mock";
		Object value = new Object();

		// default test
		testSubject = createTestSubject();
		testSubject.put(key, value);
	}

	@Test
	public void testGet() throws Exception {
		TypeMap testSubject;
		Class clazz = Object.class;
		String key = "mock";
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.get(clazz, key);
	}
}
