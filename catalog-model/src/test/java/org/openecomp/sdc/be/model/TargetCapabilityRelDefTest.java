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

import java.util.LinkedList;
import java.util.List;

public class TargetCapabilityRelDefTest {

	private TargetCapabilityRelDef createTestSubject() {
		return new TargetCapabilityRelDef();
	}

	@Test
	public void testCtor() throws Exception {
		new TargetCapabilityRelDef("mock", new LinkedList<>());
	}
	
	@Test
	public void testGetToNode() throws Exception {
		TargetCapabilityRelDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToNode();
	}

	@Test
	public void testSetToNode() throws Exception {
		TargetCapabilityRelDef testSubject;
		String toNode = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToNode(toNode);
	}

	@Test
	public void testGetRelationships() throws Exception {
		TargetCapabilityRelDef testSubject;
		List<CapabilityRequirementRelationship> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationships();
	}

	@Test
	public void testResolveSingleRelationship() throws Exception {
		TargetCapabilityRelDef testSubject;
		CapabilityRequirementRelationship result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.resolveSingleRelationship();
	}

	@Test
	public void testGetUid() throws Exception {
		TargetCapabilityRelDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUid();
	}

	@Test
	public void testSetUid() throws Exception {
		TargetCapabilityRelDef testSubject;
		String uid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUid(uid);
	}

	@Test
	public void testSetRelationships() throws Exception {
		TargetCapabilityRelDef testSubject;
		List<CapabilityRequirementRelationship> relationships = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationships(relationships);
	}

	@Test
	public void testToString() throws Exception {
		TargetCapabilityRelDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
