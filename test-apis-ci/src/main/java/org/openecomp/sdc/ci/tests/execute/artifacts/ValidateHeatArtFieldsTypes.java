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

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.RespJsonKeysEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ArtifactValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ValidateHeatArtFieldsTypes extends ComponentBaseTest {

	protected User sdncDesignerDetails;
	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;

	private static final String heatExtension = "yaml";
	private static final String yangXmlExtension = "xml";
	private static final String muranoPkgExtension = "zip";
	private final String folderName = "yamlFieldsValidation";

	private final String uuidString = RespJsonKeysEnum.UUID.getRespJsonKeyName().toString();

	public ValidateHeatArtFieldsTypes() {
		super(name, ValidateHeatArtFieldsTypes.class.getName());
	}

	@Rule
	public static TestName name = new TestName();

	@Test
	public void validateHeatArtFiledTypes() throws Exception {

		// get relevant resource and service

		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		resourceDetails = ElementFactory.getDefaultResource();

		RestResponse response = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails);
		AssertJUnit.assertTrue("create request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 201);

		// add artifact to resource1

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		List<String> listOfArtifactFromFolder = ArtifactValidationUtils.getListOfArtifactFromFolder(folderName);
		for (int i = 0; i < listOfArtifactFromFolder.size(); i++) {
			heatArtifactDetails = ArtifactValidationUtils.replaceDefaultArtWithArtFromList(heatArtifactDetails,
					heatExtension, folderName, i);
			response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails,
					resourceDetails.getUniqueId());

			if (heatArtifactDetails.getArtifactName().contains("bool")) {
				if (heatArtifactDetails.getArtifactName().contains("negative")) {
					// validate negative response
					List<String> variables = Arrays.asList("HEAT", "boolean", "city_name");
					ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
							variables, response.getResponse());
				}
				if (heatArtifactDetails.getArtifactName().contains("positive")) {
					AssertJUnit.assertTrue(
							"add HEAT artifact to resource request returned status:" + response.getErrorCode()
									+ " fileName: " + heatArtifactDetails.getArtifactName(),
							response.getErrorCode() == 200);
					ArtifactDefinition artifactDefinitionJavaObject = ResponseParser
							.convertArtifactDefinitionResponseToJavaObject(response.getResponse());
					List<HeatParameterDataDefinition> heatParameters = artifactDefinitionJavaObject.getHeatParameters();
					String currentValue = null;
					for (HeatParameterDataDefinition heatParameterDefinition : heatParameters) {
						if (heatParameterDefinition.getName().equals("city_name")) {
							currentValue = heatParameterDefinition.getCurrentValue();
						}
					}
					if (heatArtifactDetails.getArtifactName().contains("true")) {
						AssertJUnit.assertTrue(currentValue.equals("true"));
					}
					if (heatArtifactDetails.getArtifactName().contains("false")) {
						AssertJUnit.assertTrue(currentValue.equals("false"));
					}
					RestResponse deleteInformationalArtifactFromResource = ArtifactRestUtils
							.deleteInformationalArtifactFromResource(resourceDetails.getUniqueId(), heatArtifactDetails,
									sdncDesignerDetails);
					AssertJUnit.assertTrue(
							"delete HEAT artifact from resource request returned status:"
									+ deleteInformationalArtifactFromResource.getErrorCode(),
							deleteInformationalArtifactFromResource.getErrorCode() == 200);
				}

			} else if (heatArtifactDetails.getArtifactName().contains("number")) {
				if (heatArtifactDetails.getArtifactName().contains("negative")) {
					// validate negative response
					List<String> variables = Arrays.asList("HEAT", "number", "city_name");
					ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
							variables, response.getResponse());
				}
				if (heatArtifactDetails.getArtifactName().contains("positive")) {
					AssertJUnit.assertTrue(
							"add HEAT artifact to resource request returned status:" + response.getErrorCode()
									+ " fileName: " + heatArtifactDetails.getArtifactName(),
							response.getErrorCode() == 200);
				}

			} else if (heatArtifactDetails.getArtifactName().contains("string")) {
				if (heatArtifactDetails.getArtifactName().contains("negative")) {
					// validate negative response
					List<String> variables = Arrays.asList("HEAT", "string", "city_name");
					ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
							variables, response.getResponse());
				}
				if (heatArtifactDetails.getArtifactName().contains("positive")) {
					AssertJUnit.assertTrue(
							"add HEAT artifact to resource request returned status:" + response.getErrorCode()
									+ " fileName: " + heatArtifactDetails.getArtifactName(),
							response.getErrorCode() == 200);
				}

			}

			else if (heatArtifactDetails.getArtifactName().contains("unsupported")) {

				// validate negative response
				List<String> variables = Arrays.asList("HEAT", "number123");
				ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_TYPE.name(),
						variables, response.getResponse());

			}

			else {
				AssertJUnit.assertTrue(
						"add HEAT artifact to resource request returned status:" + response.getErrorCode(),
						response.getErrorCode() == 200);
			}
		}

	}

}
