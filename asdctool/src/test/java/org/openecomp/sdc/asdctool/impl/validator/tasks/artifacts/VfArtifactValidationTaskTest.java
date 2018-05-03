package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

public class VfArtifactValidationTaskTest {

	private VfArtifactValidationTask createTestSubject() {
		return new VfArtifactValidationTask();
	}

	@Test(expected=NullPointerException.class)
	public void testValidate() throws Exception {
		VfArtifactValidationTask testSubject;
		GraphVertex vertex = null;
		VertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertex);
	}

}