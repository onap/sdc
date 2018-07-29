package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import java.util.List;
import java.util.Map;

public class ComponentInstanceTest {

	private ComponentInstance createTestSubject() {
		return new ComponentInstance();
	}

	@Test
	public void testCtor() throws Exception {
		new ComponentInstance(new ComponentInstanceDataDefinition());
	}
	
	@Test
	public void testGetCapabilities() throws Exception {
		ComponentInstance testSubject;
		Map<String, List<CapabilityDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	@Test
	public void testSetCapabilities() throws Exception {
		ComponentInstance testSubject;
		Map<String, List<CapabilityDefinition>> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	@Test
	public void testGetRequirements() throws Exception {
		ComponentInstance testSubject;
		Map<String, List<RequirementDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	@Test
	public void testSetRequirements() throws Exception {
		ComponentInstance testSubject;
		Map<String, List<RequirementDefinition>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}

	@Test
	public void testGetDeploymentArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeploymentArtifacts();
	}

	@Test
	public void testSafeGetDeploymentArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.safeGetDeploymentArtifacts();
	}

	@Test
	public void testSafeGetInformationalArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.safeGetInformationalArtifacts();
	}

	@Test
	public void testSetDeploymentArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> deploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentArtifacts(deploymentArtifacts);
	}

	@Test
	public void testGetArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	@Test
	public void testSafeGetArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.safeGetArtifacts();
	}

	@Test
	public void testSetArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	@Test
	public void testGetGroupInstances() throws Exception {
		ComponentInstance testSubject;
		List<GroupInstance> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupInstances();
	}

	@Test
	public void testSetGroupInstances() throws Exception {
		ComponentInstance testSubject;
		List<GroupInstance> groupInstances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupInstances(groupInstances);
	}

	@Test
	public void testGetActualComponentUid() throws Exception {
		ComponentInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getActualComponentUid();
	}

	@Test
	public void testIsArtifactExists() throws Exception {
		ComponentInstance testSubject;
		ArtifactGroupTypeEnum groupType = null;
		String artifactLabel = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isArtifactExists(groupType, artifactLabel);
		testSubject = createTestSubject();
		result = testSubject.isArtifactExists(ArtifactGroupTypeEnum.DEPLOYMENT, artifactLabel);
	}
}