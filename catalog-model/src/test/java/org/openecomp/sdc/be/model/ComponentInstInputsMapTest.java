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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentInstInputsMapTest {

	private ComponentInstInputsMap createTestSubject() {
		return new ComponentInstInputsMap();
	}

	@Test
	public void testComponentInstanceInputsMap() throws Exception {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = null;

		// default test
		testSubject.setComponentInstanceInputsMap(componentInstanceInputsMap);

		Map<String, List<ComponentInstancePropInput>> result = testSubject.getComponentInstanceInputsMap();
		assertEquals(0, result.size());
	}

	@Test
	public void testComponentInstanceProperties() throws Exception {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();
		testSubject.setComponentInstancePropertiesToPolicies(componentInstanceProperties);
		Map<String, List<ComponentInstancePropInput>> result = testSubject.getComponentInstanceProperties();
		assertEquals(0, result.size());
	}

	@Test
	public void testResolvePropertiesToDeclareEmpty() throws Exception {
		ComponentInstInputsMap testSubject;

		// default test
		testSubject = createTestSubject();
		try {
			testSubject.resolvePropertiesToDeclare();
		} catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
		}
	}

	@Test
	public void testResolvePropertiesToDeclare() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> inputs = new HashMap<>();
		inputs.put("test", new LinkedList<>());
		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceInputsMap(inputs);
		Pair<String, List<ComponentInstancePropInput>> result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());

		testSubject = createTestSubject();
		testSubject.setComponentInstancePropertiesToPolicies(inputs);
		result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());

		testSubject = createTestSubject();
		testSubject.setPolicyProperties(inputs);
		result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());

		testSubject = createTestSubject();
		testSubject.setComponentInstancePropInput(inputs);
		result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());

		testSubject = createTestSubject();
		testSubject.setServiceProperties(inputs);
		result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());

		testSubject = createTestSubject();
		testSubject.setGroupProperties(inputs);
		result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());

		testSubject = createTestSubject();
		testSubject.setComponentPropertiesToPolicies(inputs);
		result = testSubject.resolvePropertiesToDeclare();
		assertEquals(0, result.getValue().size());
	}
	
	@Test
	public void testPolicyProperties() {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> policyProperties = new HashMap<>();
		testSubject.setPolicyProperties(policyProperties);
		Map<String, List<ComponentInstancePropInput>> result = testSubject.getPolicyProperties();
		assertEquals(0, result.size());
	}

	@Test
	public void testServiceProperties() {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> serviceProperties = new HashMap<>();
		testSubject.setServiceProperties(serviceProperties);
		Map<String, List<ComponentInstancePropInput>> result = testSubject.getServiceProperties();
		assertEquals(0, result.size());
	}

	@Test
	public void testGroupProperties() {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> groupProperties = new HashMap<>();
		testSubject.setGroupProperties(groupProperties);
		Map<String, List<ComponentInstancePropInput>> result = testSubject.getGroupProperties();
		assertEquals(0, result.size());
	}

	@Test
	public void testComponentPropertiesToPolicies() {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> componentPropertiesToPolicies = new HashMap<>();
		testSubject.setComponentPropertiesToPolicies(componentPropertiesToPolicies);
		Map<String, List<ComponentInstancePropInput>> result = testSubject.getComponentPropertiesToPolicies();
		assertEquals(0, result.size());
	}

	@Test
	public void testComponentInstancePropertiesToPolicies() {
		ComponentInstInputsMap testSubject = createTestSubject();
		Map<String, List<ComponentInstancePropInput>> componentInstancePropertiesToPolicies = new HashMap<>();
		testSubject.setComponentInstancePropertiesToPolicies(componentInstancePropertiesToPolicies);
		Map<String, List<ComponentInstancePropInput>> result = testSubject.getComponentInstancePropertiesToPolicies();
		assertEquals(0, result.size());
	}
}
