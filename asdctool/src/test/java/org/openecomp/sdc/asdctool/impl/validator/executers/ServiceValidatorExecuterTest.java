package org.openecomp.sdc.asdctool.impl.validator.executers;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;

public class ServiceValidatorExecuterTest {

	private ServiceValidatorExecuter createTestSubject() {
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		return new ServiceValidatorExecuter(janusGraphDaoMock);
	}

	@Test
	public void testGetName() {
		ServiceValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test(expected=NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		ServiceValidatorExecuter testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}
}