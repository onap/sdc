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

package org.openecomp.sdc.ci.tests.execute.service;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentInstanceBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GetServiceLatestVersionTest extends ComponentInstanceBaseTest {

	protected ArtifactReqDetails heatArtifactDetails;

	@Rule
	public static TestName name = new TestName();

	public GetServiceLatestVersionTest() {
		super(name, GetServiceLatestVersionTest.class.getName());
	}

	@BeforeMethod
	public void before() throws Exception {
		initMembers();
		createAtomicResource(resourceDetailsVFC_01);
		changeResourceStateToCertified(resourceDetailsVFC_01);
		createAtomicResource(resourceDetailsCP_01);
		changeResourceStateToCertified(resourceDetailsCP_01);
		createAtomicResource(resourceDetailsVL_01);
		changeResourceStateToCertified(resourceDetailsVL_01);
		createVF(resourceDetailsVF_01);
		certifyVf(resourceDetailsVF_01);
		createService(serviceDetails_01);
		createService(serviceDetails_02);
		createService(serviceDetails_03);
		createProduct(productDetails_01);
		createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails); // create
																									// certified
																									// VF
																									// instance
																									// in
																									// service
		/*
		 * RestResponse restResponse =
		 * LifecycleRestUtils.changeServiceState(serviceDetails_01,
		 * sdncDesignerDetails, LifeCycleStates.CHECKIN);
		 * ResourceRestUtils.checkSuccess(restResponse);
		 */
	}

	public void initMembers() throws Exception {
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		sdncPsDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);
		sdncPmDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncTesterDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		resourceDetailsVFC_01 = ElementFactory.getDefaultResourceByType("VFC100", NormativeTypesEnum.COMPUTE,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.VFC.toString()); // resourceType = VFC
		resourceDetailsVF_01 = ElementFactory.getDefaultResourceByType("VF100", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.VF.toString());
		resourceDetailsCP_01 = ElementFactory.getDefaultResourceByType("CP100", NormativeTypesEnum.PORT,
				ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.CP.toString());
		resourceDetailsVL_01 = ElementFactory.getDefaultResourceByType("VL100", NormativeTypesEnum.NETWORK,
				ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.VL.toString());
		serviceDetails_01 = ElementFactory.getDefaultService("newtestservice1", ServiceCategoriesEnum.MOBILITY,
				sdncDesignerDetails.getUserId());
		serviceDetails_02 = ElementFactory.getDefaultService("newtestservice2", ServiceCategoriesEnum.MOBILITY,
				sdncDesignerDetails.getUserId());
		serviceDetails_03 = ElementFactory.getDefaultService("newtestservice3", ServiceCategoriesEnum.MOBILITY,
				sdncDesignerDetails.getUserId());
		productDetails_01 = ElementFactory.getDefaultProduct("product01");
	}

	@Test
	public void getServicesLatestVersionServiceInCheckOutState() throws Exception {
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncPsDetails1);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertNull("No Service returned, one service in checkout state 0.1", servcieFromList);
	}

	@Test
	public void getServicesLatestVersionServiceInCheckInState() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncPsDetails1);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.1"));
	}

	@Test
	public void getServicesLatestVersionByPm() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncPmDetails1);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.1"));
	}

	@Test
	public void getServicesLatestVersionByAdmin() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncAdminDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.1"));
	}

	@Test
	public void getServicesLatestVersionService02CheckOutState() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		String serviceUniqueID = ResponseParser.getUniqueIdFromResponse(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceUniqueID);
		assertTrue(servcieFromList.getVersion().equals("0.1"));
		servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertNull(servcieFromList);
	}

	@Test
	public void getServicesLatestVersionService02CheckInState() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.2"));
	}

	@Test
	public void getServicesLatestVersionServiceWaitingForCertification() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.2"));
	}

	@Test
	public void getServicesLatestVersionServiceCertificationInProgress() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.2"));
	}

	@Test
	public void getServicesLatestVersionServiceCertificationFail() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.FAILCERTIFICATION);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.2"));
	}

	@Test
	public void getServicesLatestVersionServiceCertifed() throws Exception {
		certifyService(serviceDetails_01);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("1.0"));
	}

	@Test
	public void getLatestVersionServiceHasSeveralCertifedVersion_01() throws Exception {
		RestResponse certifyServiceResponse;
		String serviceUniqueIdFromResponse = null;
		int numberOfCertifiedService = 3;
		for (int i = 0; i < numberOfCertifiedService; i++) {
			certifyServiceResponse = certifyService(serviceDetails_01);
			ServiceRestUtils.checkSuccess(certifyServiceResponse);
			if (i == (numberOfCertifiedService - 1)) {
				serviceUniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(certifyServiceResponse);
			}
			RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
					LifeCycleStatesEnum.CHECKOUT);
			ServiceRestUtils.checkSuccess(restResponse);
		}
		// We have service with following versions : 1.0, 2.0 ,3.0 and
		// 3.1(checkedOut)
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceUniqueIdFromResponse);
		assertTrue(servcieFromList.getVersion().equals("3.0"));
	}

	@Test
	public void getLatestVersionServiceHasSeveralCertifedVersions02() throws Exception {
		RestResponse certifyServiceResponse;
		certifyServiceResponse = certifyService(serviceDetails_01);
		ServiceRestUtils.checkSuccess(certifyServiceResponse);
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		certifyServiceResponse = certifyService(serviceDetails_01);
		ServiceRestUtils.checkSuccess(certifyServiceResponse);
		// We have service with following versions : 1.0, 2.0
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("2.0"));
	}

	@Test
	public void getLatestVersionServiceCertifedWasCheckedOutAndCheckedin() throws Exception {
		RestResponse certifyServiceResponse;
		int numberOfCertifiedService = 3;
		for (int i = 0; i < numberOfCertifiedService; i++) {
			certifyServiceResponse = certifyService(serviceDetails_01);
			ServiceRestUtils.checkSuccess(certifyServiceResponse);
			RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
					LifeCycleStatesEnum.CHECKOUT);
			ServiceRestUtils.checkSuccess(restResponse);
		}
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		// We have service with following versions : 1.0, 2.0 and 2.1(checkedIn)
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("3.1"));
	}

	@Test
	public void getLatestVersionServiceCheckOutCertifedService() throws Exception {
		RestResponse restResponse;
		String serviceUniqueIdFromResponse = null;
		RestResponse certifyServiceResponse = certifyService(serviceDetails_01);
		ServiceRestUtils.checkSuccess(certifyServiceResponse);
		for (int i = 0; i < 11; i++) {
			restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
					LifeCycleStatesEnum.CHECKOUT);
			ServiceRestUtils.checkSuccess(restResponse);
			restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
					LifeCycleStatesEnum.CHECKIN);
			ServiceRestUtils.checkSuccess(restResponse);
			if (i == (10)) {
				serviceUniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(restResponse);
			}
		}
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		// We have service with following versions : 1.0 and 1.11(Check-out)
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceUniqueIdFromResponse);
		assertTrue(servcieFromList.getVersion().equals("1.11"));
	}

	@Test
	public void getLatestVersionServiceCheckOutCheckInCertifedService() throws Exception {
		RestResponse restResponse;
		String serviceUniqueIdFromResponse = null;
		RestResponse certifyServiceResponse = certifyService(serviceDetails_01);
		ServiceRestUtils.checkSuccess(certifyServiceResponse);
		for (int i = 0; i < 12; i++) {
			restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
					LifeCycleStatesEnum.CHECKOUT);
			ServiceRestUtils.checkSuccess(restResponse);
			restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
					LifeCycleStatesEnum.CHECKIN);
			ServiceRestUtils.checkSuccess(restResponse);
			if (i == (11)) {
				serviceUniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(restResponse);
			}
		}
		// We have service with following versions : 1.0 and 1.11(Check-out)
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceUniqueIdFromResponse);
		assertTrue(servcieFromList.getVersion().equals("1.12"));
	}

	@Test
	public void getLatestVersionServiceCertifedCheckedOutAndInWaitingForCertificationState() throws Exception {
		certifyService(serviceDetails_01); // 1.0
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ServiceRestUtils.checkSuccess(restResponse);
		// We have service with following versions : 1.0 and 1.1(Waiting For
		// Certification)
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("1.1"));
	}

	@Test
	public void getLatestVersionServiceCertifedCheckedOutAndInCertificationInProgressState() throws Exception {
		certifyService(serviceDetails_01); // 1.0
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		ServiceRestUtils.checkSuccess(restResponse);
		// We have service with following versions : 1.0 and 1.1(Certification
		// In Progress)
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("1.1"));
	}

	// DE190818
	@Test(enabled = false)
	public void getLatestVersionByNonAsdcUser() throws Exception {
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		nonAsdcUser.setUserId("gg750g");
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(nonAsdcUser);
		assertTrue(getServicesLatestVersion.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				getServicesLatestVersion.getResponse());
	}

	// DE190818
	@Test(enabled = false)
	public void getLatestVersionUserIdIsEmpty() throws Exception {
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		nonAsdcUser.setUserId("");
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(nonAsdcUser);
		assertTrue(getServicesLatestVersion.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				getServicesLatestVersion.getResponse());
	}

	@Test
	public void getServicesLatestVersionByTester() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncTesterDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 1);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_01.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.1"));
	}

	@Test
	public void getLatestVersionSeveralServicesInDifferentVersion() throws Exception {
		RestResponse restResponse = certifyService(serviceDetails_01); // 1.0
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = certifyService(serviceDetails_01);
		ServiceRestUtils.checkSuccess(restResponse);
		String service1_UniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse); // serviceDetails_01
														// version is 2.1
														// (check-out)

		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_02, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_02, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_02, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse); // serviceDetails_02
														// version 0.2
														// (Check-in)

		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_03, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		String service3_UniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_03, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse); // serviceDetails_03
														// version 0.2
														// (Check-out)

		RestResponse getServicesLatestVersion = ServiceRestUtils.getServiceLatestVersionList(sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(getServicesLatestVersion);
		List<Service> serviceList = restResponseToResourceObjectList(getServicesLatestVersion);
		assertTrue(serviceList.size() == 3);
		Service servcieFromList = getResourceObjectFromResourceListByUid(serviceList, service1_UniqueIdFromResponse);
		assertTrue(servcieFromList.getVersion().equals("2.0"));
		servcieFromList = getResourceObjectFromResourceListByUid(serviceList, serviceDetails_02.getUniqueId());
		assertTrue(servcieFromList.getVersion().equals("0.2"));
		servcieFromList = getResourceObjectFromResourceListByUid(serviceList, service3_UniqueIdFromResponse);
		assertTrue(servcieFromList.getVersion().equals("0.1"));
	}

	///////////////////////////////////////////////////////////////
	private RestResponse certifyService(ServiceReqDetails serviceDetails) throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncTesterDetails,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		ServiceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncTesterDetails,
				LifeCycleStatesEnum.CERTIFY);
		ServiceRestUtils.checkSuccess(restResponse);
		return restResponse;
	}

	protected List<Service> restResponseToResourceObjectList(RestResponse restResponse) {
		JsonElement jelement = new JsonParser().parse(restResponse.getResponse());
		JsonArray jsonArray = jelement.getAsJsonArray();
		List<Service> restResponseArray = new ArrayList<>();
		Service service = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			String serviceString = (String) jsonArray.get(i).toString();
			service = ResponseParser.convertServiceResponseToJavaObject(serviceString);
			restResponseArray.add(service);
		}
		return restResponseArray;
	}

	protected Service getResourceObjectFromResourceListByUid(List<Service> serviceList, String uid) {
		if (serviceList != null && serviceList.size() > 0) {
			for (Service service : serviceList) {
				if (service.getUniqueId().equals(uid))
					return service;
			}
		} else
			return null;
		return null;
	}

	private RestResponse changeResourceStateToCertified(ResourceReqDetails resourceDetails) throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		if (restResponse.getErrorCode() == 200) {
			restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncTesterDetails,
					LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponse;
		if (restResponse.getErrorCode() == 200) {
			restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncTesterDetails,
					LifeCycleStatesEnum.CERTIFY);
			if (restResponse.getErrorCode() == 200) {
				String newVersion = ResponseParser.getVersionFromResponse(restResponse);
				resourceDetails.setVersion(newVersion);
				resourceDetails.setLifecycleState(LifecycleStateEnum.CERTIFIED);
				resourceDetails.setLastUpdaterUserId(sdncTesterDetails.getUserId());
				resourceDetails.setLastUpdaterFullName(sdncTesterDetails.getFullName());
				String uniqueIdFromRresponse = ResponseParser.getValueFromJsonResponse(restResponse.getResponse(),
						"uniqueId");
				resourceDetails.setUniqueId(uniqueIdFromRresponse);
			}
		}
		return restResponse;
	}

	// private void certifyVf(ResourceReqDetails resource) throws Exception {
	// RestResponse createAtomicResourceInstance =
	// createAtomicInstanceForVFDuringSetup(resource, resourceDetailsVFC_01,
	// sdncDesignerDetails);
	// ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
	// createAtomicResourceInstance =
	// createAtomicInstanceForVFDuringSetup(resource, resourceDetailsCP_01,
	// sdncDesignerDetails);
	// ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
	// createAtomicResourceInstance =
	// createAtomicInstanceForVFDuringSetup(resource, resourceDetailsVL_01,
	// sdncDesignerDetails);
	// ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
	// //createVFInstanceDuringSetup(service, resource, sdncDesignerDetails);
	// RestResponse response =
	// ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails,
	// sdncDesignerDetails, resource.getUniqueId());
	// ResourceRestUtils.checkSuccess(response);
	// RestResponse changeResourceStateToCertified =
	// changeResourceStateToCertified(resource);
	// ResourceRestUtils.checkSuccess(changeResourceStateToCertified);
	// }

	private void certifyVf(ResourceReqDetails resource) throws Exception {
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String cpCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, resourceDetailsVFC_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String computeCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, resourceDetailsVL_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String vlCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		// Fixing Vl/Cp req/cap
		ComponentTypeEnum containerCompType = ComponentTypeEnum.RESOURCE;
		User user = sdncDesignerDetails;
		fulfillCpRequirement(resource, cpCompInstId, computeCompInstId, computeCompInstId, user, containerCompType);
		consumeVlCapability(resource, cpCompInstId, vlCompInstId, cpCompInstId, user, containerCompType);

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails,
				sdncDesignerDetails, resource.getUniqueId());
		ResourceRestUtils.checkSuccess(response);
		RestResponse changeResourceStateToCertified = changeResourceStateToCertified(resource);
		ResourceRestUtils.checkSuccess(changeResourceStateToCertified);
	}

}
