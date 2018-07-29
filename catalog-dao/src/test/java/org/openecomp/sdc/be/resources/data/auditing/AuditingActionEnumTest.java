package org.openecomp.sdc.be.resources.data.auditing;

import org.junit.Test;

public class AuditingActionEnumTest {

	private AuditingActionEnum createTestSubject() {
		return AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
	}

	@Test
	public void testGetName() throws Exception {
		AuditingActionEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetAuditingEsType() throws Exception {
		AuditingActionEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditingEsType();
	}

	@Test
	public void testGetActionByName() throws Exception {
		String name = "";
		AuditingActionEnum result;

		// default test
		result = AuditingActionEnum.fromName(name);
	}
}