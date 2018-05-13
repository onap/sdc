package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;

public class AuditDistribNotificationEventFactoryTest {

	private AuditDistribNotificationEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditDistribNotificationEventFactory(build, "", "", "", new User(), "", "", "",
				new OperationalEnvAuditData("", "", ""));
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditDistribNotificationEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditDistribNotificationEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}