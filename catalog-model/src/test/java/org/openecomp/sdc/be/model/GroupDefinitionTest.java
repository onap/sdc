package org.openecomp.sdc.be.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public class GroupDefinitionTest {

	private GroupDefinition createTestSubject() {
		return new GroupDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupDefinition(new GroupDefinition());
		new GroupDefinition(new GroupDataDefinition());
	}
	
	@Test
	public void testConvertToGroupProperties() throws Exception {
		GroupDefinition testSubject;
		List<GroupProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToGroupProperties();
		List<PropertyDataDefinition> properties = new LinkedList<>();
		properties.add(new PropertyDataDefinition());
		testSubject.setProperties(properties);
		result = testSubject.convertToGroupProperties();
	}

	
	@Test
	public void testConvertFromGroupProperties() throws Exception {
		GroupDefinition testSubject;
		List<GroupProperty> properties = null;

		// test 1
		testSubject = createTestSubject();
		testSubject.convertFromGroupProperties(properties);
		properties = new LinkedList<>();
		properties.add(new GroupProperty());
		testSubject.convertFromGroupProperties(properties);
	}

	
	@Test
	public void testIsSamePrefix() throws Exception {
		GroupDefinition testSubject;
		String resourceName = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isSamePrefix(resourceName);
		testSubject.setName("mock");
		result = testSubject.isSamePrefix("mock");
	}
}