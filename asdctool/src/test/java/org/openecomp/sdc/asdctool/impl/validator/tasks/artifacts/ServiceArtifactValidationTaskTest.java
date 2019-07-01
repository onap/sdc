package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.testng.Assert;

public class ServiceArtifactValidationTaskTest {

	private ServiceArtifactValidationTask createTestSubject() {
		ArtifactValidationUtils artifactValidationUtilsMock = mock(ArtifactValidationUtils.class);
		return new ServiceArtifactValidationTask(artifactValidationUtilsMock);
	}

	@Test
	public void testValidate() throws Exception {
		ServiceArtifactValidationTask testSubject;
		GraphVertex vertex = null;
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertex);
	}

}