package org.openecomp.sdc.be.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;


public class CapabilityDefinitionTest {

	private CapabilityDefinition createTestSubject() {
		return new CapabilityDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		CapabilityDefinition other = new CapabilityDefinition();
		new CapabilityDefinition(other);
		other.setProperties(new LinkedList<>());
		new CapabilityDefinition(other);
		new CapabilityDefinition(new CapabilityDataDefinition());
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
		result = testSubject.equals(new Object());
		result = testSubject.equals(testSubject);
		CapabilityDefinition createTestSubject = createTestSubject();
		result = testSubject.equals(createTestSubject);
		createTestSubject.setProperties(new LinkedList<>());
		result = testSubject.equals(createTestSubject);
		testSubject.setProperties(new LinkedList<>());
		result = testSubject.equals(createTestSubject);
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