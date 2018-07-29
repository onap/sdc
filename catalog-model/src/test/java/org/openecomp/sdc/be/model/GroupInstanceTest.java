package org.openecomp.sdc.be.model;

import mockit.Deencapsulation;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GroupInstanceTest {

	private GroupInstance createTestSubject() {
		return new GroupInstance();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupInstance(new GroupInstanceDataDefinition());
	}
	
	@Test
	public void testConvertToGroupInstancesProperties() throws Exception {
		GroupInstance testSubject;
		List<GroupInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToGroupInstancesProperties();
		List<PropertyDataDefinition> properties = new LinkedList<>();
		properties.add(new PropertyDataDefinition());
		testSubject.setProperties(properties);
		result = testSubject.convertToGroupInstancesProperties();
	}

	@Test
	public void testConvertFromGroupInstancesProperties() throws Exception {
		GroupInstance testSubject;
		List<GroupInstanceProperty> groupInstancesProperties = null;

		// test 1
		testSubject = createTestSubject();
		groupInstancesProperties = null;
		testSubject.convertFromGroupInstancesProperties(groupInstancesProperties);
		groupInstancesProperties = new LinkedList<>();
		groupInstancesProperties.add(new GroupInstanceProperty());
		testSubject.convertFromGroupInstancesProperties(groupInstancesProperties);
	}

	@Test
	public void testRemoveArtifactsDuplicates() throws Exception {
		GroupInstance testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "removeArtifactsDuplicates");
		LinkedList<String> artifacts = new LinkedList<>();
		artifacts.add("mock");
		testSubject.setArtifacts(artifacts);
		LinkedList<String> groupInstanceArtifacts = new LinkedList<>();
		groupInstanceArtifacts.add("mock");
		testSubject.setGroupInstanceArtifacts(groupInstanceArtifacts);
		Deencapsulation.invoke(testSubject, "removeArtifactsDuplicates");
	}

	@Test
	public void testClearArtifactsUuid() throws Exception {
		GroupInstance testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "clearArtifactsUuid");
	}

	@Test
	public void testAlignArtifactsUuid() throws Exception {
		GroupInstance testSubject;
		Map<String, ArtifactDefinition> deploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.alignArtifactsUuid(deploymentArtifacts);
		LinkedList<String> artifacts = new LinkedList<>();
		artifacts.add("mock");
		testSubject.setArtifacts(artifacts);
		testSubject.alignArtifactsUuid(deploymentArtifacts);
		deploymentArtifacts = new HashMap<>();
		deploymentArtifacts.put("mock", new ArtifactDefinition());
		testSubject.alignArtifactsUuid(deploymentArtifacts);
	}

	@Test
	public void testAddArtifactsIdToCollection() throws Exception {
		GroupInstance testSubject;
		List<String> artifactUuids = new LinkedList<>();
		ArtifactDefinition artifact = new ArtifactDefinition();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "addArtifactsIdToCollection", artifactUuids, artifact);
		artifact.setArtifactUUID("mock");
		Deencapsulation.invoke(testSubject, "addArtifactsIdToCollection", artifactUuids, artifact);
	}
}