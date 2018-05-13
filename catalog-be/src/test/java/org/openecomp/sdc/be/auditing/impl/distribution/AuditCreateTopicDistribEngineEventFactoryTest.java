package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

public class AuditCreateTopicDistribEngineEventFactoryTest {

	private AuditCreateTopicDistribEngineEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditCreateTopicDistribEngineEventFactory(build, "", "", "", "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditCreateTopicDistribEngineEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}