package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;

public class NodeToscaArtifactsValidatorExecuterTest {

	private NodeToscaArtifactsValidatorExecuter createTestSubject() {
		return new NodeToscaArtifactsValidatorExecuter();
	}
	
	@Test(expected=NullPointerException.class)
	public void testExecuteValidations() throws Exception {
		NodeToscaArtifactsValidatorExecuter testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.executeValidations();
	}

	@Test
	public void testGetName() throws Exception {
		NodeToscaArtifactsValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		NodeToscaArtifactsValidatorExecuter testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}
}