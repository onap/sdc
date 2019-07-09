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

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;

import java.util.List;


public class ComponentInstanceInputTest {

	private ComponentInstanceInput createTestSubject() {
		return new ComponentInstanceInput();
	}

	@Test
	public void testCtor() throws Exception {
		new ComponentInstanceInput(new PropertyDataDefinition());
		new ComponentInstanceInput(new InputDefinition(), "mock", "mock");
		new ComponentInstanceInput(new PropertyDataDefinition(), "mock", "mock", "mock");
	}
	
	@Test
	public void testGetComponentInstanceName() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceName();
	}

	
	@Test
	public void testSetComponentInstanceName() throws Exception {
		ComponentInstanceInput testSubject;
		String componentInstanceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceName(componentInstanceName);
	}

	
	@Test
	public void testGetComponentInstanceId() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceId();
	}

	
	@Test
	public void testSetComponentInstanceId() throws Exception {
		ComponentInstanceInput testSubject;
		String componentInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceId(componentInstanceId);
	}

	
	@Test
	public void testGetValueUniqueUid() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueUniqueUid();
	}

	
	@Test
	public void testSetValueUniqueUid() throws Exception {
		ComponentInstanceInput testSubject;
		String valueUniqueUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValueUniqueUid(valueUniqueUid);
	}

	
	@Test
	public void testGetPath() throws Exception {
		ComponentInstanceInput testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPath();
	}

	
	@Test
	public void testSetPath() throws Exception {
		ComponentInstanceInput testSubject;
		List<String> path = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPath(path);
	}

	
	@Test
	public void testGetRules() throws Exception {
		ComponentInstanceInput testSubject;
		List<PropertyRule> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRules();
	}

	
	@Test
	public void testSetRules() throws Exception {
		ComponentInstanceInput testSubject;
		List<PropertyRule> rules = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRules(rules);
	}

	
	@Test
	public void testToString() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
