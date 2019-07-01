package org.openecomp.sdc.asdctool.impl.validator.executers;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public class VFToscaArtifactValidatorExecutorTest {

	private VFToscaArtifactValidatorExecutor createTestSubject() {
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		return new VFToscaArtifactValidatorExecutor(janusGraphDaoMock, toscaOperationFacade);
	}

	@Test(expected=NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		VFToscaArtifactValidatorExecutor testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}

	@Test
	public void testGetName() throws Exception {
		VFToscaArtifactValidatorExecutor testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		VFToscaArtifactValidatorExecutor testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}
}