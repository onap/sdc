package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;

public class ServiceArtifactInfoImplTest {

	private ServiceArtifactInfoImpl createTestSubject() {
		return new ServiceArtifactInfoImpl();
	}

	@Test
	public void testToString() throws Exception {
		ServiceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}