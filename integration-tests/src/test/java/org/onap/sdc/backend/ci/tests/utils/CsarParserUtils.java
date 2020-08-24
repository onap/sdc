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

package org.onap.sdc.backend.ci.tests.utils;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.onap.sdc.backend.ci.tests.datatypes.GroupHeatMetaDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.PropertyHeatMetaDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaParameterConstants;
import org.onap.sdc.backend.ci.tests.utils.rest.ArtifactRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.backend.ci.tests.utils.validation.CsarValidationUtils;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.testng.AssertJUnit.assertTrue;

public class CsarParserUtils {
	public static final String CSAR_ARTIFACT = "assettoscacsar";
	private static Logger log = LoggerFactory.getLogger(CsarValidationUtils.class.getName());

	public static byte[] downloadComponentCsar(Component csarOwner, User user) throws Exception {
		String artifactUniqeId = csarOwner.getToscaArtifacts().get(CSAR_ARTIFACT).getUniqueId();
		RestResponse csarResponse = ArtifactRestUtils.downloadResourceArtifactInternalApi(csarOwner.getUniqueId(), user, artifactUniqeId);
		Assert.assertNotNull(csarResponse);
		BaseRestUtils.checkSuccess(csarResponse);
		ArtifactUiDownloadData artifactUiDownloadData = ResponseParser.parseToObject(csarResponse.getResponse(),
				ArtifactUiDownloadData.class);
		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		return Base64.decodeBase64(fromUiDownload);
	}
	
	public static List<TypeHeatMetaDefinition> getListTypeHeatMetaDefinition(File csarFileLocation) throws Exception {

		String artifactHeatMetaLocation = ToscaParameterConstants.HEAT_META_PATH;
		String csarPayload = ToscaParserUtils.getYamlPayloadFromCsar(csarFileLocation, artifactHeatMetaLocation);
		return getListTypeHeatMetaDefinitionByPayload(csarPayload);
	}

	public static List<TypeHeatMetaDefinition> getListTypeHeatMetaDefinition(String csarUUID) throws Exception {
		
		String artifactHeatMetaLocation = ToscaParameterConstants.HEAT_META_PATH;
		String csarPayload = ToscaParserUtils.getCsarPayload(csarUUID, artifactHeatMetaLocation);
		return getListTypeHeatMetaDefinitionByPayload(csarPayload);
	}

	public static List<TypeHeatMetaDefinition> getListTypeHeatMetaDefinitionByPayload(String csarPayload) throws ParseException {
		JSONParser parser = new JSONParser();
		if (csarPayload != null) {
			Object parse = parser.parse(csarPayload);
			JSONObject jsonObject = (JSONObject) parse;
			JSONObject jsonObjectImportStructure = (JSONObject) jsonObject.get(ToscaParameterConstants.IMPORT_STRUCTURE);
			List<TypeHeatMetaDefinition> listHeatMetaDefenition = new ArrayList<>();
			listHeatMetaDefenition = getArtifactsByGroup(jsonObjectImportStructure, listHeatMetaDefenition);
			return listHeatMetaDefenition;
		}
		return null;
	}
	
	
	protected static List<TypeHeatMetaDefinition> getArtifactsByGroup(JSONObject jsonObjectImportStructure, List<TypeHeatMetaDefinition> listHeatMetaDefenition) {

		@SuppressWarnings("unchecked")
		Set<Object> typeSet = jsonObjectImportStructure.keySet();
		for (Object type : typeSet) {
			TypeHeatMetaDefinition heatMetaDefenition = new TypeHeatMetaDefinition();
			log.debug(type.toString());
			log.debug("{}", jsonObjectImportStructure.get(type));
			JSONArray array = (JSONArray) jsonObjectImportStructure.get(type);
			heatMetaDefenition.setTypeName((String) type);
			List<GroupHeatMetaDefinition> groupHeatMetaDefinitions = new ArrayList<>();
			heatMetaDefenition.setGroupHeatMetaDefinition(fetchArtifactByGroupFromJsonArray(array, groupHeatMetaDefinitions, true, false));
			listHeatMetaDefenition.add(heatMetaDefenition);
		}
		return listHeatMetaDefenition;
	}
	
	protected static List<GroupHeatMetaDefinition> fetchArtifactByGroupFromJsonArray(JSONArray array, List<GroupHeatMetaDefinition> listGroupHeatMetaDefinition, Boolean openNewGroup, Boolean isNested) {

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
				if (openNewGroup) {
					String groupName;
					int lastIndexOfDot = jsonObject.get("fileName").toString().lastIndexOf(".");
					if(lastIndexOfDot == -1){
						groupName = jsonObject.get("fileName").toString();
					}else {
						groupName = jsonObject.get("fileName").toString().substring(0, lastIndexOfDot);
					}
					groupHeatMetaDefinition.setGroupName(groupName);
				}
				fetchArtifactByGroupFromJsonObject(listGroupHeatMetaDefinition, groupHeatMetaDefinition, jsonObject, isNested);
			}
		}
		return listGroupHeatMetaDefinition;
	}
	
	
	public static void fetchArtifactByGroupFromJsonObject(List<GroupHeatMetaDefinition> listGroupHeatMetaDefinition, GroupHeatMetaDefinition groupHeatMetaDefinition, JSONObject jsonObject, Boolean isNested) {
		@SuppressWarnings("unchecked")
		Set<Object> groupsKey = jsonObject.keySet();
		for (Object groupKey : groupsKey) {
			String groupKeyStr = (String) groupKey;
			if (groupKeyStr.equals("isBase")) {
				PropertyHeatMetaDefinition propertyHeatMetaDefinition = new PropertyHeatMetaDefinition();
				propertyHeatMetaDefinition.setName(groupKeyStr);
				propertyHeatMetaDefinition.setValue((boolean) jsonObject.get(groupKeyStr));
				if (!groupHeatMetaDefinition.getPropertyHeatMetaDefinition().equals(propertyHeatMetaDefinition)) {
					groupHeatMetaDefinition.getPropertyHeatMetaDefinition().setValue((boolean) jsonObject.get(groupKeyStr));
				}
			}
 			if (groupKeyStr.equals("fileName")) {
				String artifactName = (String) jsonObject.get(groupKeyStr);
				String artifactType = ArtifactTypeEnum.HEAT_ARTIFACT.getType();
				if(isNested){
					artifactType = ArtifactTypeEnum.HEAT_NESTED.getType();
				}
				if(jsonObject.get("type") != null && isNested == false){
					artifactType = (String) jsonObject.get("type");
				}
				HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition = new HeatMetaFirstLevelDefinition(artifactName, artifactType, null);
				List<HeatMetaFirstLevelDefinition> listArtifactNames = groupHeatMetaDefinition.getArtifactList();
				listArtifactNames.add(heatMetaFirstLevelDefinition);
				groupHeatMetaDefinition.setArtifactList(listArtifactNames);
			} else {
				if((groupKeyStr.equals("env"))){
					if (jsonObject.get(groupKeyStr) instanceof JSONObject){
						fetchArtifactByGroupFromJsonObject(listGroupHeatMetaDefinition, groupHeatMetaDefinition, (JSONObject) jsonObject.get(groupKeyStr), false);
					}else{
						assertTrue("Expected object is JSONObject, but actual: " + jsonObject.get(groupKeyStr).getClass(), jsonObject.get(groupKeyStr).getClass().equals("JSONObject"));
					}
				}
				if((groupKeyStr.equals("nested"))){
					if (jsonObject.get(groupKeyStr) instanceof JSONArray){
						fetchArtifactByGroupFromJsonArray((JSONArray) jsonObject.get(groupKeyStr), listGroupHeatMetaDefinition, false, true);
					}else{
						assertTrue("Expected object is JSONArray, but actual: " + jsonObject.get(groupKeyStr).getClass(), jsonObject.get(groupKeyStr).getClass().equals("JSONArray"));
					}
					
				}else if (!(groupKeyStr.equals("isBase") || groupKeyStr.equals("type") || groupKeyStr.equals("env"))) {
					if (jsonObject.get(groupKeyStr) instanceof JSONArray){
						fetchArtifactByGroupFromJsonArray((JSONArray) jsonObject.get(groupKeyStr), listGroupHeatMetaDefinition, false, false);
					}else{
						assertTrue("Expected object is JSONArray, but actual: " + jsonObject.get(groupKeyStr).getClass(), jsonObject.get(groupKeyStr).getClass().equals("JSONArray"));
					}
				}
			}
		}
	}
	
}
