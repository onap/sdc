package org.openecomp.sdc.be.auditing.impl;

import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

public class AuditConsumerEventFactoryTest {

	private AuditConsumerEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditConsumerEventFactory(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, build, new User(),
				new ConsumerDefinition());
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditConsumerEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditConsumerEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testBuildConsumerName() throws Exception {
		ConsumerDefinition consumer = null;
		String result;

		// test 1
		consumer = null;
		result = Deencapsulation.invoke(AuditConsumerEventFactory.class, "buildConsumerName",
				new Object[] { ConsumerDefinition.class });
		Assert.assertEquals("", result);
	}
}