package org.openecomp.sdc.be.model;

import java.util.Map;

import org.junit.Test;


public class CapabilityTypeDefinitionTest {

	private CapabilityTypeDefinition createTestSubject() {
		return new CapabilityTypeDefinition();
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		CapabilityTypeDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		CapabilityTypeDefinition testSubject;
		String derivedFrom = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		CapabilityTypeDefinition testSubject;
		Map<String, PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		CapabilityTypeDefinition testSubject;
		Map<String, PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testToString() throws Exception {
		CapabilityTypeDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}