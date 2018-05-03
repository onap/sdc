package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;

public class ServiceToscaArtifactsValidatorExecutorTest {

	private ServiceToscaArtifactsValidatorExecutor createTestSubject() {
		return new ServiceToscaArtifactsValidatorExecutor();
	}

	@Test(expected = NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		ServiceToscaArtifactsValidatorExecutor testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}

	@Test
	public void testGetName() throws Exception {
		ServiceToscaArtifactsValidatorExecutor testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		ServiceToscaArtifactsValidatorExecutor testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}
}