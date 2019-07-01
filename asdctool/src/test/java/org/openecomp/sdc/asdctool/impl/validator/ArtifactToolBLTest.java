package org.openecomp.sdc.asdctool.impl.validator;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.executers.NodeToscaArtifactsValidatorExecuter;

import java.util.LinkedList;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public class ArtifactToolBLTest {

	private ArtifactToolBL createTestSubject() {
		return new ArtifactToolBL(new ArrayList<>());
	}

	//Generated test
	@Test(expected=NullPointerException.class)
	public void testValidateAll() throws Exception {
		ArtifactToolBL testSubject;
		boolean result;

		// default test
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		testSubject = createTestSubject();
		testSubject.validators = new LinkedList();
		testSubject.validators.add(new NodeToscaArtifactsValidatorExecuter(janusGraphDaoMock,toscaOperationFacade));
		result = testSubject.validateAll();
	}
}