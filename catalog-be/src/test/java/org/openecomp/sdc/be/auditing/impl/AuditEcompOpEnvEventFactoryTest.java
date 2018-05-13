package org.openecomp.sdc.be.auditing.impl;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;

public class AuditEcompOpEnvEventFactoryTest {

	private AuditEcompOpEnvEventFactory createTestSubject() {
		return new AuditEcompOpEnvEventFactory(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, "", "", "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditEcompOpEnvEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditEcompOpEnvEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}