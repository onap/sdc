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

import java.util.Map;

import org.junit.Test;


public class RequirementImplDefTest {

	private RequirementImplDef createTestSubject() {
		return new RequirementImplDef();
	}

	
	@Test
	public void testGetNodeId() throws Exception {
		RequirementImplDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeId();
	}

	
	@Test
	public void testSetNodeId() throws Exception {
		RequirementImplDef testSubject;
		String nodeId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNodeId(nodeId);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		RequirementImplDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		RequirementImplDef testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetRequirementProperties() throws Exception {
		RequirementImplDef testSubject;
		Map<String, CapabiltyInstance> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementProperties();
	}

	
	@Test
	public void testSetRequirementProperties() throws Exception {
		RequirementImplDef testSubject;
		Map<String, CapabiltyInstance> requirementProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementProperties(requirementProperties);
	}

	
	@Test
	public void testGetPoint() throws Exception {
		RequirementImplDef testSubject;
		Point result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPoint();
	}

	
	@Test
	public void testSetPoint() throws Exception {
		RequirementImplDef testSubject;
		Point point = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPoint(point);
	}

	
	@Test
	public void testToString() throws Exception {
		RequirementImplDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
