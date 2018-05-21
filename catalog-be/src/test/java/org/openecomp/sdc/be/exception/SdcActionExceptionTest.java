package org.openecomp.sdc.be.exception;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;

public class SdcActionExceptionTest {

	private SdcActionException createTestSubject() {
		return new SdcActionException(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED);
	}

	@Test
	public void testGetActionStatus() throws Exception {
		SdcActionException testSubject;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getActionStatus();
	}
}