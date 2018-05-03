package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

public class ServiceArtifactValidationTaskTest {

	private ServiceArtifactValidationTask createTestSubject() {
		return new ServiceArtifactValidationTask();
	}

	@Test(expected=NullPointerException.class)
	public void testValidate() throws Exception {
		ServiceArtifactValidationTask testSubject;
		GraphVertex vertex = null;
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertex);
	}

}