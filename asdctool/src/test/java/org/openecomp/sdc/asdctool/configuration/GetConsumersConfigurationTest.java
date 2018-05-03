package org.openecomp.sdc.asdctool.configuration;

import org.junit.Test;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;

public class GetConsumersConfigurationTest {

	private GetConsumersConfiguration createTestSubject() {
		return new GetConsumersConfiguration();
	}

	@Test
	public void testConsumerOperation() throws Exception {
		GetConsumersConfiguration testSubject;
		TitanGenericDao titanGenericDao = null;
		ConsumerOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.consumerOperation(titanGenericDao);
	}
}