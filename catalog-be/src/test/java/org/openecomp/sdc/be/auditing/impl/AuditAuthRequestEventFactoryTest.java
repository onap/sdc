package org.openecomp.sdc.be.auditing.impl;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

public class AuditAuthRequestEventFactoryTest {

	private AuditAuthRequestEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditAuthRequestEventFactory(build, "mock", "mock", "mock", "mock");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditAuthRequestEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditAuthRequestEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}