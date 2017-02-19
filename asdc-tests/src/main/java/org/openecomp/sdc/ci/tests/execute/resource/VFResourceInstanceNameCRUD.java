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

package org.openecomp.sdc.ci.tests.execute.resource;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import fj.data.Either;

public class VFResourceInstanceNameCRUD extends ComponentBaseTest {

	protected static ServiceReqDetails serviceDetails;
	protected static ResourceReqDetails resourceDetailsVFC;
	protected static ResourceReqDetails resourceDetailsVL;
	protected static ResourceReqDetails resourceDetailsVF;
	protected static ResourceReqDetails resourceDetailsCP;
	protected static ComponentInstanceReqDetails resourceInstanceReqDetailsVF;
	protected static ComponentInstanceReqDetails resourceInstanceReqDetailsVFC;
	protected static ComponentInstanceReqDetails resourceInstanceReqDetailsVL;
	protected static ComponentInstanceReqDetails resourceInstanceReqDetailsCP;
	protected static User sdncDesignerDetails1;
	protected static User sdncTesterDeatails1;
	protected static User sdncAdminDetails1;
	protected static ArtifactReqDetails heatArtifactDetails;
	protected static ArtifactReqDetails defaultArtifactDetails;
	protected static int maxLength = 50;
	protected static Resource resourceVF = null;

	@Rule
	public static TestName name = new TestName();

	public VFResourceInstanceNameCRUD() {
		super(name, VFResourceInstanceNameCRUD.class.getName());
	}

	@BeforeMethod

	public void init() throws Exception {

		// serviceDetails = ElementFactory.getDefaultService();
		// resourceDetailsVFC =
		// ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VFC.toString(),
		// "resourceVFC");
		// resourceDetailsVF =
		// ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF.toString(),
		// "resourceVF3");
		// resourceDetailsVL =
		// ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VL.toString(),
		// "resourceVL");
		// resourceDetailsCP =
		// ElementFactory.getDefaultResourceByType(ResourceTypeEnum.CP.toString(),
		// "resourceCP");
		sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncAdminDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// heatArtifactDetails =
		// ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());

		Either<Resource, RestResponse> resourceDetailsCP_01e = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.CP, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsCP_01e.left().value(), UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CHECKIN, true);
		resourceDetailsCP = new ResourceReqDetails(resourceDetailsCP_01e.left().value());
		Either<Resource, RestResponse> resourceDetailsVL_01e = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VL, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsVL_01e.left().value(), UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CHECKIN, true);
		resourceDetailsVL = new ResourceReqDetails(resourceDetailsVL_01e.left().value());
		Either<Resource, RestResponse> resourceDetailsVF_01e = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF = new ResourceReqDetails(resourceDetailsVF_01e.left().value());
		Either<Resource, RestResponse> resourceDetailsVFC_01e = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsVFC_01e.left().value(), UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CHECKIN, true);
		resourceDetailsVFC = new ResourceReqDetails(resourceDetailsVFC_01e.left().value());

		resourceInstanceReqDetailsVFC = ElementFactory.getDefaultComponentInstance("VFC", resourceDetailsVFC);
		resourceInstanceReqDetailsVF = ElementFactory.getDefaultComponentInstance("VF", resourceDetailsVF);
		resourceInstanceReqDetailsVL = ElementFactory.getDefaultComponentInstance("VL", resourceDetailsVL);
		resourceInstanceReqDetailsCP = ElementFactory.getDefaultComponentInstance("CP", resourceDetailsCP);
		sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncAdminDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

	}

	@Test
	public void addResourceInstanceToVF() throws Exception {

		createVFWithCertifiedResourceInstance(resourceDetailsCP, resourceInstanceReqDetailsCP);
		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsCP.getName() + " 1");
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	@Test
	public void updateResourceInstanceName() throws Exception {

		// update resource instance name
		String resourceInstanceUpdatedName = "resource New 2";

		ResourceReqDetails updatedResourceDetailsVLC = changeResouceName(resourceDetailsVFC,
				resourceInstanceUpdatedName);
		createVFWithCertifiedResourceInstance(updatedResourceDetailsVLC, resourceInstanceReqDetailsVFC);

		resourceInstanceReqDetailsVFC.setName(resourceInstanceUpdatedName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetailsVFC, sdncDesignerDetails1, resourceVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		assertTrue(updateResourceInstanceResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceUpdatedName);
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	@Test
	public void updateResourceInstanceNameToNextGeneratedName() throws Exception {

		// update resource instance name
		String resourceInstanceUpdatedName = resourceInstanceReqDetailsCP.getName() + " 2";

		ResourceReqDetails updatedResourceDetailsVL = changeResouceName(resourceDetailsVL, resourceInstanceUpdatedName);
		createVFWithCertifiedResourceInstance(updatedResourceDetailsVL, resourceInstanceReqDetailsVL);
		resourceInstanceReqDetailsCP.setName(resourceInstanceUpdatedName);

		// add second resource instance
		RestResponse response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetailsCP,
				sdncDesignerDetails1, resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());
		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsVL.getName() + " 1");
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsCP.getName() + " 2");
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	@Test
	public void normolizeUpdatedResourceInstanceName() throws Exception {

		String resourceInstanceUpdatedName = "resource   new -  .2";
		String normalizedName = "resourcenew2";

		ResourceReqDetails updatedResourceDetailsVL = changeResouceName(resourceDetailsVL, resourceInstanceUpdatedName);

		createVFWithCertifiedResourceInstance(updatedResourceDetailsVL, resourceInstanceReqDetailsVL);
		// update resource instance name
		resourceInstanceReqDetailsCP.setName(resourceInstanceUpdatedName);

		// add second resource instance
		RestResponse response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetailsCP,
				sdncDesignerDetails1, resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());
		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsVL.getName() + " 1");
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsCP.getName() + " 2");
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	@Test
	public void updatedResourceInstanceNameToEmpty() throws Exception {

		createVFWithCertifiedResourceInstance(resourceDetailsVL, resourceInstanceReqDetailsVL);
		String resourceInstanceUpdatedName = "";
		String resourceInstancePreviousName = resourceDetailsCP.getName();

		// add second resource instance
		RestResponse response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetailsCP,
				sdncDesignerDetails1, resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());
		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		resourceInstanceReqDetailsCP.setName(resourceInstanceUpdatedName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails1, resourceVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code after RI update request", 200,
				updateResourceInstanceResponse.getErrorCode().intValue());
		// change request
		// ErrorInfo errorInfo =
		// Utils.parseYaml(ActionStatus.MISSING_COMPONENT_NAME.name());
		// utils.validateResponseCode(updateResourceInstanceResponse,
		// errorInfo.getCode(), "update resource instance");
		//
		// List<String> variables = Arrays.asList("Resource Instance");
		// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_COMPONENT_NAME.name(),
		// variables, updateResourceInstanceResponse.getResponse());

		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsVL.getName() + " 1");
		resourceInstanceExpectedListName.add(resourceInstancePreviousName + " 3");
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	@Test
	public void updatedResourceNameLengthExceedMaximumCharacters() throws Exception {

		String resourceInstancePreviousName = resourceDetailsCP.getName();
		// update resource instance name
		String resourceInstanceUpdatedName = "a";
		for (int i = 0; i < maxLength; i++) {
			resourceInstanceUpdatedName += "b";
		}
		// ResourceReqDetails updatedResourceDetailsVL =
		// changeResouceName(resourceDetailsVL, resourceInstanceUpdatedName);

		createVFWithCertifiedResourceInstance(resourceDetailsVL, resourceInstanceReqDetailsVL);
		// add second resource instance
		RestResponse response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetailsCP,
				sdncDesignerDetails1, resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());
		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		String prevName = resourceInstanceReqDetailsCP.getName();
		resourceInstanceReqDetailsCP.setName(resourceInstanceUpdatedName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails1, resourceVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT.name());
		// utils.validateResponseCode(updateResourceInstanceResponse,
		// errorInfo.getCode(), "update resource instance");

		List<String> variables = Arrays.asList("Resource Instance", "50");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT.name(), variables,
				updateResourceInstanceResponse.getResponse());

		resourceInstanceReqDetailsCP.setName(prevName);
		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsVL.getName() + " 1");
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsCP.getName() + " 2");
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	@Test
	public void updatedResourceNameWithUnSupportedCharacters() throws Exception {

		createVFWithCertifiedResourceInstance(resourceDetailsVL, resourceInstanceReqDetailsVL);
		String resourceInstancePreviousName = resourceDetailsCP.getName();
		// update resource instance name
		String resourceInstanceUpdatedName = "a???<>";

		// add second resource instance
		RestResponse response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetailsCP,
				sdncDesignerDetails1, resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());
		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		String prevValue = resourceInstanceReqDetailsCP.getName();
		resourceInstanceReqDetailsCP.setName(resourceInstanceUpdatedName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails1, resourceVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_COMPONENT_NAME.name());
		// ResourceRestUtils.validateResponseCode(updateResourceInstanceResponse,
		// errorInfo.getCode(), "update resource instance");

		List<String> variables = Arrays.asList("Resource Instance");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPONENT_NAME.name(), variables,
				updateResourceInstanceResponse.getResponse());

		resourceInstanceReqDetailsCP.setName(prevValue);

		// validate RI name
		List<ComponentInstance> resourceInstances = resourceVF.getComponentInstances();
		List<String> resourceInstanceListName = new ArrayList<String>();
		for (int i = 0; i < resourceInstances.size(); i++) {
			resourceInstanceListName.add(resourceInstances.get(i).getName());
		}
		List<String> resourceInstanceExpectedListName = new ArrayList<String>();
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsVL.getName() + " 1");
		resourceInstanceExpectedListName.add(resourceInstanceReqDetailsCP.getName() + " 2");
		String message = "resource instance name";
		Utils.compareArrayLists(resourceInstanceListName, resourceInstanceExpectedListName, message);

	}

	private static ResourceReqDetails changeResouceName(ResourceReqDetails resourceDet,
			String resourceInstanceUpdatedName) throws Exception {

		ResourceReqDetails updatedResourceDetails = new ResourceReqDetails();
		updatedResourceDetails = resourceDet;
		updatedResourceDetails.setName(resourceInstanceUpdatedName);
		List<String> tags = new ArrayList<String>();
		tags.add(resourceInstanceUpdatedName);
		updatedResourceDetails.setTags(tags);
		Gson gson = new Gson();
		String updatedResourceBodyJson = gson.toJson(updatedResourceDetails);
		RestResponse response = LifecycleRestUtils.changeResourceState(resourceDet, sdncDesignerDetails1,
				resourceDet.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertTrue("change LS state to CHECKOUT, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		response = ResourceRestUtils.updateResourceMetadata(updatedResourceBodyJson, sdncDesignerDetails1,
				updatedResourceDetails.getUniqueId());
		assertEquals("Check response code after updateresource name", 200, response.getErrorCode().intValue());
		response = LifecycleRestUtils.changeResourceState(updatedResourceDetails, sdncDesignerDetails1,
				resourceDet.getVersion(), LifeCycleStatesEnum.CHECKIN);

		return updatedResourceDetails;

	}

	// private Component changeResouceName(Resource resourceDet, String
	// resourceInstanceUpdatedName) throws Exception{
	//
	// User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	// Resource updatedResourceDetails = resourceDet;
	// RestResponse response =
	// LifecycleRestUtils.changeComponentState(updatedResourceDetails,
	// defaultUser, LifeCycleStatesEnum.CHECKOUT, "state changed");
	// assertTrue("change LS state to CHECKOUT, returned status:" +
	// response.getErrorCode(),response.getErrorCode() == 200);
	// updatedResourceDetails.setName(resourceInstanceUpdatedName);
	// List<String> tags = new ArrayList<String>();
	// tags.add(resourceInstanceUpdatedName);
	// updatedResourceDetails.setTags(tags);
	// Gson gson = new Gson();
	// ResourceReqDetails resourceReqDetails = new
	// ResourceReqDetails(updatedResourceDetails);
	// String updatedResourceBodyJson = gson.toJson(resourceReqDetails);
	// response = ResourceRestUtils.updateResource(updatedResourceBodyJson,
	// defaultUser, updatedResourceDetails.getUniqueId());
	// assertEquals("Check response code after updateresource name", 200,
	// response.getErrorCode().intValue());
	// response =
	// LifecycleRestUtils.changeComponentState(updatedResourceDetails,
	// defaultUser, LifeCycleStatesEnum.CHECKIN, "state changed");
	// assertEquals("Check response code after updateresource name", 200,
	// response.getErrorCode().intValue());
	//
	// return updatedResourceDetails;
	//
	// }

	private void createVFWithCertifiedResourceInstance(ResourceReqDetails resourceDetails,
			ComponentInstanceReqDetails resourceInstanceReqDetails) throws Exception {

		RestResponse response = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails1,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after CHECKOUT", 200, response.getErrorCode().intValue());

		// add heat artifact to resource and certify
		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1,
				resourceDetails.getUniqueId());
		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		response = LifecycleRestUtils.certifyResource(resourceDetails);
		assertEquals("Check response code after CERTIFY request", 200, response.getErrorCode().intValue());

		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		resourceInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, sdncDesignerDetails1,
				resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());

		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);
	}

	protected Resource convertResourceGetResponseToJavaObject(ResourceReqDetails resourceDetails) throws IOException {
		RestResponse response = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails1);
		assertEquals("Check response code after get resource", 200, response.getErrorCode().intValue());
		return ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
	}

}
