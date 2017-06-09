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

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_ALREADY_EXISTS;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_INVALID_CONTENT;
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
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentInstanceBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductComponentInstanceCRUDTest extends ComponentInstanceBaseTest {

	protected ArtifactReqDetails heatArtifactDetails;
	@Rule
	public static TestName name = new TestName();

	public ProductComponentInstanceCRUDTest() {
		super(name, ProductComponentInstanceCRUDTest.class.getName());
	}

	@BeforeMethod
	public void before() throws Exception {
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

		// addresourceDetailsCP_02 ,resourceDetailsVFC_02 and
		// resourceDetailsVL_02 to resourceDetailsVF_02 check-in VF
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resourceDetailsVF_02, resourceDetailsVFC_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resourceDetailsVF_02, resourceDetailsCP_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resourceDetailsVF_02, resourceDetailsVL_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		resourceDetailsVF_02.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		// addresourceDetailsCP_01 ,resourceDetailsVFC_01 and
		// resourceDetailsVL_01 to resourceDetailsVF_01 and certify
		// resourceDetailsVF_01
		certifyVf(resourceDetailsVF_01);
		createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails); // serviceDetails_01
																									// has
																									// certified
																									// VF
		createVFInstanceDuringSetup(serviceDetails_02, resourceDetailsVF_02, sdncDesignerDetails); // serviceDetails_02
																									// has
																									// check-in
																									// VF
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_02, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
	}

	// pass
	@Test
	public void createServiceInstanceTest() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	// DE189427
	@Test(enabled = false)
	public void createServiceInstanceFromCheckedOutState() throws Exception {
		// can't create instance of checked-out component
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceInToAnotherServiceInstance() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String uniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, uniqueIdFromResponse, ComponentTypeEnum.PRODUCT);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_NOT_FOUND);
	}

	@Test
	public void createSeveralServiceInstanceFromSameServices() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void createSeveralServiceInstanceFromDifferentServices() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void createCertifiedServiceInstance() throws Exception {
		changeServiceStateToCertified(serviceDetails_01);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceByPm() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceWithoutVf() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails_03, sdncAdminDetails, "0.1", LifeCycleStatesEnum.CHECKIN);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_02, serviceDetails_03, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_02, 1, 0);
	}

	@Test
	public void createServiceInstanceByNonProductOwner() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails2);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceByNonAsdcUser() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		User nonExistingSdncUser = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		;
		nonExistingSdncUser.setUserId("bt1234");
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, nonExistingSdncUser);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceToNotCheckOutProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	// pass
	@Test
	public void createServiceInstanceNameIsEmpty() throws Exception {
		String expectedServiceInstanceName = serviceDetails_01.getName() + " 1";
		String expectedServiceInstancenormalizedName = serviceDetails_01.getName() + "1";
		serviceDetails_01.setName("");
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (expectedServiceInstancenormalizedName).toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance Name ", expectedServiceInstanceName, instanceName);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(expectedServiceInstanceName, actualComponentInstance.getName());
		assertEquals((expectedServiceInstancenormalizedName).toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	// pass
	@Test
	public void createServiceInstanceNameIsNull() throws Exception {
		String expectedServiceInstanceName = serviceDetails_01.getName() + " 1";
		String expectedServiceInstancenormalizedName = serviceDetails_01.getName() + "1";
		serviceDetails_01.setName(null);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (expectedServiceInstancenormalizedName).toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance Name ", expectedServiceInstanceName, instanceName);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(expectedServiceInstanceName, actualComponentInstance.getName());
		assertEquals((expectedServiceInstancenormalizedName).toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	@Test(enabled = false)
	public void createServiceInstanceToNonExistingProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, "blabla", ComponentTypeEnum.PRODUCT);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, createServiceInstanceResp.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("blabla");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PRODUCT_NOT_FOUND.name(), varibales, createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceToNonSupportedComponentType() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_UNSUPPORTED_ERROR);
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("null");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.UNSUPPORTED_ERROR.name(), varibales, createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	// pass
	@Test
	public void createServiceInstancePositionIsEmpty() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		serviceInstanceReqDetails.setPosX("");
		serviceInstanceReqDetails.setPosY("");
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		ComponentInstance componentInstance = ResponseParser.parseToObjectUsingMapper(createServiceInstanceResp.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstancePositionIsNull() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		serviceInstanceReqDetails.setPosX(null);
		serviceInstanceReqDetails.setPosY(null);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		ComponentInstance componentInstance = ResponseParser.parseToObjectUsingMapper(createServiceInstanceResp.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.PRODUCT);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceByDesigner() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncDesignerDetails);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void createServiceInstanceUserIdIsEmpty() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		User nonSdncDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		nonSdncDetails.setUserId("");
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, nonSdncDetails);
		assertTrue(createServiceInstanceResp.getErrorCode() == STATUS_CODE_MISSING_INFORMATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), new ArrayList<String>(), createServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	//// Update Service instance

	@Test
	public void updateServiceInstanceNameByPm() throws Exception {
		// Check-in Product by PM and Check-out by PM
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create service instance by PM
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "abcD";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (newName).toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance Name ", newName, instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(newName, actualComponentInstance.getName());
		assertEquals((newName).toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	@Test
	public void updateServiceInstanceNewNameAndLocation() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "updaatedName100";
		serviceInstanceReqDetails.setPosX("100");
		serviceInstanceReqDetails.setPosY("100");
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(serviceInstanceReqDetails.getPosX(), actualComponentInstance.getPosX());
		assertEquals(serviceInstanceReqDetails.getPosY(), actualComponentInstance.getPosY());
		assertEquals(newName, actualComponentInstance.getName());
		assertEquals(newName.toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	@Test(enabled = false)
	public void updateServiceInstanceNameRemoveSpacesFromBiginningAndEnd() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "  Abcd   ";
		String expectedNewName = "  Abcd   ";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (expectedNewName).toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance Name ", expectedNewName, instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(expectedNewName, actualComponentInstance.getName());
		assertEquals((expectedNewName).toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	// pass
	@Test
	public void updateServiceInstanceNameAllowedCharacters() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "qwer-TYUIOP_asd_0987654321.Abcd";
		String ExpectedNormalizName = "qwertyuiopasd0987654321abcd";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", ExpectedNormalizName, instanceNormalizedName);
		assertEquals("check Resource Instance Name ", newName, instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(newName, actualComponentInstance.getName());
		assertEquals(ExpectedNormalizName, actualComponentInstance.getNormalizedName());

	}

	@Test
	public void updateInstanceNameInvalidCharacters() throws Exception {
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/', '|', '\\', ',' };
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "Abcd1";
		String updateName;
		for (int i = 0; i < invalidChars.length; i++) {
			updateName = newName + invalidChars[i];
			serviceInstanceReqDetails.setName(updateName);
			RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
			assertEquals("Check response code ", STATUS_CODE_INVALID_CONTENT, updateServiceInstanceResponse.getErrorCode().intValue());
			ArrayList<String> varibales = new ArrayList<String>();
			varibales.add("Service Instance");
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPONENT_NAME.name(), varibales, updateServiceInstanceResponse.getResponse());
		}
	}

	// pass
	@Test
	public void updateInstanceNameMaxLength() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "Qwertyuiop1234567890asdfAhjklzxcvbnmasdfghjkl12345";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (newName).toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance Name ", newName, instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(newName, actualComponentInstance.getName());
		assertEquals((newName).toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	@Test
	public void updateInstanceNameExceedMaxLength() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String expectedName = ResponseParser.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "name");
		String expectedNormalizedName = ResponseParser.getValueFromJsonResponse(createServiceInstanceResp.getResponse(), "normalizedName");
		String newName = "Qwertyuiop1234567890asdfAhjklzxcvbnmasdfghjkl123456";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		assertEquals("Check response code ", STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT, updateServiceInstanceResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("Service Instance");
		varibales.add("50");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT.name(), varibales, updateServiceInstanceResponse.getResponse());
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(expectedName, actualComponentInstance.getName());
		assertEquals(expectedNormalizedName, actualComponentInstance.getNormalizedName());
	}

	@Test
	public void updateServiceInstanceNameEmpty() throws Exception {
		// see US534663 In case a PS/PM removes the current service instance
		// name then BE has to generate again the "default" service instance
		// name
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (serviceDetails_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance normalizedName ", (serviceDetails_01.getName() + " 2"), instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(instanceName, actualComponentInstance.getName());
		assertEquals(instanceNormalizedName, actualComponentInstance.getNormalizedName());
	}

	// pass
	@Test
	public void updateServiceInstanceNameNull() throws Exception {
		// see US534663 In case a PS/PM removes the current service instance
		// name then BE has to generate again the "default" service instance
		// name
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = null;
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (serviceDetails_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance normalizedName ", (serviceDetails_01.getName() + " 2"), instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(instanceName, actualComponentInstance.getName());
		assertEquals(instanceNormalizedName, actualComponentInstance.getNormalizedName());
	}

	@Test
	public void updateServiceInstanceCheckedByOtherUser() throws Exception {
		// see US534663 In case a PS/PM removes the current service instance
		// name then BE has to generate again the "default" service instance
		// name
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "blabla";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails2, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION, updateServiceInstanceResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), updateServiceInstanceResponse.getResponse());
	}

	@Test
	public void updateServiceInstance_UserIdIsNonAsdcUser() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "blabla";
		serviceInstanceReqDetails.setName(newName);
		User nonSdncUserDetails = new User();
		nonSdncUserDetails.setUserId("bt4567");
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, nonSdncUserDetails, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION, updateServiceInstanceResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), updateServiceInstanceResponse.getResponse());
	}

	@Test
	public void updateServiceInstanceNameToAlreadyExisting() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String ServiceName1 = ResponseParser.getNameFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// Update service instance2 name to service instance1
		serviceInstanceReqDetails.setName(ServiceName1);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		assertEquals("Check response code ", STATUS_CODE_ALREADY_EXISTS, updateServiceInstanceResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("Service Instance");
		varibales.add(ServiceName1);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name(), varibales, updateServiceInstanceResponse.getResponse());
	}

	@Test
	public void updateServiceInstanceForNonExistingProduct() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "blabla";
		serviceInstanceReqDetails.setName(newName);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, "blablabla", ComponentTypeEnum.PRODUCT);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, updateServiceInstanceResponse.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PRODUCT_NOT_FOUND.name(), varibales, updateServiceInstanceResponse.getResponse());

	}

	@Test
	public void updateNonExistingServiceInstance() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String newName = "blabla";
		serviceInstanceReqDetails.setName(newName);
		serviceInstanceReqDetails.setUniqueId("11111111");
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(newName);
		varibales.add("service instance");
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, updateServiceInstanceResponse.getErrorCode().intValue());
		// need to change ActionStatus.RESOURCE_INSTANCE_NOT_FOUND.name() to
		// ActionStatus.SERVICE_INSTANCE_NOT_FOUND.name()
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND.name(), varibales, updateServiceInstanceResponse.getResponse());
	}

	@Test
	public void updateServiceInstanceLocation() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		serviceInstanceReqDetails.setPosX("50");
		serviceInstanceReqDetails.setPosY("100");
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(serviceInstanceReqDetails.getPosX(), actualComponentInstance.getPosX());
		assertEquals(serviceInstanceReqDetails.getPosY(), actualComponentInstance.getPosY());
	}

	@Test
	public void updateServiceInstanceToNonExistingLocation() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		String nameFromResponse = ResponseParser.getNameFromResponse(createServiceInstanceResp);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		serviceInstanceReqDetails.setPosX("-50");
		serviceInstanceReqDetails.setPosY("-100");
		serviceInstanceReqDetails.setName(nameFromResponse);
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(serviceInstanceReqDetails.getPosX(), actualComponentInstance.getPosX());
		assertEquals(serviceInstanceReqDetails.getPosY(), actualComponentInstance.getPosY());
		assertEquals(nameFromResponse, actualComponentInstance.getName());
	}

	@Test
	public void updateServiceInstanceLocationNameIsEmpty() throws Exception {
		String expectedServiceInstanceName = serviceDetails_01.getName() + " 2";
		String expectedServiceInstancenormalizedName = serviceDetails_01.getName() + "2";
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		serviceInstanceReqDetails.setPosX("100");
		serviceInstanceReqDetails.setPosY("200");
		serviceInstanceReqDetails.setName("");
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String nameFromResponse = ResponseParser.getNameFromResponse(updateServiceInstanceResponse);
		String postX = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "posX");
		String postY = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "posY");
		assertEquals(nameFromResponse, expectedServiceInstanceName);
		assertEquals(postX, "100");
		assertEquals(postY, "200");
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(serviceInstanceReqDetails.getPosX(), actualComponentInstance.getPosX());
		assertEquals(serviceInstanceReqDetails.getPosY(), actualComponentInstance.getPosY());
		assertEquals(nameFromResponse, actualComponentInstance.getName());
		assertEquals(expectedServiceInstancenormalizedName.toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	// pass
	@Test
	public void updateServiceInstanceNameToProductName() throws Exception {
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_01);
		RestResponse createServiceInstanceResp = ComponentInstanceRestUtils.createComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// ComponentInstance componentInstance =
		// ResponseParser.parseToObjectUsingMapper(createServiceInstanceResp.getResponse(),
		// ComponentInstance.class);
		// addCompInstReqCapToExpected(componentInstance,
		// ComponentTypeEnum.PRODUCT);
		serviceInstanceReqDetails.setName(productDetails_01.getName());
		RestResponse updateServiceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ResourceRestUtils.checkSuccess(updateServiceInstanceResponse);
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateServiceInstanceResponse.getResponse(), "name");
		assertEquals("check Resource Instance normalizedName ", (serviceInstanceReqDetails.getName()).toLowerCase(), instanceNormalizedName);
		assertEquals("check Resource Instance Name ", serviceInstanceReqDetails.getName(), instanceName);
		// get product and verify that service instanceName is correct
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		assertEquals(serviceInstanceReqDetails.getName(), actualComponentInstance.getName());
		assertEquals((serviceInstanceReqDetails.getName()).toLowerCase(), actualComponentInstance.getNormalizedName());
	}

	//// Delete Service Instance

	@Test
	public void deleteAllServiceInstanceFromProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails1);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 0, 0);
	}

	@Test
	public void deleteServiceWhileServiceInstanceExistInProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		// Delete service while service instance of it exist in product
		RestResponse deleteServiceResponse = ServiceRestUtils.deleteServiceById(serviceDetails_01.getUniqueId(), sdncDesignerDetails.getUserId());
		assertTrue(deleteServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		// Get product and verify that service instance still exists
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		// ComponentInstance actualComponentInstance =
		// actualProduct.getComponentInstances().get(0);
		// assertTrue(serviceInstanceUniqueIdFromResponse ==
		// actualComponentInstance.getUniqueId());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	// pass
	@Test
	public void deleteServiceInstanceByPm() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails1);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void deleteServiceInstanceByPmCreatedByPm() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		productDetails_01.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails1);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void deleteServiceInstanceByPmWhichIsCheckedOutByAnotherPm() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails2);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	// DE190189
	@Test
	public void deleteServiceInstanceByPmCreatedByPs() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		String productOldUniqueId = productDetails_01.getUniqueId();
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(restResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get product and get service instance new uniquId
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPmDetails1.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		String serviceInstanceUniqueId = actualComponentInstance.getUniqueId();
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails1);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 0, 0);
	}

	// DE190189
	@Test
	public void deleteServiceInstanceByAdminCreatedByPs() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		String productOldUniqueId = productDetails_01.getUniqueId();
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncAdminDetails, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		String productNewUniqueId = ResponseParser.getUniqueIdFromResponse(restResponse);
		updateExpectedReqCapAfterChangeLifecycleState(productOldUniqueId, productNewUniqueId);
		// get product and get service instance new uniquId
		RestResponse getActualProductResponse = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncAdminDetails.getUserId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getActualProductResponse.getResponse(), Product.class);
		ComponentInstance actualComponentInstance = actualProduct.getComponentInstances().get(0);
		String serviceInstanceUniqueId = actualComponentInstance.getUniqueId();
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncAdminDetails);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 0, 0);
	}

	@Test
	public void createAndDeleteServiceInstanceByAdmin() throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncAdminDetails, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(restResponse);
		// productDetails_01.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncAdminDetails);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncAdminDetails);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncAdminDetails);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void deleteServiceInstanceFromNonCheckOutProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		RestResponse restResponse = LifecycleRestUtils.changeProductState(productDetails_01, sdncPmDetails1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails1);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void deleteServiceInstanceByDesigner() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncDesignerDetails);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteServiceInstanceByTester() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncTesterDetails);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteServiceInstanceByPsWhichIsCheckedOutByAnotherPs() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPsDetails2);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	// pass
	@Test
	public void deleteServiceInstanceByNonAsdcUser() throws Exception {
		User nonExistingSdncUser = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		;
		nonExistingSdncUser.setUserId("bt1234");
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, nonExistingSdncUser);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_RESTRICTED_OPERATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteServiceInstanceFromNonExistingProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(sdncPmDetails1, "1234567890", serviceInstanceUniqueId, ComponentTypeEnum.PRODUCT);
		assertTrue(deleteResourceInstanceResponse.getErrorCode() == STATUS_CODE_NOT_FOUND);
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PRODUCT_NOT_FOUND.name(), varibales, deleteResourceInstanceResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteNonExistingServiceInstanceFromProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		// String serviceInstanceUniqueId =
		// ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(sdncPmDetails1, productDetails_01.getUniqueId(), "1234567890123456unExistingServiceInstance", ComponentTypeEnum.PRODUCT);
		assertTrue(deleteResourceInstanceResponse.getErrorCode() == STATUS_CODE_NOT_FOUND);
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("1234567890123456unExistingServiceInstance");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PRODUCT_NOT_FOUND.name(), varibales, deleteResourceInstanceResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteServiceInstanceFromNonSupportedComponentType() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(sdncPmDetails1, productDetails_01.getUniqueId(), serviceInstanceUniqueId, ComponentTypeEnum.RESOURCE_INSTANCE);
		assertTrue(deleteResourceInstanceResponse.getErrorCode() == STATUS_CODE_UNSUPPORTED_ERROR);
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("null");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.UNSUPPORTED_ERROR.name(), varibales, deleteResourceInstanceResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteServiceInstanceComponentTypeIsNotProduct() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(sdncPmDetails1, productDetails_01.getUniqueId(), serviceInstanceUniqueId, ComponentTypeEnum.SERVICE);
		assertTrue(deleteResourceInstanceResponse.getErrorCode() == STATUS_CODE_NOT_FOUND);
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), varibales, deleteResourceInstanceResponse.getResponse());
		getComponentAndValidateRIs(productDetails_01, 2, 0);
	}

	@Test
	public void deleteServiceInstanceUserIdIsEmpty() throws Exception {
		User nonSdncDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		nonSdncDetails.setUserId("");
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, nonSdncDetails);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_MISSING_INFORMATION);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), new ArrayList<String>(), deleteServiceInstanceResp.getResponse());
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	@Test
	public void deleteCertifiedServiceInstance() throws Exception {
		changeServiceStateToCertified(serviceDetails_01);
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		RestResponse deleteServiceInstanceResp = deleteServiceInstance(serviceInstanceUniqueId, productDetails_01, sdncPmDetails1);
		assertTrue(deleteServiceInstanceResp.getErrorCode() == STATUS_CODE_SUCCESS_DELETE);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
	}

	////////////////////////////////////

	private void certifyVf(ResourceReqDetails resource) throws Exception {
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String cpCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, resourceDetailsVFC_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String computeCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resource, resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String vlCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		// Fixing Vl/Cp req/cap
		ComponentTypeEnum containerCompType = ComponentTypeEnum.RESOURCE;
		User user = sdncDesignerDetails;
		fulfillCpRequirement(resource, cpCompInstId, computeCompInstId, computeCompInstId, user, containerCompType);
		consumeVlCapability(resource, cpCompInstId, vlCompInstId, cpCompInstId, user, containerCompType);

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails, resource.getUniqueId());
		ResourceRestUtils.checkSuccess(response);
		RestResponse changeResourceStateToCertified = changeResourceStateToCertified(resource);
		ResourceRestUtils.checkSuccess(changeResourceStateToCertified);
	}

	private RestResponse changeResourceStateToCertified(ResourceReqDetails resourceDetails) throws Exception {
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		if (restResponse.getErrorCode() == 200) {
			restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncTesterDetails, LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponse;
		if (restResponse.getErrorCode() == 200) {
			restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncTesterDetails, LifeCycleStatesEnum.CERTIFY);
			if (restResponse.getErrorCode() == 200) {
				String newVersion = ResponseParser.getVersionFromResponse(restResponse);
				resourceDetails.setVersion(newVersion);
				resourceDetails.setLifecycleState(LifecycleStateEnum.CERTIFIED);
				resourceDetails.setLastUpdaterUserId(sdncTesterDetails.getUserId());
				resourceDetails.setLastUpdaterFullName(sdncTesterDetails.getFullName());
				String uniqueIdFromRresponse = ResponseParser.getValueFromJsonResponse(restResponse.getResponse(), "uniqueId");
				resourceDetails.setUniqueId(uniqueIdFromRresponse);
			}
		}
		return restResponse;
	}

	private RestResponse changeServiceStateToCertified(ServiceReqDetails serviceDetails) throws Exception {
		/*
		 * RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN); ResourceRestUtils.checkSuccess(restResponse);
		 */
		RestResponse restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		if (restResponse.getErrorCode() == 200) {
			restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncTesterDetails, LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponse;
		if (restResponse.getErrorCode() == 200) {
			restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, sdncTesterDetails, LifeCycleStatesEnum.CERTIFY);
			if (restResponse.getErrorCode() == 200) {
				serviceDetails.setVersion("1.0");
				serviceDetails.setLifecycleState(LifecycleStateEnum.CERTIFIED);
				serviceDetails.setLastUpdaterUserId(sdncTesterDetails.getUserId());
				serviceDetails.setLastUpdaterFullName(sdncTesterDetails.getFullName());
				String uniqueIdFromRresponse = ResponseParser.getValueFromJsonResponse(restResponse.getResponse(), "uniqueId");
				serviceDetails.setUniqueId(uniqueIdFromRresponse);
			}
		}
		return restResponse;
	}

	@Test
	public void deleteServiceInstanceTest() throws Exception {
		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);

		createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_02, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 2, 0);
		String compInstId2 = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);

		RestResponse deleteServiceInstanceResp = deleteServiceInstance(compInstId, productDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkDeleteResponse(deleteServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 1, 0);

		deleteServiceInstanceResp = deleteServiceInstance(compInstId2, productDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkDeleteResponse(deleteServiceInstanceResp);
		getComponentAndValidateRIs(productDetails_01, 0, 0);
	}

	@Test
	public void returnedServiceInstanceTypeAttributeTest() throws Exception {
		String expectedServiceType = ComponentTypeEnum.SERVICE.getValue().toUpperCase();

		RestResponse createServiceInstanceResp = createServiceInstance(productDetails_01, serviceDetails_01, sdncPmDetails1);
		ResourceRestUtils.checkCreateResponse(createServiceInstanceResp);
		String serviceUniqueIdFromResponse = ResponseParser.getUniqueIdFromResponse(createServiceInstanceResp);
		ComponentInstanceRestUtils.checkComponentInstanceType(createServiceInstanceResp, expectedServiceType);

		RestResponse getProductResp = ProductRestUtils.getProduct(productDetails_01.getUniqueId(), sdncPsDetails1.getUserId());
		ProductRestUtils.checkSuccess(getProductResp);
		Product productObject = ResponseParser.parseToObjectUsingMapper(getProductResp.getResponse(), Product.class);
		List<ComponentInstance> productComponentInstances = productObject.getComponentInstances();
		for (ComponentInstance comp : productComponentInstances) {
			String actualOriginType = comp.getOriginType().getValue().toUpperCase();
			assertTrue(expectedServiceType.equals(actualOriginType));
		}

		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory.getComponentResourceInstance(serviceDetails_02);
		serviceInstanceReqDetails.setUniqueId(serviceUniqueIdFromResponse);
		serviceInstanceReqDetails.setComponentUid(serviceDetails_01.getUniqueId());
		RestResponse updateResourceInstance = ComponentInstanceRestUtils.updateComponentInstance(serviceInstanceReqDetails, sdncPmDetails1, productDetails_01.getUniqueId(), ComponentTypeEnum.PRODUCT);
		ComponentInstanceRestUtils.checkSuccess(updateResourceInstance);
		ComponentInstanceRestUtils.checkComponentInstanceType(updateResourceInstance, expectedServiceType);

	}
}
