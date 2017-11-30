package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;


public class VfValidatorExecuterTest {

	private VfValidatorExecuter createTestSubject() {
		return new VfValidatorExecuter();
	}


	
	@Test
	public void testGetName() throws Exception {
		VfValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}
}