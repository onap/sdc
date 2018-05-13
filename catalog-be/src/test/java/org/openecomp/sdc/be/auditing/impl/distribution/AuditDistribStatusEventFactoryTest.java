package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

public class AuditDistribStatusEventFactoryTest {

	private AuditDistribStatusEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditDistribStatusEventFactory(build, "", "", "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditDistribStatusEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditDistribStatusEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}