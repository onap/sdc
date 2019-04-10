package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

import java.util.LinkedList;

public class ComponentParametersViewTest {

	private ComponentParametersView createTestSubject() {
		return new ComponentParametersView();
	}

	@Test
	public void testCtor() throws Exception {
		new ComponentParametersView(true);
		
		LinkedList<String> linkedList = new LinkedList<>();
		for (ComponentFieldsEnum iterable_element : ComponentFieldsEnum.values()) {
			linkedList.add(iterable_element.getValue());
		}
		new ComponentParametersView(linkedList);
	}
	
	@Test
	public void testFilter() throws Exception {
		ComponentParametersView testSubject;
		Component component = null;
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		Component result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.filter(component, componentType);
		testSubject.disableAll();
		result = testSubject.filter(new Resource(), componentType);
	}

	
	@Test
	public void testDisableAll() throws Exception {
		ComponentParametersView testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.disableAll();
	}

	
	@Test
	public void testIsIgnoreGroups() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreGroups();
	}

	
	@Test
	public void testSetIgnoreGroups() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreGroups = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreGroups(ignoreGroups);
	}

	
	@Test
	public void testIsIgnoreComponentInstances() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreComponentInstances();
	}

	
	@Test
	public void testSetIgnoreComponentInstances() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreComponentInstances = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreComponentInstances(ignoreComponentInstances);
	}

	
	@Test
	public void testIsIgnoreProperties() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreProperties();
	}

	
	@Test
	public void testSetIgnoreProperties() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreProperties = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreProperties(ignoreProperties);
	}

	
	@Test
	public void testIsIgnoreCapabilities() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreCapabilities();
	}

	
	@Test
	public void testSetIgnoreCapabilities() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreCapabilities = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreCapabilities(ignoreCapabilities);
	}

	
	@Test
	public void testIsIgnoreRequirements() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreRequirements();
	}

	
	@Test
	public void testSetIgnoreRequirements() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreRequirements = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreRequirements(ignoreRequirements);
	}

	
	@Test
	public void testIsIgnoreCategories() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreCategories();
	}

	
	@Test
	public void testSetIgnoreCategories() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreCategories = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreCategories(ignoreCategories);
	}

	
	@Test
	public void testIsIgnoreAllVersions() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreAllVersions();
	}

	
	@Test
	public void testSetIgnoreAllVersions() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreAllVersions = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreAllVersions(ignoreAllVersions);
	}

	
	@Test
	public void testIsIgnoreAdditionalInformation() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreAdditionalInformation();
	}

	
	@Test
	public void testIsIgnoreArtifacts() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreArtifacts();
	}

	
	@Test
	public void testSetIgnoreArtifacts() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreArtifacts = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreArtifacts(ignoreArtifacts);
	}

	
	@Test
	public void testIsIgnoreComponentInstancesProperties() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreComponentInstancesProperties();
	}

	
	@Test
	public void testSetIgnoreComponentInstancesProperties() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreComponentInstancesProperties = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreComponentInstancesProperties(ignoreComponentInstancesProperties);
	}

	
	@Test
	public void testIsIgnoreComponentInstancesInputs() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreComponentInstancesInputs();
	}

	
	@Test
	public void testSetIgnoreComponentInstancesInputs() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreComponentInstancesInputs = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreComponentInstancesInputs(ignoreComponentInstancesInputs);
	}

	
	@Test
	public void testIsIgnoreInterfaces() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreInterfaces();
	}


	
	@Test
	public void testIsIgnoreAttributesFrom() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreAttributesFrom();
	}

	
	@Test
	public void testSetIgnoreAttributesFrom() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreAttributesFrom = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreAttributesFrom(ignoreAttributesFrom);
	}

	
	@Test
	public void testIsIgnoreComponentInstancesAttributesFrom() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreComponentInstancesAttributesFrom();
	}


	
	@Test
	public void testIsIgnoreDerivedFrom() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreDerivedFrom();
	}


	
	@Test
	public void testIsIgnoreUsers() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreUsers();
	}

	
	@Test
	public void testSetIgnoreUsers() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreUsers = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreUsers(ignoreUsers);
	}

	
	@Test
	public void testIsIgnoreInputs() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreInputs();
	}

	
	@Test
	public void testSetIgnoreInputs() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreInputs = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreInputs(ignoreInputs);
	}

	
	@Test
	public void testIsIgnoreCapabiltyProperties() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreCapabiltyProperties();
	}

	
	@Test
	public void testSetIgnoreCapabiltyProperties() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreCapabiltyProperties = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreCapabiltyProperties(ignoreCapabiltyProperties);
	}

	
	@Test
	public void testIsIgnoreForwardingPath() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreForwardingPath();
	}

	
	@Test
	public void testSetIgnoreForwardingPath() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreServicePath = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreForwardingPath(ignoreServicePath);
	}

	
	@Test
	public void testIsIgnorePolicies() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnorePolicies();
	}

	
	@Test
	public void testSetIgnorePolicies() throws Exception {
		ComponentParametersView testSubject;
		boolean ignorePolicies = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnorePolicies(ignorePolicies);
	}

	
	@Test
	public void testIsIgnoreNodeFilter() throws Exception {
		ComponentParametersView testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIgnoreNodeFilter();
	}

	
	@Test
	public void testSetIgnoreNodeFilter() throws Exception {
		ComponentParametersView testSubject;
		boolean ignoreNodeFilter = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreNodeFilter(ignoreNodeFilter);
	}


	@Test
	public void testDetectParseFlag() throws Exception {
		ComponentParametersView testSubject;
		JsonParseFlagEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.detectParseFlag();
	}
}