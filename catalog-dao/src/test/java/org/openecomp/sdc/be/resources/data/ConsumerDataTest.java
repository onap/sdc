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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class ConsumerDataTest {

	private ConsumerData createTestSubject() {
		return new ConsumerData();
	}

	@Test
	public void testCtor() throws Exception {
		new ConsumerData(new ConsumerDataDefinition());
		new ConsumerData(new HashMap<>());
	}
	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ConsumerData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ConsumerData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetConsumerDataDefinition() throws Exception {
		ConsumerData testSubject;
		ConsumerDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerDataDefinition();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ConsumerData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		ConsumerData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
