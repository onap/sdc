package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Test;
import org.openecomp.sdc.be.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataForMergeHolderTest {

	private DataForMergeHolder createTestSubject() {
		return new DataForMergeHolder();
	}

	@Test
	public void testGetOrigComponentInstanceHeatEnvArtifacts() throws Exception {
		DataForMergeHolder testSubject;
		List<ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentInstanceHeatEnvArtifacts();
	}

	@Test
	public void testSetOrigComponentInstanceHeatEnvArtifacts() throws Exception {
		DataForMergeHolder testSubject;
		List<ArtifactDefinition> origComponentInstanceHeatEnvArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentInstanceHeatEnvArtifacts(origComponentInstanceHeatEnvArtifacts);
	}

	@Test
	public void testGetOrigComponentInstanceInputs() throws Exception {
		DataForMergeHolder testSubject;
		List<ComponentInstanceInput> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentInstanceInputs();
	}

	@Test
	public void testSetOrigComponentInstanceInputs() throws Exception {
		DataForMergeHolder testSubject;
		List<ComponentInstanceInput> origComponentInstanceInputs = new LinkedList<>();
		ComponentInstanceInput e = new ComponentInstanceInput();
		origComponentInstanceInputs.add(e);

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentInstanceInputs(origComponentInstanceInputs);
	}

	@Test
	public void testGetOrigComponentInstanceProperties() throws Exception {
		DataForMergeHolder testSubject;
		List<ComponentInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentInstanceProperties();
	}

	@Test
	public void testSetOrigComponentInstanceProperties() throws Exception {
		DataForMergeHolder testSubject;
		List<ComponentInstanceProperty> origComponentInstanceProperties = new LinkedList<>();
		ComponentInstanceProperty e = new ComponentInstanceProperty();
		origComponentInstanceProperties.add(e);
		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentInstanceProperties(origComponentInstanceProperties);
	}

	@Test
	public void testGetOrigComponentInputs() throws Exception {
		DataForMergeHolder testSubject;
		List<InputDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentInputs();
	}

	@Test
	public void testSetOrigComponentInputs() throws Exception {
		DataForMergeHolder testSubject;
		List<InputDefinition> origComponentInputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentInputs(origComponentInputs);
	}

	@Test
	public void testGetOrigComponentDeploymentArtifactsCreatedOnTheInstance() throws Exception {
		DataForMergeHolder testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentDeploymentArtifactsCreatedOnTheInstance();
	}

	@Test
	public void testGetOrigComponentInformationalArtifactsCreatedOnTheInstance() throws Exception {
		DataForMergeHolder testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentInformationalArtifactsCreatedOnTheInstance();
	}

	@Test
	public void testSetOrigComponentDeploymentArtifactsCreatedOnTheInstance() throws Exception {
		DataForMergeHolder testSubject;
		Map<String, ArtifactDefinition> origDeploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentDeploymentArtifactsCreatedOnTheInstance(origDeploymentArtifacts);
	}

	@Test
	public void testSetOrigComponentInformationalArtifactsCreatedOnTheInstance() throws Exception {
		DataForMergeHolder testSubject;
		Map<String, ArtifactDefinition> origInformationalArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentInformationalArtifactsCreatedOnTheInstance(origInformationalArtifacts);
	}

	@Test
	public void testSetVfRelationsInfo() throws Exception {
		DataForMergeHolder testSubject;
		ContainerRelationsMergeInfo vfRelationsMergeInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVfRelationsInfo(vfRelationsMergeInfo);
	}

	@Test
	public void testGetVfRelationsMergeInfo() throws Exception {
		DataForMergeHolder testSubject;
		ContainerRelationsMergeInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContainerRelationsMergeInfo();
	}

	@Test
	public void testGetOrigInstanceCapabilities() throws Exception {
		DataForMergeHolder testSubject;
		List<CapabilityDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigInstanceCapabilities();
	}

	@Test
	public void testSetOrigInstanceCapabilities() throws Exception {
		DataForMergeHolder testSubject;
		List<CapabilityDefinition> origInstanceCapabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigInstanceCapabilities(origInstanceCapabilities);
	}

	@Test
	public void testGetOrigInstanceNode() throws Exception {
		DataForMergeHolder testSubject;
		Component result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigInstanceNode();
	}

	@Test
	public void testSetOrigInstanceNode() throws Exception {
		DataForMergeHolder testSubject;
		Component origInstanceNode = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigInstanceNode(origInstanceNode);
	}

	@Test
	public void testGetOrigComponentInstId() throws Exception {
		DataForMergeHolder testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOrigComponentInstId();
	}

	@Test
	public void testSetOrigComponentInstId() throws Exception {
		DataForMergeHolder testSubject;
		String origComponentInstId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOrigComponentInstId(origComponentInstId);
	}
}