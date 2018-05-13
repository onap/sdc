package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditImportResourceAdminEventFactoryTest {

	private AuditImportResourceAdminEventFactory createTestSubject() {
		org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData.Builder newBuilder2 = ResourceAuditData.newBuilder();
		ResourceAuditData resAuData = newBuilder2.build();
		new AuditImportResourceAdminEventFactory(commonAuData, resAuData, resAuData, "", "", "", new User(), "");
		return new AuditImportResourceAdminEventFactory(commonAuData, resAuData,
				resAuData, "", "", "", new User(), "", "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditImportResourceAdminEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}