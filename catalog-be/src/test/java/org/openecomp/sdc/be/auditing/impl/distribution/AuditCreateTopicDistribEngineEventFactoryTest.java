package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public class AuditCreateTopicDistribEngineEventFactoryTest {

	private AuditCreateTopicDistributionEngineEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditCreateTopicDistributionEngineEventFactory(build,DistributionTopicData.newBuilder().build(),"", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditCreateTopicDistributionEngineEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}
}