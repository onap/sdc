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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.datatypes.GroupHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.PropertyHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaNodeTemplatesTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaRequirementsNodeTemplatesDefinition;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.common.rest.api.RestResponseAsByteArray;
import org.openecomp.sdc.common.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsarValidationUtils {
	private static Logger log = LoggerFactory.getLogger(CsarValidationUtils.class.getName());

	public static String getCsarPayload(String csarName, String fileLocation) throws Exception {

		RestResponseAsByteArray csar = ImportRestUtils.getCsar(csarName,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertTrue("Return response code different from 200",
				csar.getHttpStatusCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		Map<String, byte[]> readZip = null;
		byte[] data = csar.getResponse();
		if (data != null && data.length > 0) {
			readZip = ZipUtil.readZip(data);

		}

		byte[] artifactsBs = readZip.get(fileLocation);
		String str = new String(artifactsBs, StandardCharsets.UTF_8);

		return str;

	}

	public static List<TypeHeatMetaDefinition> getListTypeHeatMetaDefinition(String csarUUID) throws Exception {

		String artifactHeatMetaLocation = "Artifacts/HEAT.meta";
		JSONParser parser = new JSONParser();
		String csarPayload = getCsarPayload(csarUUID, artifactHeatMetaLocation);
		if (csarPayload != null) {
			Object parse = parser.parse(csarPayload);
			JSONObject jsonObject = (JSONObject) parse;
			JSONObject jsonObjectImportStructure = (JSONObject) jsonObject.get("importStructure");
			List<TypeHeatMetaDefinition> listHeatMetaDefenition = new ArrayList<TypeHeatMetaDefinition>();
			listHeatMetaDefenition = getArtifactsByGroup(jsonObjectImportStructure, listHeatMetaDefenition);
			return listHeatMetaDefenition;
		}
		return null;

	}

	protected static List<TypeHeatMetaDefinition> getArtifactsByGroup(JSONObject jsonObjectImportStructure,
			List<TypeHeatMetaDefinition> listHeatMetaDefenition) {

		@SuppressWarnings("unchecked")
		Set<Object> typeSet = jsonObjectImportStructure.keySet();
		for (Object type : typeSet) {
			TypeHeatMetaDefinition heatMetaDefenition = new TypeHeatMetaDefinition();
			log.debug(type.toString());
			log.debug("{}", jsonObjectImportStructure.get(type));
			JSONArray array = (JSONArray) jsonObjectImportStructure.get(type);
			heatMetaDefenition.setTypeName((String) type);
			List<GroupHeatMetaDefinition> groupHeatMetaDefinitions = new ArrayList<GroupHeatMetaDefinition>();
			heatMetaDefenition.setGroupHeatMetaDefinition(fetchArtifactByGroup(array, groupHeatMetaDefinitions, true));
			listHeatMetaDefenition.add(heatMetaDefenition);
		}
		return listHeatMetaDefenition;
	}

	protected static List<GroupHeatMetaDefinition> fetchArtifactByGroup(JSONArray array,
			List<GroupHeatMetaDefinition> listGroupHeatMetaDefinition, Boolean openNewGroup) {

		GroupHeatMetaDefinition groupHeatMetaDefinition;

		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				if (openNewGroup) {
					groupHeatMetaDefinition = new GroupHeatMetaDefinition();
					int groupNumber = listGroupHeatMetaDefinition.size() + 1;
					log.debug("groupName={}", groupNumber);
					groupHeatMetaDefinition.setGroup(groupNumber);
					listGroupHeatMetaDefinition.add(groupHeatMetaDefinition);
					PropertyHeatMetaDefinition propertyHeatMetaDefinition = new PropertyHeatMetaDefinition();
					propertyHeatMetaDefinition.setName("isBase");
					propertyHeatMetaDefinition.setValue(false);
					groupHeatMetaDefinition.setPropertyHeatMetaDefinition(propertyHeatMetaDefinition);
				}
				groupHeatMetaDefinition = listGroupHeatMetaDefinition.get(listGroupHeatMetaDefinition.size() - 1);
				JSONObject jsonObject = (JSONObject) array.get(i);
				@SuppressWarnings("unchecked")
				Set<Object> groupsKey = jsonObject.keySet();
				for (Object groupKey : groupsKey) {
					String groupKeyStr = (String) groupKey;
					if (groupKeyStr.equals("isBase")) {
						PropertyHeatMetaDefinition propertyHeatMetaDefinition = new PropertyHeatMetaDefinition();
						propertyHeatMetaDefinition.setName(groupKeyStr);
						propertyHeatMetaDefinition.setValue((boolean) jsonObject.get(groupKeyStr));
						if (!groupHeatMetaDefinition.getPropertyHeatMetaDefinition()
								.equals(propertyHeatMetaDefinition)) {
							groupHeatMetaDefinition.getPropertyHeatMetaDefinition()
									.setValue((boolean) jsonObject.get(groupKeyStr));
						}
					}
					if (groupKeyStr.equals("fileName") || groupKeyStr.equals("env")) {
						String artifactName = (String) jsonObject.get(groupKeyStr);
						List<String> listArtifactNames = groupHeatMetaDefinition.getArtifactList();
						listArtifactNames.add(artifactName);
						groupHeatMetaDefinition.setArtifactList(listArtifactNames);
					} else {
						if (!groupKeyStr.equals("isBase")) {
							fetchArtifactByGroup((JSONArray) jsonObject.get(groupKeyStr), listGroupHeatMetaDefinition,
									false);
						}
					}
				}
			}
		}
		return listGroupHeatMetaDefinition;
	}

	private static Integer getArtifactCount(List<TypeHeatMetaDefinition> listHeatMetaDefenition,
			Boolean isEnvIncluded) {
		int count = 0;
		List<String> uniqeArtifactList = new ArrayList<>();

		for (TypeHeatMetaDefinition typeHeatMetaDefinition : listHeatMetaDefenition) {
			for (GroupHeatMetaDefinition groupHeatMetaDefinition : typeHeatMetaDefinition
					.getGroupHeatMetaDefinition()) {
				if (isEnvIncluded) {
					count = count + groupHeatMetaDefinition.getArtifactList().size();
				} else {
					for (String fileName : groupHeatMetaDefinition.getArtifactList()) {
						if (!fileName.contains(".env") && !uniqeArtifactList.contains(fileName)) {
							uniqeArtifactList.add(fileName);
							count = count + 1;
						}
					}
				}
			}
		}
		return count;
	}

	private static Integer getGroupCount(List<TypeHeatMetaDefinition> listHeatMetaDefenition) {
		int count = 0;
		for (TypeHeatMetaDefinition typeHeatMetaDefinition : listHeatMetaDefenition) {
			count = count + typeHeatMetaDefinition.getGroupHeatMetaDefinition().size();
		}
		return count;
	}

	private static String groupNameBuilder(Resource resource) {
		String separator = "::";
		String module = "module-";
		String groupName = resource.getSystemName() + separator + module;
		return groupName;
	}

	public static void validateCsarVfArtifact(String csarUUID, Resource resource) throws Exception {

		List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = getListTypeHeatMetaDefinition(csarUUID);
		assertTrue(
				"check group count, expected: " + getGroupCount(listTypeHeatMetaDefinition) + ", actual: "
						+ resource.getGroups().size(),
				getGroupCount(listTypeHeatMetaDefinition) == resource.getGroups().size());
		assertTrue(
				"check artifact count, expected: " + getArtifactCount(listTypeHeatMetaDefinition, false) + ", actual: "
						+ resource.getDeploymentArtifacts().size(),
				getArtifactCount(listTypeHeatMetaDefinition, false) == resource.getDeploymentArtifacts().size());

	}

	public static void validateToscaDefinitonObjectVsResource(ToscaDefinition toscaDefinition, Resource resource)
			throws Exception {

		assertTrue(
				"check resource instance count, expected: " + getResourceInstanceCount(toscaDefinition) + ", actual: "
						+ resource.getComponentInstances().size(),
				getResourceInstanceCount(toscaDefinition) == resource.getComponentInstances().size());
		assertTrue(
				"check resource instance relation count, expected: " + getResourceInstanceRelationCount(toscaDefinition)
						+ ", actual: " + resource.getComponentInstancesRelations().size(),
				getResourceInstanceRelationCount(toscaDefinition) == resource.getComponentInstancesRelations().size());

	}

	public static Integer getResourceInstanceCount(ToscaDefinition toscaDefinition) {

		return toscaDefinition.getToscaTopologyTemplate().getToscaNodeTemplatesTopologyTemplateDefinition().size();
	}

	public static Integer getResourceInstanceRelationCount(ToscaDefinition toscaDefinition) {
		int count = 0;
		List<ToscaNodeTemplatesTopologyTemplateDefinition> toscaNodeTemplatesTopologyTemplateDefinition = toscaDefinition
				.getToscaTopologyTemplate().getToscaNodeTemplatesTopologyTemplateDefinition();
		for (int i = 0; i < toscaNodeTemplatesTopologyTemplateDefinition.size(); i++) {
			List<ToscaRequirementsNodeTemplatesDefinition> requirements = toscaNodeTemplatesTopologyTemplateDefinition
					.get(i).getRequirements();
			if (requirements != null) {
				for (ToscaRequirementsNodeTemplatesDefinition requirement : requirements) {
					if (requirement.getNode() != null) {
						count = count + 1;
					}
				}
			}
		}
		return count;
	}

	// not finished yet
	private static void validateCsarVfgroup(String csarUUID, Resource resource) {

		List<GroupDefinition> groups = resource.getGroups();
		for (GroupDefinition groupDefinition : groups) {
			List<String> artifacts = groupDefinition.getArtifacts();
			assertTrue("group description is null", groupDefinition.getDescription() != null);
			assertTrue("InvariantUUID is null", groupDefinition.getInvariantUUID() != null);
			// groupDefinition.getMembers();
			assertTrue(
					"name format mismatch, expected: " + groupNameBuilder(resource) + "[0-9], actual: "
							+ groupDefinition.getName(),
					groupDefinition.getName().contains(groupNameBuilder(resource)));
			// groupDefinition.getProperties();
			// groupDefinition.getPropertyValueCounter();
			assertTrue(groupDefinition.getType().equals(getGroupType()));
		}

		String expectedCsarUUID = csarUUID;
		// String expectedToscaResourceName = "org.openecomp.resource.vf." +
		// WordUtils.capitalize(resourceDetails.getName().toLowerCase());
		//
		// assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID,
		// resource.getCsarUUID()),
		// expectedCsarUUID.equals(resource.getCsarUUID()));
		// assertTrue("toscaResourceName : " +
		// buildAssertMessage(expectedToscaResourceName,
		// resource.getToscaResourceName()),
		// expectedToscaResourceName.equals(resource.getToscaResourceName()));
		//
		// RestResponse getResourceResponse =
		// ResourceRestUtils.getResource(resource.getUniqueId());
		// Resource getResource =
		// ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(),
		// Resource.class);
		// assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID,
		// getResource.getCsarUUID()),
		// expectedCsarUUID.equals(getResource.getCsarUUID()));
		// assertTrue("toscaResourceName : " +
		// buildAssertMessage(expectedToscaResourceName,
		// getResource.getToscaResourceName()),
		// expectedToscaResourceName.equals(getResource.getToscaResourceName()));

	}

	private static String getGroupType() {
		return "org.openecomp.groups.VfModule";
	}

}
