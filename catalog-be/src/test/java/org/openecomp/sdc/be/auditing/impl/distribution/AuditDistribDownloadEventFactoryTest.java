package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;

public class AuditDistribDownloadEventFactoryTest {

	private AuditDistribDownloadEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditDistribDownloadEventFactory(build, new DistributionData("", ""));
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditDistribDownloadEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditDistribDownloadEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}