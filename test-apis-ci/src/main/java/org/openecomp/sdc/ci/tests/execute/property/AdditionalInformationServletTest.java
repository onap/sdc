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

package org.openecomp.sdc.ci.tests.execute.property;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.resource.ResourceApiTest;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class AdditionalInformationServletTest extends ComponentBaseTest {

	protected Type constraintType = new TypeToken<PropertyConstraint>() {
	}.getType();

	protected Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();

	@Rule
	public static TestName name = new TestName();

	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";

	protected JSONParser jsonParser = new JSONParser();

	public AdditionalInformationServletTest() {
		super(name, AdditionalInformationServletTest.class.getName());
	}

	// @Before
	// public void deleteResources() {
	// //TODO Evg : will be new API added for delete by name and version
	//
	// ResourceReqDetails resource = getResource();
	// User user = getUser();
	//
	// try {
	// String resourceName = resource.getResourceName();
	// ResourceRestUtils.deleteResourceByNameAndVersion(user, resourceName,
	// "0.1");
	// ResourceRestUtils.deleteResourceByNameAndVersion(user, resourceName,
	// "0.2");
	// ResourceRestUtils.deleteResourceByNameAndVersion(user, resourceName,
	// "1.0");
	// ResourceRestUtils.deleteResourceByNameAndVersion(user, resourceName,
	// "1.1");
	// ResourceRestUtils.deleteResourceByNameAndVersion(user, resourceName +
	// "aa", "0.1");
	// resourceUtils.deleteResource_allVersions(resource, user);
	//
	// } catch (IOException e) {
	// assertTrue(false);
	// }
	//
	// try {
	// ServiceReqDetails serviceDetails = getServiceDetails();
	//
	// RestResponse deleteServiceResponse =
	// serviceUtils.deleteServiceByNameAndVersion(UserUtils.getAdminDetails(),
	// serviceDetails.getServiceName(), "0.1");
	//
	// assertNotNull("check response object is not null after delete
	// service",deleteServiceResponse);
	// assertNotNull("check error code exists in response after delete
	// service",deleteServiceResponse.getErrorCode());
	// assertTrue("delete service failed status:" +
	// deleteServiceResponse.getErrorCode(),
	// deleteServiceResponse.getErrorCode() != 500);
	//
	// deleteServiceResponse =
	// serviceUtils.deleteServiceByNameAndVersion(UserUtils.getAdminDetails(),
	// serviceDetails.getServiceName(), "1.0");
	//
	// assertNotNull("check response object is not null after delete
	// service",deleteServiceResponse);
	// assertNotNull("check error code exists in response after delete
	// service",deleteServiceResponse.getErrorCode());
	// assertTrue("delete service failed status:" +
	// deleteServiceResponse.getErrorCode(),
	// deleteServiceResponse.getErrorCode() != 500);
	//
	// serviceUtils.deleteService_allVersions(serviceDetails, user);
	//
	// } catch (IOException e) {
	// assertTrue(false);
	// }
	// }

	@Test
	public void updateResourceAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		AssertJUnit.assertNotNull("check response object is not null after create resource", createResourceResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		String updatedKey = "ZZZ  ZZZ";
		String updatedValue = "JJJJ";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		AssertJUnit.assertNotNull("check response object is not null after create property", createProperty);
		AssertJUnit.assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		AssertJUnit.assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		AssertJUnit.assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		fromJson.setKey(updatedKey);
		fromJson.setValue(updatedValue);

		RestResponse updatedProperty = updateAdditionalInformation(resourceId, fromJson, user, fromJson.getUniqueId());
		AssertJUnit.assertNotNull("check response object is not null after update additional information", updatedProperty);
		AssertJUnit.assertNotNull("check error code exists in response after additional information", updatedProperty.getErrorCode());
		AssertJUnit.assertEquals("Check response code after additional information", 200, updatedProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo updatedJson = gson.fromJson(updatedProperty.getResponse(), AdditionalInfoParameterInfo.class);
		AssertJUnit.assertFalse("check number of spaces", updatedJson.getKey().contains("  "));
		AssertJUnit.assertEquals("check returned key", "ZZZ ZZZ", updatedJson.getKey());
		AssertJUnit.assertEquals("check returned value", updatedValue, updatedJson.getValue());
		AssertJUnit.assertEquals("check returned id", fromJson.getUniqueId(), updatedJson.getUniqueId());

	}

	@Test
	public void deleteResourceAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		AssertJUnit.assertNotNull("check response object is not null after create resource", createResourceResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		AssertJUnit.assertNotNull("check response object is not null after create property", createProperty);
		AssertJUnit.assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		AssertJUnit.assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		AssertJUnit.assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		RestResponse deletedProperty = deleteAdditionalInformation(resourceId, fromJson.getUniqueId(), user);
		AssertJUnit.assertNotNull("check response object is not null after update additional information", deletedProperty);
		AssertJUnit.assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		AssertJUnit.assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo updatedJson = gson.fromJson(deletedProperty.getResponse(), AdditionalInfoParameterInfo.class);
		AssertJUnit.assertFalse("check number of spaces", updatedJson.getKey().contains("  "));
		AssertJUnit.assertEquals("check returned key", "AAA AAA", updatedJson.getKey());
		AssertJUnit.assertEquals("check returned value", value, updatedJson.getValue());
		AssertJUnit.assertEquals("check returned id", fromJson.getUniqueId(), updatedJson.getUniqueId());

		deletedProperty = deleteAdditionalInformation(resourceId, fromJson.getUniqueId(), user);
		AssertJUnit.assertNotNull("check response object is not null after update additional information", deletedProperty);
		AssertJUnit.assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		AssertJUnit.assertEquals("Check response code after additional information", 409, deletedProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceAdditionalInformationTestDuringLifecycle() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		AssertJUnit.assertNotNull("check response object is not null after create resource", createResourceResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		AssertJUnit.assertNotNull("check response object is not null after create property", createProperty);
		AssertJUnit.assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		AssertJUnit.assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		AssertJUnit.assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		resource.setUniqueId(resourceId);

		// resourceUtils.addResourceMandatoryArtifacts(user,
		// createResourceResponse);

		certifyResource(user, resource, null, 1);

	}

	public RestResponse createService() {

		User user = getUser();
		ServiceReqDetails serviceDetails = getServiceDetails();

		RestResponse createServiceResponse = null;
		try {
			createServiceResponse = ServiceRestUtils.createService(serviceDetails, user);
			AssertJUnit.assertNotNull("check response object is not null after create user", createServiceResponse);
			AssertJUnit.assertNotNull("check error code exists in response after create resource", createServiceResponse.getErrorCode());
			AssertJUnit.assertEquals("Check response code after checkout resource", 201, createServiceResponse.getErrorCode().intValue());
		} catch (Exception e) {
			AssertJUnit.assertTrue(false);
		}

		return createServiceResponse;

	}

	protected User getUser() {
		String adminFirstName = "Jimmy";
		String adminLastName = "Hendrix";
		String adminUserId = "jh0003";
		return new User(adminFirstName, adminLastName, adminUserId, null, null, null);
	}

	protected ResourceReqDetails getResource() {
		String resourceName = "ciResourceforproperty4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "pe0123";
		String icon = "myICON";
		ResourceReqDetails resource = new ResourceReqDetails(resourceName, description, resourceTags, null, derivedFrom, vendorName, vendorRelease, contactId, icon);
		resource.addCategoryChain(ResourceCategoryEnum.GENERIC_DATABASE.getCategory(), ResourceCategoryEnum.GENERIC_DATABASE.getSubCategory());
		return resource;
	}

	protected RestResponse createResource(ResourceReqDetails resourceDetails, User sdncModifierDetails) throws IOException {

		ResourceApiTest rat = new ResourceApiTest();
		ResourceReqDetails resourceObj = rat.getResourceObj();

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());
		RestResponse createResourceResponse = http.httpSendPost(url, userBodyJson, headersMap);

		return createResourceResponse;

	}

	protected RestResponse getResource(User sdncModifierDetails, String resourceUid) throws IOException {

		ResourceApiTest rat = new ResourceApiTest();
		ResourceReqDetails resourceObj = rat.getResourceObj();

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUid);
		RestResponse createResourceResponse = http.httpSendGet(url, headersMap);

		return createResourceResponse;

	}

	protected RestResponse deleteResource(String resourceName, String resourceVersion, User sdncModifierDetails) throws IOException {
		RestResponse deleteResourceResponse = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceName, resourceVersion);

		return deleteResourceResponse;

	}

	protected RestResponse updateAdditionalInformation(String resourceId, AdditionalInfoParameterInfo additionalInfo, User sdncModifierDetails, String id) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String body = gson.toJson(additionalInfo);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_ADDITIONAL_INFORMATION_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, id);
		return http.httpSendPut(url, body, headersMap);

	}

	protected RestResponse updateServiceAdditionalInformation(String resourceId, AdditionalInfoParameterInfo additionalInfo, User sdncModifierDetails, String id) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String body = gson.toJson(additionalInfo);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_ADDITIONAL_INFORMATION_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, id);
		return http.httpSendPut(url, body, headersMap);

	}

	protected RestResponse deleteAdditionalInformation(String resourceId, String id, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.DELETE_ADDITIONAL_INFORMATION_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, id);
		return http.httpSendDelete(url, headersMap);

	}

	protected RestResponse deleteServiceAdditionalInformation(String resourceId, String id, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.DELETE_ADDITIONAL_INFORMATION_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, id);
		return http.httpSendDelete(url, headersMap);

	}

	protected RestResponse getAdditionalInformation(String resourceId, String id, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ADDITIONAL_INFORMATION_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, id);
		return http.httpSendGet(url, headersMap);

	}

	protected RestResponse getServiceAdditionalInformation(String resourceId, String id, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ADDITIONAL_INFORMATION_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, id);
		return http.httpSendGet(url, headersMap);

	}

	protected RestResponse getResourceAllAdditionalInformation(String resourceId, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ALL_ADDITIONAL_INFORMATION_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId);
		return http.httpSendGet(url, headersMap);

	}

	protected RestResponse getServiceAllAdditionalInformation(String resourceId, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ALL_ADDITIONAL_INFORMATION_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId);
		return http.httpSendGet(url, headersMap);

	}

	protected RestResponse createResourceAdditionalInformation(String resourceId, AdditionalInfoParameterInfo additionalInfo, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String body = gson.toJson(additionalInfo);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_ADDITIONAL_INFORMATION_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId);
		return http.httpSendPost(url, body, headersMap);

	}

	protected RestResponse createServiceAdditionalInformation(String serviceId, AdditionalInfoParameterInfo additionalInfo, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String body = gson.toJson(additionalInfo);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_ADDITIONAL_INFORMATION_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId);
		return http.httpSendPost(url, body, headersMap);

	}

	protected ServiceReqDetails getServiceDetails() {
		String serviceName = "ciNewTestService21";
		String category = ServiceCategoriesEnum.MOBILITY.getValue();
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("serviceTag");
		tags.add(serviceName);
		String description = "service Description";
		String vendorName = "Oracle";
		String vendorRelease = "0.1";
		String contactId = "al1976";
		String icon = "myIcon";

		return new ServiceReqDetails(serviceName, category, tags, description, contactId, icon);
	}

	// TODO Tal: Since Cashing change partial resource returned that causes null
	// pointer exception in line:
	// commented out till fixing
	protected Resource certifyResource(User user, ResourceReqDetails resource, String resourceVersion, int numberOfAI) throws IOException {
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKIN);

		AssertJUnit.assertNotNull("check response object is not null after create user", checkInResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create user", checkInResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create user", 200, checkInResponse.getErrorCode().intValue());

		Resource resourceAfterOperation = gson.fromJson(checkInResponse.getResponse(), Resource.class);
		// TODO Tal: Since Cashing change partial resource returned that causes
		// null pointer exception
		/*
		 * AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		 */
		/*
		 * AssertJUnit.assertEquals("check size of additional information", numberOfAI, resourceAfterOperation.getAdditionalInformation().get(0). getParameters().size());
		 */

		RestResponse req4certResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);

		AssertJUnit.assertNotNull("check response object is not null after create user", req4certResponse);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200, req4certResponse.getErrorCode().intValue());

		resourceAfterOperation = gson.fromJson(req4certResponse.getResponse(), Resource.class);
		// TODO Tal: Since Cashing change partial resource returned that causes
		// null pointer exception
		/*
		 * AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		 */
		/*
		 * AssertJUnit.assertEquals("check size of additional information", numberOfAI, resourceAfterOperation.getAdditionalInformation().get(0). getParameters().size());
		 */

		// change modifier
		user.setUserId(UserRoleEnum.TESTER.getUserId());
		// start certification

		RestResponse startCertResourceResponse3 = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		AssertJUnit.assertNotNull("check response object is not null after create user", startCertResourceResponse3);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200, startCertResourceResponse3.getErrorCode().intValue());

		resourceAfterOperation = gson.fromJson(startCertResourceResponse3.getResponse(), Resource.class);
		// TODO Tal: Since Cashing change partial resource returned that causes
		// null pointer exception
		/*
		 * AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		 */
		/*
		 * AssertJUnit.assertEquals("check size of additional information", numberOfAI, resourceAfterOperation.getAdditionalInformation().get(0). getParameters().size());
		 */

		// certify

		RestResponse certifyResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CERTIFY);
		AssertJUnit.assertNotNull("check response object is not null after create user", certifyResponse);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200, certifyResponse.getErrorCode().intValue());

		resourceAfterOperation = gson.fromJson(certifyResponse.getResponse(), Resource.class);
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		AssertJUnit.assertEquals("check size of additional information", numberOfAI, resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());

		Resource certifyResource = gson.fromJson(certifyResponse.getResponse(), Resource.class);
		return certifyResource;
	}

	protected Resource certifyService(User user, ServiceReqDetails service, String resourceVersion) throws Exception {
		RestResponse checkInResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.CHECKIN);

		AssertJUnit.assertNotNull("check response object is not null after create user", checkInResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create user", checkInResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create user", 200, checkInResponse.getErrorCode().intValue());

		Resource resourceAfterOperation = gson.fromJson(checkInResponse.getResponse(), Resource.class);
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());

		RestResponse req4certResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);

		AssertJUnit.assertNotNull("check response object is not null after create user", req4certResponse);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200, req4certResponse.getErrorCode().intValue());

		resourceAfterOperation = gson.fromJson(req4certResponse.getResponse(), Resource.class);
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());

		// change modifier
		user.setUserId(UserRoleEnum.TESTER.getUserId());
		// start certification

		RestResponse startCertResourceResponse3 = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		AssertJUnit.assertNotNull("check response object is not null after create user", startCertResourceResponse3);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200, startCertResourceResponse3.getErrorCode().intValue());

		resourceAfterOperation = gson.fromJson(startCertResourceResponse3.getResponse(), Resource.class);
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());

		// certify

		RestResponse certifyResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.CERTIFY);
		AssertJUnit.assertNotNull("check response object is not null after create user", certifyResponse);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200, certifyResponse.getErrorCode().intValue());

		resourceAfterOperation = gson.fromJson(certifyResponse.getResponse(), Resource.class);
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
		AssertJUnit.assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());

		Resource certifyResource = gson.fromJson(certifyResponse.getResponse(), Resource.class);
		return certifyResource;
	}

	@Test
	public void createResourceAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceAdditionalInfoFormatWithTags() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "<b>Bold<</b>";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertEquals("check returned key", "Bold&amp;lt;", fromJson.getValue());

	}

	@Test
	public void createServiceAdditionalInfoFormatWithTags() throws Exception {
		User user = getUser();
		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		assertNotNull("check response object is not null after create resource", createServiceResponse);
		assertNotNull("check error code exists in response after create resource", createServiceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createServiceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "<b>Bold<</b>";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertEquals("check returned key", "Bold&amp;lt;", fromJson.getValue());

	}

	@Test
	public void createResourceAdditionalInfoFormatWithWhiteSpaces() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "      ";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

		key = "    ";
		value = "AAA  AAA";

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceAdditionalInfoFormatWithWhiteSpaces() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();
		ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		assertNotNull("check response object is not null after create resource", createServiceResponse);
		assertNotNull("check error code exists in response after create resource", createServiceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createServiceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "      ";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

		key = "    ";
		value = "AAA  AAA";

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceAndUpdateAdditionalInfo() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		key = "BBB  BBB";
		value = "BBBB";

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "BBB BBB", fromJson.getKey());

		String updatedKey = "AAA  AAA";
		String updatedValue = "JJJJ";

		fromJson.setKey(updatedKey);
		fromJson.setValue(updatedValue);

		RestResponse updatedProperty = updateAdditionalInformation(resourceId, fromJson, user, fromJson.getUniqueId());
		assertNotNull("check response object is not null after update additional information", updatedProperty);
		assertNotNull("check error code exists in response after additional information", updatedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 409, updatedProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceEmptyAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

		key = "BBBB";
		value = "";

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceEmptyAdditionalInformationTest() throws Exception {

		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceAllSpacesAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "           ";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceAllSpacesAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "           ";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceInvalidKeyAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "abc?";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceInvalidKeyAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "abc?";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceAdditionalInformationNullKeyTest() throws Exception {

		User user = getUser();
		ResourceReqDetails resource = getResource();

		RestResponse createResourceResponse = createResource(resource, user);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		String key = null;
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceAdditionalInformationNullKeyTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = null;
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceMaximumInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		for (int i = 0; i < 50; i++) {

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + i, value);

			RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		}

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);
		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceLifeCycleAndMaximumInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);
		resource.setUniqueId(resourceId);

		String key = "AAA  AAA";
		String value = "BBBB";

		for (int i = 0; i < 49; i++) {

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + i, value);

			RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		}

		String resourceVersion = "0.1";
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);

		assertNotNull("check response object is not null after create property", checkInResponse);
		assertNotNull("check error code exists in response after create property", checkInResponse.getErrorCode());
		assertEquals("Check response code after create property", 200, checkInResponse.getErrorCode().intValue());

		resourceVersion = "0.2";

		RestResponse checkOutResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT, null);

		resourceId = ResponseParser.getUniqueIdFromResponse(checkOutResponse);
		resource.setUniqueId(resourceId);

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 50, value);
		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 51, value);
		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

		RestResponse checkUndoOutResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.UNDOCHECKOUT, null);

		resourceVersion = "0.1";

		checkOutResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT, null);
		resourceId = ResponseParser.getUniqueIdFromResponse(checkOutResponse);
		resource.setUniqueId(resourceId);

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 50, value);
		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 51, value);
		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createResourceLifeCycleCertifyAndMaximumInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);
		resource.setUniqueId(resourceId);

		String key = "AAA  AAA";
		String value = "BBBB";

		for (int i = 0; i < 49; i++) {

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + i, value);

			RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		}

		String resourceVersion = "0.1";
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";

		// resourceUtils.addResourceMandatoryArtifacts(user,
		// createResourceResponse);

		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);

		assertNotNull("check response object is not null after create property", checkInResponse);
		assertNotNull("check error code exists in response after create property", checkInResponse.getErrorCode());
		assertEquals("Check response code after create property", 200, checkInResponse.getErrorCode().intValue());

		RestResponse changeStateResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST, null);
		changeStateResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION, null);
		changeStateResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CERTIFY, null);

		assertNotNull("check response object is not null after create property", checkInResponse);
		assertNotNull("check error code exists in response after create property", checkInResponse.getErrorCode());
		assertEquals("Check response code after create property", 200, checkInResponse.getErrorCode().intValue());

		resourceId = ResponseParser.getUniqueIdFromResponse(changeStateResponse);
		resource.setUniqueId(resourceId);

		resourceVersion = "1.0";

		changeStateResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT, null);
		resourceId = ResponseParser.getUniqueIdFromResponse(changeStateResponse);
		resource.setUniqueId(resourceId);

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 50, value);
		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 51, value);
		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceCycleAndMaximumInformationTest() throws Exception {

		User user = getUser();

		ServiceReqDetails service = getServiceDetails();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		assertNotNull("check response object is not null after create resource", createServiceResponse);
		assertNotNull("check error code exists in response after create resource", createServiceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createServiceResponse.getErrorCode().intValue());

		service.setUniqueId(serviceId);

		String key = "AAA  AAA";
		String value = "BBBB";

		for (int i = 0; i < 49; i++) {

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + i, value);

			RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		}

		String resourceVersion = "0.1";
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);

		assertNotNull("check response object is not null after create property", checkInResponse);
		assertNotNull("check error code exists in response after create property", checkInResponse.getErrorCode());
		assertEquals("Check response code after create property", 200, checkInResponse.getErrorCode().intValue());

		resourceVersion = "0.2";

		RestResponse checkOutResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT, null);

		serviceId = ResponseParser.convertServiceResponseToJavaObject(checkOutResponse.getResponse()).getUniqueId();
		service.setUniqueId(serviceId);

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 50, value);
		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 51, value);
		createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

		RestResponse checkUndoOutResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.UNDOCHECKOUT, null);

		resourceVersion = "0.1";

		checkOutResponse = LifecycleRestUtils.changeServiceState(service, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT, null);
		serviceId = ResponseParser.convertServiceResponseToJavaObject(checkOutResponse.getResponse()).getUniqueId();
		service.setUniqueId(serviceId);

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 50, value);
		createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + 51, value);
		createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

	}

	@Test
	public void createServiceMaximumInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "AAA  AAA";
		String value = "BBBB";

		String lastCreatedProperty = null;

		for (int i = 0; i < 50; i++) {

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key + i, value);

			RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

			AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
			lastCreatedProperty = fromJson.getUniqueId();

		}

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);
		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 409, createProperty.getErrorCode().intValue());

		RestResponse deletedProperty = deleteServiceAdditionalInformation(serviceId, lastCreatedProperty, user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);
		createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);

		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

	}

	@Test
	public void updateServiceAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "AAA  AAA";
		String value = "BBBB";

		String updatedKey = "ZZZ  ZZZ";
		String updatedValue = "JJJJ";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		fromJson.setKey(updatedKey);
		fromJson.setValue(updatedValue);

		RestResponse updatedProperty = updateServiceAdditionalInformation(serviceId, fromJson, user, fromJson.getUniqueId());
		assertNotNull("check response object is not null after update additional information", updatedProperty);
		assertNotNull("check error code exists in response after additional information", updatedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, updatedProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo updatedJson = gson.fromJson(updatedProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", updatedJson.getKey().contains("  "));
		assertEquals("check returned key", "ZZZ ZZZ", updatedJson.getKey());
		assertEquals("check returned value", updatedValue, updatedJson.getValue());
		assertEquals("check returned id", fromJson.getUniqueId(), updatedJson.getUniqueId());

		fromJson.setKey(updatedKey);
		fromJson.setValue("\uC2B5");

		updatedProperty = updateServiceAdditionalInformation(serviceId, fromJson, user, fromJson.getUniqueId());
		assertNotNull("check response object is not null after update additional information", updatedProperty);
		assertNotNull("check error code exists in response after additional information", updatedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 400, updatedProperty.getErrorCode().intValue());

	}

	@Test
	public void deleteServiceAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		RestResponse deletedProperty = deleteServiceAdditionalInformation(serviceId, fromJson.getUniqueId(), user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo updatedJson = gson.fromJson(deletedProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", updatedJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", updatedJson.getKey());
		assertEquals("check returned value", value, updatedJson.getValue());
		assertEquals("check returned id", fromJson.getUniqueId(), updatedJson.getUniqueId());

		deletedProperty = deleteServiceAdditionalInformation(serviceId, fromJson.getUniqueId(), user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 409, deletedProperty.getErrorCode().intValue());

	}

	@Test
	public void getResourceAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		RestResponse deletedProperty = getAdditionalInformation(resourceId, fromJson.getUniqueId(), user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo updatedJson = gson.fromJson(deletedProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", updatedJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", updatedJson.getKey());
		assertEquals("check returned value", value, updatedJson.getValue());
		assertEquals("check returned id", fromJson.getUniqueId(), updatedJson.getUniqueId());

	}

	@Test
	public void getServiceAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		RestResponse deletedProperty = getServiceAdditionalInformation(serviceId, fromJson.getUniqueId(), user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo updatedJson = gson.fromJson(deletedProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", updatedJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", updatedJson.getKey());
		assertEquals("check returned value", value, updatedJson.getValue());
		assertEquals("check returned id", fromJson.getUniqueId(), updatedJson.getUniqueId());

	}

	@Test
	public void getResourceAllAdditionalInformationTest() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		RestResponse deletedProperty = getResourceAllAdditionalInformation(resourceId, user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		AdditionalInformationDefinition updatedJson = gson.fromJson(deletedProperty.getResponse(), AdditionalInformationDefinition.class);
		assertEquals("check number of parameters", 1, updatedJson.getParameters().size());
		AdditionalInfoParameterInfo info = updatedJson.getParameters().iterator().next();

		assertFalse("check number of spaces", info.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", info.getKey());
		assertEquals("check returned value", value, info.getValue());
		assertEquals("check returned id", fromJson.getUniqueId(), info.getUniqueId());

	}

	@Test
	public void getServiceAllAdditionalInformationTest() throws Exception {
		User user = getUser();

		RestResponse createServiceResponse = createService();

		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();

		String key = "AAA  AAA";
		String value = "BBBB";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		RestResponse deletedProperty = getServiceAllAdditionalInformation(serviceId, user);
		assertNotNull("check response object is not null after update additional information", deletedProperty);
		assertNotNull("check error code exists in response after additional information", deletedProperty.getErrorCode());
		assertEquals("Check response code after additional information", 200, deletedProperty.getErrorCode().intValue());

		AdditionalInformationDefinition updatedJson = gson.fromJson(deletedProperty.getResponse(), AdditionalInformationDefinition.class);
		assertEquals("check number of parameters", 1, updatedJson.getParameters().size());
		AdditionalInfoParameterInfo info = updatedJson.getParameters().iterator().next();

		assertFalse("check number of spaces", info.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", info.getKey());
		assertEquals("check returned value", value, info.getValue());
		assertEquals("check returned id", fromJson.getUniqueId(), info.getUniqueId());

	}

	@Test
	public void createServiceAdditionalInformationTestDuringLifecycle() throws Exception {

		User user = getUser();
		RestResponse createServiceResponse = createService();
		String serviceId = ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse()).getUniqueId();
		String key = "AAA  AAA";
		String value = "BBBB";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createServiceAdditionalInformation(serviceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

		AdditionalInfoParameterInfo fromJson = gson.fromJson(createProperty.getResponse(), AdditionalInfoParameterInfo.class);
		assertFalse("check number of spaces", fromJson.getKey().contains("  "));
		assertEquals("check returned key", "AAA AAA", fromJson.getKey());

		ServiceReqDetails serviceDetails = getServiceDetails();

		serviceDetails.setUniqueId(serviceId);

		// serviceUtils.addServiceMandatoryArtifacts(user,
		// createServiceResponse);

		certifyService(user, serviceDetails, null);

	}

	@Test
	public void createCascadeResource() {

		// TODO: to check after rebase

		User user = getUser();
		ResourceReqDetails resource = getResource();
		String newResourceNameSuffix = "aa";

		RestResponse createResourceResponse = null;
		try {

			createResourceResponse = createResource(resource, user);
			assertEquals("check invalid type", 201, createResourceResponse.getErrorCode().intValue());
			String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

			String resourceVersion = "0.1";
			// resourceUtils.addResourceMandatoryArtifacts(user,
			// createResourceResponse);

			String key = "AAA  AAA";
			String value = "BBBB";

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

			RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

			resource.setUniqueId(resourceId);
			Resource certifiedResource = certifyResource(user, resource, resourceVersion, 1);

			ResourceReqDetails newResourceDetails = getResource();
			String newResourceName = newResourceDetails.getName() + newResourceNameSuffix;
			newResourceDetails.setName(newResourceName);
			List<String> derivedFrom = new ArrayList<>();
			derivedFrom.add(certifiedResource.getName());
			newResourceDetails.setDerivedFrom(derivedFrom);
			newResourceDetails.getTags().add(newResourceName);

			user.setUserId(UserRoleEnum.ADMIN.getUserId());
			RestResponse newCreateResourceResponse = createResource(newResourceDetails, user);
			assertEquals("Check response code after creating resource", 201, newCreateResourceResponse.getErrorCode().intValue());
			Resource newResource = gson.fromJson(newCreateResourceResponse.getResponse(), Resource.class);

			RestResponse allAdditionalInformation = getResourceAllAdditionalInformation(newResource.getUniqueId(), user);

			assertNotNull("check response object is not null after update additional information", allAdditionalInformation);
			assertNotNull("check error code exists in response after additional information", allAdditionalInformation.getErrorCode());
			assertEquals("Check response code after additional information", 200, allAdditionalInformation.getErrorCode().intValue());

			AdditionalInformationDefinition updatedJson = gson.fromJson(allAdditionalInformation.getResponse(), AdditionalInformationDefinition.class);
			assertEquals("check number of parameters", 0, updatedJson.getParameters().size());
			// AdditionalInfoParameterInfo info =
			// updatedJson.getParameters().iterator().next();

			String newResourceId = newResource.getUniqueId();
			createProperty = createResourceAdditionalInformation(newResourceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

			allAdditionalInformation = getResourceAllAdditionalInformation(newResourceId, user);

			assertNotNull("check response object is not null after update additional information", allAdditionalInformation);
			assertNotNull("check error code exists in response after additional information", allAdditionalInformation.getErrorCode());
			assertEquals("Check response code after additional information", 200, allAdditionalInformation.getErrorCode().intValue());

			updatedJson = gson.fromJson(allAdditionalInformation.getResponse(), AdditionalInformationDefinition.class);
			assertEquals("check number of parameters", 1, updatedJson.getParameters().size());

		} catch (IOException e) {
			assertTrue(false);
		}

	}

	@Test
	public void createSamePropertyAfterCiCOResource() {

		User user = getUser();
		ResourceReqDetails resource = getResource();

		RestResponse createResourceResponse = null;
		try {

			createResourceResponse = createResource(resource, user);
			assertEquals("check invalid type", 201, createResourceResponse.getErrorCode().intValue());
			String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

			String resourceVersion = "0.1";
			// resourceUtils.addResourceMandatoryArtifacts(user,
			// createResourceResponse);

			String key = "AAA  AAA";
			String value = "BBBB";

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

			RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
			assertNotNull("check response object is not null after create property", createProperty);
			assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

			resource.setUniqueId(resourceId);
			RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKIN);

			assertNotNull("check response object is not null after create user", checkInResponse);
			assertNotNull("check error code exists in response after create user", checkInResponse.getErrorCode());
			assertEquals("Check response code after create user", 200, checkInResponse.getErrorCode().intValue());

			Resource resourceAfterOperation = gson.fromJson(checkInResponse.getResponse(), Resource.class);
			assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().size());
			assertEquals("check size of additional information", 1, resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());

			RestResponse checkOutResponse = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT);

			assertNotNull("check response object is not null after create user", checkOutResponse);
			assertNotNull("check error code exists in response after create user", checkOutResponse.getErrorCode());
			assertEquals("Check response code after create user", 200, checkOutResponse.getErrorCode().intValue());

			Resource resourceAfterCoOperation = gson.fromJson(checkOutResponse.getResponse(), Resource.class);
			assertEquals("check size of additional information", 1, resourceAfterCoOperation.getAdditionalInformation().size());
			assertEquals("check size of additional information", 1, resourceAfterCoOperation.getAdditionalInformation().get(0).getParameters().size());

			String newResourceId = ResponseParser.getUniqueIdFromResponse(checkOutResponse);

			String key2 = "ZZZ";
			String value2 = "BBBB";

			AdditionalInfoParameterInfo additionalInfoParameterInfo2 = new AdditionalInfoParameterInfo(null, key2, value2);

			RestResponse createProperty2 = createResourceAdditionalInformation(newResourceId, additionalInfoParameterInfo2, user);
			assertNotNull("check response object is not null after create property", createProperty2);
			assertNotNull("check error code exists in response after create property", createProperty2.getErrorCode());
			assertEquals("Check response code after create property", 201, createProperty2.getErrorCode().intValue());

			RestResponse afterCreateAI = ResourceRestUtils.getResource(user, newResourceId);
			Resource resourceNew = gson.fromJson(afterCreateAI.getResponse(), Resource.class);
			assertEquals("check size of additional information", 1, resourceNew.getAdditionalInformation().size());
			assertEquals("check size of additional information", 2, resourceNew.getAdditionalInformation().get(0).getParameters().size());

			resource.setUniqueId(newResourceId);
			Resource certifiedResource = certifyResource(user, resource, resourceVersion, 2);
			assertEquals("check size of additional information", 1, certifiedResource.getAdditionalInformation().size());
			assertEquals("check size of additional information", 2, certifiedResource.getAdditionalInformation().get(0).getParameters().size());

			user.setUserId(UserRoleEnum.DESIGNER.getUserId());
			resource.setUniqueId(certifiedResource.getUniqueId());
			RestResponse checkOutResponseAfterCertify = LifecycleRestUtils.changeResourceState(resource, user, resourceVersion, LifeCycleStatesEnum.CHECKOUT);

			assertNotNull("check response object is not null after create user", checkOutResponseAfterCertify);
			assertNotNull("check error code exists in response after create user", checkOutResponseAfterCertify.getErrorCode());
			assertEquals("Check response code after create user", 200, checkOutResponseAfterCertify.getErrorCode().intValue());

			Resource resourceAfterCertifyCoOperation = gson.fromJson(checkOutResponseAfterCertify.getResponse(), Resource.class);
			assertEquals("check size of additional information", 1, resourceAfterCertifyCoOperation.getAdditionalInformation().size());
			assertEquals("check size of additional information", 2, resourceAfterCertifyCoOperation.getAdditionalInformation().get(0).getParameters().size());

		} catch (IOException e) {
			assertTrue(false);
		}

	}

	// public Resource certifyService(User user, ServiceReqDetails service,
	// String resourceVersion) throws Exception {
	//
	// RestResponse checkInResponse =
	// LifecycleRestUtils.changeServiceState(service, user, resourceVersion,
	// LifeCycleStates.CHECKIN);
	//
	// assertNotNull("check response object is not null after create user",
	// checkInResponse);
	// assertNotNull("check error code exists in response after create user",
	// checkInResponse.getErrorCode());
	// assertEquals("Check response code after create user", 200,
	// checkInResponse.getErrorCode().intValue());
	//
	// Resource resourceAfterOperation =
	// gson.fromJson(checkInResponse.getResponse(), Resource.class);
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().size());
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());
	//
	//// TODO Andrey
	// createAndAddCertResourceToService(service, user);
	//
	// RestResponse req4certResponse =
	// LifecycleRestUtils.changeServiceState(service, user, resourceVersion,
	// LifeCycleStates.CERTIFICATIONREQUEST);
	//
	// assertNotNull("check response object is not null after create user",
	// req4certResponse);
	// assertEquals("Check response code after checkout resource", 200,
	// req4certResponse.getErrorCode().intValue());
	//
	// resourceAfterOperation = gson.fromJson(req4certResponse.getResponse(),
	// Resource.class);
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().size());
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());
	//
	// //change modifier
	// user.setUserId(UserRoleEnum.TESTER.getUserId());
	//
	// //start certification
	// RestResponse startCertResourceResponse3 =
	// LifecycleRestUtils.changeServiceState(service, user, resourceVersion,
	// LifeCycleStates.STARTCERTIFICATION);
	// assertNotNull("check response object is not null after create user",
	// startCertResourceResponse3);
	// assertEquals("Check response code after checkout resource", 200,
	// startCertResourceResponse3.getErrorCode().intValue());
	//
	// resourceAfterOperation =
	// gson.fromJson(startCertResourceResponse3.getResponse(), Resource.class);
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().size());
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());
	//
	// //certify
	//
	// RestResponse certifyResponse =
	// LifecycleRestUtils.changeServiceState(service, user, resourceVersion,
	// LifeCycleStates.CERTIFY);
	// assertNotNull("check response object is not null after create user",
	// certifyResponse);
	// assertEquals("Check response code after checkout resource", 200,
	// certifyResponse.getErrorCode().intValue());
	//
	// resourceAfterOperation = gson.fromJson(certifyResponse.getResponse(),
	// Resource.class);
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().size());
	// assertEquals("check size of additional information", 1,
	// resourceAfterOperation.getAdditionalInformation().get(0).getParameters().size());
	//
	// Resource certifyResource = gson.fromJson(certifyResponse.getResponse(),
	// Resource.class);
	// return certifyResource;
	// }

	private void createAndAddCertResourceToService(ServiceReqDetails serviceDetails, User user) throws Exception {

		User sdncTesterUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory.getDefaultComponentInstance();

		RestResponse response = ResourceRestUtils.createResource(resourceDetails, user);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetails.getUniqueId());

		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, user, resourceDetails.getUniqueId());
		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(), response.getErrorCode() == 200);

		// certified resource
		// response = LCSbaseTest.certifyResource(resourceDetails);
		RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user, LifeCycleStatesEnum.CHECKIN);
		assertTrue("certify resource request returned status:" + restResponseResource.getErrorCode(), response.getErrorCode() == 200);
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertTrue("certify resource request returned status:" + restResponseResource.getErrorCode(), response.getErrorCode() == 200);
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncTesterUser, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertTrue("certify resource request returned status:" + restResponseResource.getErrorCode(), response.getErrorCode() == 200);
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncTesterUser, LifeCycleStatesEnum.CERTIFY);
		assertTrue("certify resource request returned status:" + restResponseResource.getErrorCode(), response.getErrorCode() == 200);

		// add resource instance with HEAT deployment artifact to the service
		restResponseResource = LifecycleRestUtils.changeServiceState(serviceDetails, user, serviceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertTrue("certify resource request returned status:" + restResponseResource.getErrorCode(), response.getErrorCode() == 200);
		resourceInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, user, serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertTrue("response code is not 201, returned: " + response.getErrorCode(), response.getErrorCode() == 201);
	}

	@Test
	public void createResourceAdditionalInformationTestAddValue() throws Exception {
		User user = getUser();
		ResourceReqDetails resource = getResource();

		// deleteResource(resourceId, user);
		RestResponse createResourceResponse = createResource(resource, user);

		String resourceId = ResponseParser.getUniqueIdFromResponse(createResourceResponse);

		assertNotNull("check response object is not null after create resource", createResourceResponse);
		assertNotNull("check error code exists in response after create resource", createResourceResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResourceResponse.getErrorCode().intValue());

		String key = "AAA  AAA";
		String value = "\uC2B5";

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		RestResponse createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

		value = "";

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 400, createProperty.getErrorCode().intValue());

		value = "----<b></b><>;";

		additionalInfoParameterInfo = new AdditionalInfoParameterInfo(null, key, value);

		createProperty = createResourceAdditionalInformation(resourceId, additionalInfoParameterInfo, user);
		assertNotNull("check response object is not null after create property", createProperty);
		assertNotNull("check error code exists in response after create property", createProperty.getErrorCode());
		assertEquals("Check response code after create property", 201, createProperty.getErrorCode().intValue());

	}
}
