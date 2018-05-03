package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;

public class VFToscaArtifactValidatorExecutorTest {

	private VFToscaArtifactValidatorExecutor createTestSubject() {
		return new VFToscaArtifactValidatorExecutor();
	}

	@Test(expected=NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		VFToscaArtifactValidatorExecutor testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}

	@Test
	public void testGetName() throws Exception {
		VFToscaArtifactValidatorExecutor testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		VFToscaArtifactValidatorExecutor testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}
}