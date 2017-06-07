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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ArtifactValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CrudArt extends ComponentBaseTest {

	private static Logger logger = LoggerFactory.getLogger(CrudArt.class.getName());
	private static final User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

	private static final String HEAT_NET_LABEL = "heatnet";
	private static final String HEAT_LABEL = "heat";

	protected String testResourcesPath;
	protected String testResourcesInstancesPath;

	protected static final String dcaeInventoryToscaFile = "toscaSampleArtifact.yml";
	protected static final String dcaeInventoryJsonFile = "jsonSampleArtifact.json";
	protected static final String dcaeInventoryPolicyFile = "emfSampleArtifact.emf";
	protected static final String dcaeInventoryDocFile = "docSampleArtifact.doc";
	protected static final String dcaeInventoryBlueprintFile = "bluePrintSampleArtifact.xml";
	protected static final String dcaeInventoryEventFile = "eventSampleArtifact.xml";

	protected static final String heatSuccessFile = "asc_heat 0 2.yaml";
	protected static final String heatNetSuccessFile = "asc_heat_net 0 2.yaml";
	protected static final String yangFile = "addYangXmlArtifactToResource.xml";
	protected static final String jsonFile = "jsonArtifact.json";
	protected static final String invalidJsonFile = "invalidJson.json";
	protected static final String invalidYangFile = "invalidYangXml.xml";
	protected static final String otherFile = "other.txt";
	protected static final String muranoFile = "asc_heat 0 2.zip";
	protected static final String heatSuccessMiniFile = "heat_mini.yaml";
	protected static final String heatInvalidFormat = "heatInvalidFormat.yaml";
	protected static final String yamlInvalidFormat = "invalidYamlFormat.yaml";
	protected static final String heatEnvfile = "heatEnvfile.env";

	protected ServiceReqDetails serviceDetails;
	protected ResourceReqDetails vfResourceDetails;
	protected ResourceReqDetails cpResourceDetails;
	protected ResourceReqDetails vfcResourceDetails;
	protected ResourceReqDetails vlResourceDetails;

	@Rule
	public static TestName name = new TestName();

	public CrudArt() {
		super(name, CrudArt.class.getName());
	}

	@DataProvider
	private static final Object[][] getDepArtByType() throws IOException, Exception {
		return new Object[][] { { ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType()) }, { ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_VOL.getType()) },
				{ ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_NET.getType()) }, { ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()) },
				{ ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType()) }, { ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType()) },
				{ ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType()) }, { ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType()) },
				{ ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType()) } };
	}

	@DataProvider
	private static final Object[][] getServiceDepArtByType() throws IOException, Exception {
		return new Object[][] { { ArtifactTypeEnum.OTHER.getType() }, { ArtifactTypeEnum.YANG_XML.getType() }, };
	}

	@BeforeMethod
	public void init() throws Exception {
		// Set files working directory
		String sourceDir = config.getResourceConfigDir();
		String workDir = "HeatDeploymentArtifacts";
		testResourcesPath = sourceDir + File.separator + workDir;
		String workDirResourceInstanceArtifacts = "ResourceInstanceArtifacts";
		testResourcesInstancesPath = sourceDir + File.separator + workDirResourceInstanceArtifacts;

		// Build the components
		Service serviceObj = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		serviceDetails = new ServiceReqDetails(serviceObj);

		Resource vfcResourceObj = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		vfcResourceDetails = new ResourceReqDetails(vfcResourceObj);

		Resource vfResourceObj = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		vfResourceDetails = new ResourceReqDetails(vfResourceObj);

		Resource cpResourceObj = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.CP, UserRoleEnum.DESIGNER, true).left().value();
		cpResourceDetails = new ResourceReqDetails(cpResourceObj);

		Resource vlResourceObj = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VL, UserRoleEnum.DESIGNER, true).left().value();
		vlResourceDetails = new ResourceReqDetails(vlResourceObj);
	}

	// ---------------------------------Resource
	// success--------------------------------
	@Test
	public void addHeatArtifactToResourceAndCertify() throws Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName.toString());

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// certified resource
		RestResponse changeResourceState = LifecycleRestUtils.certifyResource(vfResourceDetails);
		int status = changeResourceState.getErrorCode();
		assertEquals("certify resource request returned status:" + status, BaseRestUtils.STATUS_CODE_SUCCESS, status);

		Resource resourceJavaObject = ResponseParser.convertResourceResponseToJavaObject(changeResourceState.getResponse());
		Map<String, ArtifactDefinition> artifactsMap = resourceJavaObject.getDeploymentArtifacts();
		boolean flag = false;
		if (artifactsMap != null) {
			for (Entry<String, ArtifactDefinition> art : artifactsMap.entrySet()) {
				if (art.getValue().getArtifactName().equals(heatArtifactDetails.getArtifactName())) {
					assertTrue("expected artifact type is " + ArtifactGroupTypeEnum.DEPLOYMENT.getType() + " but was " + art.getValue().getArtifactGroupType(), art.getValue().getArtifactGroupType().equals(ArtifactGroupTypeEnum.DEPLOYMENT));
					flag = true;
					break;
				}
			}
			assertTrue("expected artifact not found", flag == true);
		}

	}

	// ---------------------------------Resource
	// success--------------------------------
	@Test
	public void addDcaeInventoryToscaArtifactToResourceInstanceAndCertify() throws Exception {
		String artifactFileName = dcaeInventoryToscaFile;
		String artifactName = dcaeInventoryToscaFile;
		String artifactLabel = "dcae inv tosca label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_TOSCA;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addDcaeInventoryJsonArtifactToResourceInstanceAndCertify() throws Exception {
		String artifactFileName = dcaeInventoryJsonFile;
		String artifactName = dcaeInventoryJsonFile;
		String artifactLabel = "dcae inv json label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_JSON;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addDcaeInventoryPolicyArtifactToResourceInstanceAndCertify() throws Exception {
		String artifactFileName = dcaeInventoryPolicyFile;
		String artifactName = dcaeInventoryPolicyFile;
		String artifactLabel = "dcae inv policy label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_POLICY;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addDcaeInventoryDocArtifactToResourceInstanceAndCertify() throws Exception {
		String artifactFileName = dcaeInventoryDocFile;
		String artifactName = dcaeInventoryDocFile;
		String artifactLabel = "dcae inv doc label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_DOC;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addDcaeInventoryBluePrintArtifactToResourceInstanceAndCertify() throws Exception {
		String artifactFileName = dcaeInventoryBlueprintFile;
		String artifactName = dcaeInventoryBlueprintFile;
		String artifactLabel = "dcae inv blueprint label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addDcaeInventoryEventArtifactToResourceInstanceAndCertify() throws Exception {
		String artifactFileName = dcaeInventoryEventFile;
		String artifactName = dcaeInventoryEventFile;
		String artifactLabel = "dcae inv event label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_EVENT;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	private RestResponse addArtifactToResourceInstanceAndCertify(String artifactFileName, String artifactName, String artifactLabel, ArtifactTypeEnum artifactType) throws Exception {

		// Get the resource
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);

		// Certify VF
		Pair<Component, RestResponse> changeComponentState = AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		assertTrue("response code is BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + changeComponentState.getRight().getErrorCode(), changeComponentState.getRight().getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Add VF instance to service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);

		// Get the VF instance
		getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ComponentInstance VfInstance = service.getComponentInstances().get(0);

		// Create the artifact
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstance(artifactFileName, artifactName, artifactLabel, artifactType, VfInstance, serviceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addArtifactToResourceInstanceResponse.getResponseMessage());
		return addArtifactToResourceInstanceResponse;
	}

	@Test
	public void updateArtifactDescriptionToResourceInstance() throws Exception {
		String artifactFileName = dcaeInventoryToscaFile;
		String artifactName = dcaeInventoryToscaFile;
		String artifactLabel = "dcae inv tosca label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_TOSCA;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		logger.debug("addInformationalArtifactToResource response: {}", addArtifactToResourceInstanceResponse.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Get the artifact from VF instance and change his description.
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ComponentInstance vfInstance = service.getComponentInstances().get(0);
		Map<String, ArtifactDefinition> deploymentArtifacts = vfInstance.getDeploymentArtifacts();
		ArtifactDefinition artifactDefinition = deploymentArtifacts.get("dcaeinvtoscalabel");
		artifactDefinition.setDescription("My new description");

		// Update the artifact
		RestResponse updateDeploymentArtifactToRI = ArtifactRestUtils.updateArtifactToResourceInstance(artifactDefinition, sdncDesignerDetails1, vfInstance.getUniqueId(), service.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", updateDeploymentArtifactToRI.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + updateDeploymentArtifactToRI.getErrorCode(), updateDeploymentArtifactToRI.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void deleteArtifactToResourceInstance() throws Exception {
		String artifactFileName = dcaeInventoryToscaFile;
		String artifactName = dcaeInventoryToscaFile;
		String artifactLabel = "dcae inv tosca label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.DCAE_INVENTORY_TOSCA;
		RestResponse addArtifactToResourceInstanceResponse = addArtifactToResourceInstanceAndCertify(artifactFileName, artifactName, artifactLabel, artifactType);
		logger.debug("addInformationalArtifactToResource response:  {}", addArtifactToResourceInstanceResponse.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addArtifactToResourceInstanceResponse.getErrorCode(), addArtifactToResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Get the artifact from VF instance and change his description.
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ComponentInstance vfInstance = service.getComponentInstances().get(0);
		Map<String, ArtifactDefinition> deploymentArtifacts = vfInstance.getDeploymentArtifacts();
		ArtifactDefinition artifactDefinition = deploymentArtifacts.get("dcaeinvtoscalabel");

		// Delete the artifact
		RestResponse deleteInformationalArtifactFromResource = ArtifactRestUtils.deleteArtifactFromResourceInstance(artifactDefinition, sdncDesignerDetails1, vfInstance.getUniqueId(), service.getUniqueId());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + deleteInformationalArtifactFromResource.getErrorCode(), deleteInformationalArtifactFromResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addHeatArtifactToResource() throws Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

	}

	@Test
	public void addHeatAndHeatNetArtifactsToResource() throws Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);

		// Add HEAT
		logger.debug("listFileName: {}", listFileName);
		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Add HEAT_NET
		String payloadNet = FileUtils.loadPayloadFile(listFileName, heatNetSuccessFile, true);
		ArtifactReqDetails heatNetArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_NET.getType());
		heatNetArtifactDetails.setPayload(payloadNet);
		heatNetArtifactDetails.setArtifactLabel(HEAT_NET_LABEL);

		RestResponse addInformationalArtifactToResource1 = ArtifactRestUtils.uploadArtifactToPlaceholderOnResource(heatNetArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId(), HEAT_NET_LABEL);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource1.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource1.getErrorCode(), addInformationalArtifactToResource1.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(vfResourceDetails, sdncDesignerDetails1);
		Resource resourceRespJavaObject = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();

		ArtifactDefinition artifactDefinition = deploymentArtifacts.get(HEAT_LABEL);
		assertNotNull(artifactDefinition);
		String heatEsId = artifactDefinition.getEsId();
		assertNotNull(heatEsId);

		ArtifactDefinition artifactDefinitionNet = deploymentArtifacts.get(HEAT_NET_LABEL);
		assertNotNull(artifactDefinitionNet);
		String heatNetEsId = artifactDefinitionNet.getEsId();
		assertNotNull(heatNetEsId);
		assertFalse(heatEsId.equalsIgnoreCase(heatNetEsId));
	}

	@Test
	public void addDeleteAddHeatArtifactToResource() throws Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName.toString());

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		RestResponse deleteInformationalArtifactFromResource = ArtifactRestUtils.deleteInformationalArtifactFromResource(vfResourceDetails.getUniqueId(), heatArtifactDetails, sdncDesignerDetails1);
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + deleteInformationalArtifactFromResource.getErrorCode(), deleteInformationalArtifactFromResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
	}

	@Test
	public void addYangXmlArtifactToResource() throws Exception {

		String fileName = yangFile;
		String artifactName = "asc_heat 0 2.XML";
		String artifactLabel = "Label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.YANG_XML;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addOtherTypeDeploymentArtifactToResource() throws Exception {

		String fileName = otherFile;
		String artifactName = "other.txt";
		String artifactLabel = "Label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.OTHER;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addYangXmlArtifactSameName() throws Exception {

		String fileName = yangFile;
		String artifactName = "asc_heat_0_2.XML";
		String artifactLabel = "Label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.YANG_XML;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// Changing label but not name
		artifactLabel = "Label1";
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is not 400, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == 400);
		BaseValidationUtils.checkErrorResponse(addInformationalArtifactToResource, ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS, new String[] { "Resource", vfResourceDetails.getName(), artifactName });

		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addInvalidYangXmlFormat() throws Exception {

		String fileName = invalidYangFile;
		String artifactName = "asc_heat_0_2.XML";
		String artifactLabel = "Label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.YANG_XML;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is not 400, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == 400);
		BaseValidationUtils.checkErrorResponse(addInformationalArtifactToResource, ActionStatus.INVALID_XML, new String[] { "YANG_XML" });

	}

	@Test
	public void addSeveralYangXmlArtifacts() throws Exception {

		// Adding 4 artifacts
		String fileName = yangFile;
		String artifactName = "asc_heat_0_2.XML";
		String artifactLabel = "Label";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.YANG_XML;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// Changing label and name
		artifactLabel = "Label1";
		artifactName = "asc_heat_0_3.XML";
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Changing label and name
		artifactLabel = "Label2";
		artifactName = "asc_heat_0_4.XML";
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);

		// Changing label and name
		artifactLabel = "Label3";
		artifactName = "asc_heat_0_5.XML";
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType);
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 4);
	}

	@Test(dataProvider = "getDepArtByType")
	public void updateHeatArtifactToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName.toString());

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		heatTypeArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// update
		heatTypeArtifactDetails.setArtifactName("UPDATE.yaml");
		heatTypeArtifactDetails.setPayloadData(null);
		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", updateInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + updateInformationalArtifactToResource.getErrorCode(), updateInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

	}

	@Test(dataProvider = "getDepArtByType")
	public void updateHeatArtifactTimeOutToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		Resource resource = getResourceByResDetails(vfResourceDetails, sdncDesignerDetails1);
		int actualTimeout = resource.getDeploymentArtifacts().get(heatTypeArtifactDetails.getArtifactLabel().toLowerCase()).getTimeout();
		assertTrue("verify " + heatTypeArtifactDetails.getArtifactLabel().toLowerCase() + " artifact timout, expected " + heatTypeArtifactDetails.getTimeout() + ", but was " + actualTimeout, heatTypeArtifactDetails.getTimeout() == actualTimeout);

		// update
		heatTypeArtifactDetails.setTimeout(35);
		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", updateInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + updateInformationalArtifactToResource.getErrorCode(), updateInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		resource = getResourceByResDetails(vfResourceDetails, sdncDesignerDetails1);
		actualTimeout = resource.getDeploymentArtifacts().get(heatTypeArtifactDetails.getArtifactLabel().toLowerCase()).getTimeout();
		assertTrue("verify " + heatTypeArtifactDetails.getArtifactLabel().toLowerCase() + " artifact timout, expected " + heatTypeArtifactDetails.getTimeout() + ", but was " + actualTimeout, heatTypeArtifactDetails.getTimeout() == actualTimeout);
	}

	@Test(dataProvider = "getDepArtByType")
	public void updateHeatArtifactDescriptionToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		Resource resource = getResourceByResDetails(vfResourceDetails, sdncDesignerDetails1);
		String actualDescription = resource.getDeploymentArtifacts().get(heatTypeArtifactDetails.getArtifactLabel().toLowerCase()).getDescription();
		assertTrue("verify " + heatTypeArtifactDetails.getArtifactLabel().toLowerCase() + " artifact Description, expected " + heatTypeArtifactDetails.getDescription() + ", but was " + actualDescription, heatTypeArtifactDetails.getDescription().equals(actualDescription));

		// update
		heatTypeArtifactDetails.setDescription("the best description was ever");
		RestResponse updateInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", updateInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + updateInformationalArtifactToResource.getErrorCode(), updateInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		resource = getResourceByResDetails(vfResourceDetails, sdncDesignerDetails1);
		actualDescription = resource.getDeploymentArtifacts().get(heatTypeArtifactDetails.getArtifactLabel().toLowerCase()).getDescription();
		assertTrue("verify " + heatTypeArtifactDetails.getArtifactLabel().toLowerCase() + " artifact Description, expected " + heatTypeArtifactDetails.getDescription() + ", but was " + actualDescription, heatTypeArtifactDetails.getDescription().equals(actualDescription));
	}

	private Resource getResourceByResDetails(ResourceReqDetails resDetails, User userDetails) throws IOException {
		RestResponse response = ResourceRestUtils.getResource(resDetails, userDetails);
		assertTrue("response code on get resource not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + response.getErrorCode(), response.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		return resource;
	}

	// ---------------------------------Service
	// success--------------------------------
	@Test()
	public void addAllTypesDepArtifactToService() throws Exception {
		ArtifactReqDetails otherArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.OTHER.getType());

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(otherArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {}", addInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		ArtifactReqDetails yangXmlArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.YANG_XML.getType());

		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(yangXmlArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {}", addInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

	}

	@Test(enabled = false)
	public void addMuranoPkgArtifactToService() throws Exception, Exception {

		String fileName = muranoFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.MURANO_PKG.getType());
		heatArtifactDetails.setPayload(payload);
		heatArtifactDetails.setArtifactName("asc_heat 0 2.zip");
		heatArtifactDetails.setArtifactLabel("Label");

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response:  {}", addInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

	}

	@Test(dataProvider = "getServiceDepArtByType")
	public void addHeatArtifactToServiceAndCertify(String artType) throws Exception, Exception {

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(artType);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {}", addInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// certified service
		RestResponse changeServiceState = LifecycleRestUtils.certifyService(serviceDetails);
		int status = changeServiceState.getErrorCode();
		assertEquals("certify service request returned status:" + status, BaseRestUtils.STATUS_CODE_SUCCESS, status);

		Service resourceJavaObject = ResponseParser.convertServiceResponseToJavaObject(changeServiceState.getResponse());
		Map<String, ArtifactDefinition> artifactsMap = resourceJavaObject.getDeploymentArtifacts();
		boolean flag = false;
		if (artifactsMap != null) {
			for (Entry<String, ArtifactDefinition> art : artifactsMap.entrySet()) {
				if (art.getValue().getArtifactName().equals(heatArtifactDetails.getArtifactName())) {
					assertTrue("expected artifact type is " + ArtifactGroupTypeEnum.DEPLOYMENT.getType() + " but was " + art.getValue().getArtifactGroupType(), art.getValue().getArtifactGroupType().equals(ArtifactGroupTypeEnum.DEPLOYMENT));
					flag = true;
					break;
				}
			}
			assertTrue("expected artifact not found", flag == true);
		}

	}

	@Test(enabled = false, dataProvider = "getServiceDepArtByType")
	public void updateHeatArtifactToService(String artType) throws Exception, Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName.toString());

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(artType);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// update
		heatArtifactDetails.setPayloadData(payload);
		RestResponse updateInformationalArtifactToService = ArtifactRestUtils.updateInformationalArtifactOfServiceByMethod(heatArtifactDetails, serviceDetails.getUniqueId(), sdncDesignerDetails1, "POST");
		logger.debug("updateInformationalArtifactToService response:  {}", updateInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + updateInformationalArtifactToService.getErrorCode(), updateInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

	}

	// --------------------------------------Resource Negative
	// Tests-------------------------------------

	// TODO Andrey the method of DEPLOYMENT artifact is update and not add
	@Test(dataProvider = "getServiceDepArtByType")
	public void addTheSameAdditionalHeatArtifactToResource(String artType) throws Exception, Exception {

		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(artType);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());

		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// add the same artifact one more time
		artifactDetails.setArtifactLabel("the second artifact");
		addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList("Resource", vfResourceDetails.getName(), artifactDetails.getArtifactName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS.name(), variables, addInformationalArtifactToResource.getResponse());

	}

	@Test
	public void addHeatArtifactTwiceSameNameToResource() throws Exception, Exception {

		String filename1 = heatSuccessFile;
		// String filename2 = heatSuccessMiniFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, filename1, true);

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());

		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());

		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Add HEAT_NET
		String payloadNet = FileUtils.loadPayloadFile(listFileName, heatNetSuccessFile, true);
		ArtifactReqDetails heatNetArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_NET.getType());
		heatNetArtifactDetails.setPayload(payloadNet);
		heatNetArtifactDetails.setArtifactLabel(HEAT_NET_LABEL);
		heatNetArtifactDetails.setArtifactName(heatArtifactDetails.getArtifactName());

		RestResponse addInformationalArtifactToResource1 = ArtifactRestUtils.uploadArtifactToPlaceholderOnResource(heatNetArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId(), HEAT_NET_LABEL);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource1.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource1.getErrorCode());

		List<String> variables = Arrays.asList("Resource", vfResourceDetails.getName(), heatNetArtifactDetails.getArtifactName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS.name(), variables, addInformationalArtifactToResource1.getResponse());

	}

	@Test(dataProvider = "getDepArtByType")
	public void addHeatArtifactTwiceToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String filename1 = heatSuccessFile;
		String filename2 = heatSuccessMiniFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, filename1, true);
		heatTypeArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// add the second artifact
		payload = FileUtils.loadPayloadFile(listFileName, heatSuccessMiniFile, true);
		heatTypeArtifactDetails.setPayload(payload);
		heatTypeArtifactDetails.setArtifactName(filename2);
		heatTypeArtifactDetails.setArtifactLabel("the second artifact");

		addInformationalArtifactToResource = ArtifactRestUtils.explicitAddInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList("Resource", vfResourceDetails.getName(), heatTypeArtifactDetails.getArtifactType(), heatTypeArtifactDetails.getArtifactType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS.name(), variables, addInformationalArtifactToResource.getResponse());

	}

	@Test(dataProvider = "getDepArtByType")
	public void addHeatArtifactInvalidHeatFormatToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String fileName = heatInvalidFormat;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		heatTypeArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList(heatTypeArtifactDetails.getArtifactType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT.name(), variables, addInformationalArtifactToResource.getResponse());

	}

	@Test(dataProvider = "getDepArtByType")
	public void addHeatArtifactInvalidYamlFormatToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String fileName = yamlInvalidFormat;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		heatTypeArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_YAML.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList(heatTypeArtifactDetails.getArtifactType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_YAML.name(), variables, addInformationalArtifactToResource.getResponse());

	}

	@Test(dataProvider = "getDepArtByType")
	public void addHeatArtifactInvalidFileExtensionToResource(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String fileName = yangFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);

		heatTypeArtifactDetails.setPayload(payload);
		heatTypeArtifactDetails.setArtifactName(fileName);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList(heatTypeArtifactDetails.getArtifactType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name(), variables, addInformationalArtifactToResource.getResponse());

	}

	@Test(dataProvider = "getDepArtByType")
	public void addHeatArtifactToResourceCertifyAndAddAdditionalHeatArtifact(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);
		heatTypeArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.updateInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());

		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// certified resource
		RestResponse changeResourceState = LifecycleRestUtils.certifyResource(vfResourceDetails);
		int status = changeResourceState.getErrorCode();
		assertEquals("certify resource request returned status:" + status, BaseRestUtils.STATUS_CODE_SUCCESS, status);

		// add second HEAT artifact to the certified resource
		changeResourceState = LifecycleRestUtils.changeResourceState(vfResourceDetails, sdncDesignerDetails1, LifeCycleStatesEnum.CHECKOUT);
		assertTrue("expected code response on change resource state to CHECKOUT BaseRestUtils.STATUS_CODE_SUCCESS, but was " + changeResourceState.getErrorCode(), changeResourceState.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// ArtifactReqDetails heatArtifactDetails1 =
		// ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatTypeArtifactDetails.setPayload(payload);
		heatTypeArtifactDetails.setArtifactName(fileName);
		heatTypeArtifactDetails.setArtifactLabel("the second artifact");

		addInformationalArtifactToResource = ArtifactRestUtils.explicitAddInformationalArtifactToResource(heatTypeArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList("Resource", vfResourceDetails.getName(), heatTypeArtifactDetails.getArtifactType(), heatTypeArtifactDetails.getArtifactType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS.name(), variables, addInformationalArtifactToResource.getResponse());

	}

	// -----------------Service Negative
	// Tests--------------------------------------------------------

	// Absolute
	@Test(enabled = false)
	public void addHeatArtifactTwiceToService() throws Exception, Exception {

		String fileName1 = heatSuccessFile;
		String fileName2 = heatSuccessMiniFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName1, true);
		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.OTHER.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {}", addInformationalArtifactToService.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// add the second artifact
		payload = FileUtils.loadPayloadFile(listFileName, fileName2, true);

		ArtifactReqDetails heatArtifactDetails1 = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.OTHER.getType());
		heatArtifactDetails1.setPayload(payload);
		heatArtifactDetails1.setArtifactName(fileName2);
		heatArtifactDetails1.setArtifactLabel("the second artifact");

		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails1, sdncDesignerDetails1, serviceDetails.getUniqueId());
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToService.getErrorCode());

		List<String> variables = Arrays.asList("Service", serviceDetails.getName(), ArtifactTypeEnum.OTHER.getType(), ArtifactTypeEnum.OTHER.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS.name(), variables, addInformationalArtifactToService.getResponse());

	}

	// TODO Andrey Obsolete
	@Test(enabled = false)
	public void addHeatArtifactInvalidHeatFormatToService() throws Exception, Exception {

		String fileName = heatInvalidFormat;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {}", addInformationalArtifactToService.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToService.getErrorCode());

		List<String> variables = Arrays.asList(ArtifactTypeEnum.HEAT.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT.name(), variables, addInformationalArtifactToService.getResponse());

	}

	@Test(enabled = false)
	public void addHeatArtifactInvalidYamlFormatToService() throws Exception, Exception {

		String fileName = yamlInvalidFormat;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {} ", addInformationalArtifactToService.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_YAML.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToService.getErrorCode());

		List<String> variables = Arrays.asList(ArtifactTypeEnum.HEAT.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_YAML.name(), variables, addInformationalArtifactToService.getResponse());

	}

	@Test
	public void addHeatArtifactInvalidFileExtensionToService() throws Exception, Exception {

		String fileName = muranoFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.YANG_XML.getType());
		heatArtifactDetails.setPayload(payload);
		heatArtifactDetails.setArtifactName(fileName);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		logger.debug("addInformationalArtifactToService response: {} ", addInformationalArtifactToService.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToService.getErrorCode());

		List<String> variables = Arrays.asList(ArtifactTypeEnum.YANG_XML.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION.name(), variables, addInformationalArtifactToService.getResponse());

	}

	@Test(dataProvider = "getDepArtByType")
	public void addHeatEnvArtifactToResourceNotSupportedType(ArtifactReqDetails heatTypeArtifactDetails) throws Exception, Exception {

		String fileName = heatEnvfile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName.toString());

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_ENV.getType());
		heatArtifactDetails.setPayload(payload);
		heatArtifactDetails.setArtifactName("asc_heat 0 2.env");
		heatArtifactDetails.setArtifactLabel(heatTypeArtifactDetails.getArtifactLabel());

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToResource.getErrorCode());

		List<String> variables = Arrays.asList(ArtifactTypeEnum.HEAT_ENV.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name(), variables, addInformationalArtifactToResource.getResponse());
	}

	// TODO Andrey
	@Test
	public void addHeatArtifactToServiceNotSupportDeploymentArt() throws Exception, Exception {

		String fileName = heatSuccessFile;
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(testResourcesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, fileName, true);

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatArtifactDetails.setPayload(payload);

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(heatArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), addInformationalArtifactToService.getErrorCode());

		List<String> variables = Arrays.asList(ArtifactTypeEnum.HEAT.getType());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name(), variables, addInformationalArtifactToService.getResponse());

	}

	protected RestResponse addArtifactToResourceInstance(String artifactFileName, String artifactName, String artifactLabel, ArtifactTypeEnum artifactType, ComponentInstance componentInstance, ServiceReqDetails serviceDetails) throws Exception {
		ArtifactReqDetails dcaeArtifactDetails = buildArtifactReqDetailsObject(testResourcesInstancesPath, artifactFileName, artifactName, artifactLabel, artifactType);
		RestResponse addArtifactToResourceInstance = ArtifactRestUtils.addArtifactToResourceInstance(dcaeArtifactDetails, sdncDesignerDetails1, componentInstance.getUniqueId(), serviceDetails.getUniqueId());
		return addArtifactToResourceInstance;
	}

	protected RestResponse addDeploymentArtifactToResource(String artifactFileName, String artifactName, String artifactLabel, ArtifactTypeEnum artifactType) throws Exception {
		ArtifactReqDetails heatArtifactDetails = buildArtifactReqDetailsObject(testResourcesPath, artifactFileName, artifactName, artifactLabel, artifactType);
		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, vfResourceDetails.getUniqueId());
		return addInformationalArtifactToResource;
	}

	protected RestResponse addDeploymentArtifactToResource(String artifactFileName, String artifactName, String artifactLabel, ArtifactTypeEnum artifactType, ResourceReqDetails resource) throws Exception {
		ArtifactReqDetails heatArtifactDetails = buildArtifactReqDetailsObject(testResourcesPath, artifactFileName, artifactName, artifactLabel, artifactType);
		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, resource.getUniqueId());
		return addInformationalArtifactToResource;
	}

	// US672293 - Support new artifact type : BEVF_LICENSE , VENDOR_LICENSE
	@Test
	public void addNewArtifactsToVFResource() throws Exception {

		String fileName = yangFile;
		String artifactName = "artifact1.xml";
		String artifactLabel = "Label1";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VNF_CATALOG;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		artifactName = "artifact2.xml";
		artifactLabel = "Label2";
		artifactType = ArtifactTypeEnum.VF_LICENSE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact4.xml";
		artifactLabel = "Label4";
		artifactType = ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact5.xml";
		artifactLabel = "Label5";
		artifactType = ArtifactTypeEnum.MODEL_QUERY_SPEC;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact6.xml";
		artifactLabel = "Label6";
		artifactType = ArtifactTypeEnum.APPC_CONFIG;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		fileName = jsonFile;
		artifactName = "artifact7.json";
		artifactLabel = "Label7";
		artifactType = ArtifactTypeEnum.APPC_CONFIG;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 2);
		
		//MIB artifacts: SNMP_POLL, SNMP_TRAP
		fileName = jsonFile;
		artifactName = "artifact8.json";
		artifactLabel = "Label8";
		artifactType = ArtifactTypeEnum.SNMP_POLL;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		
		fileName = jsonFile;
		artifactName = "artifact9.json";
		artifactLabel = "Label9";
		artifactType = ArtifactTypeEnum.SNMP_TRAP;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		//MIB artifacts: SNMP_POLL, SNMP_TRAP
		fileName = jsonFile;
		artifactName = "artifact8.json";
		artifactLabel = "Label8";
		artifactType = ArtifactTypeEnum.SNMP_POLL;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		
		fileName = jsonFile;
		artifactName = "artifact9.json";
		artifactLabel = "Label9";
		artifactType = ArtifactTypeEnum.SNMP_TRAP;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactsToVFCResource() throws Exception {

		String fileName = yangFile;
		String artifactName = "artifact1.xml";
		String artifactLabel = "Label1";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VNF_CATALOG;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact2.xml";
		artifactLabel = "Label2";
		artifactType = ArtifactTypeEnum.VF_LICENSE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact4.xml";
		artifactLabel = "Label4";
		artifactType = ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifac5.xml";
		artifactLabel = "Label5";
		artifactType = ArtifactTypeEnum.MODEL_QUERY_SPEC;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactsToVfc() throws Exception {
		String fileName = yangFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VF_LICENSE;
		RestResponse addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactsToCp() throws Exception {
		String fileName = yangFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VF_LICENSE;
		RestResponse addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, cpResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(cpResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, cpResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(cpResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactsToVl() throws Exception {
		String fileName = yangFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VF_LICENSE;
		RestResponse addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vlResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vlResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vlResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vlResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addVfInstanceWithNewArtifactsToService() throws Exception {
		String fileName = yangFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VF_LICENSE;
		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		// Certify VF
		Pair<Component, RestResponse> changeComponentState = AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		assertTrue("response code is BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + changeComponentState.getRight().getErrorCode(), changeComponentState.getRight().getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// Add VF instance to service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		// get service and verify VF instance contain the Artifacts :VF_LICENSE
		// and VENDOR_LICENSE
		getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ComponentInstance VfInstance = service.getComponentInstances().get(0);
		ArtifactValidationUtils.validateArtifactsNumberInComponentInstance(VfInstance, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.VENDOR_LICENSE, 1);
		ArtifactValidationUtils.validateArtifactsNumberInComponentInstance(VfInstance, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.VF_LICENSE, 1);
	}

	@Test
	public void addNotSupportedArtifactsTypeToService01() throws Exception, Exception {
		// Artifact type : VF_LICENSE
		ArtifactReqDetails deploymentArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.VF_LICENSE.getType());
		RestResponse addDeploymentArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(deploymentArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code  eturned :" + addDeploymentArtifactToService.getErrorCode(), addDeploymentArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add("VF_LICENSE");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name(), variables, addDeploymentArtifactToService.getResponse());
	}

	@Test
	public void addNotSupportedArtifactsTypeToService02() throws Exception, Exception {
		// Artifact type : VENDOR_LICENSE
		ArtifactReqDetails deploymentArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.VENDOR_LICENSE.getType());
		RestResponse addDeploymentArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(deploymentArtifactDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code  eturned :" + addDeploymentArtifactToService.getErrorCode(), addDeploymentArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add("VENDOR_LICENSE");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name(), variables, addDeploymentArtifactToService.getResponse());
	}

	@Test
	public void addInvalidFileForArtifactTypeVendorLicenseToResource() throws Exception {
		String fileName = yangFile;
		String NonXmlFile = heatSuccessFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VF_LICENSE;
		RestResponse addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		// Artifact type VENDOR_LICENSE must be XML file
		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		addDeploymentArtifactToResource = addDeploymentArtifactToResource(NonXmlFile, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code  400 returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add("VENDOR_LICENSE");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_XML.name(), variables, addDeploymentArtifactToResource.getResponse());
		// get resource and verify that file not exist within
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 0);
	}

	@Test
	public void addInvalidFileForArtifactTypeVfLicenseToResource() throws Exception {
		String fileName = yangFile;
		String NonXmlFile = heatSuccessFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		RestResponse addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		// Artifact type VF_LICENSE must be XML file
		artifactName = "artifact3.xml";
		artifactLabel = "Label3";
		artifactType = ArtifactTypeEnum.VF_LICENSE;
		addDeploymentArtifactToResource = addDeploymentArtifactToResource(NonXmlFile, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code  400 returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add("VF_LICENSE");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_XML.name(), variables, addDeploymentArtifactToResource.getResponse());
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 0);
	}

	@Test
	public void addVendorLicenseArtifactAlreadyExistsInResource() throws Exception {
		String fileName = yangFile;
		String artifactName = "artifact2.xml";
		String artifactLabel = "Label2";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VENDOR_LICENSE;
		RestResponse addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		// Add same file again to resource
		addDeploymentArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response: {}", addDeploymentArtifactToResource.getResponseMessage());
		assertTrue("response code is not 400, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add(artifactLabel.toLowerCase());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_EXIST.name(), variables, addDeploymentArtifactToResource.getResponse());
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	// US672294

	@Test()
	public void addVnfCatalogArtifactsToService() throws Exception, Exception {

		ArtifactReqDetails artDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.VNF_CATALOG.getType());
		RestResponse resp = ArtifactRestUtils.addInformationalArtifactToService(artDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + resp.getErrorCode(), resp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// get service and verify the Artifacts :VNF_CATALOG
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(service, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.VNF_CATALOG, 1);
	}

	@Test()
	public void addModelInventoryProfileArtifactsToService() throws Exception, Exception {

		ArtifactReqDetails artDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType());
		RestResponse resp = ArtifactRestUtils.addInformationalArtifactToService(artDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + resp.getErrorCode(), resp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// get service and verify the Artifacts :VNF_CATALOG
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(service, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, 1);
	}

	@Test()
	public void addModelQuerySpecArtifactsToService() throws Exception, Exception {

		ArtifactReqDetails artDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.MODEL_QUERY_SPEC.getType());
		RestResponse resp = ArtifactRestUtils.addInformationalArtifactToService(artDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + resp.getErrorCode(), resp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// get service and verify the Artifacts :VNF_CATALOG
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(service, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.MODEL_QUERY_SPEC, 1);
	}

	@Test
	public void addVfInstanceWithNewArtifactsToService02() throws Exception {

		String fileName = yangFile;
		String artifactName = "artifact1.xml";
		String artifactLabel = "Label1";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VNF_CATALOG;
		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		fileName = yangFile;
		artifactName = "artifact2.xml";
		artifactLabel = "Label2";
		artifactType = ArtifactTypeEnum.APPC_CONFIG;
		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact4.xml";
		artifactLabel = "Label4";
		artifactType = ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifac5.xml";
		artifactLabel = "Label5";
		artifactType = ArtifactTypeEnum.MODEL_QUERY_SPEC;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
		// Certify VF
		Pair<Component, RestResponse> changeComponentState = AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		assertTrue("response code is BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + changeComponentState.getRight().getErrorCode(), changeComponentState.getRight().getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		// Add VF instance to service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);

		// get service and verify VF instance contain the Artifacts :VF_LICENSE
		// and VENDOR_LICENSE
		getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ComponentInstance VfInstance = service.getComponentInstances().get(0);
		ArtifactValidationUtils.validateArtifactsNumberInComponentInstance(VfInstance, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.VNF_CATALOG, 1);
		ArtifactValidationUtils.validateArtifactsNumberInComponentInstance(VfInstance, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, 1);
		ArtifactValidationUtils.validateArtifactsNumberInComponentInstance(VfInstance, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.MODEL_QUERY_SPEC, 1);
		ArtifactValidationUtils.validateArtifactsNumberInComponentInstance(VfInstance, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.APPC_CONFIG, 1);
	}

	@Test
	public void addAppcConfigArtifactToVfc() throws Exception {
		String fileName = jsonFile;
		String artifactName = "artifact7.json";
		String artifactLabel = "Label7";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.APPC_CONFIG;
		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code 400, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add(artifactName);
		variables.add("[VF]");
		variables.add("VFC (Virtual Function Component)");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE.name(), variables, addInformationalArtifactToResource.getResponse());
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 0);
	}

	@Test
	public void addAppcConfigArtifactToCp() throws Exception {
		String fileName = jsonFile;
		String artifactName = "artifact7.json";
		String artifactLabel = "Label7";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.APPC_CONFIG;
		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, cpResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code 400, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add(artifactName);
		variables.add("[VF]");
		variables.add("CP (Connection Point)");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE.name(), variables, addInformationalArtifactToResource.getResponse());
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 0);
	}

	@Test
	public void addAppcConfigArtifactToVl() throws Exception {
		String fileName = jsonFile;
		String artifactName = "artifact7.json";
		String artifactLabel = "Label7";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.APPC_CONFIG;
		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vlResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code 400, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add(artifactName);
		variables.add("[VF]");
		variables.add("VL (Virtual Link)");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE.name(), variables, addInformationalArtifactToResource.getResponse());
		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 0);
	}

	@Test()
	public void addAppcConfigArtifactsToService() throws Exception, Exception {
		ArtifactReqDetails artDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.APPC_CONFIG.getType());
		RestResponse addDeploymentArtifactToResource = ArtifactRestUtils.addInformationalArtifactToService(artDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code 400, returned :" + addDeploymentArtifactToResource.getErrorCode(), addDeploymentArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add(ArtifactTypeEnum.APPC_CONFIG.toString());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name(), variables, addDeploymentArtifactToResource.getResponse());
	}

	@Test
	public void addAppcConfigInvalidJsonToVFResourceFailed() throws Exception {

		String fileName = invalidJsonFile;
		String artifactName = "invalidJson.json";
		String artifactLabel = "Label7";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.APPC_CONFIG;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is 400, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);

		ArrayList<String> variables = new ArrayList<>();
		variables.add(ArtifactTypeEnum.APPC_CONFIG.toString());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_JSON.name(), variables, addInformationalArtifactToResource.getResponse());

		RestResponse getResource = ResourceRestUtils.getResource(vfResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 0);

	}

	@Test
	public void addNewArtifactsToCp02() throws Exception {

		String fileName = yangFile;
		String artifactName = "artifact1.xml";
		String artifactLabel = "Label1";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VNF_CATALOG;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, cpResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(cpResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact4.xml";
		artifactLabel = "Label4";
		artifactType = ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, cpResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(cpResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifac5.xml";
		artifactLabel = "Label5";
		artifactType = ArtifactTypeEnum.MODEL_QUERY_SPEC;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, cpResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(cpResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactsToVl02() throws Exception {

		String fileName = yangFile;
		String artifactName = "artifact1.xml";
		String artifactLabel = "Label1";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VNF_CATALOG;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vlResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vlResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact4.xml";
		artifactLabel = "Label4";
		artifactType = ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vlResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vlResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifac5.xml";
		artifactLabel = "Label5";
		artifactType = ArtifactTypeEnum.MODEL_QUERY_SPEC;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vlResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vlResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactsToVfc02() throws Exception {

		String fileName = yangFile;
		String artifactName = "artifact1.xml";
		String artifactLabel = "Label1";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.VNF_CATALOG;

		RestResponse addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifact4.xml";
		artifactLabel = "Label4";
		artifactType = ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);

		artifactName = "artifac5.xml";
		artifactLabel = "Label5";
		artifactType = ArtifactTypeEnum.MODEL_QUERY_SPEC;

		addInformationalArtifactToResource = addDeploymentArtifactToResource(fileName, artifactName, artifactLabel, artifactType, vfcResourceDetails);
		logger.debug("addInformationalArtifactToResource response:  {}", addInformationalArtifactToResource.getResponseMessage());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToResource.getErrorCode(), addInformationalArtifactToResource.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		getResource = ResourceRestUtils.getResource(vfcResourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(resource, ArtifactGroupTypeEnum.DEPLOYMENT, artifactType, 1);
	}

	@Test
	public void addNewArtifactAlreadyExistsInService() throws Exception {
		ArtifactReqDetails artDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.MODEL_QUERY_SPEC.getType());
		RestResponse addDeploymentArtifactoService = ArtifactRestUtils.addInformationalArtifactToService(artDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code is  BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addDeploymentArtifactoService.getErrorCode(), addDeploymentArtifactoService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		// get service and verify the Artifacts :VNF_CATALOG
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(service, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.MODEL_QUERY_SPEC, 1);
		// Add same file again to resource
		addDeploymentArtifactoService = ArtifactRestUtils.addInformationalArtifactToService(artDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
		assertTrue("response code is 400, returned :" + addDeploymentArtifactoService.getErrorCode(), addDeploymentArtifactoService.getErrorCode() == BaseRestUtils.STATUS_CODE_INVALID_CONTENT);
		ArrayList<String> variables = new ArrayList<>();
		variables.add(artDetails.getArtifactLabel().toLowerCase());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.ARTIFACT_EXIST.name(), variables, addDeploymentArtifactoService.getResponse());
		// get service and verify the Artifacts :VNF_CATALOG is still exist and
		// has one occurrences
		getServiceResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails1);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ArtifactValidationUtils.validateArtifactsNumberInComponent(service, ArtifactGroupTypeEnum.DEPLOYMENT, ArtifactTypeEnum.MODEL_QUERY_SPEC, 1);
	}

	private ArtifactReqDetails buildArtifactReqDetailsObject(String filesPath, String artifactFileName, String artifactName, String artifactLabel, ArtifactTypeEnum artifactType) throws IOException, Exception {
		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(filesPath);
		logger.debug("listFileName: {}", listFileName);

		String payload = FileUtils.loadPayloadFile(listFileName, artifactFileName, true);

		ArtifactReqDetails dcaeArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(artifactType.getType());
		dcaeArtifactDetails.setPayload(payload);
		dcaeArtifactDetails.setArtifactName(artifactName);
		dcaeArtifactDetails.setArtifactLabel(artifactLabel);
		return dcaeArtifactDetails;
	}

}
