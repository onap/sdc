package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;

public class VfValidatorExecuterTest {

	private VfValidatorExecuter createTestSubject() {
		return new VfValidatorExecuter();
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