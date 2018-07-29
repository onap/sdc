package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditCertificationResourceAdminEventFactoryTest {

	private AuditCertificationResourceAdminEventFactory createTestSubject() {
		CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData commonAuData = newBuilder.build();
		ResourceVersionInfo.Builder newBuilder2 = ResourceVersionInfo.newBuilder();
		ResourceVersionInfo resAuData = newBuilder2.build();
		return new AuditCertificationResourceAdminEventFactory(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, commonAuData,new ResourceCommonInfo(),
				resAuData, resAuData, "",  new User(), "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditCertificationResourceAdminEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}