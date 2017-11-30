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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ResourceValidationUtils;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class UpdateResourceMetadataTest extends ComponentBaseTest {
	private static Logger logger = LoggerFactory.getLogger(UpdateResourceMetadataTest.class.getName());
	protected List<String> Empty_List = new ArrayList<String>();
	protected String extendedChars;

	protected final String contentTypeHeaderData = "application/json";
	protected final String acceptHeaderDate = "application/json";
	protected final String CHARSET_ISO_8859 = "charset=ISO-8859-1";

	public static TestName name = new TestName();
	protected User sdncModifierDetails;
	protected ResourceReqDetails resourceDetails;

	public UpdateResourceMetadataTest() {
		super(name, UpdateResourceMetadataTest.class.getName());

	}

	public String extendedCharsStringBuilder() throws Exception {
		char[] extendedCharsArray = new char[128];
		char ch = 128;
		for (int i = 0; i < extendedCharsArray.length - 1; i++) {
			extendedCharsArray[i] = ch;
			ch++;
		}
		extendedChars = new String(extendedCharsArray);
		return extendedChars;

	}

	@BeforeMethod
	public void setup() throws Exception {
		sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		resourceDetails = defineResourse();

	}

	// Keep
	@Test
	public void UpdateDerivedFromSuccess() throws Exception {

		String oldDerivedFromName = NormativeTypesEnum.ROOT.getNormativeName();
		String newDerivedFromName = NormativeTypesEnum.SOFTWARE_COMPONENT.getNormativeName();

		// Getting both derived from resources for validation
		/*
		 * RestResponse resourceByNameAndVersion =
		 * resourceUtils.getResourceByNameAndVersion(sdncModifierDetails,
		 * oldDerivedFromName, "1.0");
		 * assertEquals("Check response code after get database normative", 200,
		 * resourceByNameAndVersion.getErrorCode().intValue()); Resource
		 * databaseNormative =
		 * resourceUtils.parseResourceResp(resourceByNameAndVersion);
		 * 
		 * resourceByNameAndVersion =
		 * resourceUtils.getResourceByNameAndVersion(sdncModifierDetails,
		 * newDerivedFromName, "1.0");
		 * assertEquals("Check response code after get database normative", 200,
		 * resourceByNameAndVersion.getErrorCode().intValue()); Resource
		 * lbNormative =
		 * resourceUtils.parseResourceResp(resourceByNameAndVersion);
		 */

		// Derived from set to Database
		List<String> derivedFrom = new ArrayList<>();
		derivedFrom.add(oldDerivedFromName);
		resourceDetails.setDerivedFrom(derivedFrom);

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		AssertJUnit.assertEquals("Check response code after create resource", 201,
				restResponse.getErrorCode().intValue());
		Resource currentResource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());

		derivedFrom.clear();
		derivedFrom.add(newDerivedFromName);
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResource.getUniqueId(), "");
		AssertJUnit.assertEquals("Check response code after create resource", 200,
				updatedRestResponse.getErrorCode().intValue());

	}

	protected ResourceReqDetails defineUpdateResourceWithNonUpdatableFields(Resource resourceBeforeUpdate) {
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceBeforeUpdate.getName());

		updatedResourceDetails.setVersion("mumu");
		updatedResourceDetails.setIsAbstract(true);
		updatedResourceDetails.setIsHighestVersion(true);
		updatedResourceDetails.setCreatorUserId("df4444");
		updatedResourceDetails.setCreatorFullName("John Doe");
		updatedResourceDetails.setLastUpdaterUserId("gf5646");
		updatedResourceDetails.setLastUpdaterFullName("Viktor Tzoy");
		updatedResourceDetails.setCreationDate(new Long(4444));
		updatedResourceDetails.setLastUpdateDate(new Long("534535"));
		updatedResourceDetails.setLifecycleState(LifecycleStateEnum.READY_FOR_CERTIFICATION);
		updatedResourceDetails.setCost("6.1");
		updatedResourceDetails.setLicenseType("Installation");
		updatedResourceDetails.setUUID("dfsfsdf");
		return updatedResourceDetails;
	}

	public void UpdateResourceNotFoundTest() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		String resourceName = "cisco4";
		// update resource
		String description = "updatedDescription";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.MOBILITY.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "newOracle";
		String vendorRelease = "2.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails updatedResourceDetails = new ResourceReqDetails(resourceName, description, resourceTags,
				category, derivedFrom, vendorName, vendorRelease, contactId, icon);
		updatedResourceDetails.setUniqueId("dummyId");
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, "0.1");

		// validate response
		AssertJUnit.assertNotNull("check response object is not null after update resource", updatedRestResponse);
		AssertJUnit.assertNotNull("check error code exists in response after update resource",
				updatedRestResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after update resource", 404,
				updatedRestResponse.getErrorCode().intValue());
		// String resourceId =
		// UniqueIdBuilder.buildResourceUniqueId(resourceName, "0.1");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), Arrays.asList("dummyId"),
				updatedRestResponse.getResponse());

		resourceName = "";
		// resourceId = UniqueIdBuilder.buildResourceUniqueId(resourceName,
		// "0.1");
		updatedResourceDetails = defineUpdatedResourse(resourceName);
		updatedResourceDetails.setUniqueId("dummyId");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
				"0.1");
		AssertJUnit.assertNotNull("check response object is not null after update resource", updatedRestResponse);
		AssertJUnit.assertNotNull("check error code exists in response after update resource",
				updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), Arrays.asList("dummyId"),
				updatedRestResponse.getResponse());

	}

	public char[] getInValidChars() throws Exception {

		char[] extendedCharsArray = new char[59];
		char ch = 1;
		for (int i = 0; i < 44; i++) {
			extendedCharsArray[i] = ch;
			ch++;
		}
		ch = 58;
		for (int i = 44; i < 51; i++) {
			extendedCharsArray[i] = ch;
			ch++;
		}
		ch = 91;
		for (int i = 51; i < 55; i++) {
			extendedCharsArray[i] = ch;
			ch++;
		}
		ch = 123;
		for (int i = 55; i < 59; i++) {
			extendedCharsArray[i] = ch;
			ch++;
		}
		return extendedCharsArray;
	}

	public char[] getTagInValidFormatChars() throws Exception {
		// Tag format is the same as defined for "Resource Name" :
		// Allowed characters: Alphanumeric (a-zA-Z0-9), space (' '), underscore
		// ('_'), dash ('-'), dot ('.')
		char[] notValidCharsArray = new char[30];
		char ch = 33;
		for (int i = 0; i < 12; i++) {
			notValidCharsArray[i] = ch;
			ch++;
		}
		notValidCharsArray[13] = 47;
		ch = 58;
		for (int i = 14; i < 21; i++) {
			notValidCharsArray[i] = ch;
			ch++;
		}
		ch = 91;
		for (int i = 21; i < 24; i++) {
			notValidCharsArray[i] = ch;
			ch++;
		}
		notValidCharsArray[24] = 96;
		ch = 123;
		for (int i = 25; i < 30; i++) {
			notValidCharsArray[i] = ch;
			ch++;
		}
		return notValidCharsArray;
	}

	public void Validation_UpdateWithIncompleteJsonBodyTest() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// define and create resource
		ResourceReqDetails resourceDetails = defineResourse();
		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceDetails.getName(), "0.1");
		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceDetails.getName(), "1.0");
		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceDetails.getName(), "1.1");

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceId = resourceDetails.getUniqueId();
		resourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));

		// build Json Object
		JSONObject jsonObject = JsonObjectBuilder(resourceDetails);

		List<String> resource = new ArrayList<>();
		resource.add("Resource");

		// remove Description
		UpdateAndValidateWithIncompletedJsonBody(sdncModifierDetails, jsonObject, resourceId, "description",
				ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), resource);
		// remove Tags
		UpdateAndValidateWithIncompletedJsonBody(sdncModifierDetails, jsonObject, resourceId, "tags",
				ActionStatus.COMPONENT_MISSING_TAGS.name(), Empty_List);
		// remove Category
		UpdateAndValidateWithIncompletedJsonBody(sdncModifierDetails, jsonObject, resourceId, "category",
				ActionStatus.COMPONENT_MISSING_CATEGORY.name(), resource);
		// remove VendorName
		UpdateAndValidateWithIncompletedJsonBody(sdncModifierDetails, jsonObject, resourceId, "vendorName",
				ActionStatus.MISSING_VENDOR_NAME.name(), Empty_List);
		// remove VendorRelease
		UpdateAndValidateWithIncompletedJsonBody(sdncModifierDetails, jsonObject, resourceId, "vendorRelease",
				ActionStatus.MISSING_VENDOR_RELEASE.name(), Empty_List);
		// remove AT&T Contact
		UpdateAndValidateWithIncompletedJsonBody(sdncModifierDetails, jsonObject, resourceId, "contactId",
				ActionStatus.COMPONENT_MISSING_CONTACT.name(), resource);

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails, "0.1");
		// validate response
		AssertJUnit.assertNotNull("check response object is not null after get resource", getRestResponse);
		AssertJUnit.assertNotNull("check error code exists in response after get resource",
				getRestResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after update resource", 200,
				getRestResponse.getErrorCode().intValue());

		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceDetails.getName(), "0.1");
		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceDetails.getName(), "1.0");
		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, resourceDetails.getName(), "1.1");
	}

	// End of validation tests
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected ResourceReqDetails defineUpdatedResourse(String resourceName) {
		String description = "updatedDescription";
		ArrayList<String> resourceTags = new ArrayList<String>();
		// Duplicate tags are allowed and should be de-duplicated by server side
		resourceTags.add(resourceName);
		resourceTags.add("tag1");
		resourceTags.add("tag1");
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "updatedOracle";
		String vendorRelease = "3.5";
		String contactId = "jh0001";
		String icon = "myUpdatedICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getCategory(),
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getSubCategory());

		return resourceDetails;
	}

	protected ResourceReqDetails defineResourse() {
		String resourceName = "cisco4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());// "tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "objectStorage";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getCategory(),
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getSubCategory());

		return resourceDetails;
	}

	protected RestResponse createResource(User sdncModifierDetails, ResourceReqDetails resourceDetails)
			throws Exception {
		// clean ES DB
		DbUtils.cleanAllAudits();

		// create resource
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		AssertJUnit.assertNotNull("check response object is not null after create resource", restResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create resource",
				restResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create resource", 201,
				restResponse.getErrorCode().intValue());

		return restResponse;
	}

	protected RestResponse TryUpdateByAnotherVerb(ResourceReqDetails updatedResourceDetails, User sdncModifierDetails,
			String uri) throws Exception {
		// delete resource
		Config config;
		RestResponse ResourceResponse;
		try {
			config = Utils.getConfig();
			Map<String, String> headersMap = new HashMap<String, String>();
			headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
			headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
			HttpRequest http = new HttpRequest();
			String url = String.format(Urls.UPDATE_RESOURCE_METADATA, config.getCatalogBeHost(),
					config.getCatalogBePort(), updatedResourceDetails.getName() + ".0.1");

			if (uri == "GET") {
				ResourceResponse = http.httpSendGet(url, headersMap);
			} else if (uri == "POST") {
				Gson gson = new Gson();
				String userBodyJson = gson.toJson(updatedResourceDetails);
				ResourceResponse = http.httpSendPost(url, userBodyJson, headersMap);
			} else if (uri == "DELETE") {
				ResourceResponse = http.httpSendDelete(url, headersMap);
			} else
				return null;

			return ResourceResponse;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;

	}

	protected JSONObject JsonObjectBuilder(ResourceReqDetails resourceDetails) throws JSONException {
		// json object: resourceName and icon are must
		JSONObject jObject = new JSONObject();

		List<String> tagsList = Arrays.asList(resourceDetails.getName());
		List<String> derivedFromList = Arrays.asList("[tosca.nodes.Root]");

		jObject.put("name", resourceDetails.getName());
		jObject.put("description", "updatedDescription");
		jObject.put("tags", tagsList);
		jObject.put("category", ServiceCategoriesEnum.VOIP.getValue());
		jObject.put("derivedFrom", derivedFromList);
		jObject.put("vendorName", "newOracle");
		jObject.put("vendorRelease", "1.5");
		jObject.put("contactId", "jh0003");
		jObject.put("icon", resourceDetails.getIcon());

		return jObject;
	}

	protected JSONObject RemoveFromJsonObject(JSONObject jObject, String removedPropery) {
		jObject.remove(removedPropery);

		return jObject;
	}

	// purpose: function for controlling json body fields and validating
	// response
	protected void UpdateAndValidateWithIncompletedJsonBody(User sdncModifierDetails, JSONObject jsonObject,
			String resourceId, String removedField, String errorMessage, List<String> variables) throws Exception {

		JSONObject jObject = new JSONObject(jsonObject, JSONObject.getNames(jsonObject));
		// remove description from jsonObject
		jObject = RemoveFromJsonObject(jObject, removedField);
		// update with incomplete body.
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(jObject.toString(),
				sdncModifierDetails, resourceId);
		// validate response
		AssertJUnit.assertNotNull("check response object is not null after update resource", updatedRestResponse);
		AssertJUnit.assertNotNull("check error code exists in response after update resource",
				updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(errorMessage, variables, updatedRestResponse.getResponse());

	}

	// purpose: function for validating error response
	protected void UpdateAndValidate(User sdncModifierDetails, ResourceReqDetails resourceDetails,
			String recievedMessage, List<String> variables) throws Exception {
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, "0.1");
		// validate response
		AssertJUnit.assertNotNull("check response object is not null after update resource", updatedRestResponse);
		AssertJUnit.assertNotNull("check error code exists in response after update resource",
				updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(recievedMessage, variables, updatedRestResponse.getResponse());

	}

	protected void parseResponseAndValidateNonUpdatable(ResourceReqDetails resourceDetails, RestResponse restResponse)
			throws Exception {
		// parse response to javaObject
		Resource updatedResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());
		AssertJUnit.assertTrue(
				!resourceDetails.getIsHighestVersion().equals(updatedResourceRespJavaObject.isHighestVersion()));
		AssertJUnit.assertTrue(!resourceDetails.getVersion().equals(updatedResourceRespJavaObject.getName()));
		AssertJUnit.assertTrue(!resourceDetails.getIsAbstract().equals(updatedResourceRespJavaObject.isAbstract()));
		AssertJUnit.assertTrue(
				!resourceDetails.getCreatorUserId().equals(updatedResourceRespJavaObject.getCreatorUserId()));
		AssertJUnit.assertTrue(
				!resourceDetails.getCreatorFullName().equals(updatedResourceRespJavaObject.getCreatorFullName()));
		AssertJUnit.assertTrue(
				!resourceDetails.getLastUpdateDate().equals(updatedResourceRespJavaObject.getLastUpdateDate()));
		AssertJUnit
				.assertTrue(!resourceDetails.getCreationDate().equals(updatedResourceRespJavaObject.getCreationDate()));
		AssertJUnit.assertTrue(
				!resourceDetails.getLastUpdaterUserId().equals(updatedResourceRespJavaObject.getLastUpdaterUserId()));
		AssertJUnit.assertTrue(!resourceDetails.getLastUpdaterFullName()
				.equals(updatedResourceRespJavaObject.getLastUpdaterFullName()));
		AssertJUnit.assertTrue(
				!resourceDetails.getLifecycleState().equals(updatedResourceRespJavaObject.getLifecycleState()));
		AssertJUnit.assertTrue(!resourceDetails.getCost().equals(updatedResourceRespJavaObject.getCost()));
		AssertJUnit
				.assertTrue(!resourceDetails.getLicenseType().equals(updatedResourceRespJavaObject.getLicenseType()));
		AssertJUnit.assertTrue(!resourceDetails.getUUID().equals(updatedResourceRespJavaObject.getUUID()));

	}

	protected void parseResponseAndValidate(ResourceReqDetails ResourceDetails, RestResponse restResponse)
			throws Exception {
		// parse response to javaObject
		Resource updatedResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());
		// validate request vs response
		ResourceValidationUtils.validateResourceReqVsResp(ResourceDetails, updatedResourceRespJavaObject);
	}

	public ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(ResourceReqDetails resourceDetails,
			String resourceVersion) {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();

		expectedResourceAuditJavaObject.setAction("Checkout");
		expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.ADMIN.getUserId());
		expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.ADMIN.getUserName());
		expectedResourceAuditJavaObject.setStatus("200.0");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setResourceName(resourceDetails.getName().toLowerCase());
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion(String.valueOf(Float.parseFloat(resourceVersion) - 0.1f));
		expectedResourceAuditJavaObject.setCurrVersion(resourceVersion);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());

		return expectedResourceAuditJavaObject;

	}

	public enum FieldToValidate {
		contactId, Tags, VendorName, VendorRelease, Description
	}

	@Test
	public void UpdateBy_postTest() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// update resource - without changing resourceName
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);

		RestResponse updatedRestResponse = TryUpdateByAnotherVerb(updatedResourceDetails, sdncModifierDetails, "POST");

		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), Empty_List,
				updatedRestResponse.getResponse());

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(resourceDetails, getRestResponse);

	}

	@Test
	public void UpdateBy_getTest() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// update resource - without changing resourceName
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		RestResponse updatedRestResponse = TryUpdateByAnotherVerb(updatedResourceDetails, sdncModifierDetails, "GET");

		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), Empty_List,
				updatedRestResponse.getResponse());

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(resourceDetails, getRestResponse);

	}

	@Test
	public void UpdateBy_deleteTest() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// update resource - without changing resourceName
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		RestResponse updatedRestResponse = TryUpdateByAnotherVerb(updatedResourceDetails, sdncModifierDetails,
				"DELETE");

		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), Empty_List,
				updatedRestResponse.getResponse());

		RestResponse getRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId(), "");
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(resourceDetails, getRestResponse);

	}

	// TODO DE
	// @Ignore("")
	@Test
	public void UpdateWithInvaldJsonBodyTest() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		resourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));
		String resourceId = resourceDetails.getUniqueId();

		// update Descirption value
		String description = "updatedDescription";

		// send update with incompleted json, only description string
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(description, sdncModifierDetails,
				resourceId);

		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("check error code after update resource", 400, updatedRestResponse.getErrorCode().intValue());

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(resourceDetails, getRestResponse);

	}

	// @Test
	// public void UpdateResourceModifierNotOwnerStateTest() throws Exception {
	//
	//
	// RestResponse restResponse = createResource(sdncModifierDetails,
	// resourceDetails);
	// String resourceName = resourceDetails.getName();
	//
	// // new user parameters
	// String userFirstName = "Kot";
	// String userLastName = "Matroskin";
	// String role = "ADMIN";
	// User sdncUserDetails = new User(userFirstName, userLastName,
	// httpCspUserId, email, role,null);
	// RestResponse deleteUserResponse = userUtils.deleteUser(sdncUserDetails,
	// ElementFactory.getDefaultUser(UserRoleEnum.ADMIN));
	//
	// RestResponse createUserResponse = UserUtils.createUser(sdncUserDetails,
	// ElementFactory.getDefaultUser(UserRoleEnum.ADMIN));
	//
	// User updatedSdncModifierDetails = new User(userFirstName, userLastName,
	// httpCspUserId, email,role,null);
	// ResourceReqDetails updatedResourceDetails =
	// defineUpdatedResourse(resourceName);
	// RestResponse updatedRestResponse =
	// ResourceRestUtils.updateResource(updatedResourceDetails,
	// updatedSdncModifierDetails, resourceDetails.getUniqueId(), "");
	//
	// // validate response
	// assertNotNull("check response object is not null after update resource",
	// updatedRestResponse);
	// assertNotNull("check error code exists in response after update
	// resource", updatedRestResponse.getErrorCode());
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(),
	// Empty_List, updatedRestResponse.getResponse());
	//
	// RestResponse getRestResponse =
	// ResourceRestUtils.getResource(sdncModifierDetails,
	// resourceDetails.getUniqueId());
	// assertNotNull("check response object is not null after update resource",
	// getRestResponse);
	// parseResponseAndValidate(resourceDetails, getRestResponse);
	//
	//
	// }

	@Test
	public void UpdateResourceNameSensitiveTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";
		String resourceName = "Ab";
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		// Delete resources
		RestResponse response = null;
		response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails,
				updatedResourceDetails.getName(), "0.1");
		BaseRestUtils.checkDeleteResponse(response);
		response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails,
				updatedResourceDetails.getName(), "0.2");
		BaseRestUtils.checkDeleteResponse(response);

		RestResponse restResponse = createResource(sdncModifierDetails, updatedResourceDetails);
		assertEquals("create resource failed", 201, restResponse.getErrorCode().intValue());

		// check-in Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(updatedResourceDetails,
				sdncModifierDetails, resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// String resourceCertifyVersion = "0.1";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(updatedResourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		updatedResourceDetails.setName("ABC_-bt.aT");
		ArrayList<String> resourceTag = new ArrayList<String>();
		resourceTag.add(0, "ABC_-bt.aT");
		updatedResourceDetails.setTags(resourceTag);
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, updatedResourceDetails.getUniqueId(), "");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(updatedResourceDetails, updatedRestResponse);

		// Delete resources
		response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails,
				updatedResourceDetails.getName(), "0.1");
		BaseRestUtils.checkDeleteResponse(response);
		response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails,
				updatedResourceDetails.getName(), "0.2");
		BaseRestUtils.checkDeleteResponse(response);

	}

	@Test
	public void UpdateIcon_InegativeFlow() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		List<String> resourceList = new ArrayList<String>();
		resourceList.add(0, "Resource");
		// check InValid Characters
		char[] notValidCharsArray = new char[59];
		notValidCharsArray = getInValidChars();
		// update metadata details
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		RestResponse updatedRestResponse;

		for (int i = 0; i < notValidCharsArray.length; i++) {
			// change icon of metadata
			updatedResourceDetails.setIcon("MyIcon" + notValidCharsArray[i]);
			// PUT request
			updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
					resourceDetails.getUniqueId(), "");
			// validate response
			assertNotNull("check response object is not null after update resource", updatedRestResponse);
			assertNotNull("check error code exists in response after update resource",
					updatedRestResponse.getErrorCode());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_ICON.name(), resourceList,
					updatedRestResponse.getResponse());
			assertEquals("Check response code after updating resource icon", 400,
					updatedRestResponse.getErrorCode().intValue());
			assertEquals("Check response code after updating resource icon", "Bad Request",
					updatedRestResponse.getResponseMessage().toString());

		}

		// empty icon
		String updateIcon = "";
		updatedResourceDetails.setIcon(updateIcon);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId(), "");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_ICON.name(), resourceList,
				updatedRestResponse.getResponse());

		// Icon length more then 25 characters
		resourceList.add(1, "25");
		updatedResourceDetails.setIcon("1234567890_-qwertyuiopASDNNN");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId(), "");
		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT.name(), resourceList,
				updatedRestResponse.getResponse());
		assertEquals("Check response code after create resource", 400, updatedRestResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating resource icon", "Bad Request",
				updatedRestResponse.getResponseMessage().toString());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());

		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResource_NoTagsEqualToResourceName() throws Exception {

		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		ResourceReqDetails updatedResourceDetails = defineResourse();
		updatedResourceDetails.setName("updatedResourceName");
		List<String> tags = updatedResourceDetails.getTags();

		for (Iterator<String> iter = tags.listIterator(); iter.hasNext();) {
			String a = iter.next();
			if (a.equals("updatedResourceName")) {
				iter.remove();
			}
		}

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		List<String> resourceList = new ArrayList<String>();
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME.name(),
				resourceList, updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource icon", 400,
				updatedRestResponse.getErrorCode().intValue());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());
		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResourceName_negativeFlow() throws Exception {
		// The validation are done in Tag's validation
		User sdncAdminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		RestResponse updatedRestResponse;
		RestResponse restResponse = createResource(sdncAdminModifierDetails, resourceDetails);
		assertEquals("create resource failed", 201, restResponse.getErrorCode().intValue());
		String uniqueId = resourceDetails.getUniqueId();
		String resourceName = resourceDetails.getName();
		// check InValid Characters
		char[] notValidCharsArray = new char[59];
		notValidCharsArray = getInValidChars();
		ArrayList<String> resource_Name = new ArrayList<String>();
		List<String> resourceList = new ArrayList<String>();

		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceList.add(0, "Resource");

		// update metadata details
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		for (int i = 0; i < notValidCharsArray.length; i++, resource_Name.clear()) {
			if (i != 1 && i != 46
					&& /*
						 * i != 8 && i != 9 && i != 10 && i != 11 && i != 12 &&
						 */ i != 31) // space ("") and dot(.)
			{
				// change resourceName parameter
				updatedResourceDetails.setName("UpdatedResourceName" + notValidCharsArray[i]);
				resource_Name.add("UpdatedResourceName" + notValidCharsArray[i]);
				updatedResourceDetails.setTags(resource_Name);
				updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
						sdncAdminModifierDetails, uniqueId, "");
				// validate response
				// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_TAG.name(),
				// Empty_List, updatedRestResponse.getResponse());
				ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPONENT_NAME.name(), resourceList,
						updatedRestResponse.getResponse());

			}
		}

		// resourceName length more then 50 characters
		// Duplicate tags are allowed and should be de-duplicated by server side
		resource_Name.add(resourceName);
		resource_Name.add("tag1");
		resource_Name.add("tag1");
		resource_Name.add("tag2");
		resource_Name.add("tag2");

		resourceList.add(1, "1024");
		// updatedResourceDetails.setName("123456789012345678901234567890123456789012345678901");
		updatedResourceDetails.setName(new String(new char[1025]).replace("\0", "a"));
		// resource_Name.add("123456789012345678901234567890123456789012345678901");
		updatedResourceDetails.setTags(resource_Name);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncAdminModifierDetails,
				uniqueId, "");
		// validate response
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT.name(), resourceList,
				updatedRestResponse.getResponse());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncAdminModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());
		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

		// delete resource
		RestResponse response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncAdminModifierDetails,
				updatedResourceDetails.getName(), "0.1");
		BaseRestUtils.checkDeleteResponse(response);
	}

	@Test
	public void UpdateResourceInformation_NotCheckedOut() throws Exception {

		String resourceBaseVersion = "0.1";
		List<String> resourceList = new ArrayList<String>();

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// CheckIn Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN); // NOT_CERTIFIED_CHECKIN
		assertNotNull("check response object is not null after checkout resource", checkoutResource);
		assertNotNull("check error code exists in response after checkIn resource", checkoutResource.getErrorCode());
		assertEquals("Check response code after checkin resource", 200, checkoutResource.getErrorCode().intValue());

		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), resourceList,
				updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource icon", 409,
				updatedRestResponse.getErrorCode().intValue());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());

		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResourceInformation_resourceVersion_11() throws Exception {

		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		List<String> resourceList = new ArrayList<String>();
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), resourceList,
				updatedRestResponse.getResponse());
		// assertEquals("Check response code after updating resource icon", 409,
		// updatedRestResponse.getErrorCode().intValue());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);

		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());

		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());
		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResourceInformation_resourceVersion_02() throws Exception {

		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// String resourceCertifyVersion = "0.1";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after updating resource icon", 200,
				updatedRestResponse.getErrorCode().intValue());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());

		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(updatedResourceDetails, getResourceRespJavaObject);

		// delete resource
		RestResponse response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails,
				updatedResourceDetails.getName(), "0.1");
		BaseRestUtils.checkDeleteResponse(response);
		response = ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails,
				updatedResourceDetails.getName(), "0.2");
		BaseRestUtils.checkDeleteResponse(response);

	}

	@Test
	public void UpdateResourceIcon_resourceVersion_11() throws Exception {
		// Can be changed only if major version is "0".

		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceCertifyVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// ResourceReqDetails updatedResourceDetails =
		// defineUpdatedResourse(resourceName);
		ResourceReqDetails updatedResourceDetails = defineResourse();
		// updatedResourceDetails.setVendorName("updatedVandorName");
		updatedResourceDetails.setIcon("updatedIcon");

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		List<String> resourceList = new ArrayList<String>();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_ICON_CANNOT_BE_CHANGED.name(), resourceList,
				updatedRestResponse.getResponse());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());
		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResourceVandorName_resourceVersion_11() throws Exception {
		// Can be changed only if the major resource version is "0".
		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceCertifyVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// ResourceReqDetails updatedResourceDetails =
		// defineUpdatedResourse(resourceName);
		ResourceReqDetails updatedResourceDetails = defineResourse();

		updatedResourceDetails.setVendorName("updatedVandorName");

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		List<String> resourceList = new ArrayList<String>();
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_VENDOR_NAME_CANNOT_BE_CHANGED.name(),
				resourceList, updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource icon", 400,
				updatedRestResponse.getErrorCode().intValue());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());
		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResourceName_resourceVersion_11() throws Exception {
		// Can be changed only if the major resource version is "0".
		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("create resource failed", 201, restResponse.getErrorCode().intValue());
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceCertifyVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		/*
		 * //ResourceReqDetails updatedResourceDetails =
		 * defineUpdatedResourse(resourceName); ResourceReqDetails
		 * updatedResourceDetails = defineResourse();
		 * 
		 * updatedResourceDetails.setResourceName("updatedResourceName");
		 * updatedResourceDetails.setIcon("updatedResourceName");
		 */
		resourceDetails.setName("updatedResourceName");
		List<String> tagList = new ArrayList<String>();
		tagList.add(0, "updatedResourceName");
		resourceDetails.setTags(tagList);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		List<String> resourceList = new ArrayList<String>();
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED.name(), resourceList,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResourceTag_resourceVersion_11() throws Exception {
		// Tag Can be updated when major version is "0".
		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceCertifyVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// ResourceReqDetails updatedResourceDetails =
		// defineUpdatedResourse(resourceName);
		ResourceReqDetails updatedResourceDetails = defineResourse();
		// updatedResourceDetails.setVendorName("updatedVandorName");

		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add("NewTag");
		resourceTags.add(resourceDetails.getName());

		updatedResourceDetails.setTags(resourceTags);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(updatedResourceDetails, getRestResponse);

	}

	@Test
	public void UpdateAllowedParames_resourceVersion_11() throws Exception {

		// Tag, contactId, vendorRelease,tags And description - Can be also
		// updated when major version is NOT "0".
		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceCertifyVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// ResourceReqDetails updatedResourceDetails =
		// defineUpdatedResourse(resourceName);
		ResourceReqDetails updatedResourceDetails = defineResourse();
		// updatedResourceDetails.setVendorName("updatedVandorName");

		// updated allowed parameters when major resource version is NOT "0"
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add("NewTag");
		resourceTags.add(resourceDetails.getName());
		updatedResourceDetails.setTags(resourceTags);
		updatedResourceDetails.setDescription("UpdatedDescription");
		updatedResourceDetails.setVendorRelease("5.1");
		updatedResourceDetails.setContactId("bt750h");

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(updatedResourceDetails, getRestResponse);

	}

	@Test
	public void UpdateResourceDerivedFrom_resourceVersion_11() throws Exception {
		// DerivedFrom parameter - Can be updated when major version is "0".
		User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		String resourceBaseVersion = "0.1";

		// create resource
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// restResponse);

		// Certify Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceBaseVersion, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		String resourceCertifyVersion = "1.0";
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceCertifyVersion, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// ResourceReqDetails updatedResourceDetails =
		// defineUpdatedResourse(resourceName);
		ResourceReqDetails updatedResourceDetails = defineResourse();
		ArrayList<String> drivenFrom = new ArrayList<String>();
		drivenFrom.add(0, "tosca.nodes.Container.Application");
		updatedResourceDetails.setDerivedFrom(drivenFrom);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");
		// validate response
		List<String> resourceList = new ArrayList<String>();
		ResourceRestUtils.checkSuccess(updatedRestResponse);

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());
		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

	}

	@Test
	public void UpdateResource_vendorNameValidation() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		String updatedVendorName = "";
		String uniqueId = resourceDetails.getUniqueId();
		resourceDetails.setVendorName(updatedVendorName);
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, uniqueId, "");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		// update resource vendorName metadata: 1 characters
		updatedVendorName = "	";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		// update resource vendorName metadata: 25 characters
		updatedVendorName = "Verification and validati";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// update resource vendorName metadata: 26 characters
		updatedVendorName = "Verification and validatii";
		// set vendorName
		List<String> myList = new ArrayList<String>();
		myList.add(0, "25");
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT.name(), myList,
				updatedRestResponse.getResponse());

		// update resource VendorRelease metadata: forbidden characters
		updatedVendorName = "A1<";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1>";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1:";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1\"";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1/";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1\\";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1|";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1?";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorName = "A1*";
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

		// update resource vendorName metadata: null
		updatedVendorName = null;
		// set vendorName
		resourceDetails.setVendorName(updatedVendorName);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_NAME.name(), Empty_List,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResource_vendorReleaseValidation() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		RestResponse updatedRestResponse;
		String uniqueId = resourceDetails.getUniqueId();
		String updatedVendorRelease;
		// set VendorRelease

		// update resource VendorRelease metadata: 1 characters
		updatedVendorRelease = "1";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// update resource VendorRelease metadata: 25 characters
		updatedVendorRelease = "(!#1.00000000000000000000";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// update resource VendorRelease metadata: 26 characters
		updatedVendorRelease = "(!#1.000000000000000000005";// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT.name(),
				Arrays.asList("" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH), updatedRestResponse.getResponse());

		// UpdateAndValidate(sdncModifierDetails, resourceDetails,
		// ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT.name(),
		// Arrays.asList(""+ValidationUtils.VENDOR_RELEASE_MAX_LENGTH));

		// update resource VendorRelease metadata: forbidden characters
		updatedVendorRelease = "A1<";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1>";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1:";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1\"";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1/";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1\\";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1|";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1?";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		updatedVendorRelease = "A1*";
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

		// update resource VendorRelease metadata: null
		updatedVendorRelease = null;
		// set VendorRelease
		resourceDetails.setVendorRelease(updatedVendorRelease);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_RELEASE.name(), Empty_List,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResource_contactIdValidation() throws Exception { // [a-zA-Z]{2}[0-9]{3}[a-zA-Z0-9]{1}
																			// (6
																			// characters
																			// now,
																			// may
																			// be
																			// expanded
																			// up
																			// to
																			// 8
																			// characters
																			// in
																			// the
																			// future).
																			// Convert
																			// Upper
																			// case
																			// character
																			// to
																			// lower
																			// case
		RestResponse updatedRestResponse;

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		String uniqueId = resourceDetails.getUniqueId();

		List<String> myList = new ArrayList<String>();
		myList.add(0, "Resource");
		String updatedcontactId = "";
		resourceDetails.setContactId(updatedcontactId);

		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "ab12345";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "      ";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "ab 50h";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "ab123c";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		updatedcontactId = "cd789E";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());

		resourceDetails.setContactId(updatedcontactId.toLowerCase());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		updatedcontactId = "ef4567";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		updatedcontactId = "AA012A";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());

		resourceDetails.setContactId(updatedcontactId.toLowerCase());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		updatedcontactId = "CD012c";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());

		resourceDetails.setContactId(updatedcontactId.toLowerCase());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		updatedcontactId = "EF0123";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());

		resourceDetails.setContactId(updatedcontactId.toLowerCase());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		////////////////////////////// **************//////////////////////////////
		List<String> resource = Arrays.asList("Resource");
		updatedcontactId = "01345a";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "0y000B";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "Y1000b";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "abxyzC";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "cdXYZc";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "efXY1D";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "EFabcD";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "EFABCD";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "EFABC1";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "efui1D";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "efui1!";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "ef555!";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = ",f555";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		updatedcontactId = "EF55.5";
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		// update resource contactId metadata: extended character set (128–255)
		resourceDetails.setContactId(extendedCharsStringBuilder());
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

		// update resource contactId metadata: null
		updatedcontactId = null;
		resourceDetails.setContactId(updatedcontactId);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CONTACT.name(), myList,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResource_TagsFieldValidation() throws Exception {
		RestResponse updatedRestResponse;
		// define and create resource

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		String uniqueId = resourceDetails.getUniqueId();

		String updatedTagField = "";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(updatedTagField);
		// set description
		resourceDetails.setTags(resourceTags);
		List<String> variables = Arrays.asList("Resource", "tag");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FIELD_FORMAT.name(), variables,
				updatedRestResponse.getResponse());

		// update resource tags metadata: empty
		resourceTags = new ArrayList<String>();
		// set Tags
		resourceDetails.setTags(resourceTags);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_TAGS.name(), Empty_List,
				updatedRestResponse.getResponse());

		// update resource description metadata: 1 characters
		updatedTagField = "A";
		resourceTags = new ArrayList<String>();
		resourceTags.add(updatedTagField);
		resourceTags.add(resourceDetails.getName());
		// set description
		resourceDetails.setTags(resourceTags);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// OK - tag up to 50 chars
		updatedTagField = "The Indian-crested.porcupine_The Indian cresteddds";
		resourceTags.add(updatedTagField);
		resourceDetails.setTags(resourceTags);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// OK - sum is 1024, 50x20+48+20(commas)+6(cisco4 - resource name)
		String updatedTagField1 = "The Indian-crested.porcupine_The Indian crestedd01";
		String updatedTagField2 = "The Indian-crested.porcupine_The Indian crestedd02";
		String updatedTagField3 = "The Indian-crested.porcupine_The Indian crestedd03";
		String updatedTagField4 = "The Indian-crested.porcupine_The Indian crestedd04";
		String updatedTagField5 = "The Indian-crested.porcupine_The Indian crestedd05";
		String updatedTagField6 = "The Indian-crested.porcupine_The Indian crestedd06";
		String updatedTagField7 = "The Indian-crested.porcupine_The Indian crestedd07";
		String updatedTagField8 = "The Indian-crested.porcupine_The Indian crestedd08";
		String updatedTagField9 = "The Indian-crested.porcupine_The Indian crestedd09";
		String updatedTagField10 = "The Indian-crested.porcupine_The Indian crestedd10";
		String updatedTagField11 = "The Indian-crested.porcupine_The Indian crestedd11";
		String updatedTagField12 = "The Indian-crested.porcupine_The Indian crestedd12";
		String updatedTagField13 = "The Indian-crested.porcupine_The Indian crestedd13";
		String updatedTagField14 = "The Indian-crested.porcupine_The Indian crestedd14";
		String updatedTagField15 = "The Indian-crested.porcupine_The Indian crestedd15";
		String updatedTagField16 = "The Indian-crested.porcupine_The Indian crestedd16";
		String updatedTagField17 = "The Indian-crested.porcupine_The Indian crestedd17";
		String updatedTagField18 = "The Indian-crested.porcupine_The Indian crestedd18";
		String updatedTagField19 = "The Indian-crested.porcupine_The Indian crestaa";

		resourceTags = new ArrayList<String>();
		resourceTags.add(updatedTagField);
		resourceTags.add(updatedTagField1);
		resourceTags.add(updatedTagField2);
		resourceTags.add(updatedTagField3);
		resourceTags.add(updatedTagField4);
		resourceTags.add(updatedTagField5);
		resourceTags.add(updatedTagField6);
		resourceTags.add(updatedTagField7);
		resourceTags.add(updatedTagField8);
		resourceTags.add(updatedTagField9);
		resourceTags.add(updatedTagField10);
		resourceTags.add(updatedTagField11);
		resourceTags.add(updatedTagField12);
		resourceTags.add(updatedTagField13);
		resourceTags.add(updatedTagField14);
		resourceTags.add(updatedTagField15);
		resourceTags.add(updatedTagField16);
		resourceTags.add(updatedTagField17);
		resourceTags.add(updatedTagField18);
		resourceTags.add(updatedTagField19);
		resourceTags.add(resourceDetails.getName());
		// set description
		resourceDetails.setTags(resourceTags);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// Add another tag-exceeds limit
		resourceTags.add("d");
		resourceDetails.setTags(resourceTags);
		ArrayList<String> myArray = new ArrayList<String>();
		myArray.add(0, "1024");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT.name(), myArray,
				updatedRestResponse.getResponse());

		// Tag exceeds limit - 51
		resourceTags = new ArrayList<String>();
		updatedTagField = "The Indian-crested.porcupine_The Indian crestedddsw";
		resourceTags.add(updatedTagField);
		resourceTags.add(resourceDetails.getName());
		// set description
		resourceDetails.setTags(resourceTags);
		myArray.remove(0);
		myArray.add(0, "50");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT.name(), myArray,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResource_DesriptionFieldValidation() throws Exception {
		// define and create resource
		RestResponse updatedRestResponse;

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		String uniqueId = resourceDetails.getUniqueId();
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		List<String> resource = new ArrayList<>();
		resource.add("Resource");
		// update resource description metadata: 0 characters
		String updatedDescription = "";
		// set description
		resourceDetails.setDescription(updatedDescription);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), resource,
				updatedRestResponse.getResponse());

		// update resource description metadata: null
		updatedDescription = null;
		// set description
		resourceDetails.setDescription(updatedDescription);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), resource,
				updatedRestResponse.getResponse());

		// update resource description metadata: 1 characters
		updatedDescription = "A";
		// set description
		resourceDetails.setDescription(updatedDescription);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// update resource description metadata: 1024 characters
		updatedDescription = "The Indian crested porcupine *{Hystrix indica}*, or Indian porcupine is a member of the Old World porcupines."
				+ "It is quite an adaptable rodent, found throughout southern Asia and the Middle East."
				+ "It is tolerant of several different habitats: mountains, tropical and subtropical grasslands, scrublands, and forests."
				+ "It is a large rodent, growing more than 0.9 m = (3 ft) long and weighing 14.5 kg = (32 lb)! [citation needed] It is covered in multiple layers of quills."
				+ "The longest quills grow from its shoulders to about a third of the animal's length."
				+ "Its tail is covered in short, hollow quills that can rattle when threatened."
				+ "It has broad feet and long claws for digging. When attacked, the Indian crested porcupine raises its quills and rattles the hollow quills on its tail."
				+ "If the predator persists past these threats, the porcupine launches a backwards assault, hoping to stab its attacker with its quills."
				+ "It does this so effectively that most brushes between predators and the Indian porcupine end in death or severe injury";
		// set description
		resourceDetails.setDescription(updatedDescription);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		parseResponseAndValidate(resourceDetails, updatedRestResponse);

		// update resource description metadata: 1025 characters
		updatedDescription = "The Indian crested porcupine *{Hystrix indica}*, or Indian porcupine is a member of the Old World porcupines."
				+ "It is quite an adaptable rodent, found throughout southern Asia and the Middle East."
				+ "It is tolerant of several different habitats: mountains, tropical and subtropical grasslands, scrublands, and forests."
				+ "It is a large rodent, growing more than 0.9 m = (3 ft) long and weighing 14.5 kg = (32 lb)! [citation needed] It is covered in multiple layers of quills."
				+ "The longest quills grow from its shoulders to about a third of the animal's length."
				+ "Its tail is covered in short, hollow quills that can rattle when threatened."
				+ "It has broad feet and long claws for digging. When attacked, the Indian crested porcupine raises its quills and rattles the hollow quills on its tail."
				+ "If the predator persists past these threats, the porcupine launches a backwards assault, hoping to stab its attacker with its quills."
				+ "It does this so effectively that most brushes between predators and the Indian porcupine end in death or severe injury.";
		// set description
		resourceDetails.setDescription(updatedDescription);
		resource.add(1, "1024");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncModifierDetails, uniqueId,
				"");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT.name(), resource,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResource_TagsFormatValidation() throws Exception {
		char[] notValidCharsArray = getTagInValidFormatChars();

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check recourse created ", 201, restResponse.getErrorCode().intValue());
		String resourceName = resourceDetails.getName();

		// update tag details
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		ArrayList<String> resourceTags = new ArrayList<String>();

		String updatedTagField;
		RestResponse updatedRestResponse;
		List<String> variables = Arrays.asList("Resource", "tag");

		for (int i = 0; i < notValidCharsArray.length; i++) {
			updatedTagField = "UpdatedTag" + notValidCharsArray[i];
			resourceTags = new ArrayList<String>();
			resourceTags.add(updatedTagField);
			resourceTags.add(resourceDetails.getName());
			// set description
			updatedResourceDetails.setTags(resourceTags);

			updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
					resourceDetails.getUniqueId(), "");
			// validate response
			assertNotNull("check response object is not null after update resource", updatedRestResponse);
			assertNotNull("check error code exists in response after update resource",
					updatedRestResponse.getErrorCode());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FIELD_FORMAT.name(), variables,
					updatedRestResponse.getResponse());
			assertEquals("Check response code after updating resource icon", 400,
					updatedRestResponse.getErrorCode().intValue());
			assertEquals("Check response code after updating resource icon", "Bad Request",
					updatedRestResponse.getResponseMessage().toString());

		}

	}

	@Test
	public void UpdateResourceCategory_negativeFlow() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after update resource", 201, restResponse.getErrorCode().intValue());
		Resource resourceBeforeUpdate = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		String uniqueID = resourceDetails.getUniqueId();

		// Update resource Category Successfully
		ResourceReqDetails updatedResourceDetails = resourceDetails;

		updatedResourceDetails.removeAllCategories();
		updatedResourceDetails.addCategoryChain(ServiceCategoriesEnum.MOBILITY.getValue(),
				ResourceCategoryEnum.APPLICATION_L4_DATABASE.getSubCategory());
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");

		// validate response
		List<String> resourceList = new ArrayList<String>();
		resourceList.add(0, "Resource");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CATEGORY.name(), resourceList,
				updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource", 400, updatedRestResponse.getErrorCode().intValue());

		// Updating resource category
		updatedResourceDetails = defineUpdateResourceWithNonUpdatableFields(resourceBeforeUpdate);
		updatedResourceDetails.addCategory("");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId(), "");
		// validate response
		resourceList = new ArrayList<String>();
		resourceList.add(0, "Resource");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CATEGORY.name(), resourceList,
				updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource", 400, updatedRestResponse.getErrorCode().intValue());

		// Updating resource category
		updatedResourceDetails = defineUpdateResourceWithNonUpdatableFields(resourceBeforeUpdate);
		updatedResourceDetails.addCategory("XXXXXX");
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId(), "");
		// validate response
		resourceList = new ArrayList<String>();
		resourceList.add(0, "Resource");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CATEGORY.name(), resourceList,
				updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource", 400, updatedRestResponse.getErrorCode().intValue());

		// CheckIn Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN); // NOT_CERTIFIED_CHECKIN
		assertEquals("Check response code after checkin resource", 200, checkoutResource.getErrorCode().intValue());

		// Update resource Category
		updatedResourceDetails = defineUpdateResourceWithNonUpdatableFields(resourceBeforeUpdate);
		updatedResourceDetails.addCategory(ServiceCategoriesEnum.VOIP.getValue());
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId(), "");
		// verify response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), Empty_List,
				updatedRestResponse.getResponse());
		assertEquals("Check response code after updating resource", 409, updatedRestResponse.getErrorCode().intValue());

		// CheckIn Resource
		logger.debug("Changing resource life cycle ");
		RestResponse checkinResource = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT); // NOT_CERTIFIED_CHECKIN
		assertNotNull("check response object is not null after checkout resource", checkoutResource);
		assertNotNull("check error code exists in response after checkIn resource", checkoutResource.getErrorCode());
		assertEquals("Check response code after checkin resource", 200, checkoutResource.getErrorCode().intValue());

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails, uniqueID);
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(resourceDetails, getRestResponse);

	}

	@Test
	public void UpdateResourceCategorySuccessfully() throws Exception {

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after update resource", 201, restResponse.getErrorCode().intValue());
		Resource resourceBeforeUpdate = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());

		// Update resource Category Successfully
		ResourceReqDetails updatedResourceDetails = resourceDetails;

		updatedResourceDetails.removeAllCategories();
		updatedResourceDetails.addCategoryChain(ResourceCategoryEnum.APPLICATION_L4_DATABASE.getCategory(),
				ResourceCategoryEnum.APPLICATION_L4_DATABASE.getSubCategory());
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncModifierDetails, resourceDetails.getUniqueId(), "");

		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, updatedRestResponse.getErrorCode().intValue());
		// parseResponseAndValidateNonUpdatable(updatedResourceDetails,
		// updatedRestResponse);
		parseResponseAndValidate(updatedResourceDetails, updatedRestResponse);

		// validate category updated
		assertTrue(updatedResourceDetails.getCategories().get(0).getName()
				.equals(ResourceCategoryEnum.APPLICATION_L4_DATABASE.getCategory()));

		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertNotNull("check response object is not null after update resource", getRestResponse);
		parseResponseAndValidate(updatedResourceDetails, getRestResponse);

		ResourceRestUtils.deleteResourceByNameAndVersion(sdncModifierDetails, updatedResourceDetails.getName(), "0.1");
	}

	// Benny

	@Test
	public void Validation_UpdateIcon() throws Exception {
		// Fields to update (Forbidden)
		String _updatedIcon = "mySecondIcon.Jpg";

		// administrator permissions
		User sdncAdminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// define and create resource
		ResourceRestUtils.deleteResourceByNameAndVersion(sdncAdminModifierDetails, resourceDetails.getName(), "0.1");

		RestResponse restResponse = createResource(sdncAdminModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		String resourceName = resourceDetails.getName();

		// update metadata details
		ResourceReqDetails updatedResourceDetails = defineUpdatedResourse(resourceName);
		// change icon of metadata
		updatedResourceDetails.setIcon(_updatedIcon);
		// PUT request
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails,
				sdncAdminModifierDetails, resourceDetails.getUniqueId(), "");

		// validate response
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_ICON.name(),
				Arrays.asList("Resource"), updatedRestResponse.getResponse());

		// empty icon
		_updatedIcon = "";
		updatedResourceDetails.setIcon(_updatedIcon);
		updatedRestResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceDetails, sdncAdminModifierDetails,
				resourceDetails.getUniqueId(), "");
		assertNotNull("check response object is not null after update resource", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_ICON.name(),
				Arrays.asList("Resource"), updatedRestResponse.getResponse());

		// get resource with original name. original metadata should be returned
		RestResponse getRestResponse = ResourceRestUtils.getResource(sdncAdminModifierDetails,
				resourceDetails.getUniqueId());
		// validate response
		assertNotNull("check response object is not null after get resource", getRestResponse);
		assertNotNull("check error code exists in response after get resource", getRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", 200, getRestResponse.getErrorCode().intValue());

		// parse updated response to javaObject
		Resource getResourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(getRestResponse.getResponse());
		// validate that metadata was not changed
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails, getResourceRespJavaObject);

		ResourceRestUtils.deleteResourceByNameAndVersion(sdncAdminModifierDetails, updatedResourceDetails.getName(),
				"0.1");

	}

	@Test
	public void UpdateResourceTypeSuccess() throws Exception {
		// LCS is CheckOut
		String newResourceType = ResourceTypeEnum.VL.toString();
		String currentResourceType = resourceDetails.getResourceType();
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		Resource currentResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		resourceDetails.setResourceType(newResourceType);
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResourceJavaObject.getUniqueId(), "");
		assertEquals("Check response code after create resource", 200, updatedRestResponse.getErrorCode().intValue());
		Resource updatedResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(updatedRestResponse.getResponse());
		// assertTrue("Check resource type after update resource",
		// updatedResourceJavaObject.getResourceType().toString().equals(resourceType));
		assertTrue("Check resource type after update resource",
				updatedResourceJavaObject.getResourceType().toString().equals(currentResourceType));

	}

	@Test
	public void UpdateResourceTypeAndNameSuccess() throws Exception {
		// LCS is CheckOut
		String newResourceType = ResourceTypeEnum.VL.toString();
		String currentResourceType = resourceDetails.getResourceType();
		String newResourceName = "new Name";

		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		Resource currentResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		resourceDetails.setResourceType(newResourceType);
		resourceDetails.setName(newResourceName);
		List<String> tags = resourceDetails.getTags();
		tags.add(newResourceName);
		resourceDetails.setTags(tags);

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResourceJavaObject.getUniqueId(), "");
		assertEquals("Check response code after create resource", 200, updatedRestResponse.getErrorCode().intValue());
		Resource updatedResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(updatedRestResponse.getResponse());
		assertTrue("Check resource type after update resource",
				updatedResourceJavaObject.getResourceType().toString().equals(currentResourceType));
		assertTrue("Check resource name after update resource",
				updatedResourceJavaObject.getName().equals(newResourceName));

	}

	@Test
	public void UpdateResourceTypeAfterResourceCertification() throws Exception {

		String newResourceType = ResourceTypeEnum.VF.toString();
		String currentResourceType = resourceDetails.getResourceType();
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		Resource currentResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		resourceDetails.setResourceType(newResourceType);
		restResponse = LifecycleRestUtils.certifyResource(resourceDetails);
		assertEquals("Check response code after resource CheckIn", 200, restResponse.getErrorCode().intValue());
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after resource CheckIn", 200, restResponse.getErrorCode().intValue());
		currentResourceJavaObject = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResourceJavaObject.getUniqueId(), "");
		assertEquals("Check response code after create resource", 200, updatedRestResponse.getErrorCode().intValue());
		Resource updatedResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(updatedRestResponse.getResponse());
		// assertTrue("Check resource type after update resource",
		// updatedResourceJavaObject.getResourceType().toString().equals(newResourceType));
		assertTrue("Check resource type after update resource",
				updatedResourceJavaObject.getResourceType().toString().equals(currentResourceType));

	}

	@Test
	public void UpdateResourceTypeCheckInLCS() throws Exception {

		String resourceType = ResourceTypeEnum.VL.toString();
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		Resource currentResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		resourceDetails.setResourceType(resourceType);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after resource CheckIn", 200, restResponse.getErrorCode().intValue());

		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResourceJavaObject.getUniqueId(), "");

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());

		assertNotNull("check response object is not null after create resouce", updatedRestResponse);
		assertNotNull("check error code exists in response after create resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after create resource", errorInfo.getCode(),
				updatedRestResponse.getErrorCode());

		List<String> variables = new ArrayList<>();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResourceTypeCertifiedLCS() throws Exception {

		String resourceType = ResourceTypeEnum.VL.toString();
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		Resource currentResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		restResponse = LifecycleRestUtils.certifyResource(resourceDetails);
		assertEquals("Check response code after resource CheckIn", 200, restResponse.getErrorCode().intValue());

		resourceDetails.setResourceType(resourceType);
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResourceJavaObject.getUniqueId(), "");

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());

		assertNotNull("check response object is not null after create resouce", updatedRestResponse);
		assertNotNull("check error code exists in response after create resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after create resource", errorInfo.getCode(),
				updatedRestResponse.getErrorCode());

		List<String> variables = new ArrayList<>();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				updatedRestResponse.getResponse());

	}

	@Test
	public void UpdateResourceTypeInvalidType() throws Exception {

		String resourceType = "INVALID TYPE";
		RestResponse restResponse = createResource(sdncModifierDetails, resourceDetails);
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());
		Resource currentResourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		resourceDetails.setResourceType(resourceType);
		RestResponse updatedRestResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails,
				sdncModifierDetails, currentResourceJavaObject.getUniqueId(), "");

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT.name());

		assertNotNull("check response object is not null after update resouce", updatedRestResponse);
		assertNotNull("check error code exists in response after update resource", updatedRestResponse.getErrorCode());
		assertEquals("Check response code after update resource", errorInfo.getCode(),
				updatedRestResponse.getErrorCode());

		List<String> variables = new ArrayList<>();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), variables,
				updatedRestResponse.getResponse());

	}
}
