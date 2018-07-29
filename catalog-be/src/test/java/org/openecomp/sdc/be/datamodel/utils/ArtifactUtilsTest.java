package org.openecomp.sdc.be.datamodel.utils;

import org.junit.Test;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArtifactUtilsTest {

	private ArtifactUtils createTestSubject() {
		return new ArtifactUtils();
	}

	@Test
	public void testFindMasterArtifact() throws Exception {
		Map<String, ArtifactDefinition> deplymentArtifact = new HashMap<>();
		List<ArtifactDefinition> artifacts = new LinkedList<>();
		List<String> artifactsList = new LinkedList<>();
		ArtifactDefinition result;

		// default test
		result = ArtifactUtils.findMasterArtifact(deplymentArtifact, artifacts, artifactsList);
	}

	@Test
	public void testBuildJsonForUpdateArtifact() throws Exception {
		String artifactId = "";
		String artifactName = "";
		String artifactType = "";
		ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.DEPLOYMENT;
		String label = "";
		String displayName = "";
		String description = "";
		byte[] artifactContentent = new byte[] { ' ' };
		List<ArtifactTemplateInfo> updatedRequiredArtifacts = null;
		boolean isFromCsar = false;
		Map<String, Object> result;

		// test 1
		artifactId = null;
		result = ArtifactUtils.buildJsonForUpdateArtifact(artifactId, artifactName, artifactType, artifactGroupType,
				label, displayName, description, artifactContentent, updatedRequiredArtifacts, isFromCsar);
	}

	@Test
	public void testBuildJsonForArtifact() throws Exception {
		ArtifactTemplateInfo artifactTemplateInfo = new ArtifactTemplateInfo();
		artifactTemplateInfo.setFileName("mock.mock.heat");
		byte[] artifactContentent = new byte[] { ' ' };
		int atrifactLabelCounter = 0;
		Map<String, Object> result;

		// default test
		result = ArtifactUtils.buildJsonForArtifact(artifactTemplateInfo, artifactContentent, atrifactLabelCounter,false);
	}

	@Test
	public void testFindArtifactInList() throws Exception {
		List<ArtifactDefinition> createdArtifacts = new LinkedList<>();
		String artifactId = "mock";
		ArtifactDefinition result;

		// default test
		result = ArtifactUtils.findArtifactInList(createdArtifacts, artifactId);
	}
}