package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetUsersListEventFactoryTest {

	private AuditGetUsersListEventFactory createTestSubject() {
		org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		return new AuditGetUsersListEventFactory(commonAuData, new User(), "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditGetUsersListEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditGetUsersListEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}