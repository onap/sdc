package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class GroupInstancePropertyTest {

	private GroupInstanceProperty createTestSubject() {
		return new GroupInstanceProperty();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupInstanceProperty(new GroupInstanceProperty());
		new GroupInstanceProperty(new PropertyDataDefinition());
		new GroupInstanceProperty(new GroupInstanceProperty(), "mock");
	}
	
	@Test
	public void testGetParentValue() throws Exception {
		GroupInstanceProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentValue();
	}

	@Test
	public void testSetParentValue() throws Exception {
		GroupInstanceProperty testSubject;
		String parentValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentValue(parentValue);
	}
}