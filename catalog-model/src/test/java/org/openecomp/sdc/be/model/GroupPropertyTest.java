package org.openecomp.sdc.be.model;

import org.junit.Test;


public class GroupPropertyTest {

	private GroupProperty createTestSubject() {
		return new GroupProperty();
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