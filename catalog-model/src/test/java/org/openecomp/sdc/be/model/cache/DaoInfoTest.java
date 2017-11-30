package org.openecomp.sdc.be.model.cache;

import org.junit.Test;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;


public class DaoInfoTest {

	private DaoInfo createTestSubject() {
		return new DaoInfo(new ToscaOperationFacade(), new ComponentCache());
	}

	
	@Test
	public void testGetToscaOperationFacade() throws Exception {
		DaoInfo testSubject;
		ToscaOperationFacade result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaOperationFacade();
	}

	
	@Test
	public void testGetComponentCache() throws Exception {
		DaoInfo testSubject;
		ComponentCache result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentCache();
	}
}