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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.ci.tests.utils.ToscaParserUtils.downloadAndParseToscaTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.InputsRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ExportToscaTest extends ComponentBaseTest {
	@Rule
	public static TestName name = new TestName();
	String rootPath = System.getProperty("user.dir");
	private static final String CSARS_PATH = "/src/test/resources/CI/csars/";
	public static String userDefinedNodeYaml = "CustomVL.yml";

	public ExportToscaTest() {
		super(name, ExportToscaTest.class.getName());
	}

	@DataProvider(name = "vfModuleCsar")
	public static Object[][] csarNames() {
		 return new Object[][] { { "VSPPackage", true }, { "csar_1", true }, { "noArtifact", false }, {"noVfModule", false} };
//		return new Object[][] { { "VSPPackage", true }, { "csar_1", true }, { "noArtifact", false } };
	}

	@Test(dataProvider = "vfModuleCsar")
	public void exportVfModuleTest(String csarname, boolean includeGroups) throws Exception {
		System.out.println("run for csar " + csarname);
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, csarname);

		validateGroupsInResource(sdncModifierDetails, createdResource, includeGroups);
	}

	@Test(dataProvider = "vfModuleCsar")
	public void exportVfModuleInstanceTest(String csarname, boolean includeGroups) throws Exception {
		System.out.println("run for csar " + csarname);
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// create resource
		Resource createdResource = createVfFromCSAR(sdncModifierDetails, csarname);

		// change state to check in
		RestResponse checkinState = LifecycleRestUtils.changeComponentState(createdResource, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());

		// 2 create service
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// 3 create vf instance in service
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(createdResource);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		List<GroupDefinition> groupsInResource = createdResource.getGroups();
		int vfModuleCount = 0;
		List<GroupDefinition> vfModulesInRes = groupsInResource.stream().filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)).collect(Collectors.toList());

		ComponentInstance componentInstance = service.getComponentInstances().get(0);
		String normalizedName = componentInstance.getNormalizedName();

		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, service);
		assertNotNull(load);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);
		Map<String, Object> groups = (Map<String, Object>) topology_template.get("groups");
		if (includeGroups) {
			assertNotNull(vfModulesInRes);
			assertNotNull(groups);

			assertEquals("Validate count of vf module instances", vfModulesInRes.size(), groups.size());

			vfModulesInRes.forEach(modInRes -> {
				validateVfModuleVsInstance(normalizedName, groups, modInRes);
			});
		}else{
			assertNull(groups);
		}
	}

	private void validateVfModuleVsInstance(String normalizedName, Map<String, Object> groups, GroupDefinition modInRes) {
		String instName = normalizedName + ".." + modInRes.getName();
		Map<String, Object> group = (Map<String, Object>) groups.get(instName);
		assertNotNull(group);

		String type = (String) group.get("type");
		assertNotNull(type);
		assertEquals("Validate group instance type", modInRes.getType(), type);

		Map<String, Object> metadata = (Map<String, Object>) group.get("metadata");
		assertNotNull(metadata);

		String invariantUUID = (String) metadata.get("vfModuleModelInvariantUUID");
		String name = (String) metadata.get("vfModuleModelName");
		String UUID = (String) metadata.get("vfModuleModelUUID");
		String version = (String) metadata.get("vfModuleModelVersion");

		String customizationUUID = (String) metadata.get("vfModuleModelCustomizationUUID");
		assertNotNull("Validate group instance customizationUUID", customizationUUID);

		assertEquals("Validate group instance InvariantUUID", modInRes.getInvariantUUID(), invariantUUID);
		assertEquals("Validate group instance name", modInRes.getName(), name);
		assertEquals("Validate group instance UUID", modInRes.getGroupUUID(), UUID);
		assertEquals("Validate group instance version", modInRes.getVersion(), version);

		Map<String, Object> propertiesInInst = (Map<String, Object>) group.get("properties");
		assertNotNull(propertiesInInst);

		List<GroupProperty> propertiesInGroup = modInRes.convertToGroupProperties();
		// property isBase not exist in tosca
		assertEquals("Validate group instance properties size", propertiesInGroup.size() - 1, propertiesInInst.size());
		propertiesInGroup.forEach(propInGroup -> {
			String propName = propInGroup.getName();
			if (!propName.equals("isBase")) {
				Object propValue = propertiesInInst.get(propName);
				String valueInGroup = propInGroup.getValue();
				if (valueInGroup != null && !valueInGroup.isEmpty()) {
					assertNotNull(propValue);
					assertEquals("Validate group instance property value for " + propName, valueInGroup, propValue.toString());
				} else {
					assertNull(propValue);
				}
			}
		});
	}

	private void validateGroupsInResource(User sdncModifierDetails, Resource createdResource, boolean includeGroups) throws Exception {
		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, createdResource);
		assertNotNull(load);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);
		Map<String, Object> groups = (Map<String, Object>) topology_template.get("groups");
		if (includeGroups) {
			assertNotNull(groups);
			List<GroupDefinition> groupsOrigin = createdResource.getGroups();

			assertEquals("Validate groups size", groupsOrigin.size(), groups.size());
			for (GroupDefinition group : groupsOrigin) {
				Map<String, Object> groupTosca = (Map<String, Object>) groups.get(group.getName());
				assertNotNull(groupTosca);

				Map<String, Object> metadata = (Map<String, Object>) groupTosca.get("metadata");
				assertNotNull(metadata);

				String invariantUUID;
				String name;
				String UUID;
				String version;
				Map<String, Object> properties = (Map<String, Object>) groupTosca.get("properties");

				if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
					invariantUUID = (String) metadata.get("vfModuleModelInvariantUUID");
					name = (String) metadata.get("vfModuleModelName");
					UUID = (String) metadata.get("vfModuleModelUUID");
					version = (String) metadata.get("vfModuleModelVersion");
					assertNotNull(properties);

					validateVfModuleProperties(createdResource, group, properties);
				} else {
					invariantUUID = (String) metadata.get("invariantUUID");
					name = (String) metadata.get("name");
					UUID = (String) metadata.get("UUID");
					version = (String) metadata.get("version");
					assertNull(properties);

				}
				assertEquals("Validate InvariantUUID", group.getInvariantUUID(), invariantUUID);
				assertEquals("Validate name", group.getName(), name);
				assertEquals("Validate UUID", group.getGroupUUID(), UUID);
				assertEquals("Validate version", group.getVersion(), version);
			}
		} else {
			assertEquals(null, groups);
		}
	}

	private void validateVfModuleProperties(Resource createdResource, GroupDefinition group, Map<String, Object> properties) {
		// vf_module_type
		String vf_module_type = (String) properties.get("vf_module_type");
		List<GroupProperty> props = group.convertToGroupProperties();

		GroupProperty isBaseProp = getGroupPropertyByName(group, Constants.IS_BASE);
		assertNotNull(isBaseProp);

		String value = isBaseProp.getValue() == null ? isBaseProp.getDefaultValue() : isBaseProp.getValue();
		boolean bvalue = Boolean.parseBoolean(value);
		if (bvalue) {
			assertEquals("Validate vf_module_type", "Base", vf_module_type);
		} else {
			assertEquals("Validate vf_module_type", "Expansion", vf_module_type);
		}

		// vf_module_description
		String vf_module_description = (String) properties.get("vf_module_description");
		assertEquals("Validate vf_module_description", group.getDescription(), vf_module_description);

		// volume_group
		Boolean volume_group = (Boolean) properties.get("volume_group");
		boolean isVolume = false;
		List<String> artifactsList = group.getArtifacts();
		List<ArtifactDefinition> artifacts = new ArrayList<>();
		if (artifactsList != null && !artifactsList.isEmpty()) {
			ArtifactDefinition masterArtifact = findMasterArtifact(createdResource.getDeploymentArtifacts(), artifacts, artifactsList);
			if (masterArtifact.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
				isVolume = true;
			}
		}
		assertEquals("Validate volume_group", isVolume, volume_group);

		// min_vf_module_instances
		Integer min_vf_module_instances = (Integer) properties.get("min_vf_module_instances");
		GroupProperty minInstProp = getGroupPropertyByName(group, "min_vf_module_instances");
		assertNotNull(minInstProp);
		assertEquals("Validate min_vf_module_instances", minInstProp.getValue(), min_vf_module_instances.toString());

		// vf_module_label
		String vf_module_label = (String) properties.get("vf_module_label");
		GroupProperty labelProp = getGroupPropertyByName(group, "vf_module_label");
		assertNotNull(labelProp);
		assertEquals("Validate vf_module_label", labelProp.getValue(), vf_module_label);

		// vf_module_label
		Integer initial_count = (Integer) properties.get("initial_count");
		GroupProperty initCountProp = getGroupPropertyByName(group, "initial_count");
		assertNotNull(initCountProp);
		assertEquals("Validate initial_count", initCountProp.getValue(), initial_count.toString());

		// max_vf_module_instances
		Integer max_vf_module_instances = (Integer) properties.get("max_vf_module_instances");
		GroupProperty maxInstProp = getGroupPropertyByName(group, "max_vf_module_instances");
		assertNotNull(maxInstProp);
		if (max_vf_module_instances != null) {
			assertEquals("Validate max_vf_module_instances", maxInstProp.getValue(), max_vf_module_instances.toString());
		} else {
			assertEquals("Validate max_vf_module_instances", maxInstProp.getValue(), max_vf_module_instances);
		}
	}

	private GroupProperty getGroupPropertyByName(GroupDefinition group, String name) {
		List<GroupProperty> props = group.convertToGroupProperties();
		for (GroupProperty prop : props) {
			if (prop.getName().equals(name)) {
				return prop;
			}
		}
		return null;
	}

	@Test(enabled = true)
	public void exportCsarInputsTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, "csar_1");
		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, createdResource);
		assertNotNull(load);

		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);

		Map<String, Object> inputs = (Map<String, Object>) topology_template.get("inputs");
		assertNotNull(inputs);

		List<InputDefinition> inputsFromResource = createdResource.getInputs();
		assertEquals("validate inputs size", inputsFromResource.size(), inputs.size());
		for (InputDefinition inputDef : inputsFromResource) {
			Map<String, Object> inputInFile = (Map<String, Object>) inputs.get(inputDef.getName());
			assertNotNull(inputInFile);
			validateInput(inputDef, inputInFile);
		}
		List<ComponentInstance> componentInstances = createdResource.getComponentInstances();
		Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = createdResource.getComponentInstancesProperties();
		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		assertNotNull(node_templates);

		JsonParser jsonParser = new JsonParser();

		for (Entry<String, List<ComponentInstanceProperty>> entry : componentInstancesProperties.entrySet()) {

			Optional<ComponentInstance> findFirst = componentInstances.stream().filter(ci -> ci.getUniqueId().equals(entry.getKey())).findFirst();
			assertTrue(findFirst.isPresent());
			String resourceName = findFirst.get().getName();
			Map<String, Object> instance = (Map<String, Object>) node_templates.get(resourceName);
			assertNotNull(instance);
			Map<String, Object> properties = (Map<String, Object>) instance.get("properties");

			for (ComponentInstanceProperty cip : entry.getValue()) {
				if (cip.getValueUniqueUid() != null && !cip.getValueUniqueUid().isEmpty()) {
					assertNotNull(properties);
					if (cip.getValue().contains("get_input")) {
						Object prop = properties.get(cip.getName());
						assertNotNull(prop);

						Gson gson = new Gson();
						String json = gson.toJson(prop);
						assertEquals("validate json property", cip.getValue(), json);
					}

				}
			}

		}

	}

	@Test
	public void importExportCsarWithJsonPropertyType() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "jsonPropertyTypeTest.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		String rootPath = System.getProperty("user.dir");
		Path path = null;
		byte[] data = null;
		String payloadData = null;
		path = Paths.get(rootPath + "/src/test/resources/CI/csars/jsonPropertyTypeTest.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setCsarUUID(payloadName);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		ComponentInstance pmaaServer = resource.getComponentInstances().stream().filter(p -> p.getName().equals("pmaa_server_0")).findAny().get();
		ComponentInstanceProperty jsonProp = resource.getComponentInstancesProperties().get(pmaaServer.getUniqueId()).stream().filter(p -> p.getType().equals(ToscaPropertyType.JSON.getType())).findAny().get();
		String jsonValue = "{\"pmaa.sb_nic\":{\"address\":{\"get_input\":\"pmaa_dpu_fixed_ip\"},\"cidr\":{\"get_input\":\"pmaa_dpu_cidr\"},\"gateway\":{\"get_input\":\"pmaa_dpu_gateway\"}}}";
		assertEquals(jsonProp.getValue(), jsonValue);
		// download and compare
		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, resource);
		assertNotNull(load);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);
		Map<String, Object> nodes = (Map<String, Object>) topology_template.get("node_templates");
		assertNotNull(nodes);
		Map<String, Object> pmaaServerObj = (Map<String, Object>) nodes.get("pmaa_server_0");
		assertNotNull(pmaaServerObj);
		Map<String, Object> props = (Map<String, Object>) pmaaServerObj.get("properties");
		assertNotNull(props);
		Map<String, Object> jsonPropObj = (Map<String, Object>) props.get("metadata");
		assertNotNull(jsonPropObj);
		Gson gson = new Gson();
		String json = gson.toJson(jsonPropObj);
		assertEquals(json, jsonValue);
	}

	@Test(enabled = true)
	public void exportServiceInputValue() throws Exception {
		// 1 create vf as certified
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, "csar_1");
		RestResponse checkinState = LifecycleRestUtils.changeComponentState(createdResource, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());

		// 2 create service
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// 3 create vf instance in service
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(createdResource);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		// 4 download tosca template
		Map<String, Object> tosca = downloadAndParseToscaTemplate(sdncModifierDetails, service);
		assertNotNull(tosca);
		Map<String, Object> topology_template = (Map<String, Object>) tosca.get("topology_template");
		assertNotNull(topology_template);

		// 5 validate no inputs in service
		Map<String, Object> inputs = (Map<String, Object>) tosca.get("inputs");
		assertNull(inputs);

		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());
		ComponentInstance vfi = componentInstances.get(0);

		// 6 add instance inputs in service
		RestResponse getComponentInstanceInputsResponse = InputsRestUtils.getComponentInstanceInputs(service, vfi);
		BaseValidationUtils.checkSuccess(getComponentInstanceInputsResponse);
		List<ComponentInstancePropInput> instanceInputs = new Gson().fromJson(getComponentInstanceInputsResponse.getResponse(), new TypeToken<ArrayList<ComponentInstancePropInput>>() {
		}.getType());
		// Take only the 2 first inputs
		List<ComponentInstancePropInput> inputsToAdd = instanceInputs.stream().limit(2).collect(Collectors.toList());

		// 7 Build component instances input map to add to server
		ComponentInstInputsMap buildComponentInstInputsMap = buildComponentInstInputsMap(vfi.getUniqueId(), inputsToAdd);
		RestResponse addInputResponse = InputsRestUtils.addInput(service, buildComponentInstInputsMap, UserRoleEnum.DESIGNER);
		BaseValidationUtils.checkSuccess(addInputResponse);

		// 8 validate inputs in service
		// 8.1 download tosca template
		getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		tosca = downloadAndParseToscaTemplate(sdncModifierDetails, service);
		assertNotNull(tosca);
		topology_template = (Map<String, Object>) tosca.get("topology_template");
		assertNotNull(topology_template);

		// 8.2 validate inputs in service
		inputs = (Map<String, Object>) topology_template.get("inputs");
		assertNotNull(inputs);
		assertEquals(2, inputs.size());

		// validate created inputs vs inputs in Tosca inputs section
		final Map<String, Object> inputsFinal = inputs;
		buildComponentInstInputsMap.getComponentInstanceInputsMap().values().forEach(listPerInstance -> {
			listPerInstance.forEach(input -> {
				Map<String, Object> inputInMap = (Map<String, Object>) inputsFinal.get(input.getName());
				assertNotNull(inputInMap);
			});
		});
		Map<String, List<ComponentInstanceInput>> componentInstancesInputs = service.getComponentInstancesInputs();

		// validate created inputs vs inputs in Tosca instance input value
		List<ComponentInstanceInput> vfiInputs = componentInstancesInputs.get(vfi.getUniqueId());
		assertNotNull(vfiInputs);
		assertEquals(2, vfiInputs.size());

		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		assertNotNull(node_templates);

		Map<String, Object> instance = (Map<String, Object>) node_templates.get(vfi.getName());
		assertNotNull(instance);
		Map<String, Object> properties = (Map<String, Object>) instance.get("properties");
		assertNotNull(properties);

		vfiInputs.forEach(vfiInput -> {
			Map<String, Object> inputPropValueInTosca = (Map<String, Object>) properties.get(vfiInput.getName());
			assertNotNull(inputPropValueInTosca);
			String instaneInputName = (String) inputPropValueInTosca.get("get_input");
			assertNotNull(instaneInputName);
			Map<String, Object> inputInMap = (Map<String, Object>) inputsFinal.get(instaneInputName);
			assertNotNull(inputInMap);
		});

	}

	@Test(enabled = true)
	public void exportComponentInstancesTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, "csar_1");

		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, createdResource);
		assertNotNull(load);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);

		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		assertNotNull(node_templates);

		RestResponse getResource = ResourceRestUtils.getResource(createdResource.getUniqueId());
		BaseRestUtils.checkSuccess(getResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		List<ComponentInstance> componentInstances = resource.getComponentInstances();

		assertEquals(componentInstances.size(), node_templates.size());

		for (ComponentInstance ci : componentInstances) {
			Map<String, Object> instance = (Map<String, Object>) node_templates.get(ci.getName());
			assertNotNull(instance);
			Map<String, Object> metadata = (Map<String, Object>) instance.get("metadata");
			assertNotNull(metadata);
			String customizationUUD = (String) metadata.get("customizationUUID");
			assertTrue(ci.getCustomizationUUID().equals(customizationUUD));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void extendNodeTemplateWithDefaultPropertyValuesTest() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		String payloadCsarName = "ToscaTemplateCsar.csar";
		Path path = Paths.get(rootPath + CSARS_PATH + "ToscaTemplateCsar.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadCsarName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource createdResource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, createdResource);
		assertNotNull(load);

		Map<String, Object> nodeTemplateProperties = findNodeTemplateProperties(load, "custom_vl");

		assertTrue(nodeTemplateProperties != null);
		assertTrue(nodeTemplateProperties.get("dhcp_enabled").equals(true));
		assertTrue(nodeTemplateProperties.get("ip_version").equals(4));
		assertTrue(nodeTemplateProperties.get("vl_name").equals("customvl"));
	}

	private Map<String, Object> findNodeTemplateProperties(Map<String, Object> load, String riName) {
		// find properties of node template (RI)
		return findToscaElement(
				// find node template (RI) by name
				findToscaElement(
						// find node templates
						findToscaElement(
								// find topology template
								findToscaElement(load, "topology_template"), "node_templates"),
						riName),
				"properties");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> findToscaElement(Map<String, Object> load, String elementName) {
		return (Map<String, Object>) load.get(elementName);
	}

	// ----------------------------------------
	private void validateInput(InputDefinition inputDef, Map<String, Object> inputInFile) {
		assertEquals("validate input type", inputDef.getType(), (String) inputInFile.get("type"));

		if (inputDef.getDefaultValue() == null) {
			assertNull(inputInFile.get("default"));
		} else {
			assertNotNull(inputInFile.get("default"));
			String value = inputDef.getDefaultValue().replace("\"", "");
			value = value.replace(" ", "");
			String expValue = inputInFile.get("default").toString().replace(" ", "");
			assertEquals("validate input default", value, expValue);
		}
		assertEquals("validate input description", inputDef.getDescription(), (String) inputInFile.get("description"));
	}

	public ArtifactDefinition findMasterArtifact(Map<String, ArtifactDefinition> deplymentArtifact, List<ArtifactDefinition> artifacts, List<String> artifactsList) {
		for (String artifactUid : artifactsList) {
			for (Entry<String, ArtifactDefinition> entry : deplymentArtifact.entrySet()) {
				ArtifactDefinition artifact = entry.getValue();
				if (artifactUid.equalsIgnoreCase(artifact.getUniqueId())) {
					artifacts.add(artifact);
				}

			}
		}
		ArtifactDefinition masterArtifact = null;
		for (ArtifactDefinition artifactInfo : artifacts) {
			String atrifactType = artifactInfo.getArtifactType();
			if (atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType()) || atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType())) {
				masterArtifact = artifactInfo;
				continue;
			}
			if (atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType())) {
				masterArtifact = artifactInfo;
				break;
			}
		}
		return masterArtifact;
	}

	private ComponentInstInputsMap buildComponentInstInputsMap(String addToInput, List<ComponentInstancePropInput> inputs) {
		Map<String, List<ComponentInstancePropInput>> map = new HashMap<>();
		map.put(addToInput, inputs);
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		componentInstInputsMap.setComponentInstanceInputsMap(map);
		return componentInstInputsMap;
	}

}
