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

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.preRequisites.SimpleOneRsrcOneServiceTest;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.UserRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PropertyApisTest extends SimpleOneRsrcOneServiceTest {

	protected static final String RESOURCE_CATEGORY = "Generic/Databases";
	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";;

	// protected User sdncDesignerDetails;
	// protected ResourceReqDetails resourceDetails;
	protected PropertyReqDetails property;
	protected String body;

	protected HttpRequest httpRequest = new HttpRequest();
	protected Map<String, String> headersMap = new HashMap<String, String>();

	@Rule
	public static TestName testName = new TestName();

	public PropertyApisTest() {
		super(testName, PropertyApisTest.class.getName());
	}

	@BeforeMethod
	public void init() throws Exception {
		//
		// //Delete resource
		//
		// resourceDetails = new ResourceReqDetails();
		// resourceDetails.setResourceName("testresourceDetails");
		//
		// resourceUtils.deleteResource_allVersions(resourceDetails,
		// sdncDesignerDetails);
		//
		// //Create resource
		// resourceDetails = createResource(sdncDesignerDetails,
		// "testresourceDetails");

		// Create property
		// property.setPropertyName("test");
		// property.setPropertyType("integer");
		// property.setPropertySource("A&AI");
		// property.setPropertyDescription("test property");

		// body = gson.toJson(property);
		property = ElementFactory.getDefaultProperty();
		body = property.propertyToJsonString();
		// System.out.println(body);
		// HTTP (for negative tests)
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncDesignerDetails.getUserId());

	}

	@Test
	public void testPropertyApis() throws Exception {
		// Create property
		// System.out.println ("---- Create Property (POST) ----");

		String propertyId = UniqueIdBuilder.buildComponentPropertyUniqueId(getResourceId(resourceDetails), property.getName());

		PropertyRestUtils.deleteProperty(getResourceId(resourceDetails), propertyId, sdncDesignerDetails);
		RestResponse createPropertyResponse = PropertyRestUtils.createProperty(getResourceId(resourceDetails), body,
				sdncDesignerDetails);
		AssertJUnit.assertTrue("Expected result code - 201, received - " + createPropertyResponse.getErrorCode(),
				createPropertyResponse.getErrorCode() == 201);

		// Get property
		// System.out.println ("---- Get Property (GET) ----");
		RestResponse getPropertyResponse = PropertyRestUtils.getProperty(getResourceId(resourceDetails), propertyId,
				sdncDesignerDetails);
		AssertJUnit.assertTrue("Expected result code - 200, received - " + getPropertyResponse.getErrorCode(),
				getPropertyResponse.getErrorCode() == 200);

		JSONObject jsonResp = (JSONObject) JSONValue.parse(getPropertyResponse.getResponse());

		// assertTrue("Wrong 'type' in the
		// response",jsonResp.get("type").equals(property.getPropertyType()));
		// assertTrue("Wrong 'source' in the
		// response",jsonResp.get("name").equals(property.getPropertyName()));
		// assertTrue("Wrong 'name' in the
		// response",jsonResp.get("source").equals(property.getPropertySource()));
		// assertTrue("Wrong 'description' in the
		// response",jsonResp.get("description").equals(property.getPropertyDescription()));

		// Update property
		// System.out.println ("---- Update Property (UPDATE) ----");
		property.setPropertyDescription("Updated description");
		// body = gson.toJson(property);
		body = property.propertyToJsonString();

		RestResponse updatePropertyResponse = PropertyRestUtils.updateProperty(getResourceId(resourceDetails),
				propertyId, body, sdncDesignerDetails);
		AssertJUnit.assertTrue("Expected result code - 200, received - " + updatePropertyResponse.getErrorCode(),
				updatePropertyResponse.getErrorCode() == 200);

		// Get property
		// System.out.println ("---- Get Property (GET) ----");
		getPropertyResponse = PropertyRestUtils.getProperty(getResourceId(resourceDetails), propertyId,
				sdncDesignerDetails);
		AssertJUnit.assertTrue("Expected result code - 200, received - " + getPropertyResponse.getErrorCode(),
				getPropertyResponse.getErrorCode() == 200);

		jsonResp = (JSONObject) JSONValue.parse(getPropertyResponse.getResponse());

		// assertTrue("Wrong 'type' in the
		// response",jsonResp.get("type").equals(property.getPropertyType()));
		// assertTrue("Wrong 'source' in the
		// response",jsonResp.get("name").equals(property.getPropertyName()));
		// assertTrue("Wrong 'name' in the
		// response",jsonResp.get("source").equals(property.getPropertySource()));
		// assertTrue("Wrong 'description' in the
		// response",jsonResp.get("description").equals(property.getPropertyDescription()));

		// Delete property
		// System.out.println ("---- Delete Property (DELETE) ----");
		RestResponse deletePropertyResponse = PropertyRestUtils.deleteProperty(getResourceId(resourceDetails),
				propertyId, sdncDesignerDetails);
		AssertJUnit.assertTrue("Expected result code - 204, received - " + deletePropertyResponse.getErrorCode(),
				deletePropertyResponse.getErrorCode() == 204);

		// Get property - verify that the property doesn't exist.
		// System.out.println("---- GET - Property Not Found ----");
		getPropertyResponse = PropertyRestUtils.getProperty(getResourceId(resourceDetails), propertyId,
				sdncDesignerDetails);
		List<String> variables = Arrays.asList("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_NOT_FOUND.name(), variables,
				getPropertyResponse.getResponse());

	}

	// --------------------------------------------------------------------------------------

	protected String getPropertyId(ResourceReqDetails resource, PropertyReqDetails property) {
		// return
		// resource.getResourceName().toLowerCase()+".0.1."+property.getPropertyName();
		return UniqueIdBuilder.buildComponentPropertyUniqueId(resource.getUniqueId(), property.getName());
	}

	protected String getResourceId(ResourceReqDetails resource) {
		// String resourceUid =
		// UniqueIdBuilder.buildResourceUniqueId(resource.getResourceName(),
		// "0.1");

		return resource.getUniqueId();
	}

	protected User createUser(String cspUserId, String firstName, String lastName, String email, String role)
			throws Exception {
		User sdncUserDetails = new User(firstName, lastName, cspUserId, email, role, null);

		User adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		UserRestUtils.createUser(sdncUserDetails, adminUser);

		return sdncUserDetails;
	}

	protected ResourceReqDetails createResource(User sdncUserDetails, String resourceName) throws Exception {
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		// String category = ResourceCategoryEnum.DATABASE.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.0";
		String contactId = sdncUserDetails.getUserId();
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_DATABASE.getCategory(),
				ResourceCategoryEnum.GENERIC_DATABASE.getSubCategory());
		// deleteResource(resourceName.toLowerCase()+".0.1",sdncUserDetails.getUserId());
		// TODO delete by name
		// deleteResource(UniqueIdBuilder.buildResourceUniqueId(resourceName,
		// "0.1"), sdncUserDetails.getUserId());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncUserDetails);
		AssertJUnit.assertTrue(createResource.getErrorCode().intValue() == 201);
		String resourceId = ResponseParser.getUniqueIdFromResponse(createResource);
		resourceDetails.setUniqueId(resourceId);

		return resourceDetails;

	}

	@Test
	public void putReqToCreateUriNotAllowed() throws Exception {
		// System.out.println("---- PUT request to Create uri - Not Allowed
		// ----");
		String url = String.format(Urls.CREATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails));
		RestResponse propertyErrorResponse = httpRequest.httpSendByMethod(url, "PUT", body, headersMap);
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void getReqToCreateUriNotAllowed() throws Exception {
		// System.out.println("---- GET request to Create uri - Not Allowed
		// ----");
		String url = String.format(Urls.CREATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails));
		RestResponse propertyErrorResponse = httpRequest.httpSendGet(url, headersMap);
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void deleteReqToCreateUriNotAllowed() throws Exception {
		// System.out.println("---- DELETE request to Create uri - Not Allowed
		// ----");
		String url = String.format(Urls.CREATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails));
		RestResponse propertyErrorResponse = httpRequest.httpSendDelete(url, headersMap);
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void postReqToUpdateUriNotAllowed() throws Exception {
		// System.out.println("---- POST request to Update uri - Not Allowed
		// ----");
		String url = String.format(Urls.UPDATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails), getPropertyId(resourceDetails, property));
		RestResponse propertyErrorResponse = httpRequest.httpSendPost(url, body, headersMap);
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void deleteReqPropertyNotFound() throws Exception {
		// System.out.println("---- DELETE - Property Not Found ----");
		String unknownPropertyId = getPropertyId(resourceDetails, property) + "111";
		String url = String.format(Urls.DELETE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails), unknownPropertyId);
		RestResponse propertyErrorResponse = httpRequest.httpSendDelete(url, headersMap);
		List<String> variables = Arrays.asList("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_NOT_FOUND.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void updateReqPropertyNotFound() throws Exception {
		// System.out.println("---- PUT - Property Not Found ----");
		String unknownPropertyId = getPropertyId(resourceDetails, property) + "111";
		String url = String.format(Urls.UPDATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails), unknownPropertyId);
		RestResponse propertyErrorResponse = httpRequest.httpSendByMethod(url, "PUT", body, headersMap);
		List<String> variables = Arrays.asList("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_NOT_FOUND.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void modifierNotTheStateOwner() throws Exception {
		// System.out.println("---- The modifier is not the state owner -
		// Operation Not Allowed ----");
		User sdncUserDetails2 = createUser("tu5555", "Test", "User", "tu5555@intl.sdc.com", "DESIGNER");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails2.getUserId());
		property.setPropertyDescription("new description");
		// body = gson.toJson(property);
		body = property.propertyToJsonString();
		String url = String.format(Urls.UPDATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails), getPropertyId(resourceDetails, property));
		RestResponse propertyErrorResponse = httpRequest.httpSendByMethod(url, "PUT", body, headersMap);
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				propertyErrorResponse.getResponse());

	}

	@Test
	public void postReqInvalidContent() throws Exception {
		// System.out.println("---- POST - Invalid Content ----");
		body = "invalid";
		String url = String.format(Urls.CREATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails), getPropertyId(resourceDetails, property));
		RestResponse propertyErrorResponse = httpRequest.httpSendPost(url, body, headersMap);

		// System.out.println(propertyErrorResponse.getResponse()+" "+
		// propertyErrorResponse.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), variables,
				propertyErrorResponse.getResponse());
	}

	@Test
	public void putReqInvalidContent() throws Exception {

		// Create property
		// System.out.println ("---- Create Property (POST) ----");
		RestResponse createPropertyResponse = PropertyRestUtils.createProperty(getResourceId(resourceDetails), body,
				sdncDesignerDetails);
		assertTrue("Expected result code - 201, received - " + createPropertyResponse.getErrorCode(),
				createPropertyResponse.getErrorCode() == 201);

		// System.out.println("---- PUT - Invalid Content ----");
		body = "invalid";

		String url = String.format(Urls.UPDATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				getResourceId(resourceDetails), getPropertyId(resourceDetails, property));

		// System.out.println(url + "\n" + body);

		RestResponse propertyErrorResponse = httpRequest.httpSendByMethod(url, "PUT", body, headersMap);

		// System.out.println(propertyErrorResponse.getResponse()+" "+
		// propertyErrorResponse.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), variables,
				propertyErrorResponse.getResponse());
	}

	// --------------------------------------------------------------------------------------

}
