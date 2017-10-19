package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.category.CategoryDefinition;


public class UiComponentDataTransferTest {

	private UiComponentDataTransfer createTestSubject() {
		return new UiComponentDataTransfer();
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, ArtifactDefinition> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	
	@Test
	public void testGetDeploymentArtifacts() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeploymentArtifacts();
	}

	
	@Test
	public void testSetDeploymentArtifacts() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, ArtifactDefinition> deploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentArtifacts(deploymentArtifacts);
	}

	
	@Test
	public void testGetToscaArtifacts() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaArtifacts();
	}

	
	@Test
	public void testSetToscaArtifacts() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, ArtifactDefinition> toscaArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaArtifacts(toscaArtifacts);
	}

	
	@Test
	public void testGetCategories() throws Exception {
		UiComponentDataTransfer testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategories();
	}

	
	@Test
	public void testSetCategories() throws Exception {
		UiComponentDataTransfer testSubject;
		List<CategoryDefinition> categories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCategories(categories);
	}

	
	@Test
	public void testGetCreatorUserId() throws Exception {
		UiComponentDataTransfer testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreatorUserId();
	}

	
	@Test
	public void testSetCreatorUserId() throws Exception {
		UiComponentDataTransfer testSubject;
		String creatorUserId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCreatorUserId(creatorUserId);
	}

	
	@Test
	public void testGetCreatorFullName() throws Exception {
		UiComponentDataTransfer testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreatorFullName();
	}

	
	@Test
	public void testSetCreatorFullName() throws Exception {
		UiComponentDataTransfer testSubject;
		String creatorFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCreatorFullName(creatorFullName);
	}

	
	@Test
	public void testGetLastUpdaterUserId() throws Exception {
		UiComponentDataTransfer testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterUserId();
	}

	
	@Test
	public void testSetLastUpdaterUserId() throws Exception {
		UiComponentDataTransfer testSubject;
		String lastUpdaterUserId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterUserId(lastUpdaterUserId);
	}

	
	@Test
	public void testGetLastUpdaterFullName() throws Exception {
		UiComponentDataTransfer testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterFullName();
	}

	
	@Test
	public void testSetLastUpdaterFullName() throws Exception {
		UiComponentDataTransfer testSubject;
		String lastUpdaterFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterFullName(lastUpdaterFullName);
	}

	
	@Test
	public void testGetComponentType() throws Exception {
		UiComponentDataTransfer testSubject;
		ComponentTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentType();
	}

	
	@Test
	public void testSetComponentType() throws Exception {
		UiComponentDataTransfer testSubject;
		ComponentTypeEnum componentType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentType(componentType);
	}

	
	@Test
	public void testGetComponentInstances() throws Exception {
		UiComponentDataTransfer testSubject;
		List<ComponentInstance> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstances();
	}

	
	@Test
	public void testSetComponentInstances() throws Exception {
		UiComponentDataTransfer testSubject;
		List<ComponentInstance> componentInstances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstances(componentInstances);
	}

	
	@Test
	public void testGetComponentInstancesRelations() throws Exception {
		UiComponentDataTransfer testSubject;
		List<RequirementCapabilityRelDef> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstancesRelations();
	}

	
	@Test
	public void testSetComponentInstancesRelations() throws Exception {
		UiComponentDataTransfer testSubject;
		List<RequirementCapabilityRelDef> componentInstancesRelations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstancesRelations(componentInstancesRelations);
	}

	
	@Test
	public void testGetComponentInstancesInputs() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<ComponentInstanceInput>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstancesInputs();
	}

	
	@Test
	public void testSetComponentInstancesInputs() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<ComponentInstanceInput>> componentInstancesInputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstancesInputs(componentInstancesInputs);
	}

	
	@Test
	public void testGetComponentInstancesProperties() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<ComponentInstanceProperty>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstancesProperties();
	}

	
	@Test
	public void testSetComponentInstancesProperties() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstancesProperties(componentInstancesProperties);
	}

	
	@Test
	public void testGetComponentInstancesAttributes() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<ComponentInstanceProperty>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstancesAttributes();
	}

	
	@Test
	public void testSetComponentInstancesAttributes() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstancesAttributes(componentInstancesAttributes);
	}

	
	@Test
	public void testGetCapabilities() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<CapabilityDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<CapabilityDefinition>> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<RequirementDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		UiComponentDataTransfer testSubject;
		Map<String, List<RequirementDefinition>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}

	
	@Test
	public void testGetInputs() throws Exception {
		UiComponentDataTransfer testSubject;
		List<InputDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		UiComponentDataTransfer testSubject;
		List<InputDefinition> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	
	@Test
	public void testGetGroups() throws Exception {
		UiComponentDataTransfer testSubject;
		List<GroupDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroups();
	}

	
	@Test
	public void testSetGroups() throws Exception {
		UiComponentDataTransfer testSubject;
		List<GroupDefinition> groups = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroups(groups);
	}

	
	@Test
	public void testGetAdditionalInformation() throws Exception {
		UiComponentDataTransfer testSubject;
		List<AdditionalInformationDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAdditionalInformation();
	}

	
	@Test
	public void testSetAdditionalInformation() throws Exception {
		UiComponentDataTransfer testSubject;
		List<AdditionalInformationDefinition> additionalInformation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAdditionalInformation(additionalInformation);
	}
}