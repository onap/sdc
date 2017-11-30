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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ServiceValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UpdateServiceMetadataTest extends ComponentBaseTest {

	protected ArrayList<String> listForMessage = new ArrayList<String>();

	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected User sdncDesignerDetails;
	protected User sdncDesignerDetails2;
	protected User sdncAdminDetails;
	protected User sdncGovernorDeatails;
	protected User sdncTesterDetails;
	protected User sdncOpsDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
	protected Component resourceDetailsVFCcomp;
	protected Component serviceDetailsCompp;

	@Rule
	public static TestName name = new TestName();
	protected ServiceReqDetails updatedServiceDetails;

	public UpdateServiceMetadataTest() {
		super(name, UpdateServiceMetadataTest.class.getName());
	}

	@BeforeMethod
	public void setUp() throws Exception {

		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncDesignerDetails2 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN4);
		sdncGovernorDeatails = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
		sdncTesterDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncOpsDetails = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
		resourceDetailsVFCcomp = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				true, true);

		AtomicOperationUtils.changeComponentState(resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFY, true);
		Service serviceServ = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp, serviceServ,
				UserRoleEnum.DESIGNER, true);

		serviceDetails = new ServiceReqDetails(serviceServ);
		updatedServiceDetails = updatedServiceDetails(serviceDetails);

	}

	protected void certifyService(ServiceReqDetails serviceDetails, String version) throws Exception {
		LifecycleRestUtils.certifyService(serviceDetails);
	}

	protected ServiceReqDetails updatedServiceDetails(ServiceReqDetails service) {
		ServiceReqDetails updatedServiceDetails = new ServiceReqDetails(service);

		updatedServiceDetails.setDescription("updatedDescription");
		updatedServiceDetails.setName(service.getName());
		updatedServiceDetails.setProjectCode("987654654");
		updatedServiceDetails.setIcon("icon-service-red3");
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("updateTag", updatedServiceDetails.getName())));
		updatedServiceDetails.removeAllCategories();
		updatedServiceDetails.setCategories(null);
		updatedServiceDetails.addCategory(ServiceCategoriesEnum.VOIP.getValue());
		updatedServiceDetails.setContactId("xy0123");

		return updatedServiceDetails;
	}

	protected void addMandatoryArtifactsToService() throws Exception {
		// TODO Andrey US575052
		// ServiceRestUtils.addServiceMandatoryArtifacts(sdncDesignerDetails,
		// createServiceResponse);
	}

	protected void getServiceAndValidate(ServiceReqDetails excpectedService, User creator, User updater,
			LifecycleStateEnum lifeCycleState) throws Exception {
		RestResponse getServiceResponse = ServiceRestUtils.getService(excpectedService.getUniqueId(),
				sdncDesignerDetails);
		AssertJUnit.assertNotNull("check response object is not null after updating service", getServiceResponse);
		AssertJUnit.assertNotNull("check if error code exists in response after updating service",
				getServiceResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after updating service", 200,
				getServiceResponse.getErrorCode().intValue());
		Service actualService = ResponseParser.convertServiceResponseToJavaObject(getServiceResponse.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(excpectedService, actualService, creator, updater,
				lifeCycleState);
	}

	public void getServiceAndValidate(ServiceReqDetails excpectedService, LifecycleStateEnum lifecycleState)
			throws Exception {
		getServiceAndValidate(excpectedService, sdncDesignerDetails, sdncDesignerDetails, lifecycleState);
	}

	protected void validateResponse(RestResponse response, int errorCode, ActionStatus actionResponse,
			List<String> listOfVariables) throws Exception {
		AssertJUnit.assertNotNull("check response object is not null after updating service", response);
		AssertJUnit.assertNotNull("check if error code exists in response after updating service",
				response.getErrorCode());
		AssertJUnit.assertEquals("Check response code after updating service", errorCode,
				response.getErrorCode().intValue());

		if (actionResponse != null) {
			ErrorValidationUtils.checkBodyResponseOnError(actionResponse.name(), listOfVariables,
					response.getResponse());
			return;
		}

		Service actualService = ResponseParser.convertServiceResponseToJavaObject(response.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(updatedServiceDetails, actualService,
				sdncDesignerDetails, sdncDesignerDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	protected void validateActualVsExpected(ServiceReqDetails expectedService, RestResponse actualServiceFromResponse) {
		Service actualService = ResponseParser
				.convertServiceResponseToJavaObject(actualServiceFromResponse.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(updatedServiceDetails, actualService,
				sdncDesignerDetails, sdncDesignerDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	protected String multipleString(String ch, int repeat) {
		return StringUtils.repeat(ch, repeat);
	}

	protected void correctUpdate() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 200, null, listForMessage);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	protected void updateWithInvalidValue(ActionStatus invalidValue, List<String> arr) throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, invalidValue, arr);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	protected void charactersInRangeChecking(int min, int max, String field) throws Exception {
		if (field != null) {
			if (field == "name") {
				for (char ch = (char) min; ch <= (char) max; ch++) {
					updatedServiceDetails.setName("testname" + String.valueOf(ch));
					updatedServiceDetails.setTags(
							addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
					updateWithInvalidValue(ActionStatus.INVALID_COMPONENT_NAME,
							new ArrayList<>(Arrays.asList("Service")));
				}
			} else if (field == "icon") {
				for (char ch = (char) min; ch <= (char) max; ch++) {
					updatedServiceDetails.setIcon("testname" + String.valueOf(ch));
					updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_ICON,
							new ArrayList<>(Arrays.asList("Service")));
				}
			} else if (field == "tags") {
				List<String> variables = Arrays.asList("Service", "tag");
				for (char ch = (char) min; ch <= (char) max; ch++) {
					updatedServiceDetails.setTags(
							new ArrayList<>(Arrays.asList(String.valueOf(ch), updatedServiceDetails.getName())));
					updateWithInvalidValue(ActionStatus.INVALID_FIELD_FORMAT, variables);
				}
			} else if (field == "category") {
				for (char ch = (char) min; ch <= (char) max; ch++) {
					updatedServiceDetails.addCategoryChain(multipleString("1", 5) + String.valueOf(ch),
							multipleString("1", 5) + String.valueOf(ch));
					updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CATEGORY,
							new ArrayList<>(Arrays.asList("Service")));
				}
			}

			else if (field == "projectCode") {
				for (char ch = (char) min; ch <= (char) max; ch++) {
					updatedServiceDetails.setProjectCode(multipleString("1", 5) + String.valueOf(ch));
					updateWithInvalidValue(ActionStatus.INVALID_PROJECT_CODE, listForMessage);
				}
			}

			else
				return;
		}

	}

	protected void specialCharsChecking(String field) throws Exception {
		charactersInRangeChecking(33, 44, field);
		charactersInRangeChecking(47, 47, field);
		charactersInRangeChecking(58, 64, field);
		charactersInRangeChecking(91, 94, field);
		charactersInRangeChecking(96, 96, field);
		charactersInRangeChecking(123, 126, field);
	}

	@Test
	public void updateServiceSuccessfully() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 200, null, listForMessage);

		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

	}

	protected void checkErrorResponse(ActionStatus actionStatus, ArrayList<String> arrList, RestResponse response)
			throws Exception, JSONException {
		ErrorValidationUtils.checkBodyResponseOnError(actionStatus.name(), arrList, response.getResponse());
	}

	protected List<String> addServiceNameToTagsList(String serviceName, List<String> tagsList) {
		tagsList.add(serviceName);
		return tagsList;

	}

	// @Test
	// public void updateMetadateSuccessTest() throws Exception {
	// CloseableHttpClient httpClient = HttpClients.createDefault();
	// HttpGet httpGet =
	// ServiceRestUtils.createGetServiceGetRquest(serviceDetails,
	// sdncDesignerDetails);
	// CloseableHttpResponse response = httpClient.execute(httpGet);
	// assertTrue(response.getStatusLine().getStatusCode() == 200);
	// String responseString = new
	// BasicResponseHandler().handleResponse(response);
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(responseString);
	// assertTrue("service object creation failed the returned object is null",
	// serviceObject != null);
	// String currentCategory = serviceObject.getCategories().get(0).getName();
	// String currentServiceName = serviceObject.getName();
	// String currentProjectCode = serviceObject.getProjectCode();
	// String currentIcon = serviceObject.getIcon();
	// String currentDescription = serviceObject.getDescription();
	// List<String> currentTags = serviceObject.getTags();
	//
	// String newCategory = ServiceCategoriesEnum.VOIP.getValue();
	// serviceDetails.addCategory(newCategory);
	// // String newServiceName = "updated name";
	// // serviceDetails.setServiceName(newServiceName);
	// String newProjectCode = "68686868";
	// serviceDetails.setProjectCode(newProjectCode);
	// String newIcon = "updated-icon";
	// serviceDetails.setIcon(newIcon);
	// String newDescription = "updated description <html></html>";
	// serviceDetails.setDescription(newDescription);
	// List<String> newTags = new ArrayList<>();
	// newTags.add("update1");
	// newTags.add("update2");
	// newTags.add(currentServiceName);
	// serviceDetails.setTags(newTags);
	// HttpPut httpPut =
	// ServiceRestUtils.createUpdateServiceMetaDataPutRequest(serviceDetails,
	// sdncDesignerDetails);
	// response = httpClient.execute(httpPut);
	// assertTrue(response.getStatusLine().getStatusCode() == 200);
	// responseString = new BasicResponseHandler().handleResponse(response);
	// String serviceUid =
	// ServiceRestUtils.getServiceUniqueIdFromString(responseString);
	//
	// ServiceReqDetails details = new ServiceReqDetails();
	// details.setUniqueId(serviceUid);
	//
	// httpGet = ServiceRestUtils.createGetServiceGetRquest(details,
	// sdncDesignerDetails);
	// response = httpClient.execute(httpGet);
	// assertTrue(response.getStatusLine().getStatusCode() == 200);
	// responseString = new BasicResponseHandler().handleResponse(response);
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(responseString);
	// assertTrue("service object creation failed the returned object is null",
	// serviceObject != null);
	// String updatedCategory = serviceObject.getCategories().get(0).getName();
	// String updatedServiceName = serviceObject.getName();
	// String updatedProjectCode = serviceObject.getProjectCode();
	// String updatedIcon = serviceObject.getIcon();
	// String updatedDescription = serviceObject.getDescription();
	// List<String> updatedTags = serviceObject.getTags();
	// assertFalse("category did not cahnge",
	// currentCategory.equals(updatedCategory));
	// assertEquals("categoruy did not match expacted value", updatedCategory,
	// newCategory);
	// // assertFalse("service name did not change",
	// currentServiceName.equals(updatedServiceName) );
	// // assertEquals("service name did not match expacted
	// value",updatedServiceName,newServiceName);
	// assertFalse("projectCode did not change", currentProjectCode.equals(updatedProjectCode));
	// assertEquals("projectCode not match expacted value", updatedProjectCode, newProjectCode);
	// assertFalse("icon did not change", currentIcon.equals(updatedIcon));
	// assertEquals("icon did not match expacted value", updatedIcon, newIcon);
	// assertFalse("description did not change",
	// currentDescription.equals(updatedDescription));
	// assertEquals("description did not match expacted value", "updated
	// description", updatedDescription);
	// assertFalse("tags did not change", currentTags.containsAll(updatedTags));
	// assertTrue("tags did not match expacted value",
	// updatedTags.containsAll(newTags));
	// }

	@Test
	public void updateService_ByOtherDesigner() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails2);
		validateResponse(updateServiceResponse, 409, ActionStatus.RESTRICTED_OPERATION, listForMessage);

		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void updateService_ByAdmin() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncAdminDetails);
		validateResponse(updateServiceResponse, 409, ActionStatus.RESTRICTED_OPERATION, listForMessage);

		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void updateServiceNotExist() throws Exception {
		updatedServiceDetails.setUniqueId("nnnnn");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 404, ActionStatus.SERVICE_NOT_FOUND,
				new ArrayList<String>(Arrays.asList("")));
	}

	@Test
	public void updateCheckedinService() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 409, ActionStatus.RESTRICTED_OPERATION, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
	}

	@Test
	public void updateCertifiedService() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());

		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 409, ActionStatus.RESTRICTED_OPERATION, listForMessage);
		getServiceAndValidate(serviceDetails, sdncDesignerDetails, sdncAdminDetails, LifecycleStateEnum.CERTIFIED);
	}

	// TODO Irrelevant
	// @Test(enabled = false)
	// public void updateService_NameCaseSensitiveTest() throws Exception {
	// ServiceRestUtils.setServiceUniqueId(serviceDetails.getName().toUpperCase());
	//
	// RestResponse updateServiceResponse =
	// ServiceRestUtils.updateService(updatedServiceDetails,
	// sdncDesignerDetails);
	// validateResponse(updateServiceResponse, 200, null, listForMessage);
	//
	// Service serviceFromJsonResponse =
	// ResponseParser.convertServiceResponseToJavaObject(updateServiceResponse.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(updatedServiceDetails,
	// serviceFromJsonResponse, sdncDesignerDetails, (LifecycleStateEnum)null);
	//
	// getServiceAndValidate(updatedServiceDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// }

	// @Test
	// public void updateApprovedDistributionServiceTest() throws Exception {
	// // addMandatoryArtifactsToService();
	// certifyService(serviceDetails, serviceDetails.getVersion());
	//
	// RestResponse approveResponse =
	// ServiceRestUtils.sendApproveDistribution(sdncAdminDetails,
	// serviceDetails.getUniqueId(), userRemarks);
	// // validateResponse(approveResponse, 200, null, listForMessage);
	//
	// RestResponse updateServiceResponse =
	// ServiceRestUtils.updateService(updatedServiceDetails,
	// sdncDesignerDetails);
	// validateResponse(updateServiceResponse, 409,
	// ActionStatus.RESTRICTED_OPERATION, listForMessage);
	//
	// getServiceAndValidate(serviceDetails, sdncDesignerDetails,
	// sdncAdminDetails,LifecycleStateEnum.CERTIFIED);
	// }

	@Test
	public void updateServiceByMethod_delete() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.createServiceByHttpMethod(updatedServiceDetails,
				sdncDesignerDetails, "DELETE", Urls.UPDATE_SERVICE_METADATA);
		validateResponse(updateServiceResponse, 405, ActionStatus.NOT_ALLOWED, listForMessage);

		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void updateServiceByMethod_get() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.createServiceByHttpMethod(updatedServiceDetails,
				sdncDesignerDetails, "GET", Urls.UPDATE_SERVICE_METADATA);
		validateResponse(updateServiceResponse, 405, ActionStatus.NOT_ALLOWED, listForMessage);

		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void updateServiceByMethod_post() throws Exception {
		RestResponse updateServiceResponse = ServiceRestUtils.createServiceByHttpMethod(updatedServiceDetails,
				sdncDesignerDetails, "POST", Urls.UPDATE_SERVICE_METADATA);
		validateResponse(updateServiceResponse, 405, ActionStatus.NOT_ALLOWED, listForMessage);

		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void updateCheckoutCertifiedService() throws Exception // certify a
																	// service
																	// and
																	// checkout
																	// it
	{
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKOUT);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.SERVICE_CATEGORY_CANNOT_BE_CHANGED, listForMessage);

		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	// ---------------------------------------------------------Validation
	// Tests---------------------------------------------------------

	@Test
	public void missingCategoryTest1() throws Exception {
		List<CategoryDefinition> categories = updatedServiceDetails.getCategories();
		CategoryDefinition categoryDefinition = categories.get(0);
		CategoryDefinition categoryDefinition2 = categoryDefinition;
		categoryDefinition2.setName("");
		categories.set(0, categoryDefinition2);
		updatedServiceDetails.setCategories(categories);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_CATEGORY, Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingCategoryTest2() throws Exception {
		updatedServiceDetails.setCategories(null);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_CATEGORY, Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingServiceNameTest1() throws Exception {
		updatedServiceDetails.setName(StringUtils.EMPTY);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.MISSING_COMPONENT_NAME, Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingServiceNameTest2() throws Exception {

		updatedServiceDetails.setName(null);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.MISSING_COMPONENT_NAME, Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void environmentContextService() throws Exception {
		updatedServiceDetails.setEnvironmentContext("General_Revenue-Bearing");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 200, null, listForMessage);
		Service actualService = ResponseParser.convertServiceResponseToJavaObject(updateServiceResponse.getResponse());
		assertEquals(updatedServiceDetails.getEnvironmentContext(), actualService.getEnvironmentContext());
	}

	// TODO Irrelevant
	@Test(enabled = false)
	public void missingProjectCodeTest1() throws Exception {
		updatedServiceDetails.setProjectCode(StringUtils.EMPTY);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.MISSING_PROJECT_CODE, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	// TODO Irrelevant
	@Test(enabled = false)
	public void missingProjectCodeTest2() throws Exception {

		updatedServiceDetails.setProjectCode(null);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.MISSING_PROJECT_CODE, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingIconTest1() throws Exception {
		updatedServiceDetails.setIcon(StringUtils.EMPTY);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_ICON, Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingIconTest2() throws Exception {
		updatedServiceDetails.setIcon(null);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_ICON, Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingDescriptionTest1() throws Exception {
		updatedServiceDetails.setDescription(StringUtils.EMPTY);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
				Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingDescriptionTest2() throws Exception {
		updatedServiceDetails.setDescription(null);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
				Arrays.asList("Service"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingTagsTest1() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<String>());
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_TAGS, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingTagsTest2() throws Exception {
		updatedServiceDetails.setTags(null);
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.COMPONENT_MISSING_TAGS, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingTagsTest3() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList(StringUtils.EMPTY)));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.INVALID_FIELD_FORMAT,
				Arrays.asList("Service", "tag"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void missingTagsTest4() throws Exception {
		updatedServiceDetails
				.setTags(new ArrayList<>(Arrays.asList(StringUtils.EMPTY, updatedServiceDetails.getName())));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		validateResponse(updateServiceResponse, 400, ActionStatus.INVALID_FIELD_FORMAT,
				Arrays.asList("Service", "tag"));
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	// update non-settable/"updatable" parameters tests

	// ------------------------------------------correct
	// values------------------------------------------
	@Test
	public void contactIdValidationTest1() throws Exception {
		updatedServiceDetails.setContactId("ab3456");
		correctUpdate();
	}

	@Test
	public void contactIdValidationTest2() throws Exception {

		updatedServiceDetails.setContactId("cd789E");
		correctUpdate();
	}

	@Test
	public void contactIdValidationTest3() throws Exception {

		updatedServiceDetails.setContactId("ef4567");
		correctUpdate();
	}

	@Test
	public void contactIdValidationTest4() throws Exception {
		updatedServiceDetails.setContactId("AA012A");
		correctUpdate();
	}

	@Test
	public void contactIdValidationTest5() throws Exception {
		updatedServiceDetails.setContactId("CD012c");
		correctUpdate();
	}

	@Test
	public void contactIdValidationTest6() throws Exception {
		updatedServiceDetails.setContactId("EF0123");
		correctUpdate();
	}

	// ------------------------------------------invalid
	// values------------------------------------------
	@Test
	public void contactIdValidationTest7() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		updatedServiceDetails.setContactId("ab0001");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void contactIdValidationTest8() throws Exception {
		// addMandatoryArtifactsToService();

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		updatedServiceDetails = new ServiceReqDetails(certifyService);
		updatedServiceDetails.setContactId("ab0001");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void contactIdValidationTest9() throws Exception {
		updatedServiceDetails.setContactId("01345a");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, Arrays.asList("Service"));
	}

	@Test
	public void contactIdValidationTest10() throws Exception {
		updatedServiceDetails.setContactId("0y000B");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, Arrays.asList("Service"));
	}

	@Test
	public void contactIdValidationTest11() throws Exception {
		updatedServiceDetails.setContactId("Y1000b");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, Arrays.asList("Service"));
	}

	@Test
	public void contactIdValidationTest12() throws Exception {
		updatedServiceDetails.setContactId("abxyzC");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, Arrays.asList("Service"));
	}

	@Test
	public void contactIdValidationTest13() throws Exception {
		updatedServiceDetails.setContactId("cdXYZc");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest14() throws Exception {
		updatedServiceDetails.setContactId("efXY1D");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest15() throws Exception {
		updatedServiceDetails.setContactId("EFabcD");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest16() throws Exception {
		updatedServiceDetails.setContactId("EFABCD");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest17() throws Exception {
		updatedServiceDetails.setContactId("EFABC1");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest18() throws Exception {
		updatedServiceDetails.setContactId("efui1D");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest19() throws Exception {
		updatedServiceDetails.setContactId("efui1!");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest20() throws Exception {
		updatedServiceDetails.setContactId("ef555!");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest21() throws Exception {
		updatedServiceDetails.setContactId(",f555");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest22() throws Exception {
		updatedServiceDetails.setContactId("EF55.5");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest23() throws Exception {
		updatedServiceDetails.setContactId("ab000");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest24() throws Exception {
		updatedServiceDetails.setContactId("ab000c0");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest25() throws Exception {
		updatedServiceDetails.setContactId("  ab0001");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CONTACT, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void contactIdValidationTest26() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKOUT);
		updatedServiceDetails = new ServiceReqDetails(serviceDetails);
		updatedServiceDetails.setContactId("xy0002");
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest1() throws Exception {
		updatedServiceDetails.setName(multipleString("a", 49));
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest2() throws Exception {
		updatedServiceDetails.setName(multipleString("b", 50));
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest3() throws Exception {
		updatedServiceDetails.setName("testNamE");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest4() throws Exception {
		updatedServiceDetails.setName("Testname");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest5() throws Exception {
		updatedServiceDetails.setName("Test_name");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest6() throws Exception {
		updatedServiceDetails.setName("Test name");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest7() throws Exception {
		updatedServiceDetails.setName("Test-name");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest8() throws Exception {
		updatedServiceDetails.setName("Test.name");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest9() throws Exception {
		updatedServiceDetails.setName("...1...");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest10() throws Exception {
		updatedServiceDetails.setName("-a_1. Arrrrrr");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest11() throws Exception {
		updatedServiceDetails.setName("Testname1234567890");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		correctUpdate();
	}

	@Test
	public void serviceNameValidationTest14() throws Exception {
		updatedServiceDetails.setName(StringUtils.SPACE); // one space with
															// nothing
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		// updateWithInvalidValue(ActionStatus.INVALID_COMPONENT_NAME, new
		// ArrayList<>(Arrays.asList("Service")));
		validateResponse(updateServiceResponse, 400, ActionStatus.MISSING_COMPONENT_NAME,
				new ArrayList<>(Arrays.asList("Service")));
	}

	// ------------------------------------------invalid
	// values------------------------------------------
	@Test
	public void serviceNameValidationTest12() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		updatedServiceDetails.setName("TestNamE");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
	}

	@Test
	public void serviceNameValidationTest13() throws Exception {
		updatedServiceDetails.setName(multipleString("c", 51));
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		updateWithInvalidValue(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT,
				new ArrayList<>(Arrays.asList("Service", "50")));
	}

	@Test
	public void serviceNameValidationTest15() throws Exception {
		specialCharsChecking("name");
	}

	@Test
	public void serviceNameValidationTest16() throws Exception {
		// addMandatoryArtifactsToService();
		LifecycleRestUtils.certifyService(serviceDetails);
		updatedServiceDetails.setName("testnamename");
		updatedServiceDetails.setCategories(serviceDetails.getCategories());
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
		getServiceAndValidate(serviceDetails, sdncDesignerDetails, sdncTesterDetails, LifecycleStateEnum.CERTIFIED);
	}

	@Test
	public void serviceNameValidationTest17() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKOUT);
		updatedServiceDetails.setName("TestNamE");
		updatedServiceDetails.setCategories(serviceDetails.getCategories());
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		RestResponse updateServiceResponse2 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		validateResponse(updateServiceResponse2, 400, ActionStatus.SERVICE_NAME_CANNOT_BE_CHANGED, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void serviceNameValidationTest18() throws Exception {
		updatedServiceDetails.setName("  testname  ");
		updatedServiceDetails
				.setTags(addServiceNameToTagsList(updatedServiceDetails.getName(), updatedServiceDetails.getTags()));
		RestResponse updateServiceResponse1 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse1);
		assertNotNull(updateServiceResponse1.getErrorCode());
		assertEquals(200, updateServiceResponse1.getErrorCode().intValue());
		updatedServiceDetails.setName(updatedServiceDetails.getName());
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse1);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void iconValidationTest1() throws Exception {
		updatedServiceDetails.setIcon(multipleString("a", 24));
		correctUpdate();
	}

	@Test
	public void iconValidationTest2() throws Exception {
		updatedServiceDetails.setIcon(multipleString("b", 25));
		correctUpdate();
	}

	@Test
	public void iconValidationTest3() throws Exception {
		updatedServiceDetails.setIcon("testNamE");
		correctUpdate();
	}

	@Test
	public void iconValidationTest4() throws Exception {
		updatedServiceDetails.setIcon("Testname");
		correctUpdate();
	}

	@Test
	public void iconValidationTest5() throws Exception {
		updatedServiceDetails.setIcon("Test_name");
		correctUpdate();
	}

	@Test
	public void iconValidationTest6() throws Exception {
		updatedServiceDetails.setIcon("Test-name");
		correctUpdate();
	}

	@Test
	public void iconValidationTest7() throws Exception {
		updatedServiceDetails.setIcon("Testname1234567890");
		correctUpdate();
	}

	@Test
	public void iconValidationTest8() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		updatedServiceDetails.setIcon("TestNamE");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void iconValidationTest9() throws Exception {
		// addMandatoryArtifactsToService();
		LifecycleRestUtils.certifyService(serviceDetails);
		updatedServiceDetails.setIcon("testnamename");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	// ------------------------------------------invalid
	// values------------------------------------------
	@Test
	public void iconValidationTest10() throws Exception {
		updatedServiceDetails.setIcon("Test name");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_ICON, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void iconValidationTest11() throws Exception {
		updatedServiceDetails.setIcon(StringUtils.SPACE); // one space with
															// nothing
		updateWithInvalidValue(ActionStatus.COMPONENT_MISSING_ICON, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void iconValidationTest12() throws Exception {
		updatedServiceDetails.setIcon("Test.name");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_ICON, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void iconValidationTest13() throws Exception {
		specialCharsChecking("icon");
		charactersInRangeChecking(46, 46, "icon");
	}

	@Test
	public void iconValidationTest14() throws Exception {
		updatedServiceDetails.setIcon(multipleString("c", 26));
		updateWithInvalidValue(ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT,
				new ArrayList<>(Arrays.asList("Service", "25")));
	}

	@Test
	public void iconValidationTest15() throws Exception {
		// addMandatoryArtifactsToService();
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		updatedServiceDetails = new ServiceReqDetails(certifyService);
		updatedServiceDetails.setIcon("testnamename");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void iconValidationTest16() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKOUT);
		updatedServiceDetails = new ServiceReqDetails(serviceDetails);
		updatedServiceDetails.setIcon("TestNamE");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.SERVICE_ICON_CANNOT_BE_CHANGED, listForMessage, updateServiceResponse);
	}

	@Test
	public void iconValidationTest17() throws Exception {
		updatedServiceDetails.setIcon("  Icon  ");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_ICON, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void categoryValidationTest1() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		updatedServiceDetails.addCategory(ServiceCategoriesEnum.VOIP.getValue());
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void categoryValidationTest2() throws Exception {
		// updatedServiceDetails.addCategory("someCategory");
		updatedServiceDetails.setCategories(null);
		updatedServiceDetails.addCategoryChain("someCategory", null);
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CATEGORY, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void categoryValidationTest3() throws Exception {
		updatedServiceDetails.setCategories(null);
		updatedServiceDetails.addCategoryChain("SomeCategory10", null);
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CATEGORY, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void categoryValidationTest4() throws Exception {
		updatedServiceDetails.setCategories(null);
		updatedServiceDetails.addCategoryChain("some Category", null);
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_CATEGORY, new ArrayList<>(Arrays.asList("Service")));
	}

	@Test
	public void categoryValidationTest5() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		updatedServiceDetails = new ServiceReqDetails(serviceDetails);
		updatedServiceDetails.addCategory("Network L1-3");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void categoryValidationTest6() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKOUT);
		updatedServiceDetails = new ServiceReqDetails(serviceDetails);
		updatedServiceDetails = serviceDetails;
		List<CategoryDefinition> categories = updatedServiceDetails.getCategories();
		CategoryDefinition categoryDefinition = categories.get(0);
		CategoryDefinition categoryDefinition2 = categoryDefinition;
		categoryDefinition2.setName("ccc");
		categories.set(0, categoryDefinition2);
		updatedServiceDetails.setCategories(categories);
		RestResponse updateServiceResponse2 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		validateResponse(updateServiceResponse2, 400, ActionStatus.SERVICE_CATEGORY_CANNOT_BE_CHANGED, listForMessage);
		getServiceAndValidate(serviceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void categoryValidationTest7() throws Exception {
		updatedServiceDetails.removeAllCategories();
		updatedServiceDetails.addCategory(ServiceCategoriesEnum.NETWORK_L3.getValue());
		correctUpdate();
	}

	@Test
	public void categoryValidationTest8() throws Exception {
		updatedServiceDetails.setCategories(null);
		updatedServiceDetails.addCategoryChain("Network L1-3", null);
		correctUpdate();
	}

	@Test
	public void tagsValidationTest1() throws Exception {
		updatedServiceDetails
				.setTags(new ArrayList<>(Arrays.asList(multipleString("a", 49), updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest2() throws Exception {
		updatedServiceDetails
				.setTags(new ArrayList<>(Arrays.asList(multipleString("B", 50), updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest3() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(
				Arrays.asList(multipleString("A", 50), multipleString("B", 50), updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest5() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("testTaG", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest6() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("Testtag", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest7() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("Test_tag", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest8() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("Test tag", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest9() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("Test-tag", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest10() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("Test.tag", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest11() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("...1...", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest12() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("-a_1. Arrrrrr", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest13() throws Exception {
		updatedServiceDetails
				.setTags(new ArrayList<>(Arrays.asList("Testtag1234567890", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest14() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("1", "2", "2", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest15() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("TestTaG", updatedServiceDetails.getName())));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void tagsValidationTest16() throws Exception {
		// addMandatoryArtifactsToService();
		LifecycleRestUtils.certifyService(serviceDetails);
		updatedServiceDetails = new ServiceReqDetails(serviceDetails);
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("testtagtag", updatedServiceDetails.getName())));
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		checkErrorResponse(ActionStatus.RESTRICTED_OPERATION, listForMessage, updateServiceResponse);
	}

	@Test
	public void tagsValidationTest17() throws Exception {
		// addMandatoryArtifactsToService();
		certifyService(serviceDetails, serviceDetails.getVersion());
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKOUT);
		updatedServiceDetails = new ServiceReqDetails(serviceDetails);
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("TestTaG", updatedServiceDetails.getName())));
		correctUpdate();
	}

	@Test
	public void tagsValidationTest18() throws Exception {
		int lengthOfServiceName = updatedServiceDetails.getName().length();
		int maxLengthTag = 50;
		int tagsCount = 1024 - lengthOfServiceName;
		ArrayList<String> tagsList = new ArrayList<>();
		tagsList.add(updatedServiceDetails.getName());
		while (tagsCount > maxLengthTag) {
			tagsList.add(multipleString("a", maxLengthTag));
			tagsCount -= maxLengthTag + 1
					+ 1/* (50 and comma of each tag + one space, totally 52) */;
		}
		tagsList.add(multipleString("a", tagsCount));
		updatedServiceDetails.setTags(tagsList);
		correctUpdate();
	}

	@Test
	public void tagsValidationTest19() throws Exception {
		updatedServiceDetails.setTags(new ArrayList<>(Arrays.asList("   Tag   ", updatedServiceDetails.getName())));
		RestResponse updateServiceResponse1 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse1);
		assertNotNull(updateServiceResponse1.getErrorCode());
		assertEquals(200, updateServiceResponse1.getErrorCode().intValue());
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse1);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void tagsValidationTest20() throws Exception {
		ArrayList<String> tagsList = new ArrayList<>();
		tagsList.add(updatedServiceDetails.getName());
		tagsList.add("");
		updatedServiceDetails.setTags(tagsList);
		updateWithInvalidValue(ActionStatus.INVALID_FIELD_FORMAT, Arrays.asList("Service", "tag"));
	}

	// ------------------------------------------invalid
	// values------------------------------------------

	@Test
	public void tagsValidationTest21() throws Exception {
		ArrayList<String> tagsList = new ArrayList<>();
		tagsList.add("onetag");
		updatedServiceDetails.setTags(tagsList);
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME, listForMessage);

	}

	@Test
	public void tagsValidationTest22() throws Exception {
		specialCharsChecking("tags");
	}

	@Test
	public void descriptionValidationTest1() throws Exception {
		updatedServiceDetails.setDescription(multipleString("a", 1023));
		correctUpdate();
	}

	@Test
	public void descriptionValidationTest2() throws Exception {
		updatedServiceDetails.setDescription(multipleString("a", 1024));
		correctUpdate();
	}

	@Test
	public void descriptionValidationTest3() throws Exception {
		updatedServiceDetails.setDescription(multipleString("aB", 1024 / 2));
		correctUpdate();
	}

	@Test
	public void descriptionValidationTest4() throws Exception {
		updatedServiceDetails.setDescription("1234567890");
		correctUpdate();
	}

	@Test
	public void descriptionValidationTest5() throws Exception {
		updatedServiceDetails.setDescription("desc ription");
		correctUpdate();
	}

	@Test
	public void descriptionValidationTest6() throws Exception {
		updatedServiceDetails.setDescription("desc\tription");
		RestResponse updateServiceResponse1 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse1);
		assertNotNull(updateServiceResponse1.getErrorCode());
		assertEquals(200, updateServiceResponse1.getErrorCode().intValue());
		updatedServiceDetails.setDescription("desc ription");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse1);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest7() throws Exception {
		updatedServiceDetails.setDescription("desc      ription     ");
		RestResponse updateServiceResponse2 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse2);
		assertNotNull(updateServiceResponse2.getErrorCode());
		assertEquals(200, updateServiceResponse2.getErrorCode().intValue());
		updatedServiceDetails.setDescription("desc ription");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse2);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest8() throws Exception {
		updatedServiceDetails.setDescription("desc" + StringUtils.LF + "ription");
		RestResponse updateServiceResponse3 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse3);
		assertNotNull(updateServiceResponse3.getErrorCode());
		assertEquals(200, updateServiceResponse3.getErrorCode().intValue());
		updatedServiceDetails.setDescription("desc ription");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse3);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest9() throws Exception {
		updatedServiceDetails.setDescription("<html>Hello, <b>world!</b></html>");
		RestResponse updateServiceResponse4 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse4);
		assertNotNull(updateServiceResponse4.getErrorCode());
		assertEquals(200, updateServiceResponse4.getErrorCode().intValue());
		updatedServiceDetails.setDescription("Hello, world!");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse4);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest10() throws Exception {
		updatedServiceDetails.setDescription("\uC2B5");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_DESCRIPTION, new ArrayList<>(Arrays.asList("Service")));

	}

	@Test
	public void descriptionValidationTest10_a() throws Exception {
		updatedServiceDetails.setDescription("文");
		updateWithInvalidValue(ActionStatus.COMPONENT_INVALID_DESCRIPTION, new ArrayList<>(Arrays.asList("Service")));

	}

	@Test
	public void descriptionValidationTest10_b() throws Exception {
		updatedServiceDetails.setDescription("\uC2B5abc");
		RestResponse updateServiceResponse5 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse5);
		assertNotNull(updateServiceResponse5.getErrorCode());
		assertEquals(200, updateServiceResponse5.getErrorCode().intValue());
		updatedServiceDetails.setDescription("abc");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse5);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

	}

	@Test
	public void descriptionValidationTest11() throws Exception {
		updatedServiceDetails.setDescription("&<>");
		RestResponse updateServiceResponse6 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse6);
		assertNotNull(updateServiceResponse6.getErrorCode());
		assertEquals(200, updateServiceResponse6.getErrorCode().intValue());
		updatedServiceDetails.setDescription("&amp;&lt;&gt;");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse6);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest12() throws Exception {
		updatedServiceDetails.setDescription("文 test");
		RestResponse updateServiceResponse7 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse7);
		assertNotNull(updateServiceResponse7.getErrorCode());
		assertEquals(200, updateServiceResponse7.getErrorCode().intValue());
		updatedServiceDetails.setDescription("test");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse7);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest13() throws Exception {
		updatedServiceDetails.setDescription("   description");
		RestResponse updateServiceResponse8 = ServiceRestUtils.updateService(updatedServiceDetails,
				sdncDesignerDetails);
		assertNotNull(updateServiceResponse8);
		assertNotNull(updateServiceResponse8.getErrorCode());
		assertEquals(200, updateServiceResponse8.getErrorCode().intValue());
		updatedServiceDetails.setDescription("description");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse8);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	@Test
	public void descriptionValidationTest14() throws Exception {
		updatedServiceDetails.setDescription(multipleString("a", 1025));
		updateWithInvalidValue(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
				new ArrayList<>(Arrays.asList("Service", "1024")));
	}

	@Test
	public void projectCodeValidationTest1() throws Exception {
		String desc = StringUtils.EMPTY;
		for (int i = 0; i < 10; i++) {
			desc += Integer.toString(i);
			if (i >= 4) {
				updatedServiceDetails.setProjectCode(desc);
				correctUpdate();
			}
		}
	}

	@Test
	public void projectCodeValidationTest2() throws Exception {
		updatedServiceDetails.setProjectCode(multipleString("1", 6));
		correctUpdate();
	}

	@Test
	public void projectCodeValidationTest3() throws Exception {
		this.specialCharsChecking("projectCode");
	}

	// TODO Irrelevant
	@Test(enabled = false)
	public void projectCodeValidationTest4() throws Exception {
		updatedServiceDetails.setProjectCode(multipleString(" ", 5) + "99999");
		RestResponse updateServiceResponse = ServiceRestUtils.updateService(updatedServiceDetails, sdncDesignerDetails);
		assertNotNull(updateServiceResponse);
		assertNotNull(updateServiceResponse.getErrorCode());
		assertEquals(200, updateServiceResponse.getErrorCode().intValue());
		updatedServiceDetails.setProjectCode("12345");
		validateActualVsExpected(updatedServiceDetails, updateServiceResponse);
		getServiceAndValidate(updatedServiceDetails, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

	}

	@Test
	public void projectCodeValidationTest5() throws Exception {
		updatedServiceDetails.setProjectCode(multipleString("0", 11));
		updateWithInvalidValue(ActionStatus.INVALID_PROJECT_CODE, listForMessage);
	}

	@Test
	public void projectCodeValidationTest6() throws Exception {
		updatedServiceDetails.setProjectCode(multipleString("1", 4));
		updateWithInvalidValue(ActionStatus.INVALID_PROJECT_CODE, listForMessage);
	}

	@Test
	public void projectCodeValidationTest7() throws Exception {
		updatedServiceDetails.setProjectCode("123456789");
		correctUpdate();
	}

	// ////US553874
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion01_isVNF_toTrue() throws Exception{
	//
	// //choose the user to create service
	// User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//
	// //Update Service IsVNF to True
	// restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 200, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion02_isVNF_toFalse() throws Exception{
	//
	// //choose the user to create service
	// User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//
	// //Update Service IsVNF to True
	// restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 200, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion01_isVNF_TrueToNull() throws Exception{
	//
	// //choose the user to create service
	// User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//
	// //Update Service IsVNF to True
	// restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 400, restResponse.getErrorCode().intValue());
	// List<String> variables = Arrays.asList("VNF Service Indicator");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_DATA.name(),
	// variables, restResponse.getResponse());
	//
	// //get service and verify that service created with isVNF is remained with
	// isVNF = true
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion01_isVNF_FalseToNull() throws Exception{
	//
	// //choose the user to create service
	// User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//
	// //Update Service IsVNF to True
	// restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 400, restResponse.getErrorCode().intValue());
	// List<String> variables = Arrays.asList("VNF Service Indicator");
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_DATA.name(),
	// variables, restResponse.getResponse());
	//
	// //get service and verify that service created with isVNF is remained with
	// isVNF = true
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion02_IsVNF_toTrue() throws Exception{
	//
	// //choose the user to create service
	// User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// LifecycleRestUtils.changeServiceState(serviceDetails,
	// sdncDesignerDetails, serviceDetails.getVersion(),
	// LifeCycleStatesEnum.CHECKIN);
	// LifecycleRestUtils.changeServiceState(serviceDetails,
	// sdncDesignerDetails, serviceDetails.getVersion(),
	// LifeCycleStatesEnum.CHECKOUT);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//
	// //Update Service IsVNF to True
	// restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 200, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion02_IsVNF_toFalse() throws Exception{
	//
	// //choose the user to create service
	// User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// LifecycleRestUtils.changeServiceState(serviceDetails,
	// sdncDesignerDetails, serviceDetails.getVersion(),
	// LifeCycleStatesEnum.CHECKIN);
	// LifecycleRestUtils.changeServiceState(serviceDetails,
	// sdncDesignerDetails, serviceDetails.getVersion(),
	// LifeCycleStatesEnum.CHECKOUT);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//
	// //Update Service IsVNF to false
	// restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// //restResponse =
	// ServiceRestUtils.updateService(serviceDetails.getUniqueId(),
	// serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 200, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion11_IsVNF_toFalse() throws Exception{
	// // Can't update isVNF when service version is 1.X
	// User sdncUserDetails =
	// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// //String serviceUniqueName =
	// ServiceRestUtils.getServiceUniqueId(serviceByNameAndVersion);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails, "0.1",
	// sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//// ServiceRestUtils.addServiceMandatoryArtifacts(sdncUserDetails,
	// restResponse);
	// RestResponse response =
	// ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
	// sdncUserDetails, serviceDetails.getUniqueId(),
	// ComponentTypeEnum.SERVICE);
	// assertTrue("response code is not 201, returned: " +
	// response.getErrorCode(),response.getErrorCode() == 201);
	// RestResponse changeServiceState =
	// LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
	// assertTrue("certify service request returned status:" +
	// changeServiceState.getErrorCode(),changeServiceState.getErrorCode() ==
	// 200);
	// LifecycleRestUtils.changeServiceState(serviceDetails, sdncUserDetails,
	// LifeCycleStatesEnum.CHECKOUT);
	//
	// //Update Service IsVNF to false
	// restResponse = ServiceRestUtils.updateService(serviceDetails,
	// sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating service metadata", 400,
	// restResponse.getErrorCode().intValue());
	// List<String> variables = new ArrayList<String>();
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_IS_VNF_CANNOT_BE_CHANGED.name(),
	// variables, restResponse.getResponse());
	//
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "1.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	//
	// }
	//
	// @JsonIgnore
	// @Test
	// public void UpdateServiceVersion11_IsVNF_toTrue() throws Exception{
	// // Can't update isVNF when service version is 1.X
	// User sdncUserDetails =
	// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	// // new service details
	// // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// // clean audit DB before updating service
	// DbUtils.cleanAllAudits();
	// ServiceRestUtils.deleteServiceById(serviceDetails.getUniqueId(),
	// sdncUserDetails.getUserId());
	// serviceDetails = ElementFactory.getDefaultService();
	//
	// //send create service toward BE
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating Interface Artifact",
	// 201, restResponse.getErrorCode().intValue());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// RestResponse serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "0.1");
	// Service serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// //String serviceUniqueName =
	// ServiceRestUtils.getServiceUniqueId(serviceByNameAndVersion);
	//
	// //validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
	// "0.1", sdncUserDetails);
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//// ServiceRestUtils.addServiceMandatoryArtifacts(sdncUserDetails,
	// restResponse);
	// RestResponse response =
	// ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
	// sdncUserDetails, serviceDetails.getUniqueId(),
	// ComponentTypeEnum.SERVICE);
	// assertTrue("response code is not 201, returned: " +
	// response.getErrorCode(),response.getErrorCode() == 201);
	// RestResponse changeServiceState =
	// LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
	// assertTrue("certify service request returned status:" +
	// changeServiceState.getErrorCode(),changeServiceState.getErrorCode() ==
	// 200);
	// LifecycleRestUtils.changeServiceState(serviceDetails, sdncUserDetails,
	// LifeCycleStatesEnum.CHECKOUT);
	//
	// //Update Service IsVNF to false
	// restResponse = ServiceRestUtils.updateService(serviceDetails,
	// sdncUserDetails);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after updating service metadata", 400,
	// restResponse.getErrorCode().intValue());
	// List<String> variables = new ArrayList<String>();
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_IS_VNF_CANNOT_BE_CHANGED.name(),
	// variables, restResponse.getResponse());
	//
	// //get service and verify that service created with isVNF defined in
	// serviceDetails
	// serviceByNameAndVersion =
	// ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
	// serviceDetails.getName(), "1.1");
	// serviceObject =
	// ResponseParser.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
	// ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails,
	// serviceObject, sdncUserDetails,
	// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	// }

}
