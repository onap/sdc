package org.openecomp.sdc.be.model.jsontitan.datamodel;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabiltyProperty;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;


public class TopologyTemplateTest {

	private TopologyTemplate createTestSubject() {
		return new TopologyTemplate();
	}

	
	@Test
	public void testGetInputs() throws Exception {
		TopologyTemplate testSubject;
		Map<String, PropertyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		TopologyTemplate testSubject;
		Map<String, PropertyDataDefinition> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	
	@Test
	public void testGetInstInputs() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstInputs();
	}

	
	@Test
	public void testSetInstInputs() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapPropertiesDataDefinition> instInputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstInputs(instInputs);
	}

	
	@Test
	public void testGetHeatParameters() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ? extends ToscaDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatParameters();
	}

	
	@Test
	public void testSetHeatParameters() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ? extends ToscaDataDefinition> heatParameters = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatParameters(heatParameters);
	}

	
	@Test
	public void testGetInstAttributes() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstAttributes();
	}

	
	@Test
	public void testSetInstAttributes() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapPropertiesDataDefinition> instAttributes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstAttributes(instAttributes);
	}

	
	@Test
	public void testGetInstProperties() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstProperties();
	}

	
	@Test
	public void testSetInstProperties() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapPropertiesDataDefinition> instProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstProperties(instProperties);
	}

	
	@Test
	public void testGetGroups() throws Exception {
		TopologyTemplate testSubject;
		Map<String, GroupDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroups();
	}

	
	@Test
	public void testSetGroups() throws Exception {
		TopologyTemplate testSubject;
		Map<String, GroupDataDefinition> groups = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroups(groups);
	}

	
	@Test
	public void testGetInstGroups() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapGroupsDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstGroups();
	}

	
	@Test
	public void testSetInstGroups() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapGroupsDataDefinition> instGroups = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstGroups(instGroups);
	}

	
	@Test
	public void testGetServiceApiArtifacts() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceApiArtifacts();
	}

	
	@Test
	public void testSetServiceApiArtifacts() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ArtifactDataDefinition> serviceApiArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceApiArtifacts(serviceApiArtifacts);
	}

	
	@Test
	public void testGetCompositions() throws Exception {
		TopologyTemplate testSubject;
		Map<String, CompositionDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCompositions();
	}

	
	@Test
	public void testSetCompositions() throws Exception {
		TopologyTemplate testSubject;
		Map<String, CompositionDataDefinition> compositions = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCompositions(compositions);
	}

	
	@Test
	public void testGetCalculatedCapabilities() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListCapabiltyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCalculatedCapabilities();
	}

	
	@Test
	public void testSetCalculatedCapabilities() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListCapabiltyDataDefinition> calculatedCapabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCalculatedCapabilities(calculatedCapabilities);
	}

	
	@Test
	public void testGetCalculatedRequirements() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListRequirementDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCalculatedRequirements();
	}

	
	@Test
	public void testSetCalculatedRequirements() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListRequirementDataDefinition> calculatedRequirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCalculatedRequirements(calculatedRequirements);
	}

	
	@Test
	public void testGetFullfilledCapabilities() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListCapabiltyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFullfilledCapabilities();
	}

	
	@Test
	public void testSetFullfilledCapabilities() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListCapabiltyDataDefinition> fullfilledCapabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setFullfilledCapabilities(fullfilledCapabilities);
	}

	
	@Test
	public void testGetFullfilledRequirements() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListRequirementDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFullfilledRequirements();
	}

	
	@Test
	public void testSetFullfilledRequirements() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapListRequirementDataDefinition> fullfilledRequirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setFullfilledRequirements(fullfilledRequirements);
	}

	
	@Test
	public void testGetInstDeploymentArtifacts() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstDeploymentArtifacts();
	}

	
	@Test
	public void testSetInstDeploymentArtifacts() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstDeploymentArtifacts(instDeploymentArtifacts);
	}

	
	@Test
	public void testGetCalculatedCapabilitiesProperties() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapCapabiltyProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCalculatedCapabilitiesProperties();
	}

	
	@Test
	public void testSetCalculatedCapabilitiesProperties() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapCapabiltyProperty> calculatedCapabilitiesProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCalculatedCapabilitiesProperties(calculatedCapabilitiesProperties);
	}

	
	@Test
	public void testGetInstanceArtifacts() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstanceArtifacts();
	}

	
	@Test
	public void testSetInstanceArtifacts() throws Exception {
		TopologyTemplate testSubject;
		Map<String, MapArtifactDataDefinition> instanceArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstanceArtifacts(instanceArtifacts);
	}

	


	
	@Test
	public void testGetComponentInstances() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ComponentInstanceDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstances();
	}

	
	@Test
	public void testSetComponentInstances() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ComponentInstanceDataDefinition> instances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstances(instances);
	}

	
	@Test
	public void testGetRelations() throws Exception {
		TopologyTemplate testSubject;
		Map<String, RelationshipInstDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelations();
	}
}