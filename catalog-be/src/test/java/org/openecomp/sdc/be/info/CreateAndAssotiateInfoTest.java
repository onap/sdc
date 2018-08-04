package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;


public class CreateAndAssotiateInfoTest {

	private CreateAndAssotiateInfo createTestSubject() {
		return new CreateAndAssotiateInfo(new ComponentInstance(), new RequirementCapabilityRelDef());
	}

	
	@Test
	public void testGetNode() throws Exception {
		CreateAndAssotiateInfo testSubject;
		ComponentInstance result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		CreateAndAssotiateInfo testSubject;
		ComponentInstance node = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetAssociate() throws Exception {
		CreateAndAssotiateInfo testSubject;
		RequirementCapabilityRelDef result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAssociate();
	}

	
	@Test
	public void testSetAssociate() throws Exception {
		CreateAndAssotiateInfo testSubject;
		RequirementCapabilityRelDef associate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAssociate(associate);
	}
}