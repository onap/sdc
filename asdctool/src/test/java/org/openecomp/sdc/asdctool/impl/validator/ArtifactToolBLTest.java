package org.openecomp.sdc.asdctool.impl.validator;

import java.util.LinkedList;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.executers.NodeToscaArtifactsValidatorExecuter;

public class ArtifactToolBLTest {

	private ArtifactToolBL createTestSubject() {
		return new ArtifactToolBL();
	}

	//Generated test
	@Test(expected=NullPointerException.class)
	public void testValidateAll() throws Exception {
		ArtifactToolBL testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		testSubject.validators = new LinkedList();
		testSubject.validators.add(new NodeToscaArtifactsValidatorExecuter());
		result = testSubject.validateAll();
	}
}