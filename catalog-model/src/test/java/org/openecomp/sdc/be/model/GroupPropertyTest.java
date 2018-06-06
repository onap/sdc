package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public class GroupPropertyTest {

	private GroupProperty createTestSubject() {
		return new GroupProperty();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupProperty(new GroupProperty());
		new GroupProperty(new PropertyDataDefinition());
		new GroupProperty(new PropertyDefinition(), "mock", "mock");
	}
	
	@Test
	public void testGetValueUniqueUid() throws Exception {
		GroupProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueUniqueUid();
	}

	
	@Test
	public void testSetValueUniqueUid() throws Exception {
		GroupProperty testSubject;
		String valueUniqueUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValueUniqueUid(valueUniqueUid);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}