package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditChangeLifecycleExternalApiEventFactoryTest {

	private AuditChangeLifecycleExternalApiEventFactory createTestSubject() {
		org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData.Builder newBuilder2 = ResourceAuditData.newBuilder();
		ResourceAuditData resAuData = newBuilder2.build();
		return new AuditChangeLifecycleExternalApiEventFactory(commonAuData, "", "", "", "",
				resAuData, resAuData, "", new User(), "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditChangeLifecycleExternalApiEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}