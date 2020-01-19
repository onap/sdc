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

package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;

import java.util.List;
import java.util.Map;


public class ToscaNodeTypeTest {

	private ToscaNodeType createTestSubject() {
		return new ToscaNodeType();
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaNodeType testSubject;
		Map<String, ToscaProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaNodeType testSubject;
		Map<String, ToscaProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testGetCapabilities() throws Exception {
		ToscaNodeType testSubject;
		Map<String, ToscaCapability> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		ToscaNodeType testSubject;
		Map<String, ToscaCapability> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		ToscaNodeType testSubject;
		List<Map<String, ToscaRequirement>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		ToscaNodeType testSubject;
		List<Map<String, ToscaRequirement>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}

	
	@Test
	public void testGetDerived_from() throws Exception {
		ToscaNodeType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerived_from();
	}

	
	@Test
	public void testSetDerived_from() throws Exception {
		ToscaNodeType testSubject;
		String derived_from = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDerived_from(derived_from);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ToscaNodeType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ToscaNodeType testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		ToscaNodeType testSubject;
		ToscaMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		ToscaNodeType testSubject;
		ToscaMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}
}
