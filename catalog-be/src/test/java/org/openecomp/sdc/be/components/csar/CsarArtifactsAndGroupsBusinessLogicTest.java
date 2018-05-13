package org.openecomp.sdc.be.components.csar;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CsarInfo;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class CsarArtifactsAndGroupsBusinessLogicTest {

	private CsarArtifactsAndGroupsBusinessLogic createTestSubject() {
		return new CsarArtifactsAndGroupsBusinessLogic();
	}

	@Test
	public void testCreateOrUpdateCsarArtifactFromJson() throws Exception {
		CsarArtifactsAndGroupsBusinessLogic testSubject;
		Resource resource = null;
		User user = null;
		Map<String, Object> json = null;
		ArtifactOperationInfo operation = null;
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createOrUpdateCsarArtifactFromJson(resource, user, json, operation);
	}

	@Test
	public void testCreateResourceArtifactsFromCsar() throws Exception {
		CsarArtifactsAndGroupsBusinessLogic testSubject;
		CsarInfo csarInfo = null;
		Resource resource = null;
		String artifactsMetaFile = "";
		String artifactsMetaFileName = "";
		List<ArtifactDefinition> createdArtifacts = null;
		boolean shouldLock = false;
		boolean inTransaction = false;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createResourceArtifactsFromCsar(csarInfo, resource, artifactsMetaFile,
				artifactsMetaFileName, createdArtifacts, shouldLock, inTransaction);
	}

	@Test
	public void testCreateVfModuleAdditionalProperties() throws Exception {
		CsarArtifactsAndGroupsBusinessLogic testSubject;
		boolean isBase = false;
		String moduleName = "";
		List<GroupProperty> properties = null;
		List<ArtifactDefinition> deploymentArtifacts = null;
		List<String> artifactsInGroup = null;
		GroupTypeDefinition groupType = null;
		List<GroupProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createVfModuleAdditionalProperties(isBase, moduleName, properties, deploymentArtifacts,
				artifactsInGroup, groupType);
	}

	@Test
	public void testDeleteVFModules() throws Exception {
		CsarArtifactsAndGroupsBusinessLogic testSubject;
		Resource resource = null;
		CsarInfo csarInfo = null;
		boolean shouldLock = false;
		boolean inTransaction = false;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteVFModules(resource, csarInfo, shouldLock, inTransaction);
	}

	@Test
	public void testParseResourceArtifactsInfoFromFile() throws Exception {
		CsarArtifactsAndGroupsBusinessLogic testSubject;
		Resource resource = null;
		String artifactsMetaFile = "";
		String artifactFileName = "";
		User user = null;
		Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.parseResourceArtifactsInfoFromFile(resource, artifactsMetaFile, artifactFileName, user);
	}

	@Test
	public void testUpdateResourceArtifactsFromCsar() throws Exception {
		CsarArtifactsAndGroupsBusinessLogic testSubject;
		CsarInfo csarInfo = null;
		Resource resource = null;
		String artifactsMetaFile = "";
		String artifactsMetaFileName = "";
		List<ArtifactDefinition> createdNewArtifacts = null;
		boolean shouldLock = false;
		boolean inTransaction = false;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateResourceArtifactsFromCsar(csarInfo, resource, artifactsMetaFile,
				artifactsMetaFileName, createdNewArtifacts, shouldLock, inTransaction);
	}
}