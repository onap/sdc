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

package org.openecomp.sdc.ci.tests.execute.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class CatalogDataApiTest extends ComponentBaseTest {

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";

	@Rule
	public static TestName name = new TestName();
	protected User user;
	protected RestResponse res1;
	protected RestResponse res2;
	protected RestResponse svc1;
	protected ResourceReqDetails resourceDetails1;
	protected ResourceReqDetails resourceDetails2;
	protected ServiceReqDetails svcDetails1;

	public CatalogDataApiTest() {
		super(name, CatalogDataApiTest.class.getName());
	}

	@BeforeMethod
	public void setUp() throws Exception {
		user = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		resourceDetails1 = buildResourceDetails(user, "TestResource1");
		resourceDetails2 = buildResourceDetails(user, "TestResource2");
		svcDetails1 = buildServiceDetails("TestService1");

		res1 = createResource(user, resourceDetails1);
		AssertJUnit.assertEquals("create resorce failed", 201, res1.getErrorCode().intValue());
		resourceDetails1.setUniqueId(ResponseParser.getUniqueIdFromResponse(res1));
		resourceDetails2.setVersion(ResponseParser.getVersionFromResponse(res1));

		res2 = createResource(user, resourceDetails2);
		AssertJUnit.assertEquals("create resorce failed", 201, res2.getErrorCode().intValue());
		resourceDetails2.setUniqueId(ResponseParser.getUniqueIdFromResponse(res2));
		resourceDetails2.setVersion(ResponseParser.getVersionFromResponse(res2));

		svc1 = createService(user, svcDetails1);
		AssertJUnit.assertEquals("create resorce failed", 201, svc1.getErrorCode().intValue());
		svcDetails1.setUniqueId(ResponseParser.convertServiceResponseToJavaObject(svc1.getResponse()).getUniqueId());
		svcDetails1.setVersion(ResponseParser.convertServiceResponseToJavaObject(svc1.getResponse()).getVersion());
	}

	@AfterMethod
	public void tearDown() throws Exception {
		deleteResource(resourceDetails1.getUniqueId(), user.getUserId());
		deleteResource(resourceDetails2.getUniqueId(), user.getUserId());
		deleteService(svcDetails1.getUniqueId(), user);
	}

	// Keep 1
	@Test
	public void getCatalogData() throws Exception {

		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails1, user, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertEquals("check in operation failed", 200, checkInResponse.getErrorCode().intValue());

		RestResponse res = CatalogRestUtils.getCatalog(user.getUserId());
		String json = res.getResponse();
		JSONObject jsonResp = (JSONObject) JSONValue.parse(json);
		JSONArray resources = (JSONArray) jsonResp.get("resources");
		JSONArray services = (JSONArray) jsonResp.get("services");

		// Verify all the expected resources received.
		AssertJUnit.assertTrue("check resource1 is in response",
				isComponentInArray(resourceDetails1.getUniqueId(), resources));
		AssertJUnit.assertTrue("check resource2 is in response",
				isComponentInArray(resourceDetails2.getUniqueId(), resources));
		AssertJUnit.assertTrue("check service1 is in response",
				isComponentInArray(svcDetails1.getUniqueId(), services));

	}

	protected void deleteResource(String resourceUniqueId, String httpCspUserId) throws Exception {
		RestResponse deleteResourceResponse = ResourceRestUtils.deleteResource(resourceUniqueId, httpCspUserId);

	}

	protected RestResponse createResource(User user, ResourceReqDetails resourceDetails) throws Exception {
		deleteResource(resourceDetails.getName(), user.getUserId());
		return ResourceRestUtils.createResource(resourceDetails, user);
	}

	protected ResourceReqDetails buildResourceDetails(User user, String resourceName) {
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.0";
		String contactId = user.getUserId();
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_DATABASE.getCategory(),
				ResourceCategoryEnum.GENERIC_DATABASE.getSubCategory());
		return resourceDetails;
	}

	protected boolean isComponentInArray(String id, JSONArray component) {
		for (int i = 0; i < component.size(); i++) {
			JSONObject jobject = (JSONObject) component.get(i);
			if (jobject.get("uniqueId").toString().equals(id.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	protected RestResponse createService(User user, ServiceReqDetails svcDetails) throws Exception {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = getHeadersMap(user);

		Gson gson = new Gson();
		String body = gson.toJson(svcDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort());
		RestResponse res = http.httpSendPost(url, body, headersMap);
		// System.out.println("Create service was finished with response:
		// "+res.getErrorCode());
		return res;
	}

	protected Map<String, String> getHeadersMap(User user) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put("USER_ID", user.getUserId());
		return headersMap;
	}

	protected ServiceReqDetails buildServiceDetails(String serviceName) {
		String description = "description";
		ArrayList<String> serviceTags = new ArrayList<String>();
		serviceTags.add("tag1");
		serviceTags.add(serviceName);
		String category = ServiceCategoriesEnum.MOBILITY.getValue();
		String vendorName = "Oracle";
		String vendorRelease = "0.1";
		String contactId = "al1976";
		String icon = "myIcon";

		ServiceReqDetails svcdetails = new ServiceReqDetails(serviceName, category, serviceTags, description,
				contactId, icon);
		return svcdetails;
	}

	public RestResponse deleteService(String serviceId, User user) throws Exception {
		HttpRequest httpRequest = new HttpRequest();
		String url = String.format(Urls.DELETE_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(),
				serviceId);

		Map<String, String> headersMap = getHeadersMap(user);
		RestResponse res = httpRequest.httpSendDelete(url, headersMap);
		// System.out.println("Delete service was finished with response:
		// "+res.getErrorCode());
		return res;
	}

	public class NewObject {
		private String _name;

		public String getName() {
			return _name;
		}

		public void setName(String name) {
			this._name = name;
		}
	}

}
