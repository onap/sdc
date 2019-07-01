package org.openecomp.sdc.asdctool.impl.validator;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceValidatorExecuter;

import java.util.LinkedList;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public class ValidationToolBLTest {

	private ValidationToolBL createTestSubject() {
		return new ValidationToolBL(new ArrayList<>());
	}

	@Test(expected=NullPointerException.class)
	public void testValidateAll() throws Exception {
		ValidationToolBL testSubject;
		boolean result;

		// default test
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		testSubject = createTestSubject();
		testSubject.validators = new LinkedList<>();
		testSubject.validators.add(new ServiceValidatorExecuter(janusGraphDaoMock));
		result = testSubject.validateAll();
	}
}