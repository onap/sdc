package org.openecomp.sdc.be.model;

import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;

public class PolicyDefinitionTest {

	private PolicyDefinition createTestSubject() {
		return new PolicyDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new PolicyDefinition(new HashMap<>());
		new PolicyDefinition(new PolicyDataDefinition());
		PolicyTypeDefinition policyType = new PolicyTypeDefinition();
		policyType.setProperties(new LinkedList<>());
		new PolicyDefinition(policyType);
	}
	
	@Test
	public void testGetNormalizedName() throws Exception {
		PolicyDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}
}