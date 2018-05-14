package org.openecomp.sdc.be.components.impl.exceptions;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.exception.ResponseFormat;

public class ComponentExceptionTest {

	private ComponentException createTestSubject() {
		return new ComponentException(new ResponseFormat());
	}

	@Test
	public void testConstructor() throws Exception {
		new ComponentException(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, "mock", "moc");
	}
	
	@Test
	public void testGetResponseFormat() throws Exception {
		ComponentException testSubject;
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormat();
	}

	@Test
	public void testGetActionStatus() throws Exception {
		ComponentException testSubject;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getActionStatus();
	}

	@Test
	public void testGetParams() throws Exception {
		ComponentException testSubject;
		String[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParams();
	}
}