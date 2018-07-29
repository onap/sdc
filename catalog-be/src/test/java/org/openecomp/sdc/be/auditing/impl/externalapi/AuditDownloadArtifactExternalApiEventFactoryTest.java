package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;


public class AuditDownloadArtifactExternalApiEventFactoryTest {

	private AuditDownloadArtifactExternalApiEventFactory createTestSubject() {
		CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		ResourceVersionInfo.Builder newBuilder2 = ResourceVersionInfo.newBuilder();
		ResourceVersionInfo resAuData = newBuilder2.build();
		return new AuditDownloadArtifactExternalApiEventFactory(commonAuData,new ResourceCommonInfo(),new DistributionData("", ""),resAuData, new User());
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