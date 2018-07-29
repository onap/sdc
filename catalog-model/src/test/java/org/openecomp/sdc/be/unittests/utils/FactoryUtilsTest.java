package org.openecomp.sdc.be.unittests.utils;

import org.junit.Test;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.resources.data.*;

import java.util.List;

public class FactoryUtilsTest {

//	private FactoryUtils createTestSubject() {
//		return FactoryUtils.Constants;
//	}

	
	@Test
	public void testCreateVFWithRI() throws Exception {
		String riVersion = "";
		Resource result;

		// default test
		result = FactoryUtils.createVFWithRI(riVersion);
	}

	
	@Test
	public void testCreateVF() throws Exception {
		Resource result;

		// default test
		result = FactoryUtils.createVF();
	}

	
	@Test
	public void testCreateResourceByType() throws Exception {
		String resourceType = "";
		ResourceMetadataData result;

		// default test
		result = FactoryUtils.createResourceByType(resourceType);
	}

	
	@Test
	public void testAddComponentInstanceToVF() throws Exception {
		Resource vf = new Resource();
		ComponentInstance resourceInstance = null;

		// default test
		FactoryUtils.addComponentInstanceToVF(vf, resourceInstance);
	}

	
	@Test
	public void testCreateResourceInstance() throws Exception {
		ComponentInstance result;

		// default test
		result = FactoryUtils.createResourceInstance();
	}

	
	@Test
	public void testCreateResourceInstanceWithVersion() throws Exception {
		String riVersion = "";
		ComponentInstance result;

		// default test
		result = FactoryUtils.createResourceInstanceWithVersion(riVersion);
	}

	
	@Test
	public void testCreateCapabilityData() throws Exception {
		CapabilityData result;

		// default test
		result = FactoryUtils.createCapabilityData();
	}

	
	@Test
	public void testCreateRequirementData() throws Exception {
		RequirementData result;

		// default test
		result = FactoryUtils.createRequirementData();
	}

	
	@Test
	public void testConvertCapabilityDataToCapabilityDefinitionAddProperties() throws Exception {
		CapabilityData capData = new CapabilityData();
		CapabilityDefinition result;

		// default test
		result = FactoryUtils.convertCapabilityDataToCapabilityDefinitionAddProperties(capData);
	}

	
	@Test
	public void testCreateComponentInstancePropertyList() throws Exception {
		List<ComponentInstanceProperty> result;

		// default test
		result = FactoryUtils.createComponentInstancePropertyList();
	}

	
	@Test
	public void testConvertRequirementDataIDToRequirementDefinition() throws Exception {
		String reqDataId = "";
		RequirementDefinition result;

		// default test
		result = FactoryUtils.convertRequirementDataIDToRequirementDefinition(reqDataId);
	}

	
	@Test
	public void testCreateGraphEdge() throws Exception {
		GraphEdge result;

		// default test
		result = FactoryUtils.createGraphEdge();
	}

	
	@Test
	public void testCreateCapabilityInstData() throws Exception {
		CapabilityInstData result;

		// default test
		result = FactoryUtils.createCapabilityInstData();
	}

	
	@Test
	public void testCreatePropertyData() throws Exception {
		PropertyValueData result;

		// default test
		result = FactoryUtils.createPropertyData();
	}

	
	@Test
	public void testConvertCapabilityDefinitionToCapabilityData() throws Exception {
		PropertyDefinition propDef = new PropertyDefinition();
		PropertyData result;

		// default test
		result = FactoryUtils.convertCapabilityDefinitionToCapabilityData(propDef);
	}

	
	@Test
	public void testConvertCapabilityDataToCapabilityDefinitionRoot() throws Exception {
		CapabilityData capData = new CapabilityData();
		CapabilityDefinition result;

		// default test
		result = FactoryUtils.convertCapabilityDataToCapabilityDefinitionRoot(capData);
	}
}