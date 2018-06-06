package org.openecomp.sdc.be.model;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;


public class PolicyTypeDefinitionTest {

	private PolicyTypeDefinition createTestSubject() {
		return new PolicyTypeDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new PolicyTypeDefinition(new PolicyTypeDataDefinition());
	}
	
	@Test
	public void testGetProperties() throws Exception {
		PolicyTypeDefinition testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		PolicyTypeDefinition testSubject;
		List<PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testToString() throws Exception {
		PolicyTypeDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}