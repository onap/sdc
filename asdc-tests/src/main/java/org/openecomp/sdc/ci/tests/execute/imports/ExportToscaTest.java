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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ExportToscaTest extends ComponentBaseTest {
	@Rule
	public static TestName name = new TestName();

	public ExportToscaTest() {
		super(name, ExportToscaTest.class.getName());
	}

	@Test(enabled = true)
	public void exportVfModuleTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, "VSPPackage");

		Map<String, Object> load = downloadAndParseToscaTemplate(sdncModifierDetails, createdResource);
		assertNotNull(load);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);
		Map<String, Object> groups = (Map<String, Object>) topology_template.get("groups");
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

				String vf_module_type = (String) properties.get("vf_module_type");
				List<GroupProperty> props = group.getProperties();
				for (GroupProperty prop : props) {
					if (prop.getName().equals(Constants.IS_BASE)) {
						String value = prop.getValue() == null ? prop.getDefaultValue() : prop.getValue();
						boolean bvalue = Boolean.parseBoolean(value);
						if (bvalue) {
							assertEquals("Validate vf_module_type", "Base", vf_module_type);
						} else {
							assertEquals("Validate vf_module_type", "Expansion", vf_module_type);
						}
						break;
					}
				}
				String vf_module_description = (String) properties.get("vf_module_description");
				assertEquals("Validate vf_module_description", group.getDescription(), vf_module_description);

				Boolean volume_group = (Boolean) properties.get("volume_group");
				boolean isVolume = false;
				List<String> artifactsList = group.getArtifacts();
				List<ArtifactDefinition> artifacts = new ArrayList<>();
				if (artifactsList != null && !artifactsList.isEmpty()) {
					ArtifactDefinition masterArtifact = findMasterArtifact(createdResource.getDeploymentArtifacts(),
							artifacts, artifactsList);
					if (masterArtifact.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
						isVolume = true;
					}
				}
				assertEquals("Validate volume_group", isVolume, volume_group);

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
		Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = createdResource
				.getComponentInstancesProperties();
		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		assertNotNull(node_templates);

		JsonParser jsonParser = new JsonParser();

		for (Map.Entry<String, List<ComponentInstanceProperty>> entry : componentInstancesProperties.entrySet()) {

			Optional<ComponentInstance> findFirst = componentInstances.stream()
					.filter(ci -> ci.getUniqueId().equals(entry.getKey())).findFirst();
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
		ComponentInstance pmaaServer = resource.getComponentInstances().stream()
				.filter(p -> p.getName().equals("pmaa_server_0")).findAny().get();
		ComponentInstanceProperty jsonProp = resource.getComponentInstancesProperties().get(pmaaServer.getUniqueId())
				.stream().filter(p -> p.getType().equals(ToscaPropertyType.JSON.getType())).findAny().get();
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

	// ----------------------------------------
	private Map<String, Object> downloadAndParseToscaTemplate(User sdncModifierDetails, Resource createdResource)
			throws Exception {
		String artifactUniqeId = createdResource.getToscaArtifacts().get("assettoscatemplate").getUniqueId();

		RestResponse toscaTemplate = ArtifactRestUtils.downloadResourceArtifactInternalApi(
				createdResource.getUniqueId(), sdncModifierDetails, artifactUniqeId);
		ArtifactUiDownloadData artifactUiDownloadData = ResponseParser.parseToObject(toscaTemplate.getResponse(),
				ArtifactUiDownloadData.class);
		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		byte[] decodeBase64 = Base64.decodeBase64(fromUiDownload);
		Yaml yaml = new Yaml();

		InputStream inputStream = new ByteArrayInputStream(decodeBase64);

		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);
		return load;
	}

	private Resource createVfFromCSAR(User sdncModifierDetails, String csarId) throws Exception {
		// create new resource from Csar
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();

		resourceDetails.setCsarUUID(csarId);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource createdResource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());
		return createdResource;
	}

	public ArtifactDefinition findMasterArtifact(Map<String, ArtifactDefinition> deplymentArtifact,
			List<ArtifactDefinition> artifacts, List<String> artifactsList) {
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
			if (atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())
					|| atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType())) {
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
}
