package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditArtifactResourceAdminEventFactoryTest {

	private AuditArtifactResourceAdminEventFactory createTestSubject() {
		org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData.Builder newBuilder2 = ResourceAuditData.newBuilder();
		ResourceAuditData resAuData = newBuilder2.build();
		return new AuditArtifactResourceAdminEventFactory(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, commonAuData,
				resAuData, resAuData, "", "", "", new User(), "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditArtifactResourceAdminEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}