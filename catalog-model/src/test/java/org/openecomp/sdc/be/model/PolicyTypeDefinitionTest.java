package org.openecomp.sdc.be.model;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class PolicyTypeDefinitionTest {

	private PolicyTypeDefinition createTestSubject() {
		return new PolicyTypeDefinition();
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