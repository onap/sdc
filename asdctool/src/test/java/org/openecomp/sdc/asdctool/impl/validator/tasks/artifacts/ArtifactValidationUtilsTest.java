package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;

import java.util.List;
import java.util.Map;

public class ArtifactValidationUtilsTest {

	private ArtifactValidationUtils createTestSubject() {
		return new ArtifactValidationUtils();
	}

	@Test(expected=NullPointerException.class)
	public void testValidateArtifactsAreInCassandra() throws Exception {
		ArtifactValidationUtils testSubject;
		GraphVertex vertex = null;
		String taskName = "";
		List<ArtifactDataDefinition> artifacts = null;
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateArtifactsAreInCassandra(vertex, taskName, artifacts);
	}

	@Test(expected=NullPointerException.class)
	public void testIsArtifcatInCassandra() throws Exception {
		ArtifactValidationUtils testSubject;
		String uniueId = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isArtifcatInCassandra(uniueId);
	}

	@Test
	public void testAddRelevantArtifacts() throws Exception {
		ArtifactValidationUtils testSubject;
		Map<String, ArtifactDataDefinition> artifactsMap = null;
		List<ArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addRelevantArtifacts(artifactsMap);
	}

	@Test(expected=NullPointerException.class)
	public void testValidateTopologyTemplateArtifacts() throws Exception {
		ArtifactValidationUtils testSubject;
		GraphVertex vertex = null;
		String taskName = "";
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateTopologyTemplateArtifacts(vertex, taskName);
	}
}