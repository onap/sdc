package org.openecomp.sdc.common.datastructure;

import javax.annotation.Generated;

import org.junit.Test;


public class AuditingFieldsKeysEnumTest {

	private AuditingFieldsKeysEnum createTestSubject() {
		return AuditingFieldsKeysEnum.AUDIT_ACTION;
	}

	
	@Test
	public void testGetValueClass() throws Exception {
		AuditingFieldsKeysEnum testSubject;
		Class<?> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueClass();
	}

	
	@Test
	public void testGetDisplayName() throws Exception {
		AuditingFieldsKeysEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayName();
	}

	
	@Test
	public void testSetDisplayName() throws Exception {
		AuditingFieldsKeysEnum testSubject;
		String displayName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDisplayName(displayName);
	}
}