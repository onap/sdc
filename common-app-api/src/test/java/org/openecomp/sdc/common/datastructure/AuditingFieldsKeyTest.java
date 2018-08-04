package org.openecomp.sdc.common.datastructure;

import org.junit.Test;


public class AuditingFieldsKeyTest {

	private AuditingFieldsKey createTestSubject() {
		return AuditingFieldsKey.AUDIT_ACTION;
	}

	
	@Test
	public void testGetValueClass() throws Exception {
		AuditingFieldsKey testSubject;
		Class<?> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueClass();
	}

	
	@Test
	public void testGetDisplayName() throws Exception {
		AuditingFieldsKey testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayName();
	}

	
	@Test
	public void testSetDisplayName() throws Exception {
		AuditingFieldsKey testSubject;
		String displayName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDisplayName(displayName);
	}
}