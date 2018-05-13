package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

public class AuditDistribDeployEventFactoryTest {

	private AuditDistribDeployEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditDistribDeployEventFactory(build, "", "", new User(), "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditDistribDeployEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditDistribDeployEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}