package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

public class VfArtifactValidationTaskTest {

	private VfArtifactValidationTask createTestSubject() {
		ArtifactValidationUtils artifactValidationUtilsMock = mock(ArtifactValidationUtils.class);
		return new VfArtifactValidationTask(artifactValidationUtilsMock);
	}

	@Test
	public void testValidate() throws Exception {
		VfArtifactValidationTask testSubject;
		GraphVertex vertex = null;
		VertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertex);
	}

}