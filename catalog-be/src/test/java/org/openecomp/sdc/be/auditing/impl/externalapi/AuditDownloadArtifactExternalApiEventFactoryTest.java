package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditDownloadArtifactExternalApiEventFactoryTest {

	private AuditDownloadArtifactExternalApiEventFactory createTestSubject() {
		org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData.Builder newBuilder2 = ResourceAuditData.newBuilder();
		ResourceAuditData resAuData = newBuilder2.build();
		return new AuditDownloadArtifactExternalApiEventFactory(commonAuData, "", "", "", "",
				resAuData, resAuData, "", new User(), "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditDownloadArtifactExternalApiEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}