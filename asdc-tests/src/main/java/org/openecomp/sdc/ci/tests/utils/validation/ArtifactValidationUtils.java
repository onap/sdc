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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ArtifactValidationUtils {

	private static String desc = "description";
	private static String artifactType = "artifactType";
	private static String artifactName = "artifactName";
	private static String artifactChecksum = "artifactChecksum";
	private static String uniqueId = "uniqueId";
	protected Utils utils;

	public static void validateInformationalArtifact(ArtifactReqDetails expectedArtifact,
			Map<String, Object> actualArtifact) {
		assertTrue("description is not as expected",
				expectedArtifact.getDescription().equals(actualArtifact.get(desc).toString()));
		assertTrue("artifactType is not as expected",
				expectedArtifact.getArtifactType().toUpperCase().equals(actualArtifact.get(artifactType).toString()));
		assertTrue("artifactName is not as expected",
				expectedArtifact.getArtifactName().equals(actualArtifact.get(artifactName).toString()));
		assertTrue("uniqueId is not as expected",
				expectedArtifact.getUniqueId().equals(actualArtifact.get(uniqueId).toString()));
		assertTrue("description is not as expected", expectedArtifact.getArtifactLabel().toLowerCase()
				.equals(actualArtifact.get("artifactLabel").toString()));
	}

	public static void validateArtifactsNumberInComponent(Component component, ArtifactGroupTypeEnum artifactGroupType,
			ArtifactTypeEnum artifactType, int expectedNumber) {
		Map<String, ArtifactDefinition> deploymentArtifacts;
		int counter = 0;
		if (artifactGroupType == ArtifactGroupTypeEnum.DEPLOYMENT) {
			deploymentArtifacts = component.getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getArtifacts();
		}
		if (deploymentArtifacts != null) {
			for (ArtifactDefinition artifactDefinition : deploymentArtifacts.values()) {
				if (artifactDefinition.getArtifactType().equals(artifactType.getType())) {
					counter++;
				}
			}
		}
		assertEquals("Unexpected number of " + artifactGroupType.getType() + " artifacts in component", expectedNumber,
				counter);
	}

	// Benny
	public static void validateArtifactsNumberInComponentInstance(ComponentInstance componentInstance,
			ArtifactGroupTypeEnum artifactGroupType, ArtifactTypeEnum artifactType, int expectedNumber) {
		Map<String, ArtifactDefinition> deploymentArtifacts = null;
		int counter = 0;
		if (artifactGroupType == ArtifactGroupTypeEnum.DEPLOYMENT) {
			deploymentArtifacts = componentInstance.getDeploymentArtifacts();
		}
		if (deploymentArtifacts != null) {
			for (ArtifactDefinition artifactDefinition : deploymentArtifacts.values()) {
				if (artifactDefinition.getArtifactType().equals(artifactType.getType())) {
					counter++;
				}
			}
		}
		assertEquals("Unexpected number of " + artifactGroupType.getType() + " artifacts in component", expectedNumber,
				counter);
	}

	public static ESArtifactData parseArtifactRespFromES(RestResponse resResponse)
			throws JsonParseException, JsonProcessingException, Exception {
		String bodyToParse = resResponse.getResponse();
		JsonElement jElement = new JsonParser().parse(bodyToParse);
		JsonElement jsourceElement = jElement.getAsJsonObject().get("_source");

		ObjectMapper mapper = new ObjectMapper();
		ESArtifactData esArtifactObject = mapper.readValue(jsourceElement.toString(), ESArtifactData.class);

		return esArtifactObject;
	}

	public static void validateArtifactReqVsResp(ArtifactReqDetails expectedArtifactDetails,
			ArtifactDefinition actualArtifactJavaObject) {
		String expected;

		expected = expectedArtifactDetails.getArtifactName();
		if (expected == null)
			expected = "";
		assertEquals("artifact name is not correct ", expected, actualArtifactJavaObject.getArtifactName());

		expected = expectedArtifactDetails.getArtifactType();
		if (expected == null)
			expected = "";
		assertEquals("artifact type is not correct ", expected, actualArtifactJavaObject.getArtifactType());

		expected = expectedArtifactDetails.getDescription();
		if (expected == null)
			expected = "";
		assertEquals("artifact description is not correct ", expected, actualArtifactJavaObject.getDescription());

		expected = expectedArtifactDetails.getArtifactLabel();
		if (expected == null || expected == "") {
			expected = expectedArtifactDetails.getArtifactName().toLowerCase().substring(0,
					expectedArtifactDetails.getArtifactName().lastIndexOf("."));
			// expected = tmp.substring(0,
			// artifactInfo.getArtifactName().lastIndexOf("."));
		}
		assertEquals("artifact label is not correct ", expected, actualArtifactJavaObject.getArtifactLabel());

		expected = expectedArtifactDetails.getUrl();
		if (expected != "") {
			assertEquals(expected, actualArtifactJavaObject.getApiUrl());
			assertEquals(expectedArtifactDetails.getArtifactDisplayName(),
					actualArtifactJavaObject.getArtifactDisplayName());
		}

		// assertEquals(validChecksum,
		// actualArtifactJavaObject.getArtifactChecksum());

		// expected = expectedArtifactDetails.getArtifactDisplayName();
		// if (expected != "")
		// {
		// assertEquals(expected,
		// actualArtifactJavaObject.getArtifactDisplayName());
		// }

		boolean actual = actualArtifactJavaObject.getMandatory();
		assertEquals(expectedArtifactDetails.isMandatory(), actual);

		if (actualArtifactJavaObject.getServiceApi()) {

			boolean actual2 = actualArtifactJavaObject.getServiceApi();
			assertEquals(expectedArtifactDetails.isServiceApi(), actual2);
		}

	}

	public static void validateEsArtifactReqVsResp(ArtifactReqDetails expectedArtifactInfo,
			ESArtifactData esArtifactData) throws Exception {
		String expectedArtifactUid = expectedArtifactInfo.getUniqueId();
		if (expectedArtifactUid == null)
			expectedArtifactUid = "";
		assertEquals("artifact name is not correct ", expectedArtifactUid, esArtifactData.getId());

		String actualPayload = Decoder.encode(esArtifactData.getData().array());
		assertEquals("artifact payloadData is not correct ", expectedArtifactInfo.getPayload(), actualPayload);
	}

	public static List<String> getListOfArtifactFromFolder(String folderName) throws IOException, Exception {
		Config config = Utils.getConfig();
		String sourceDir = config.getResourceConfigDir();
		String testResourcesPath = sourceDir + File.separator + folderName;
		List<String> listofFiles = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		return listofFiles;
	}

	public static ArtifactReqDetails replaceDefaultArtWithArtFromList_(ArtifactReqDetails heatArtifactDetails,
			String heatExtension, String folderName, int positionInlist) throws IOException, Exception {

		Config config = Utils.getConfig();
		String ext = heatExtension;
		String sourceDir = config.getResourceConfigDir();
		String testResourcesPath = sourceDir + File.separator + folderName;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		String payload = FileUtils.loadPayloadFile(listFileName, ext, true);
		heatArtifactDetails.setPayload(payload);
		heatArtifactDetails.setArtifactName(listFileName.get(positionInlist) + "." + ext);
		return heatArtifactDetails;
	}

	public static ArtifactReqDetails replaceDefaultArtWithArtFromList(ArtifactReqDetails heatArtifactDetails,
			String heatExtension, String folderName, int positionInlist) throws IOException, Exception {
		List<String> listOfArtifactFromFolder = getListOfArtifactFromFolder(folderName);
		String payload = FileUtils.loadPayloadFileFromListUsingPosition(listOfArtifactFromFolder, heatExtension, true,
				positionInlist);
		heatArtifactDetails.setPayload(payload);
		heatArtifactDetails.setArtifactName(heatArtifactDetails.getArtifactType()
				+ listOfArtifactFromFolder.get(positionInlist) + "." + heatExtension);
		return heatArtifactDetails;
	}
}
