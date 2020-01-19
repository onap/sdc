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

package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ComponentInstInputsMapTest {

	private ComponentInstInputsMap createTestSubject() {
		return new ComponentInstInputsMap();
	}

	@Test
	public void testGetComponentInstanceInputsMap() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceInputsMap();
	}

	@Test
	public void testSetComponentInstanceInputsMap() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceInputsMap(componentInstanceInputsMap);
	}

	@Test
	public void testGetComponentInstanceProperties() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceProperties();
	}

	@Test
	public void testSetComponentInstancePropInput() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstancePropertiesToPolicies(componentInstanceProperties);
	}

	@Test
	public void testResolvePropertiesToDeclareEmpty() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		// default test
		testSubject = createTestSubject();
		try {
			testSubject.resolvePropertiesToDeclare();
		} catch (Exception e) {
            Assert.assertTrue(e.getClass() == IllegalStateException.class);
		}
	}

	@Test
	public void testResolvePropertiesToDeclare() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		Map<String, List<ComponentInstancePropInput>> inputs = new HashMap<>();
		inputs.put("mock", new LinkedList<>());
		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceInputsMap(inputs);
		testSubject.resolvePropertiesToDeclare();
		testSubject = createTestSubject();
		testSubject.setComponentInstancePropertiesToPolicies(inputs);
		testSubject.resolvePropertiesToDeclare();
		testSubject = createTestSubject();
		testSubject.setPolicyProperties(inputs);
		testSubject.resolvePropertiesToDeclare();
	}
	
	@Test
	public void testGetPolicyProperties() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.getPolicyProperties();
	}
}
