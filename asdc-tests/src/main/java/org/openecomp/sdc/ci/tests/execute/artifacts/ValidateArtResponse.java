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

package org.openecomp.sdc.ci.tests.execute.artifacts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ResourceValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class ValidateArtResponse extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();
	protected String serviceVersion;

	public ValidateArtResponse() {
		super(name, ArtifactServletTest.class.getName());

	}

	protected final String pathToFile = "heatArtifactParameters";
	protected final String heatWithValidParams = "heatWithValidParams.yaml";
	protected final String heatWithParamsMissingDefault = "heatWithParamsMissingDefault.yaml";
	protected final String heatWithParamsMissingDesc = "heatWithParamsMissingDesc.yaml";
	protected final String heatWithParamsMissingType = "heatWithParamsMissingType.yaml";
	protected final String importNoDerivedFromFile = "myComputeDerivedFromNotExists.yml";
	protected final String decodedPayload = "decodedPayload";
	protected final String encodedPayload = "encodedPayload";

	protected Resource resourceDetailsObj;
	protected ResourceReqDetails resourceDetails;
	protected User sdncDesignerDetails;

	@BeforeMethod
	public void init() throws Exception {

		resourceDetailsObj = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resourceDetails = new ResourceReqDetails(resourceDetailsObj);
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

	}

	@Test
	public void compareParamtersVsYaml() throws Exception {

		// select file to upload

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithValidParams);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		// create MAP from received JSON

		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS

		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs

		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	protected void assertnull(String string, boolean equals) {
		// TODO Auto-generated method stub

	}

	public Map<String, String> extractSingleParameter(Map<String, String> curr) {
		Map<String, String> innerMap = new HashMap<String, String>();
		if (curr.containsKey("description")) {
			innerMap.put("description", curr.get("description"));
		}

		if (curr.containsKey("defaultValue")) {
			innerMap.put("default", curr.get("defaultValue"));
		} else {
			// System.out.println("kuku");
		}
		innerMap.put("type", curr.get("type"));
		return innerMap;
	}

	public Map<String, Map> createMapFromYaml(String payload) {
		ArrayList<String> parametersList = new ArrayList<String>();

		Yaml yaml = new Yaml();

		Map<String, Map> result = (Map<String, Map>) yaml.load(payload);
		Map<String, Map> paramters = (Map<String, Map>) result.get("parameters");

		for (Map.Entry<String, Map> entry : paramters.entrySet()) {
			Map<String, String> origInnerMap = (Map<String, String>) entry.getValue();

			if (origInnerMap.containsKey("label")) {
				origInnerMap.remove("label");
				paramters.remove(entry);
				paramters.put(entry.getKey(), origInnerMap);
			}
		}
		return paramters;
	}

	public Map<String, Map<String, String>> jsonToMap(RestResponse addInformationalArtifactToResource,
			String section2extract, String createKeyMapBy) {
		Map<String, Object> JsonToMap = new HashMap<String, Object>();
		JsonToMap = (Map<String, Object>) ResponseParser.parseToObject(addInformationalArtifactToResource.getResponse(),
				JsonToMap.getClass());

		List<Map<String, String>> listOfParamters = (List<Map<String, String>>) JsonToMap.get(section2extract);
		Map<String, Map<String, String>> mapOfActualParameters = new HashMap<String, Map<String, String>>();

		for (Map<String, String> curr : listOfParamters) {
			Map<String, String> innerMap = extractSingleParameter(curr);

			mapOfActualParameters.put(curr.get(createKeyMapBy), innerMap);
		}
		return mapOfActualParameters;
	}

	public Map<String, String> selectFileToUpload(String pathToFile, String fileName) throws IOException {
		String sourceDir = config.getResourceConfigDir();
		String testResourcesPath = sourceDir + File.separator + pathToFile;
		String file = fileName;
		Map<String, String> filePayload = new HashMap<String, String>();
		String payload = Decoder.readFileToString(testResourcesPath + File.separator + file);
		filePayload.put(decodedPayload, payload);
		filePayload.put(encodedPayload, Decoder.encode(payload.getBytes()));

		return filePayload;
	}

	@Test
	public void missingDescParam() throws Exception {

		// select file to upload

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithParamsMissingDesc);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		// create MAP from received JSON

		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS

		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs

		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	@Test
	public void missingDefaultParam() throws Exception {

		// select file to upload

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithParamsMissingDefault);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		// create MAP from received JSON

		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS

		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs

		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	@Test
	public void missingTypeParam() throws Exception {

		// select file to upload

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithParamsMissingType);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		// System.out.println(addInformationalArtifactToResource);
		AssertJUnit.assertTrue(
				"response code is not 400, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 400);

	}

	@Test
	public void updateValueParam() throws Exception {

		String updateValueParam = "changed";

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithValidParams);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);
		// System.out.println(resourceGetResponse.getResponse().toString());
		String atifactUniqueId = ResponseParser
				.getValueFromJsonResponse(addInformationalArtifactToResource.getResponse(), "uniqueId");

		ArtifactReqDetails artifacJavaObject = ResponseParser
				.convertArtifactReqDetailsToJavaObject(addInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters2 = artifacJavaObject.getHeatParameters();

		for (HeatParameterDefinition heatParameterDefinition : heatParameters2) {
			heatParameterDefinition.setCurrentValue(updateValueParam);
		}
		artifacJavaObject.setHeatParameters(heatParameters2);
		artifacJavaObject.setPayloadData(null);

		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateDeploymentArtifactToResource(
				artifacJavaObject, sdncDesignerDetails, resourceDetails.getUniqueId());

		// verify change in update response

		ArtifactDefinition ArtifactDefinitionRespJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(updateInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters = ArtifactDefinitionRespJavaObject.getListHeatParameters();
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			String verify = updateValueParam;
			AssertJUnit.assertTrue("verification failed", verify.equals(heatParameterDefinition.getCurrentValue()));
		}

		// verify change in getResource

		resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);

		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();
		deploymentArtifacts.get(heatArtifactDetails.getArtifactName());
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			String verify = updateValueParam;
			AssertJUnit.assertTrue("verification failed", verify.equals(heatParameterDefinition.getCurrentValue()));
		}

		// create MAP from received JSON

		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS

		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs

		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	@Test
	public void updateValueParamMissingDefault() throws Exception {

		String updateValueParam = "changed";

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithParamsMissingDefault);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);
		// System.out.println(resourceGetResponse.getResponse().toString());
		String atifactUniqueId = ResponseParser
				.getValueFromJsonResponse(addInformationalArtifactToResource.getResponse(), "uniqueId");

		ArtifactReqDetails artifacJavaObject = ResponseParser
				.convertArtifactReqDetailsToJavaObject(addInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters2 = artifacJavaObject.getHeatParameters();

		for (HeatParameterDefinition heatParameterDefinition : heatParameters2) {
			heatParameterDefinition.setCurrentValue(updateValueParam);
		}
		artifacJavaObject.setHeatParameters(heatParameters2);
		artifacJavaObject.setPayloadData(null);

		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateDeploymentArtifactToResource(
				artifacJavaObject, sdncDesignerDetails, resourceDetails.getUniqueId());

		// verify change in update response

		ArtifactDefinition ArtifactDefinitionRespJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(updateInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters = ArtifactDefinitionRespJavaObject.getListHeatParameters();
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			String verify = updateValueParam;
			AssertJUnit.assertTrue("verification failed", verify.equals(heatParameterDefinition.getCurrentValue()));
		}

		// verify change in getResource

		resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);

		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();
		deploymentArtifacts.get(heatArtifactDetails.getArtifactName());
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			String verify = updateValueParam;
			AssertJUnit.assertTrue("verification failed", verify.equals(heatParameterDefinition.getCurrentValue()));
		}

		// create MAP from received JSON

		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS

		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs

		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	@Test
	public void updateValueParamNull() throws Exception {

		String updateValueParam = null;

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithValidParams);

		// upload HEAT file and save JSON response
		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);
		// System.out.println(resourceGetResponse.getResponse().toString());
		String atifactUniqueId = ResponseParser
				.getValueFromJsonResponse(addInformationalArtifactToResource.getResponse(), "uniqueId");

		ArtifactReqDetails artifacJavaObject = ResponseParser
				.convertArtifactReqDetailsToJavaObject(addInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters2 = artifacJavaObject.getHeatParameters();

		for (HeatParameterDefinition heatParameterDefinition : heatParameters2) {
			heatParameterDefinition.setCurrentValue(updateValueParam);
		}
		artifacJavaObject.setHeatParameters(heatParameters2);
		artifacJavaObject.setPayloadData(null);

		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateDeploymentArtifactToResource(
				artifacJavaObject, sdncDesignerDetails, resourceDetails.getUniqueId());

		// verify change in update response
		ArtifactDefinition ArtifactDefinitionRespJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(updateInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters = ArtifactDefinitionRespJavaObject.getListHeatParameters();
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			// String verify = updateValueParam;
			if (heatParameterDefinition.getDefaultValue() != null) {
				AssertJUnit.assertTrue(
						heatParameterDefinition.getDefaultValue().equals(heatParameterDefinition.getCurrentValue()));
			} else {
				AssertJUnit.assertNull("verification failed", heatParameterDefinition.getCurrentValue());
			}
		}

		// verify change in getResource
		resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);

		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();
		deploymentArtifacts.get(heatArtifactDetails.getArtifactName());
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			// String verify = updateValueParam;
			if (heatParameterDefinition.getDefaultValue() != null) {
				AssertJUnit.assertTrue(
						heatParameterDefinition.getDefaultValue().equals(heatParameterDefinition.getCurrentValue()));
			} else {
				AssertJUnit.assertNull("verification failed", heatParameterDefinition.getCurrentValue());
			}
		}

		// create MAP from received JSON
		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS
		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs
		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	@Test
	public void updateValueParamEmpty() throws Exception {

		String updateValueParam = "";

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithValidParams);

		// upload HEAT file and save JSON response

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);
		// System.out.println(resourceGetResponse.getResponse().toString());
		String atifactUniqueId = ResponseParser
				.getValueFromJsonResponse(addInformationalArtifactToResource.getResponse(), "uniqueId");

		ArtifactReqDetails artifacJavaObject = ResponseParser
				.convertArtifactReqDetailsToJavaObject(addInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters2 = artifacJavaObject.getHeatParameters();

		for (HeatParameterDefinition heatParameterDefinition : heatParameters2) {
			heatParameterDefinition.setCurrentValue(updateValueParam);
		}
		artifacJavaObject.setHeatParameters(heatParameters2);
		artifacJavaObject.setPayloadData(null);

		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateDeploymentArtifactToResource(
				artifacJavaObject, sdncDesignerDetails, resourceDetails.getUniqueId());

		// verify change in update response

		ArtifactDefinition ArtifactDefinitionRespJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(updateInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters = ArtifactDefinitionRespJavaObject.getListHeatParameters();
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			String verify = updateValueParam;
			AssertJUnit.assertTrue("verification failed", verify.equals(heatParameterDefinition.getCurrentValue()));
		}

		// verify change in getResource

		resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);

		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();
		deploymentArtifacts.get(heatArtifactDetails.getArtifactName());
		for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
			String verify = updateValueParam;
			AssertJUnit.assertTrue("verification failed", verify.equals(heatParameterDefinition.getCurrentValue()));
		}

		// create MAP from received JSON
		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS
		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs
		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

	@Test
	public void onlyValueParamPermited() throws Exception {

		Map<String, String> filePayload = selectFileToUpload(pathToFile, heatWithValidParams);

		// upload HEAT file and save JSON response
		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(filePayload.get(encodedPayload));

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails, resourceDetails.getUniqueId());

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);
		// System.out.println(resourceGetResponse.getResponse().toString());
		String atifactUniqueId = ResponseParser
				.getValueFromJsonResponse(addInformationalArtifactToResource.getResponse(), "uniqueId");

		ArtifactReqDetails artifacJavaObject = ResponseParser
				.convertArtifactReqDetailsToJavaObject(addInformationalArtifactToResource.getResponse());
		List<HeatParameterDefinition> heatParameters2 = artifacJavaObject.getHeatParameters();

		for (HeatParameterDefinition heatParameterDefinition : heatParameters2) {
			heatParameterDefinition.setDefaultValue("changed");
			heatParameterDefinition.setName("changed");
			heatParameterDefinition.setDescription("changed");
			heatParameterDefinition.setType("changed");
			heatParameterDefinition.setCurrentValue("changed");
		}
		artifacJavaObject.setHeatParameters(heatParameters2);
		artifacJavaObject.setPayloadData(null);

		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateDeploymentArtifactToResource(
				artifacJavaObject, sdncDesignerDetails, resourceDetails.getUniqueId());

		resourceGetResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails);

		// create MAP from received JSON

		String section2extract = "heatParameters";
		String createKeyMapBy = "name";
		Map<String, Map<String, String>> mapOfActualParameters = jsonToMap(addInformationalArtifactToResource,
				section2extract, createKeyMapBy);

		// Prepare map to validate JS

		Map<String, Map> paramters = createMapFromYaml(filePayload.get(decodedPayload));

		// compare MAPs

		ResourceValidationUtils.compareElements(mapOfActualParameters, paramters);

	}

}
