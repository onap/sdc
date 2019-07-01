package org.openecomp.sdc.asdctool.impl.validator.executers;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;

public class VfValidatorExecuterTest {

	private VfValidatorExecuter createTestSubject() {
		List<VfValidationTask> validationTasks = new ArrayList<>();
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);

		return new VfValidatorExecuter(validationTasks, janusGraphDaoMock);
	}

	@Test
	public void testGetName() {
		VfValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test(expected=NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		VfValidatorExecuter testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}
}