package org.openecomp.sdc.be.model;

import java.util.List;

import org.junit.Test;


public class CapabilityDefinitionTest {

	private CapabilityDefinition createTestSubject() {
		return new CapabilityDefinition();
	}

	
	@Test
	public void testHashCode() throws Exception {
		CapabilityDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		CapabilityDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
	}

	
	@Test
	public void testToString() throws Exception {
		CapabilityDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetProperties() throws Exception {
		CapabilityDefinition testSubject;
		List<ComponentInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		CapabilityDefinition testSubject;
		List<ComponentInstanceProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}