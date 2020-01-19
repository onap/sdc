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

import java.util.Map;


public class SubstitutionMappingTest {

	private SubstitutionMapping createTestSubject() {
		return new SubstitutionMapping();
	}

	
	@Test
	public void testGetNode_type() throws Exception {
		SubstitutionMapping testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode_type();
	}

	
	@Test
	public void testSetNode_type() throws Exception {
		SubstitutionMapping testSubject;
		String node_type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode_type(node_type);
	}

	
	@Test
	public void testGetCapabilities() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}
}
