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

//import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.cassandra.CassandraUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.datastax.driver.core.Row;

import fj.data.Either;

public class PlaceHolderValidations extends ComponentBaseTest {
	private static Logger logger = LoggerFactory.getLogger(PlaceHolderValidations.class.getName());
	private static final String heatExtension = "yaml";
	// private static final String yangXmlExtension = "xml";
	// private static final String muranoPkgExtension = "zip";
	private final String folderName = "addHeatArtifactToServiceAndSertify";
	private Resource resource;
	private final int timeOut = 60;
	private ArtifactReqDetails updateArtifactReqDetails = null;
	protected User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	protected User sdncDesignerDetails2 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2);
	protected ResourceReqDetails resourceDetails1;
	protected ResourceReqDetails resourceVF;
	protected ResourceReqDetails resourceCP;
	protected ResourceReqDetails resourceVL;

	protected ArtifactReqDetails heatArtifactDetails;
	protected ArtifactReqDetails heatVolArtifactDetails;
	protected ArtifactReqDetails heatNetArtifactDetails;

	public PlaceHolderValidations() {
		super(name, PlaceHolderValidations.class.getName());
	}

	@Rule
	public static TestName name = new TestName();

	@BeforeMethod
	public void init() throws IOException, Exception {

		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatNetArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_NET.getType());
		heatVolArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_VOL.getType());
		Resource resourceObject = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resourceDetails1 = new ResourceReqDetails(resourceObject);
		resourceObject = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true)
				.left().value();
		resourceVF = new ResourceReqDetails(resourceObject);
		resourceObject = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.CP, UserRoleEnum.DESIGNER, true)
				.left().value();
		resourceCP = new ResourceReqDetails(resourceObject);
		resourceObject = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VL, UserRoleEnum.DESIGNER, true)
				.left().value();
		resourceVL = new ResourceReqDetails(resourceObject);
	}

	@Test
	public void validateDeploymentPlaceHoldersByConfig() throws IOException {
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails1, sdncDesignerDetails1);
		Resource resourceObject = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceObject.getDeploymentArtifacts();
		assertNotNull("deploymentArtifacts list is null", deploymentArtifacts);
		List<String> listOfResDepArtTypesFromConfig = Utils.getListOfDepResArtLabels(true);
		assertNotNull("deployment artifact types list is null", listOfResDepArtTypesFromConfig);
		for (String resDepArtType : listOfResDepArtTypesFromConfig) {
			assertNotNull("placeholder of " + resDepArtType + " type doesn't exist",
					deploymentArtifacts.get(resDepArtType));
		}
	}

	private void validateToscaArtifactsBeforeAndAfterSFT(ResourceReqDetails resourceDetails)
			throws IOException, Exception {
		RestResponse componentResponse = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails1);
		Component component = ResponseParser.convertResourceResponseToJavaObject(componentResponse.getResponse());
		Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
		for (ArtifactDefinition artifact : toscaArtifacts.values()) {
			assertNull(artifact.getEsId());
		}

		componentResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails1,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		component = ResponseParser.convertResourceResponseToJavaObject(componentResponse.getResponse());
		toscaArtifacts = component.getToscaArtifacts();

		for (ArtifactDefinition artifact : toscaArtifacts.values()) {
			assertEquals(artifact.getEsId(), artifact.getUniqueId());
			List<Pair<String, String>> fields = new ArrayList();
			fields.add(new Pair<String, String>("id", artifact.getEsId()));
			List<Row> fetchFromTable = CassandraUtils.fetchFromTableQuery("sdcartifact", "resources", fields);
			assertTrue(1 == fetchFromTable.size());
		}
	}

	@Test
	public void validateToscaArtifactsBeforeAndAfterSFT() throws IOException, Exception {
		// TODO ADD VF and Service
		validateToscaArtifactsBeforeAndAfterSFT(resourceDetails1);
		validateToscaArtifactsBeforeAndAfterSFT(resourceCP);
		validateToscaArtifactsBeforeAndAfterSFT(resourceVL);
	}

	@Test
	public void validateToscaPlaceHoldersByConfig() throws IOException, Exception {
		List<Component> components = new ArrayList<>();
		RestResponse componentGetResponse = ResourceRestUtils.getResource(resourceDetails1, sdncDesignerDetails1);
		components.add(ResponseParser.convertResourceResponseToJavaObject(componentGetResponse.getResponse()));

		componentGetResponse = ResourceRestUtils.getResource(resourceCP, sdncDesignerDetails1);
		components.add(ResponseParser.convertResourceResponseToJavaObject(componentGetResponse.getResponse()));

		componentGetResponse = ResourceRestUtils.getResource(resourceVF, sdncDesignerDetails1);
		components.add(ResponseParser.convertResourceResponseToJavaObject(componentGetResponse.getResponse()));

		componentGetResponse = ResourceRestUtils.getResource(resourceVL, sdncDesignerDetails1);
		components.add(ResponseParser.convertResourceResponseToJavaObject(componentGetResponse.getResponse()));

		Service service = AtomicOperationUtils
				.createServiceByCategory(ServiceCategoriesEnum.MOBILITY, UserRoleEnum.DESIGNER, true).left().value();
		componentGetResponse = ServiceRestUtils.getService(service.getUniqueId(),
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		components.add(ResponseParser.parseToObjectUsingMapper(componentGetResponse.getResponse(), Service.class));

		for (Component component : components) {
			Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
			assertNotNull("toscaArtifacts list is null", toscaArtifacts);
			List<String> listOfToscaArtTypesFromConfig = Utils.getListOfToscaArtLabels(true);
			assertNotNull("tosca artifact types list is null", listOfToscaArtTypesFromConfig);
			for (String toscaArtType : listOfToscaArtTypesFromConfig) {
				assertNotNull("placeholder of " + toscaArtType + " type doesn't exist",
						toscaArtifacts.get(toscaArtType));
			}
		}

	}

	// test check configuration of "displayName" field for "heat" type
	// deployment artifact
	@Test
	public void validateDeploymentPlaceHoldersDescriptionOfHeatByConfig() throws IOException {

		Map<String, Object> mapOfDepResArtTypesObjects = getMapOfDepResArtTypesObjects();
		assertNotNull("deployment artifact types list is null", mapOfDepResArtTypesObjects);
		Object object = mapOfDepResArtTypesObjects.get("heat");
		if (object instanceof Map<?, ?>) {
			Map<String, Object> map = (Map<String, Object>) object;
			assertTrue(map.get("displayName").equals("Base HEAT Template"));
		} else {
			assertTrue("return object does not instance of map", false);
		}
	}

	@Test
	public void addDepResArtEachType() throws Exception {

		String artType;

		addDeploymentArtifactByTypeToResource(resourceDetails1, heatArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatVolArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatNetArtifactDetails);
		RestResponse response = ResourceRestUtils.getResource(resourceDetails1.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		List<String> listOfResDepArtTypesFromConfig = Utils.getListOfDepResArtLabels(true);
		assertNotNull("deployment artifact types list is null", listOfResDepArtTypesFromConfig);
		for (String iter : listOfResDepArtTypesFromConfig) {
			artType = iter;
			verifyDepArtPlaceHoldersByType(artType);
		}
	}

	@Test
	public void checkHeatParametersExistingForEachType() throws Exception {

		String artType;

		addDeploymentArtifactByTypeToResource(resourceDetails1, heatArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatVolArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatNetArtifactDetails);
		RestResponse response = ResourceRestUtils.getResource(resourceDetails1.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		List<String> listOfResDepArtTypesFromConfig = Utils.getListOfDepResArtLabels(true);
		assertNotNull("deployment artifact types list is null", listOfResDepArtTypesFromConfig);
		for (String iter : listOfResDepArtTypesFromConfig) {
			artType = iter;
			verifyDepArtPlaceHoldersByType(artType);
			verifyHeatParametersExistance(artType, false);
		}
	}

	@Test
	public void checkHeatParametersExistingForSpecificType() throws Exception {

		String artType;

		addDeploymentArtifactByTypeToResource(resourceDetails1, heatVolArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatNetArtifactDetails);
		RestResponse response = ResourceRestUtils.getResource(resourceDetails1.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		List<String> listOfResDepArtTypesFromConfig = Utils.getListOfDepResArtLabels(true);
		assertNotNull("deployment artifact types list is null", listOfResDepArtTypesFromConfig);
		for (String iter : listOfResDepArtTypesFromConfig) {
			artType = iter;
			if (heatArtifactDetails.getArtifactLabel().equals(iter)) {
				verifyHeatParametersExistance(artType, true);
			} else {
				verifyHeatParametersExistance(artType, false);
			}
		}
	}

	@Test
	public void addAndDeleteDepResArtEachType() throws Exception {

		String artType;

		addDeploymentArtifactByTypeToResource(resourceDetails1, heatArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatVolArtifactDetails);
		addDeploymentArtifactByTypeToResource(resourceDetails1, heatNetArtifactDetails);
		RestResponse response = ResourceRestUtils.getResource(resourceDetails1.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		List<String> listOfResDepArtTypesFromConfig = Utils.getListOfDepResArtLabels(true);
		assertNotNull("deployment artifact types list is null", listOfResDepArtTypesFromConfig);
		for (String iter : listOfResDepArtTypesFromConfig) {
			artType = iter;
			verifyDepArtPlaceHoldersByType(artType);
		}
		RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails1,
				sdncDesignerDetails1, LifeCycleStatesEnum.CHECKIN);
		assertTrue("expected response code in CHECKIN 200", restResponseResource.getErrorCode() == 200);
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails1, sdncDesignerDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		assertTrue("expected response code in CHECKOUT 200", restResponseResource.getErrorCode() == 200);

		// delete all deployment artifacts
		deleteDeploymentArtifactByTypeToResource(resourceDetails1, heatArtifactDetails);
		deleteDeploymentArtifactByTypeToResource(resourceDetails1, heatVolArtifactDetails);
		deleteDeploymentArtifactByTypeToResource(resourceDetails1, heatNetArtifactDetails);
		response = ResourceRestUtils.getResource(resourceDetails1.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		listOfResDepArtTypesFromConfig = Utils.getListOfDepResArtLabels(true);
		assertNotNull("deployment artifact types list is null", listOfResDepArtTypesFromConfig);
		for (String iter : listOfResDepArtTypesFromConfig) {
			artType = iter;
			verifyDepArtPlaceHoldersByType(artType);
		}
	}

	@Test
	public void addRemoveAddAgainArtifact() throws Exception {

		// get MAP before upload artifact
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails1, sdncDesignerDetails1);
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();

		ArtifactDefinition artifactDefinition = deploymentArtifacts.get("heat");

		// validate place holder exist
		assertNotNull(artifactDefinition);

		// add artifact
		updateArtifactReqDetails = getUpdateArtifactDetails(ArtifactTypeEnum.HEAT.getType());

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(
				updateArtifactReqDetails, sdncDesignerDetails1, resourceDetails1.getUniqueId());
		logger.debug("addInformationalArtifactToResource response:  "
				+ addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 200);

		ArtifactDefinition artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addInformationalArtifactToResource.getResponse());
		ArtifactDefinition artDef1 = fillArtDefFromResponse(artifactDefinitionResponseJavaObject);

		// remove artifact
		RestResponse deleteArtifactFromResource = ArtifactRestUtils.deleteInformationalArtifactFromResource(
				resourceDetails1.getUniqueId(), updateArtifactReqDetails, sdncDesignerDetails1);
		logger.debug(
				"addInformationalArtifactToResource response:  " + deleteArtifactFromResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + deleteArtifactFromResource.getErrorCode(),
				deleteArtifactFromResource.getErrorCode() == 200);

		RestResponse getResourceResp = ResourceRestUtils.getResource(resourceDetails1, sdncDesignerDetails1);

		artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(deleteArtifactFromResource.getResponse());
		assertTrue(artifactDefinitionResponseJavaObject.getArtifactName().isEmpty());
		assertTrue(artifactDefinitionResponseJavaObject.getDescription().isEmpty());
		assertTrue(artifactDefinitionResponseJavaObject.getArtifactChecksum().isEmpty());
		assertTrue(artifactDefinitionResponseJavaObject.getEsId().isEmpty());
		assertTrue(artifactDefinitionResponseJavaObject.getArtifactUUID().isEmpty());
		assertNull(artifactDefinitionResponseJavaObject.getHeatParameters());

		// add artifact again with different user
		addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails1, resourceDetails1.getUniqueId());
		logger.debug("addInformationalArtifactToResource response:  "
				+ addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 200);

		artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addInformationalArtifactToResource.getResponse());
		ArtifactDefinition artDef2 = fillArtDefFromResponse(artifactDefinitionResponseJavaObject);

		assertFalse("check artifact checksum", artDef1.getArtifactChecksum().equals(artDef2.getArtifactChecksum()));
		assertTrue("check artifact EsId", artDef1.getEsId().equals(artDef2.getEsId()));
		assertFalse("check artifact UUID", artDef1.getArtifactUUID().equals(artDef2.getArtifactUUID()));
		assertTrue("check UserIdCreator", artDef1.getUserIdCreator().equals(artDef2.getUserIdCreator()));
		assertTrue("check UserIdLastUpdater", artDef1.getUserIdLastUpdater().equals(artDef2.getUserIdLastUpdater()));
	}

	@Test
	public void addUpdateArtifactByType() throws Exception {

		// get MAP before upload artifact
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails1, sdncDesignerDetails1);
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();

		ArtifactDefinition artifactDefinition = deploymentArtifacts.get("heat");

		// validate place holder exist
		assertNotNull(artifactDefinition);

		// add artifact
		updateArtifactReqDetails = getUpdateArtifactDetails(ArtifactTypeEnum.HEAT.getType());

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(
				updateArtifactReqDetails, sdncDesignerDetails1, resourceDetails1.getUniqueId());
		logger.debug("addInformationalArtifactToResource response:  "
				+ addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 200);

		ArtifactDefinition artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addInformationalArtifactToResource.getResponse());
		ArtifactDefinition artDef1 = fillArtDefFromResponse(artifactDefinitionResponseJavaObject);

		RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails1,
				sdncDesignerDetails1, LifeCycleStatesEnum.CHECKIN);
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails1, sdncDesignerDetails2,
				LifeCycleStatesEnum.CHECKOUT);

		// update with different user artifact
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setUniqueId(artifactDefinition.getUniqueId());
		heatArtifactDetails.setArtifactName("2.yaml");
		heatArtifactDetails.setArtifactLabel(artifactDefinition.getArtifactLabel());

		addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails2, resourceDetails1.getUniqueId(), "heat");
		logger.debug("addInformationalArtifactToResource response:  "
				+ addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 200);

		artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addInformationalArtifactToResource.getResponse());
		ArtifactDefinition artDef2 = fillArtDefFromResponse(artifactDefinitionResponseJavaObject);
		verifyArtDefFields(artDef1, artDef2);

	}

	@Test
	public void addUpdateDeleteArtifact() throws Exception {

		// get MAP before upload artifact
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(resourceDetails1, sdncDesignerDetails1);
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();

		ArtifactDefinition artifactDefinition = deploymentArtifacts.get("heat");

		// validate place holder exist
		assertNotNull(artifactDefinition);

		updateArtifactReqDetails = getUpdateArtifactDetails(ArtifactTypeEnum.HEAT.getType());

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(
				updateArtifactReqDetails, sdncDesignerDetails1, resourceDetails1.getUniqueId());
		logger.debug("addInformationalArtifactToResource response:  "
				+ addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 200);

		ArtifactDefinition artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addInformationalArtifactToResource.getResponse());
		ArtifactDefinition artDef1 = fillArtDefFromResponse(artifactDefinitionResponseJavaObject);

		RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails1,
				sdncDesignerDetails1, LifeCycleStatesEnum.CHECKIN);
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails1, sdncDesignerDetails2,
				LifeCycleStatesEnum.CHECKOUT);

		// update with different user artifact
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setArtifactName("2.yaml");

		addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails2, resourceDetails1.getUniqueId(), "heat");
		logger.debug("addInformationalArtifactToResource response:  "
				+ addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + addInformationalArtifactToResource.getErrorCode(),
				addInformationalArtifactToResource.getErrorCode() == 200);

		artifactDefinitionResponseJavaObject = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addInformationalArtifactToResource.getResponse());
		ArtifactDefinition artDef2 = fillArtDefFromResponse(artifactDefinitionResponseJavaObject);

		verifyArtDefFields(artDef1, artDef2);

		RestResponse delteArtifactFromResource = ArtifactRestUtils.deleteInformationalArtifactFromResource(
				resourceDetails1.getUniqueId(), heatArtifactDetails, sdncDesignerDetails2);
		logger.debug("addInformationalArtifactToResource response: {} ",delteArtifactFromResource.getResponseMessage());
		assertTrue("response code is not 200, returned :" + delteArtifactFromResource.getErrorCode(),
				delteArtifactFromResource.getErrorCode() == 200);

	}

	@Test
	public void addHeatVolArtInvalidExtension() throws Exception {

		heatVolArtifactDetails.setArtifactName("heatVol.txt");
		RestResponse response = getResponseOnAddDeploymentArtifactByTypeToResource(resourceDetails1,
				heatVolArtifactDetails);
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name());
		assertEquals("Check response code after upload artifact", errorInfo.getCode(), response.getErrorCode());
		List<String> variables = Arrays.asList(ArtifactTypeEnum.HEAT_VOL.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name(), variables,
				response.getResponse());
	}

	@Test
	public void addHeatNetArtInvalidExtension() throws Exception {

		heatNetArtifactDetails.setArtifactName("yaml");
		RestResponse response = getResponseOnAddDeploymentArtifactByTypeToResource(resourceDetails1,
				heatNetArtifactDetails);
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name());
		assertEquals("Check response code after upload artifact", errorInfo.getCode(), response.getErrorCode());
		List<String> variables = Arrays.asList(ArtifactTypeEnum.HEAT_NET.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name(), variables,
				response.getResponse());
	}

	@Test
	public void checkServiceSecurityTemplateInformationalArtifactsCreation() throws IOException, Exception {

		Either<Service, RestResponse> createServiceResponse = AtomicOperationUtils
				.createServiceByCategory(ServiceCategoriesEnum.MOBILITY, UserRoleEnum.DESIGNER, true);
		Map<String, ArtifactDefinition> artifacts = null;
		ArtifactDefinition securitytemplate = null;
		if (createServiceResponse.isLeft()) {
			Component component = createServiceResponse.left().value();
			artifacts = component.getArtifacts();
			securitytemplate = artifacts.get("servicesecuritytemplate");
			assertNotNull(securitytemplate);
			assertEquals("Service Security Template", securitytemplate.getArtifactDisplayName());
		} else {
			logger.debug("checkSecurityTemplateInformationalArtifactsCreation service creation response:  "
					+ createServiceResponse.right().value().getResponseMessage());
		}
	}

	@Test
	public void checkResourceSecurityTemplateInformationalArtifactsCreation() throws IOException, Exception {

		Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.CONTAINER_APPLICATION, ResourceCategoryEnum.APPLICATION_L4_BORDER,
				UserRoleEnum.DESIGNER, true).left().value();
		Map<String, ArtifactDefinition> artifacts = resource.getArtifacts();
		ArtifactDefinition securitytemplate = artifacts.get("resourcesecuritytemplate");
		assertNotNull(securitytemplate);
		assertEquals("Resource Security Template", securitytemplate.getArtifactDisplayName());
	}

	// Benny
	@Test
	public void serviceSecurityTemplateInformationalArtifact() throws IOException, Exception {
		String artifactPlaceHolder = "servicesecuritytemplate";
		Service service = AtomicOperationUtils
				.createServiceByCategory(ServiceCategoriesEnum.MOBILITY, UserRoleEnum.DESIGNER, true).left().value();
		Map<String, ArtifactDefinition> artifacts = service.getArtifacts();
		ArtifactDefinition securitytemplate = artifacts.get(artifactPlaceHolder);
		assertNotNull(securitytemplate);
		assertEquals("Service Security Template", securitytemplate.getArtifactDisplayName());
		assertEquals("OTHER", securitytemplate.getArtifactType());
		assertEquals(artifactPlaceHolder, securitytemplate.getArtifactLabel());
		// Get service
		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId(),
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, getService.getErrorCode().intValue());
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		artifacts = service.getArtifacts();
		securitytemplate = artifacts.get(artifactPlaceHolder);
		assertNotNull(securitytemplate);
		assertEquals("Service Security Template", securitytemplate.getArtifactDisplayName());
		assertEquals("OTHER", securitytemplate.getArtifactType());
		assertEquals(artifactPlaceHolder, securitytemplate.getArtifactLabel());
	}

	@Test
	public void resourceSecurityTemplateInformationalArtifacts() throws IOException, Exception {
		String artifactPlaceHolder = "resourcesecuritytemplate";
		Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.CONTAINER_APPLICATION, ResourceCategoryEnum.APPLICATION_L4_BORDER,
				UserRoleEnum.DESIGNER, true).left().value();
		Map<String, ArtifactDefinition> artifacts = resource.getArtifacts();
		ArtifactDefinition securitytemplate = artifacts.get("resourcesecuritytemplate");
		assertNotNull(securitytemplate);
		assertEquals("Resource Security Template", securitytemplate.getArtifactDisplayName());
		assertEquals("OTHER", securitytemplate.getArtifactType());
		assertEquals(artifactPlaceHolder, securitytemplate.getArtifactLabel());
		// Get resource
		RestResponse getresource = ResourceRestUtils.getResource(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				resource.getUniqueId());
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, getresource.getErrorCode().intValue());
		resource = ResponseParser.parseToObjectUsingMapper(getresource.getResponse(), Resource.class);
		artifacts = resource.getArtifacts();
		securitytemplate = artifacts.get(artifactPlaceHolder);
		assertNotNull(securitytemplate);
		assertEquals("Resource Security Template", securitytemplate.getArtifactDisplayName());
		assertEquals("OTHER", securitytemplate.getArtifactType());
		assertEquals(artifactPlaceHolder, securitytemplate.getArtifactLabel());
	}

	// ================================================

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMapOfDepResArtTypesObjects() throws FileNotFoundException {

		return (Map<String, Object>) Utils.parseYamlConfig("deploymentResourceArtifacts");

	}

	private void addDeploymentArtifactByTypeToResource(ResourceReqDetails resourceReqDetails,
			ArtifactReqDetails artReqDetails) throws IOException, Exception {

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(artReqDetails,
				sdncDesignerDetails1, resourceReqDetails.getUniqueId());
		assertTrue("add" + artReqDetails.getArtifactLabel() + " artifact to resource request returned status:"
				+ response.getErrorCode(), response.getErrorCode() == 200);
	}

	private RestResponse getResponseOnAddDeploymentArtifactByTypeToResource(ResourceReqDetails resourceReqDetails,
			ArtifactReqDetails artReqDetails) throws IOException, Exception {

		return ArtifactRestUtils.addInformationalArtifactToResource(artReqDetails, sdncDesignerDetails1,
				resourceReqDetails.getUniqueId());
	}

	private void deleteDeploymentArtifactByTypeToResource(ResourceReqDetails resourceReqDetails,
			ArtifactReqDetails artReqDetails) throws IOException, Exception {

		RestResponse response = ArtifactRestUtils.deleteInformationalArtifactFromResource(
				resourceReqDetails.getUniqueId(), artReqDetails, sdncDesignerDetails1);
		assertTrue("delete" + artReqDetails.getArtifactLabel() + " artifact to resource request returned status:"
				+ response.getErrorCode(), response.getErrorCode() == 200);
	}

	private void verifyDepArtPlaceHoldersByType(String artType) {

		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		assertNotNull("deployment artifact data is null", deploymentArtifacts.get(artType));
		assertNotNull("deployment artifact data is null", deploymentArtifacts.get(artType).getEsId());
		assertNotNull("deployment artifact data is null", deploymentArtifacts.get(artType).getDescription());
		assertTrue(
				"deployment artifact timeout does not equal to default value " + timeOut + " expected " + timeOut
						+ ", actual - " + deploymentArtifacts.get(artType).getTimeout(),
				deploymentArtifacts.get(artType).getTimeout() == timeOut);
		assertTrue("deployment artifact label value ",
				deploymentArtifacts.get(artType).getArtifactLabel().equals(artType));
	}

	private void verifyHeatParametersExistance(String artType, Boolean isNull) {
		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		if (isNull) {
			assertNull("heatParameters list for type " + artType + " is not null",
					deploymentArtifacts.get(artType).getHeatParameters());
		} else {
			assertNotNull("heatParameters list for type " + artType + " is null",
					deploymentArtifacts.get(artType).getHeatParameters());
		}
	}

	private void verifyArtDefFields(ArtifactDefinition artDef1, ArtifactDefinition artDef2) {

		assertFalse("check artifact checksum", artDef1.getArtifactChecksum().equals(artDef2.getArtifactChecksum()));
		assertFalse("check artifact EsId", artDef1.getEsId().equals(artDef2.getEsId()));
		assertFalse("check artifact UUID", artDef1.getArtifactUUID().equals(artDef2.getArtifactUUID()));
		assertTrue("check UserIdCreator", artDef1.getUserIdCreator().equals(artDef2.getUserIdCreator()));
		assertFalse("check UserIdLastUpdater", artDef1.getUserIdLastUpdater().equals(artDef2.getUserIdLastUpdater()));

	}

	private ArtifactDefinition fillArtDefFromResponse(ArtifactDefinition artifactDefinitionResponseJavaObject) {
		ArtifactDefinition artDef = new ArtifactDefinition();
		artDef.setArtifactChecksum(artifactDefinitionResponseJavaObject.getArtifactChecksum());
		artDef.setEsId(artifactDefinitionResponseJavaObject.getEsId());
		artDef.setArtifactUUID(artifactDefinitionResponseJavaObject.getArtifactUUID());
		artDef.setUserIdCreator(artifactDefinitionResponseJavaObject.getUserIdCreator());
		artDef.setUserIdLastUpdater(artifactDefinitionResponseJavaObject.getUserIdLastUpdater());
		return artDef;
	}

	private ArtifactReqDetails getUpdateArtifactDetails(String artType) throws IOException, Exception {
		String ext = heatExtension;
		String sourceDir = config.getResourceConfigDir();
		String testResourcesPath = sourceDir + File.separator + folderName;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}",listFileName.toString());

		String payload = FileUtils.loadPayloadFile(listFileName, ext, true);
		ArtifactReqDetails updateArtifactReqDetails = ElementFactory.getDefaultDeploymentArtifactForType(artType);
		updateArtifactReqDetails.setPayload(payload);
		updateArtifactReqDetails.setArtifactName("1.yaml");
		return updateArtifactReqDetails;
	}
}
