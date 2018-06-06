package org.openecomp.sdc.be.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;


public class GroupTypeDefinitionTest {

	private GroupTypeDefinition createTestSubject() {
		return new GroupTypeDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupTypeDefinition(new GroupTypeDataDefinition());
	}
	
	@Test
	public void testGetProperties() throws Exception {
		GroupTypeDefinition testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		GroupTypeDefinition testSubject;
		List<PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	@Test
	public void testGetCapabilityTypes() throws Exception {
		GroupTypeDefinition testSubject;
		List<PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.getCapabilityTypes();
	}
	
	@Test
	public void testSetCapabilityTypes() throws Exception {
		GroupTypeDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityTypes(new LinkedList<>());
	}
	
	@Test
	public void testToString() throws Exception {
		GroupTypeDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}