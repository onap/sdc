package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;


public class ServiceValidatorExecuterTest {

	private ServiceValidatorExecuter createTestSubject() {
		return new ServiceValidatorExecuter();
	}

	


	
	@Test
	public void testGetName() throws Exception {
		ServiceValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}
}