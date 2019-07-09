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
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;

public class CapabilityRequirementRelationshipTest {

	private CapabilityRequirementRelationship createTestSubject() {
		return new CapabilityRequirementRelationship();
	}

	@Test
	public void testGetRelation() throws Exception {
		CapabilityRequirementRelationship testSubject;
		RelationshipInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelation();
	}

	@Test
	public void testSetRelation() throws Exception {
		CapabilityRequirementRelationship testSubject;
		RelationshipInfo relation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelation(relation);
	}

	@Test
	public void testGetCapability() throws Exception {
		CapabilityRequirementRelationship testSubject;
		CapabilityDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	@Test
	public void testSetCapability() throws Exception {
		CapabilityRequirementRelationship testSubject;
		CapabilityDataDefinition capability = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	@Test
	public void testGetRequirement() throws Exception {
		CapabilityRequirementRelationship testSubject;
		RequirementDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirement();
	}

	@Test
	public void testSetRequirement() throws Exception {
		CapabilityRequirementRelationship testSubject;
		RequirementDataDefinition requirement = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirement(requirement);
	}

}
