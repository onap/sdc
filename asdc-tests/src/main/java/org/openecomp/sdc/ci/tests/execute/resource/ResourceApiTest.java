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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ResourceValidationUtils;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class ResourceApiTest extends ComponentBaseTest {

	protected final String contentTypeHeaderData = "application/json";
	protected final String acceptHeaderDate = "application/json";

	@Rule
	public static TestName name = new TestName();

	public ResourceApiTest() {
		super(name, ResourceApiTest.class.getName());
	}

	// Keep
	@Test
	public void updateResourceMetadataSuccess() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncModifierDetails.setUserId("jh0003");
		RestResponse restResponse = createResourceForUpdate(sdncModifierDetails);
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());

		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		// set resource details
		ResourceReqDetails resourceDetails = new ResourceReqDetails();
		resourceDetails.setDescription("updatedDescription");
		ArrayList<String> resourceTags = new ArrayList<String>();
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		resourceTags.add(resourceRespJavaObject.getName());
		resourceTags.add("tag1");
		resourceTags.add("tag1");
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		resourceDetails.setTags(resourceTags);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.NETWORK_L2_3_ROUTERS.getCategory(),
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS.getSubCategory());
		resourceDetails.setVendorName("OracleUp");
		resourceDetails.setVendorRelease("1.5Up");
		resourceDetails.setContactId("pe1116");

		resourceDetails.setIcon(resourceRespJavaObject.getIcon());
		resourceDetails.setName(resourceRespJavaObject.getName());
		resourceDetails.setDerivedFrom(resourceRespJavaObject.getDerivedFrom());

		// ResourceReqDetails resourceDetails = new
		// ResourceReqDetails(resourceName, description, resourceTags, category,
		// derivedFrom, vendorName, vendorRelease, contactId, null);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_RESOURCE_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceRespJavaObject.getUniqueId());
		RestResponse updateResourceResponse = http.httpSendByMethod(url, "PUT", userBodyJson, headersMap);

		// resourceDetails.setResourceName(resourceRespJavaObject.getResourceName());
		ResourceValidationUtils.validateResourceReqVsResp(resourceDetails,
				ResponseParser.convertResourceResponseToJavaObject(updateResourceResponse.getResponse()));

		// Delete resource
		deleteResource(resourceRespJavaObject.getUniqueId(), sdncModifierDetails.getUserId());

	}

	protected void deleteResource(String resourceUniqueId, String httpCspUserId) throws Exception {
		RestResponse res = ResourceRestUtils.deleteResource(resourceUniqueId, httpCspUserId);

		// System.out.println("Delete resource was finished with response: " +
		// res.getErrorCode());
	}

	protected RestResponse createResourceForUpdate(User sdncModifierDetails) throws Exception {

		ResourceReqDetails resourceDetails = getResourceObj();

		// create resource
		return ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

	}

	public ResourceReqDetails getResourceObj() {
		// set resource details
		String resourceName = "ResourceForUpdate" + (int) (Math.random() * 100);
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		// String category = ResourceCategoriesEnum.MOBILITY.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "pe1116";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getCategory(),
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getSubCategory());
		return resourceDetails;
	}

	// -------------------------------------------------------------------

	protected ResourceReqDetails defineResourse_Benny(int n) {
		String resourceName = "cisco" + String.valueOf(n);
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add("tag1");
		String category = ServiceCategoriesEnum.MOBILITY.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "borderElement";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		return resourceDetails;
	}

	@Test
	public void getAllAbstractResources() throws Exception {
		RestResponse abstractResources = CatalogRestUtils.getAbstractResources();

		int status = abstractResources.getErrorCode();
		assertTrue(status == 200);
		String json = abstractResources.getResponse();
		JSONArray array = (JSONArray) JSONValue.parse(json);
		for (Object o : array) {
			JSONObject value = (JSONObject) o;
			Boolean element = (Boolean) value.get("abstract");
			assertTrue(element);
		}

	}

	@Test
	public void getAllNotAbstractResources() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = String.format(Urls.GET_ALL_NOT_ABSTRACT_RESOURCES, config.getCatalogBeHost(),
					config.getCatalogBePort());
			HttpGet httpget = new HttpGet(url);

			httpget.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);

			httpget.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);

			httpget.addHeader(HttpHeaderEnum.USER_ID.getValue(),
					ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());

			// System.out.println("Executing request " +
			// httpget.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httpget);
			int status = response.getStatusLine().getStatusCode();
			assertTrue(status == 200);
			try {
				String json = EntityUtils.toString(response.getEntity());
				JSONArray array = (JSONArray) JSONValue.parse(json);
				for (Object o : array) {
					JSONObject value = (JSONObject) o;
					Boolean element = (Boolean) value.get("abstract");
					assertTrue(!element);
				}

			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	@Test
	public void updateResourceMetadata_methodNotAllowed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		// set resource details
		String resourceName = "ResForUpdate";
		String description = "updatedDescription";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add("tag1");
		resourceTags.add("tag2");
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.root");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		String vendorName = "OracleUp";
		String vendorRelease = "1.5Up";
		String contactId = "pe1117";
		String icon = "myICON.jpgUp";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_RESOURCE_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				"NotExistsId");

		RestResponse updateResourceResponse = http.httpSendByMethod(url, "POST", userBodyJson, headersMap);

		assertNotNull("Check error code exists in response after wrong update resource",
				updateResourceResponse.getErrorCode());
		assertEquals("Check error code after update resource", 405, updateResourceResponse.getErrorCode().intValue());
	}

	@Test
	public void validateResourceNameTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncModifierDetails.setUserId("jh0003");

		ResourceReqDetails resourceDetails = getResourceObj();

		// create resource
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(restResponse.getResponse());
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			// check invalid
			String url = String.format(Urls.VALIDATE_RESOURCE_NAME, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetails.getName());

			HttpGet httpget = new HttpGet(url);

			httpget.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);

			httpget.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);

			httpget.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

			// System.out.println("Executing request " +
			// httpget.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httpget);
			int status = response.getStatusLine().getStatusCode();
			assertTrue(status == 200);
			try {
				String json = EntityUtils.toString(response.getEntity());
				JSONObject object = (JSONObject) JSONValue.parse(json);
				Boolean element = (Boolean) object.get("isValid");
				assertTrue(!element);

			} finally {
				response.close();
			}
			// check valid
			url = String.format(Urls.VALIDATE_RESOURCE_NAME, config.getCatalogBeHost(), config.getCatalogBePort(),
					resourceDetails.getName() + "temp");

			httpget = new HttpGet(url);

			httpget.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);

			httpget.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);

			httpget.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

			// System.out.println("Executing request " +
			// httpget.getRequestLine());
			response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			assertTrue(status == 200);
			try {
				String json = EntityUtils.toString(response.getEntity());
				JSONObject object = (JSONObject) JSONValue.parse(json);
				Boolean element = (Boolean) object.get("isValid");
				assertTrue(element);

			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}

		// Delete resource
		ResourceRestUtils.deleteResource(resourceDetails, sdncModifierDetails, "0.1");

	}

	// -------------------------------------------------------------------
	// //Benny Tal
	// @Test
	// public void createResource_Benny() throws Exception {
	// for (int i = 0; i < 100; i++) {
	// ResourceReqDetails resourceDetails = defineResourse_Benny(i);
	//
	// ResourceRestUtils.createResource(resourceDetails,
	// UserUtils.getDesignerDetails());
	// // resourceUtils.deleteResource(resourceDetails,
	// UserUtils.getDesignerDetails(), "0.1");
	// }
	// }

}
