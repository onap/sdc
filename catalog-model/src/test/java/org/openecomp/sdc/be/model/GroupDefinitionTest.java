package org.openecomp.sdc.be.model;

import java.util.List;

import org.junit.Test;


public class GroupDefinitionTest {

	private GroupDefinition createTestSubject() {
		return new GroupDefinition();
	}

	
	@Test
	public void testConvertToGroupProperties() throws Exception {
		GroupDefinition testSubject;
		List<GroupProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToGroupProperties();
	}

	
	@Test
	public void testConvertFromGroupProperties() throws Exception {
		GroupDefinition testSubject;
		List<GroupProperty> properties = null;

		// test 1
		testSubject = createTestSubject();
		properties = null;
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
	}
}