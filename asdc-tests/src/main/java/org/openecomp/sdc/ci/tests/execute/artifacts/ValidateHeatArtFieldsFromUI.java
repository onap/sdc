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

//
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.Rule;
//import org.junit.rules.TestName;
//import org.testng.AssertJUnit;
//import org.testng.annotations.Test;
//
//import org.openecomp.sdc.be.dao.api.ActionStatus;
//import org.openecomp.sdc.be.model.ArtifactDefinition;
//import org.openecomp.sdc.be.model.ComponentInstance;
//import org.openecomp.sdc.be.model.HeatParameterDefinition;
//import org.openecomp.sdc.be.model.Resource;
//import org.openecomp.sdc.be.model.Service;
//import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
//import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
//import org.openecomp.sdc.ci.tests.datatypes.enums.RespJsonKeysEnum;
//import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
//import org.openecomp.sdc.ci.tests.preRequisites.HeatEnvBaseTest;
//import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
//import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
//import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
//import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
//import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
//import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
//
//public class ValidateHeatArtFieldsFromUI extends HeatEnvBaseTest {
public class ValidateHeatArtFieldsFromUI {
	//
	//
	//
	// private static final String heatExtension = "yaml";
	// private static final String yangXmlExtension = "xml";
	// private static final String muranoPkgExtension = "zip";
	// private final String folderName= "yamlFieldsValidation";
	//
	//
	// private final String uuidString =
	// RespJsonKeysEnum.UUID.getRespJsonKeyName().toString();
	//
	// public ValidateHeatArtFieldsFromUI() {
	// super(name, ValidateHeatArtFieldsFromUI.class.getName());
	// }
	//
	// @Rule
	// public static TestName name = new TestName();
	//
	//
	// @Test
	// public void heatEnvValidateHeatArtFiledTypes_UpdateFailed() throws
	// Exception {
	//
	// //get relevant service
	// RestResponse serviceGetResponse =
	// ServiceRestUtils.getService(serviceDetails2, sdncDesignerDetails);
	// Service service =
	// ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
	// List<ComponentInstance> resourceInstances =
	// service.getComponentInstances();
	// ComponentInstance resourceInstance = resourceInstances.get(0);
	//
	//
	// String defaultParam = null;
	//
	// //update heatEnv with invalid value in number field
	//
	// ArtifactDefinition artifact = getCurrentArtifactDefinitionFromService();
	// String currentHeatEnvParamBeforeUpdate = replaceHeatParamsValue(artifact,
	// "home_number", "Tel Aviv");
	// ArtifactReqDetails artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// RestResponse res = ArtifactRestUtils.updateDeploymentArtifactToRI(artReq,
	// sdncDesignerDetails, resourceInstance.getUniqueId(),
	// service.getUniqueId());
	//
	// // validate negative response
	// List<String> variables = Arrays.asList("HEAT_ENV", "number",
	// "home_number");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
	// variables, res.getResponse());
	//
	// //validate current value not affected
	// artifact = getCurrentArtifactDefinitionFromService();
	// String currentHeatEnvParamAfterUpdate = getHeatParamsValue(artifact,
	// "home_number");
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBeforeUpdate,
	// currentHeatEnvParamBeforeUpdate.equals(currentHeatEnvParamAfterUpdate));
	//
	// //update heatEnv with invalid value in boolean field
	//
	// artifact = getCurrentArtifactDefinitionFromService();
	// currentHeatEnvParamBeforeUpdate = replaceHeatParamsValue(artifact,
	// "private_building", "Tel Aviv");
	// artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// res = ArtifactRestUtils.updateDeploymentArtifactToRI(artReq,
	// sdncDesignerDetails, resourceInstance.getUniqueId(),
	// service.getUniqueId());
	//
	// // validate negative response
	// variables = Arrays.asList("HEAT_ENV", "boolean", "private_building");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
	// variables, res.getResponse());
	//
	// //validate current value not affected
	// artifact = getCurrentArtifactDefinitionFromService();
	// currentHeatEnvParamAfterUpdate = getHeatParamsValue(artifact,
	// "private_building");
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBeforeUpdate,
	// currentHeatEnvParamBeforeUpdate.equals(currentHeatEnvParamAfterUpdate));
	//
	//
	//
	//
	// //update heatEnv with invalid value in boolean field
	//
	// artifact = getCurrentArtifactDefinitionFromService();
	// currentHeatEnvParamBeforeUpdate = replaceHeatParamsValue(artifact,
	// "city_name", "\uC2B5");
	// artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// res = ArtifactRestUtils.updateDeploymentArtifactToRI(artReq,
	// sdncDesignerDetails, resourceInstance.getUniqueId(),
	// service.getUniqueId());
	// // validate negative response
	// variables = Arrays.asList("HEAT_ENV", "string", "city_name");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
	// variables, res.getResponse());
	//
	// //validate current value not affected
	//
	// artifact = getCurrentArtifactDefinitionFromService();
	// currentHeatEnvParamAfterUpdate = getHeatParamsValue(artifact,
	// "city_name");
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBeforeUpdate,
	// currentHeatEnvParamBeforeUpdate.equals(currentHeatEnvParamAfterUpdate));
	// }
	//
	// @Test
	// public void heatValidateHeatArtFiledTypes_UpdateFailed() throws Exception
	// {
	//
	//
	// RestResponse checkOutResponse =
	// LifecycleRestUtils.changeResourceState(resourceSC, sdncDesignerDetails,
	// LifeCycleStatesEnum.CHECKOUT);
	// AssertJUnit.assertTrue("response code is not 200, returned: " +
	// checkOutResponse.getErrorCode(),checkOutResponse.getErrorCode() == 200);
	// //get relevant service
	// RestResponse resourceGetResponse =
	// ResourceRestUtils.getResource(resourceSC, sdncDesignerDetails);
	// Resource resource =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	//
	// //update heatEnv with invalid value in number field
	//
	// ArtifactDefinition artifact = getCurrentArtifactDefinitionFromResource();
	// String currentHeatEnvParamBeforeUpdate = replaceHeatParamsValue(artifact,
	// "home_number", "Tel Aviv");
	// ArtifactReqDetails artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// RestResponse res =
	// ArtifactRestUtils.updateInformationalArtifactToResource(artReq,
	// sdncDesignerDetails, resource.getUniqueId());
	// // validate negative response
	// List<String> variables = Arrays.asList("HEAT", "number", "home_number");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
	// variables, res.getResponse());
	//
	// //validate current value not affected
	//
	// artifact = getCurrentArtifactDefinitionFromResource();
	// String currentHeatEnvParamAfterUpdate = getHeatParamsValue(artifact,
	// "home_number");
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBeforeUpdate,
	// currentHeatEnvParamBeforeUpdate.equals(currentHeatEnvParamAfterUpdate));
	//
	//
	// //update heatEnv with invalid value in boolean field
	//
	// artifact = getCurrentArtifactDefinitionFromResource();
	// currentHeatEnvParamBeforeUpdate = replaceHeatParamsValue(artifact,
	// "private_building", "Tel Aviv");
	// artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// res = ArtifactRestUtils.updateDeploymentArtifactToResource(artReq,
	// sdncDesignerDetails, resource.getUniqueId());
	// // validate negative response
	// variables = Arrays.asList("HEAT", "boolean", "private_building");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
	// variables, res.getResponse());
	//
	// //validate current value not affected
	//
	// artifact = getCurrentArtifactDefinitionFromResource();
	//
	// currentHeatEnvParamAfterUpdate = getHeatParamsValue(artifact,
	// "private_building");
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBeforeUpdate,
	// currentHeatEnvParamBeforeUpdate.equals(currentHeatEnvParamAfterUpdate));
	//
	//
	// //update heatEnv with invalid value in boolean field
	//
	// artifact = getCurrentArtifactDefinitionFromResource();
	// currentHeatEnvParamBeforeUpdate = replaceHeatParamsValue(artifact,
	// "city_name", "\uC2B5");
	//
	// artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// res = ArtifactRestUtils.updateDeploymentArtifactToResource(artReq,
	// sdncDesignerDetails, resource.getUniqueId());
	// // validate negative response
	// variables = Arrays.asList("HEAT", "string", "city_name");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_HEAT_PARAMETER_VALUE.name(),
	// variables, res.getResponse());
	//
	// //validate current value not affected
	//
	// artifact = getCurrentArtifactDefinitionFromResource();
	// currentHeatEnvParamAfterUpdate = getHeatParamsValue(artifact,
	// "city_name");
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBeforeUpdate,
	// currentHeatEnvParamBeforeUpdate.equals(currentHeatEnvParamAfterUpdate));
	//
	// }
	//
	// @Test
	// public void heatEnvValidateHeatArtFiledTypes_boolNormalization_suc()
	// throws Exception {
	//
	// //get relevant service
	// RestResponse serviceGetResponse =
	// ServiceRestUtils.getService(serviceDetails2, sdncDesignerDetails);
	// Service service =
	// ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
	// List<ComponentInstance> resourceInstances =
	// service.getComponentInstances();
	// ComponentInstance resourceInstance = resourceInstances.get(0);
	//
	// String defaultParam = null;
	// String currentHeatEnvParamBefore = null;
	// String currentHeatEnvParamAfter = null;
	//
	//
	// List<String> listOfBoolValuesToValidate = Arrays.asList("T", "on", "1",
	// "yes", "y");
	//
	// for (String element : listOfBoolValuesToValidate) {
	//
	// //update heatEnv with invalid value in boolean field
	//
	// ArtifactDefinition artifact = getCurrentArtifactDefinitionFromService();
	// List<HeatParameterDefinition> heatParameters =
	// artifact.getHeatParameters();
	// for(HeatParameterDefinition param : heatParameters){
	// if (param.getName().equals("private_building")){
	// defaultParam = param.getDefaultValue();
	// currentHeatEnvParamBefore = param.getCurrentValue();
	// param.setCurrentValue(element);
	// }
	// }
	// artifact.setHeatParameters(heatParameters);
	// ArtifactReqDetails artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// RestResponse res = ArtifactRestUtils.updateDeploymentArtifactToRI(artReq,
	// sdncDesignerDetails, resourceInstance.getUniqueId(),
	// service.getUniqueId());
	// AssertJUnit.assertTrue("response code is not 200, returned: " +
	// res.getErrorCode(), res.getErrorCode() == 200);
	//
	// //validate current value not affected
	//
	// artifact = getCurrentArtifactDefinitionFromService();
	// heatParameters = artifact.getHeatParameters();
	// for(HeatParameterDefinition param : heatParameters){
	// if (param.getName().equals("private_building")){
	// currentHeatEnvParamAfter = param.getCurrentValue();
	// }
	// }
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBefore, currentHeatEnvParamAfter.equals("true"));
	//
	// }
	//
	// }
	//
	//
	// @Test
	// public void heatValidateHeatArtFiledTypes_boolNormalization_suc() throws
	// Exception {
	//
	// RestResponse checkOutResponse =
	// LifecycleRestUtils.changeResourceState(resourceSC, sdncDesignerDetails,
	// LifeCycleStatesEnum.CHECKOUT);
	// AssertJUnit.assertTrue("response code is not 200, returned: " +
	// checkOutResponse.getErrorCode(),checkOutResponse.getErrorCode() == 200);
	// //get relevant service
	// RestResponse resourceGetResponse =
	// ResourceRestUtils.getResource(resourceSC, sdncDesignerDetails);
	// Resource resource =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	//
	//
	// String defaultParam = null;
	// String currentHeatEnvParamBefore = null;
	// String currentHeatEnvParamAfter = null;
	//
	//
	// List<String> listOfBoolValuesToValidate = Arrays.asList("T", "on", "1",
	// "yes", "y");
	//
	// for (String element : listOfBoolValuesToValidate) {
	//
	// //update heatEnv with invalid value in boolean field
	//
	// ArtifactDefinition artifact = getCurrentArtifactDefinitionFromResource();
	// List<HeatParameterDefinition> heatParameters =
	// artifact.getHeatParameters();
	// for(HeatParameterDefinition param : heatParameters){
	// if (param.getName().equals("private_building")){
	// defaultParam = param.getDefaultValue();
	// currentHeatEnvParamBefore = param.getCurrentValue();
	// param.setCurrentValue(element);
	// }
	// }
	// artifact.setHeatParameters(heatParameters);
	// ArtifactReqDetails artReq =
	// ResponseParser.convertArtifactDefinitionToArtifactReqDetailsObject(artifact);
	// RestResponse res =
	// ArtifactRestUtils.updateInformationalArtifactToResource(artReq,
	// sdncDesignerDetails, resource.getUniqueId());
	// AssertJUnit.assertTrue("response code is not 200, returned: " +
	// res.getErrorCode(), res.getErrorCode() == 200);
	//
	// //validate current value not affected
	//
	// artifact = getCurrentArtifactDefinitionFromResource();
	// heatParameters = artifact.getHeatParameters();
	// for(HeatParameterDefinition param : heatParameters){
	// if (param.getName().equals("private_building")){
	// currentHeatEnvParamAfter = param.getCurrentValue();
	// }
	// }
	// AssertJUnit.assertTrue("HeatEnvParam was not updated: " +
	// currentHeatEnvParamBefore, currentHeatEnvParamAfter.equals("true"));
	//
	// }
	//
	// }
	//
	//
	// private ArtifactDefinition getCurrentArtifactDefinitionFromResource()
	// throws Exception {
	//
	// RestResponse resourceGetResponse =
	// ResourceRestUtils.getResource(resourceSC, sdncDesignerDetails);
	// Resource resource =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	//
	// ArtifactDefinition artifactDefinition =
	// resource.getDeploymentArtifacts().get("heat");
	//
	//
	// return artifactDefinition;
	//
	// }
	//
	//
	// private ArtifactDefinition getCurrentArtifactDefinitionFromService()
	// throws Exception {
	//
	// RestResponse serviceGetResponse =
	// ServiceRestUtils.getService(serviceDetails2, sdncDesignerDetails);
	// Service service =
	// ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
	// ArtifactDefinition artifactDefinition = new ArtifactDefinition();
	// artifactDefinition =
	// service.getComponentInstances().get(0).getDeploymentArtifacts().get("heatenv");
	//
	//
	// return artifactDefinition;
	//
	// }
	//
	// private String replaceHeatParamsValue(ArtifactDefinition artifact, String
	// paramName, String paramValue) {
	// String defaultParam;
	// String currentHeatEnvParam = null;
	// List<HeatParameterDefinition> heatParameters =
	// artifact.getHeatParameters();
	// for(HeatParameterDefinition param : heatParameters){
	// if (param.getName().equals(paramName)){
	// defaultParam = param.getDefaultValue();
	// currentHeatEnvParam = param.getCurrentValue();
	// param.setCurrentValue(paramValue);
	// }
	// }
	// artifact.setHeatParameters(heatParameters);
	// return currentHeatEnvParam;
	// }
	//
	// private String getHeatParamsValue(ArtifactDefinition artifact,
	// String paramName) {
	// List<HeatParameterDefinition> heatParameters =
	// artifact.getHeatParameters();
	// String currentHeatEnvParamValue = null;
	// for(HeatParameterDefinition param : heatParameters){
	// if (param.getName().equals(paramName)){
	// currentHeatEnvParamValue = param.getCurrentValue();
	// }
	// }
	// return currentHeatEnvParamValue;
	// }
}
