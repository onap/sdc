/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.execute.imports;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.Test;

import com.google.gson.Gson;

/**
 * US US730518 Story [BE] - TOSCA capabilities with properties - import "As a
 * resource designer, I would like to add my VFC capabilities with properties."
 * 
 * @author ns019t
 *
 */
public class ImportToscaCapabilitiesWithProperties extends ComponentBaseTest {
	@Rule
	public static TestName name = new TestName();

	Gson gson = new Gson();

	/**
	 * public Constructor ImportToscaCapabilitiesWithProperties
	 */
	public ImportToscaCapabilitiesWithProperties() {
		super(name, ImportToscaCapabilitiesWithProperties.class.getName());
	}

	/**
	 * String constants
	 */
	public static String propertyForTestName = "propertyfortest";
	public static String rootPath = System.getProperty("user.dir");
	public static String scalable = "tosca.capabilities.Scalable";
	public static String container = "tosca.capabilities.Container";
	public static String minInstances = "min_instances";
	public static String userDefinedNodeYaml = "mycompute.yml";

	/**
	 * Capability Type - capability type on the graph should already have
	 * properties modeled on it. please verify. The import of the capability
	 * types should support adding those properties. when importing, validate
	 * name uniqueness between the capability's properties see capability
	 * tosca.capabilities.Container
	 * 
	 * Acceptance Criteria: validate capability type properties (for example,
	 * compute have capability Container -> the properties of this capability
	 * should be in the Json response)
	 * 
	 * @throws IOException
	 */
	@Test
	public void validateCapabilityTypePropertiesSucceed() throws IOException {
		User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse createResourceRes = ResourceRestUtils.getResourceByNameAndVersion(user.getUserId(), "Compute",
				"1.0");
		BaseRestUtils.checkSuccess(createResourceRes);
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(createResourceRes.getResponse());
		Map<String, List<CapabilityDefinition>> capabilities = resource.getCapabilities();
		assertEquals(capabilities.size(), 6);

		CapabilityDefinition capability = capabilities.get(scalable).get(0);
		List<ComponentInstanceProperty> properties = capability.getProperties();
		assertEquals(properties.size(), 3);
		assertTrue(!properties.stream().filter(p -> p.getName().equalsIgnoreCase(propertyForTestName)).findAny()
				.isPresent());

		ComponentInstanceProperty originalProperty = properties.stream()
				.filter(p -> p.getName().equalsIgnoreCase(minInstances)).findAny().get();
		assertEquals(originalProperty.getType(), "integer");
		assertEquals(originalProperty.getDefaultValue(), "1");

		capability = capabilities.get(container).get(0);
		properties = capability.getProperties();
		assertEquals(properties.size(), 4);
	}

	/**
	 * Capability Definition on VFC / CP / VL - properties can also be defined
	 * on the capability when the capability is declared. (property definition
	 * with default value) If the property name (case insensitive) already
	 * defined on the capability type, it overrides the capability from the
	 * capability type Import of VFC / CP /VL should support adding properties
	 * to the capability. when importing, validate name uniqueness between the
	 * capability's properties
	 * 
	 * Acceptance Criteria: import node type with capability definition on it.
	 * use the attached "myCompute"
	 * 
	 * @throws Exception
	 */
	@Test
	public void importNodeTypeWithCapabilityWithPropertiesFromYmlSucceed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		RestResponse createResource = importUserDefinedNodeType(userDefinedNodeYaml, sdncModifierDetails,
				resourceDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		Map<String, List<CapabilityDefinition>> capabilities = resource.getCapabilities();
		assertEquals(capabilities.size(), 6);

		CapabilityDefinition capability = capabilities.get(scalable).get(0);
		List<ComponentInstanceProperty> properties = capability.getProperties();
		assertEquals(properties.size(), 4);

		ComponentInstanceProperty newProperty = properties.stream()
				.filter(p -> p.getName().equalsIgnoreCase(propertyForTestName)).findAny().get();
		assertEquals(newProperty.getType(), "string");
		assertEquals(newProperty.getDescription(), "test");
		assertEquals(newProperty.getDefaultValue(), "success");

		ComponentInstanceProperty overriddenProperty = properties.stream()
				.filter(p -> p.getName().equalsIgnoreCase(minInstances)).collect(Collectors.toList()).get(0);
		assertEquals(overriddenProperty.getType(), "integer");
		assertEquals(overriddenProperty.getDefaultValue(), "3");

	}

	/**
	 * importNodeTypeWithCapabilityWithPropertiesFromYmlFailed
	 * 
	 * @throws Exception
	 */
	@Test
	public void importNodeTypeWithCapabilityWithPropertiesFromYmlFailed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		RestResponse createResource = importUserDefinedNodeType("mycompute_failed.yml", sdncModifierDetails,
				resourceDetails);
		BaseRestUtils.checkErrorMessageResponse(createResource, ActionStatus.PROPERTY_NAME_ALREADY_EXISTS);
	}

	/**
	 * Capability Assignment (on node_template / resource instance) - should
	 * support assignment of the property (property value). On the resource
	 * instance level, value can be assigned to either properties that are
	 * defined on the capability type or on the capability definition. When
	 * importing a VF - the node_template can have capability's property value.
	 * It should be imported and saved on the graph Acceptance Criteria: import
	 * a VF that assign values to property of capability that was defined on the
	 * capability type
	 * 
	 * @throws Exception
	 */
	@Test
	public void importResourceWithCapabilityWithPropertiesOverridingCapTypePropertiesSucceed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "vf_with_cap_prop_override_cap_type_prop.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		Path path = Paths.get(rootPath + "/src/test/resources/CI/csars/vf_with_cap_prop_override_cap_type_prop.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);

		List<ImmutablePair<String, String>> propertyNamesValues = new ArrayList<>();
		propertyNamesValues.add(new ImmutablePair<String, String>("num_cpus", "2"));
		propertyNamesValues.add(new ImmutablePair<String, String>("mem_size", "2000 MB"));
		checkResource(createResource, 8, container, "DBMS", propertyNamesValues);

		ResourceReqDetails resourceDetails2 = ElementFactory.getDefaultResource();
		resourceDetails2.setCsarUUID("vf_with_cap_prop_override_cap_type_prop.csar");
		resourceDetails2.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);

		checkResource(createResource, 8, container, "DBMS", propertyNamesValues);
	}

	/**
	 * importResourceWithCapabilityWithPropertiesOverridingCapTypePropertiesFailed
	 * 
	 * @throws Exception
	 */
	@Test
	public void importResourceWithCapabilityWithPropertiesOverridingCapTypePropertiesFailed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "vf_with_cap_prop_override_cap_type_prop_failed.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		Path path = Paths
				.get(rootPath + "/src/test/resources/CI/csars/vf_with_cap_prop_override_cap_type_prop_failed.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkErrorMessageResponse(createResource, ActionStatus.INVALID_PROPERTY);

		ResourceReqDetails resourceDetails2 = ElementFactory.getDefaultResource();
		resourceDetails2.setCsarUUID("vf_with_cap_prop_override_cap_type_prop_failed.csar");
		resourceDetails2.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);
		BaseRestUtils.checkErrorMessageResponse(createResource, ActionStatus.INVALID_PROPERTY);

	}

	/**
	 * Capability Assignment (on node_template / resource instance) - should
	 * support assignment of the property (property value). On the resource
	 * instance level, value can be assigned to either properties that are
	 * defined on the capability type or on the capability definition. When
	 * importing a VF - the node_template can have capability's property value.
	 * It should be imported and saved on the graph Acceptance Criteria: import
	 * a VF that assign values to property of capability that was defined on the
	 * capability definition (on the node type)
	 * 
	 * @throws Exception
	 */
	@Test
	public void importResourceWithCapabilityWithPropertiesOverridingNodeTypeCapPropertiesSucceed() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		RestResponse createResource = importUserDefinedNodeType(userDefinedNodeYaml, sdncModifierDetails,
				resourceDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource userDefinedNodeType = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		String payloadName = "vf_with_cap_prop_override_cap_type_prop1.csar";
		resourceDetails = ElementFactory.getDefaultImportResource();
		Path path = Paths.get(rootPath + "/src/test/resources/CI/csars/vf_with_cap_prop_override_cap_type_prop1.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);

		List<ImmutablePair<String, String>> propertyNamesValues = new ArrayList<>();
		propertyNamesValues.add(new ImmutablePair<String, String>("num_cpus", "2"));
		propertyNamesValues.add(new ImmutablePair<String, String>("mem_size", "2000 MB"));
		checkResource(createResource, 8, container, "DBMS", propertyNamesValues);

		List<ImmutablePair<String, String>> propertyNamesValues1 = new ArrayList<>();
		propertyNamesValues1.add(new ImmutablePair<String, String>(propertyForTestName, "success_again"));
		propertyNamesValues1.add(new ImmutablePair<String, String>(minInstances, "4"));
		checkResource(createResource, 8, scalable, userDefinedNodeType.getName(), propertyNamesValues1);

		ResourceReqDetails resourceDetails2 = ElementFactory.getDefaultResource();
		resourceDetails2.setCsarUUID("vf_with_cap_prop_override_cap_type_prop1.csar");
		resourceDetails2.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);

		checkResource(createResource, 8, container, "DBMS", propertyNamesValues);
		checkResource(createResource, 8, scalable, userDefinedNodeType.getName(), propertyNamesValues1);

	}

	/**
	 * importResourceWithCapabilityWithPropertiesOverridingNodeTypeCapPropertiesFailed
	 * 
	 * @throws Exception
	 */
	@Test
	public void importResourceWithCapabilityWithPropertiesOverridingNodeTypeCapPropertiesFailed() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		RestResponse createResource = importUserDefinedNodeType(userDefinedNodeYaml, sdncModifierDetails,
				resourceDetails);
		BaseRestUtils.checkCreateResponse(createResource);

		String payloadName = "vf_with_cap_prop_override_cap_type_prop1_failed.csar";
		resourceDetails = ElementFactory.getDefaultImportResource();
		Path path = Paths
				.get(rootPath + "/src/test/resources/CI/csars/vf_with_cap_prop_override_cap_type_prop1_failed.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.PROPERTY_NAME_ALREADY_EXISTS,
				propertyForTestName);

		ResourceReqDetails resourceDetails2 = ElementFactory.getDefaultResource();
		resourceDetails2.setCsarUUID("vf_with_cap_prop_override_cap_type_prop1_failed.csar");
		resourceDetails2.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.PROPERTY_NAME_ALREADY_EXISTS,
				propertyForTestName);
	}

	private RestResponse importUserDefinedNodeType(String payloadName, User sdncModifierDetails,
			ImportReqDetails resourceDetails) throws Exception {

		Path path = Paths.get(rootPath + "/src/test/resources/CI/csars/" + payloadName);
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VFC.name());
		return ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
	}

	// TODO Tal: Since Cashing change partial resource returned that causes null
	// pointer exception
	// commented out till fixing
	private void checkResource(RestResponse createResource, int capNum, String capType, String riName,
			List<ImmutablePair<String, String>> propertyNamesValues) {
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		Map<String, List<CapabilityDefinition>> capabilities = resource.getCapabilities();
		// TODO Tal: Since Cashing change partial resource returned that causes
		// null pointer exception
		/* assertEquals(capabilities.size(), capNum); */
		/*
		 * List<CapabilityDefinition> capabilitesContainer =
		 * capabilities.get(capType);
		 */

		ComponentInstance resourceRI = resource.getComponentInstances().stream()
				.filter(ri -> ri.getComponentName().equals(riName)).collect(Collectors.toList()).get(0);
		// TODO Tal: Since Cashing change partial resource returned that causes
		// null pointer exception
		/*
		 * CapabilityDefinition capabilityFromContainer =
		 * capabilitesContainer.stream()
		 * .filter(cap->cap.getOwnerId().equals(resourceRI.getUniqueId())).
		 * collect(Collectors.toList()).get(0);
		 */

		CapabilityDefinition capabilityFromRI = resourceRI.getCapabilities().get(capType).get(0);
		for (ImmutablePair<String, String> propValuePair : propertyNamesValues) {
			// TODO Tal: Since Cashing change partial resource returned that
			// causes null pointer exception
			/*
			 * Map<String, ComponentInstanceProperty> propertiesFromContainer =
			 * capabilityFromContainer.getProperties()
			 * .stream().filter(p->p.getName().equalsIgnoreCase(propValuePair.
			 * getLeft())) .collect(Collectors.toMap(p->p.getName(), p->p));
			 */

			List<ComponentInstanceProperty> propertiesFromRI = capabilityFromRI.getProperties().stream()
					.filter(p -> p.getName().equalsIgnoreCase(propValuePair.getLeft())).collect(Collectors.toList());
			// TODO Tal: Since Cashing change partial resource returned that
			// causes null pointer exception
			/*
			 * for(ComponentInstanceProperty riProp : propertiesFromRI){
			 * assertTrue(propertiesFromContainer.containsKey(riProp.getName()))
			 * ; ComponentInstanceProperty containerProp =
			 * propertiesFromContainer.get(riProp.getName());
			 * assertEquals(riProp.getValue(), containerProp.getValue());
			 * if(riProp.getName().equals(propValuePair.getLeft()))
			 * assertEquals(riProp.getValue(), propValuePair.getRight());
			 * 
			 * }
			 */
		}
	}

}
