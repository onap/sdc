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
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class ComponentInstanceDataTest {

	private ComponentInstanceData createTestSubject() {
		return new ComponentInstanceData();
	}

	@Test
	public void testCtor() throws Exception {
		new ComponentInstanceData(new ComponentInstanceDataDefinition());
		new ComponentInstanceData(new ComponentInstanceDataDefinition(), new Integer(0));
		new ComponentInstanceData(new HashMap<>());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		ComponentInstanceData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetName() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetComponentInstDataDefinition() throws Exception {
		ComponentInstanceData testSubject;
		ComponentInstanceDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstDataDefinition();
	}

	
	@Test
	public void testSetComponentInstDataDefinition() throws Exception {
		ComponentInstanceData testSubject;
		ComponentInstanceDataDefinition componentInstDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstDataDefinition(componentInstDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetGroupInstanceCounter() throws Exception {
		ComponentInstanceData testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupInstanceCounter();
	}

	
	@Test
	public void testSetGroupInstanceCounter() throws Exception {
		ComponentInstanceData testSubject;
		Integer componentInstanceCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupInstanceCounter(componentInstanceCounter);
	}

	
	@Test
	public void testIncreaseAndGetGroupInstanceCounter() throws Exception {
		ComponentInstanceData testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.increaseAndGetGroupInstanceCounter();
	}
}
