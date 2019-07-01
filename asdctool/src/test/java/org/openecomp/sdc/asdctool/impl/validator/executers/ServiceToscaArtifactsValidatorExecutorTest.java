package org.openecomp.sdc.asdctool.impl.validator.executers;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public class ServiceToscaArtifactsValidatorExecutorTest {

	private ServiceToscaArtifactsValidatorExecutor createTestSubject() {
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		return new ServiceToscaArtifactsValidatorExecutor(janusGraphDaoMock, toscaOperationFacade);
	}

	@Test(expected = NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		ServiceToscaArtifactsValidatorExecutor testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}

	@Test
	public void testGetName() throws Exception {
		ServiceToscaArtifactsValidatorExecutor testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		ServiceToscaArtifactsValidatorExecutor testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}
}