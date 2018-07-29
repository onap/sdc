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