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

package org.openecomp.sdc.ci.tests.execute.product;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_MISSING_INFORMATION;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_NOT_FOUND;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS_DELETE;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_UNSUPPORTED_ERROR;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentInstanceBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChangeServiceInstanceVersionTest extends ComponentInstanceBaseTest {

	protected ArtifactReqDetails heatArtifactDetails;
	public String firstVfInstanceUniqueId;
	public String firstVfInstanceName;
	public String secondVfInstanceUniqueId;
	public String secoundVfInstanceName;
	public String serviceInstanceToReplaceUniqueId;
	public String expectedServiceName;
	public String expectedPosX;
	public String expectedPosY;
	public String actualServiceInstanceName;
	public String actualPosX;
	public String actualPosY;

	@Rule
	public static TestName name = new TestName();

	public ChangeServiceInstanceVersionTest() {
		super(name, ChangeServiceInstanceVersionTest.class.getName());
	}

	@BeforeMethod
	public void before() throws Exception {
		firstVfInstanceName = null;
		secoundVfInstanceName = null;
		firstVfInstanceUniqueId = null;
		secondVfInstanceUniqueId = null;
		serviceInstanceToReplaceUniqueId = null;
		expectedServiceName = null;
		expectedPosX = null;
		expectedPosY = null;
		actualServiceInstanceName = null;
		actualPosX = null;
		actualPosY = null;
		init();
		createComponents();
	}

	private void createComponents() throws Exception {
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		createAtomicResource(resourceDetailsVFC_01);
		changeResourceStateToCertified(resourceDetailsVFC_01);
		createAtomicResource(resourceDetailsCP_01);
		changeResourceStateToCertified(resourceDetailsCP_01);
		createAtomicResource(resourceDetailsVL_01);
		changeResourceStateToCertified(resourceDetailsVL_01);
		createAtomicResource(resourceDetailsVFC_02);
		changeResourceStateToCertified(resourceDetailsVFC_02);
		createAtomicResource(resourceDetailsCP_02);
		changeResourceStateToCertified(resourceDetailsCP_02);
		createAtomicResource(resourceDetailsVL_02);
		changeResourceStateToCertified(resourceDetailsVL_02);
		createVF(resourceDetailsVF_02);
		createVF(resourceDetailsVF_01);
		// create check-In services
		createService(serviceDetails_01);
		createService(serviceDetails_02);
		createService(serviceDetails_03);
		createProduct(productDetails_01);
		createProduct(productDetails_02);

		// add resourceDetailsCP_01 ,resourceDetailsVFC_01 and
		// resourceDetailsCP_01 to resourceDetailsVF_01 and certify
		// resourceDetailsVF_01
		certifyVf(resourceDetailsVF_01, resourceDetailsVFC_02, resourceDetailsCP_01);
		// add resourceDetailsCP_02 ,resourceDetailsVFC_02 and
		// resourceDetailsVL_02 to resourceDetailsVF_02 and certify
		// resourceDetailsVF_02
		certifyVf(resourceDetailsVF_02, resourceDetailsVFC_02, resourceDetailsCP_02);
		RestResponse createVFInstanceResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails); // serviceDetails_01 has certified
										// resourceDetailsVF_01
		firstVfInstanceName = ResponseParser.getNameFromResponse(createVFInstanceResponse);
		createVFInstanceResponse = createVFInstanceDuringSetup(serviceDetails_02, resourceDetailsVF_02,
				sdncDesignerDetails); // serviceDetails_01 has certified
										// resourceDetailsVF_02
		secoundVfInstanceName = ResponseParser.getUniqueIdFromResponse(createVFInstanceResponse);
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_02, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
	}

	@Test
	public void changeServiceInstanceVersionByPm() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// get the new VF instance uniqueId after checkout service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	@Test
	public void changeServiceInstanceVersionByAdmin() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// get the new VF instance uniqueId after checkout service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// service [0.2] state to CERTIFICATIONREQUEST
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncAdminDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncAdminDetails, ComponentTypeEnum.PRODUCT,
				true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncAdminDetails,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	@Test
	public void changeServiceInstanceToOlderVersion() throws Exception {
		// Get VF Instance UniquId [Service version 0.1]
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		ComponentInstance actualComponentInstance = service.getComponentInstances().get(0);
		firstVfInstanceUniqueId = actualComponentInstance.getUniqueId();
		String serviceOlderVersionUniquId = ResponseParser.getUniqueIdFromResponse(getServiceResponse);

		// Checkout service [0.2]
		RestResponse changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01,
				sdncDesignerDetails, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// get the new VF instance uniqueId after checkout service
		getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// Adding service instance (serviceDetails_01 V0.2) to product without
		// saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to Older version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceOlderVersionUniquId, sdncPmDetails1, ComponentTypeEnum.PRODUCT,
				true);
		// RestResponse changeServiceInstanceVersionResponse =
		// changeServiceInstanceVersion(productDetails_01.getUniqueId(),
		// serviceInstanceToReplaceUniqueId , serviceNewUniqueUid,
		// sdncPmDetails1, ComponentTypeEnum.PRODUCT , true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	// DE190201
	@Test
	public void changeServiceInstanceVersionToCertifiedVersion() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		/*
		 * String serviceNewUniqueUid =
		 * ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		 * serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		 */
		// get the new VF instance uniqueId after checkout service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncTesterDetails,
				LifeCycleStatesEnum.CERTIFY);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		// RestResponse changeServiceInstanceVersionResponse =
		// changeServiceInstanceVersion(productDetails_01.getUniqueId(),
		// serviceInstanceToReplaceUniqueId , serviceNewUniqueUid,
		// sdncPmDetails1, ComponentTypeEnum.PRODUCT , true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	// DE191927
	@Test(enabled = false)
	public void changeServiceInstanceVersionThenReCheckInProduct() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// get the new VF instance uniqueId after checkout service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		/////////////////////
		productOldUniqueId = productDetails_01.getUniqueId();
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		/////////////////////////////////////////////
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	@Test
	public void changeServiceInstanceToHisVersion() throws Exception {
		// Get VF Instance UniquId [Service version 0.1]
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		String serviceOlderVersionUniquId = ResponseParser.getUniqueIdFromResponse(getServiceResponse);
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// get the new VF instance uniqueId after checkout service
		getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceOlderVersionUniquId, sdncPmDetails1, ComponentTypeEnum.PRODUCT,
				true);
		// RestResponse changeServiceInstanceVersionResponse =
		// changeServiceInstanceVersion(productDetails_01.getUniqueId(),
		// serviceInstanceToReplaceUniqueId , serviceNewUniqueUid,
		// sdncPmDetails1, ComponentTypeEnum.PRODUCT , true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	@Test
	public void changeServiceInstanceVersionByAdminNotByProductOwner() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);

		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncAdminDetails, ComponentTypeEnum.PRODUCT,
				true);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);

	}

	@Test
	public void changeServiceInstanceVersionByPmNotByProductOwner() throws Exception {
		// Adding service instance (serviceDetails_01) to product AND --->
		// saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// change uniqueId after product check-out in expected Req&Cap
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		updateNewComponentInstanceId(createServiceInstanceResp, productNewUniqueId);
		// CHANGE Service Instance VERSION BY NON PRODUCT OWNER (sdncPmDetails1
		// instead sdncPmDetails1)
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(
				productDetails_01.getUniqueId(), serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails2,
				ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void changeServiceInstanceVersionByTester() throws Exception {
		// Adding service instance (serviceDetails_01) to product AND --->
		// saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceToReplaceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// change uniqueId after product check-out in expected Req&Cap
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// CHANGE Service Instance VERSION BY NON PRODUCT OWNER (sdncPmDetails1
		// instead sdncPmDetails1)
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(
				productDetails_01.getUniqueId(), serviceInstanceToReplaceUniqueId, serviceNewUniqueUid,
				sdncTesterDetails, ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void changeServiceInstanceVersionProductIsNotCheckOut() throws Exception {
		// Adding service instance (serviceDetails_01) to product AND --->
		// saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceToReplaceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// CHANGE Service Instance VERSION for Non checkedOut product
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productOldUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeServiceInstanceVersionResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	// DE191727
	@Test(enabled = false)
	public void changeServiceInstanceVersionServiceIsInCheckOutState() throws Exception {
		// Adding service instance (serviceDetails_01) to product AND --->
		// saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceToReplaceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// change uniqueId after product check-out in expected Req&Cap
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// CHANGE Service Instance VERSION to service in checkOut state
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(
				productDetails_01.getUniqueId(), serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1,
				ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(serviceNewUniqueUid);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), varibales,
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);

	}

	@Test
	public void changeServiceInstanceVersionServiceInstanceDoesNotExist() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// change service instance to newer version
		String serviceUniqueUidNotExist = "1234567890";
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceUniqueUidNotExist, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(serviceUniqueUidNotExist);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND.name(), varibales,
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	// DE189392
	@Test(enabled = false)
	public void changeServiceInstanceNonExistingProduct() throws Exception {
		// Adding service instance (serviceDetails_01) to product saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// change service instance to newer version - Non existing Product
		String productNewUniqueIdNotExist = "1234567890";
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueIdNotExist,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(productNewUniqueIdNotExist);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND.name(), varibales,
				changeServiceInstanceVersionResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void changeServiceInstanceVersionToNonExisitingServiceVersion() throws Exception {
		// Adding service instance (serviceDetails_01) to product saving Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		updateNewComponentInstanceId(createServiceInstanceResp, productNewUniqueId);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// change service instance to Non-existing version
		String serviceUniqueUidNotExist = "1234567890";
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceUniqueUidNotExist, sdncPmDetails1, ComponentTypeEnum.PRODUCT,
				true);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(serviceUniqueUidNotExist);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), varibales,
				changeServiceInstanceVersionResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void changeServiceInstanceComponentTypeIsNotProduct() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version for NON-Component Type =
		// Product (e.g. service)
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.SERVICE, true);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), varibales,
				changeServiceInstanceVersionResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void changeServiceInstanceComponentTypeNotSupported() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version for NON-Component Type =
		// Product (e.g. service)
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1,
				ComponentTypeEnum.SERVICE_INSTANCE, true);
		assertEquals("Check response code ", STATUS_CODE_UNSUPPORTED_ERROR,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("null");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.UNSUPPORTED_ERROR.name(), varibales,
				changeServiceInstanceVersionResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void SeveralServiceInstanceFromSameServiceVersionChangeVersionOnlyForOneServiceInstance() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstanceDuringSetup(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		serviceDetails_01.setUniqueId(serviceNewUniqueUid);
		// get the new VF instance uniqueId after checkout service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01.getUniqueId(), sdncPmDetails1);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		List<ComponentInstance> serviceComponentInstances = service.getComponentInstances();
		for (ComponentInstance component : serviceComponentInstances) {
			if (component.getName().equals(firstVfInstanceName)) {
				firstVfInstanceUniqueId = component.getUniqueId();
			}
		}
		assertTrue(firstVfInstanceUniqueId != null);
		// delete resource instance (resourceDetailsVF_01) from Service
		RestResponse deleteVfFromServiceResponse = deleteVFInstanceDuringSetup(firstVfInstanceUniqueId,
				serviceDetails_01, sdncDesignerDetails);
		assertTrue(deleteVfFromServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Add different VF instance (resourceDetailsVF_02) to Service
		RestResponse restResponse = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(restResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get service instance new uniqueId , name and position after checkout
		// product
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, sdncPmDetails1, ComponentTypeEnum.PRODUCT, true);
		ProductRestUtils.checkSuccess(changeServiceInstanceVersionResponse);
		actualServiceInstanceName = ResponseParser.getNameFromResponse(changeServiceInstanceVersionResponse);
		actualPosX = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posX");
		actualPosY = ResponseParser.getValueFromJsonResponse(changeServiceInstanceVersionResponse.getResponse(),
				"posY");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(changeServiceInstanceVersionResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Verify that Service instance name and position didn't change after
		// changing service instance version
		assertTrue(actualServiceInstanceName.equals(expectedServiceName));
		assertTrue(actualPosX.equals(expectedPosX));
		assertTrue(actualPosY.equals(expectedPosY));
	}

	@Test
	public void changeServiceInstanceVersionByNonAsdcUser() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		nonAsdcUser.setUserId("bt760h");
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, nonAsdcUser, ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void changeServiceInstanceVersionEmptyUserId() throws Exception {
		// Adding service instance (serviceDetails_01) to product without saving
		// Req&Cap
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01,
				sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Adding service instance (serviceDetails_02) to product AND ---> Save
		// Req&Cap
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// check-in product
		RestResponse changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productOldUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Checkout service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		String serviceNewUniqueUid = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		// Check-In service [0.2]
		changeStatusResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeStatusResponse);
		// check-out product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(changeStatusResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		nonAsdcUser.setUserId("");
		// change service instance to newer version
		RestResponse changeServiceInstanceVersionResponse = changeServiceInstanceVersion(productNewUniqueId,
				serviceInstanceToReplaceUniqueId, serviceNewUniqueUid, nonAsdcUser, ComponentTypeEnum.PRODUCT, true);
		assertEquals("Check response code ", STATUS_CODE_MISSING_INFORMATION,
				changeServiceInstanceVersionResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), new ArrayList<String>(),
				changeServiceInstanceVersionResponse.getResponse());
		// Check-in product
		changeStatusResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeStatusResponse);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	////////////////////////////////////
	private void updateNewComponentInstanceId(RestResponse createServiceInstanceResp, String productNewUniqueId)
			throws Exception {
		String firstServiceInstanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		RestResponse getProductResponse = ProductRestUtils.getProduct(productNewUniqueId, sdncPmDetails1.getUserId());
		Product product = ResponseParser.parseToObjectUsingMapper(getProductResponse.getResponse(), Product.class);
		List<ComponentInstance> componentInstances = product.getComponentInstances();
		for (ComponentInstance component : componentInstances) {
			if (component.getNormalizedName().equals(firstServiceInstanceNormalizedName)) {
				serviceInstanceToReplaceUniqueId = component.getUniqueId();
				expectedServiceName = component.getName();
				expectedPosX = component.getPosX();
				expectedPosY = component.getPosY();
			}
		}
		assertTrue(serviceInstanceToReplaceUniqueId != null);
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

	private void certifyVf(ResourceReqDetails resource, ResourceReqDetails computeResource,
			ResourceReqDetails cpResource) throws Exception {
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, cpResource,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, computeResource,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails,
				sdncDesignerDetails, resource.getUniqueId());
		ResourceRestUtils.checkSuccess(response);

		String capOwnerId = toCompInstId;
		User user = sdncDesignerDetails;
		ComponentTypeEnum containerCompType = ComponentTypeEnum.RESOURCE;

		fulfillCpRequirement(resource, fromCompInstId, toCompInstId, capOwnerId, user, containerCompType);

		RestResponse changeResourceStateToCertified = changeResourceStateToCertified(resource);
		ResourceRestUtils.checkSuccess(changeResourceStateToCertified);
	}
}
