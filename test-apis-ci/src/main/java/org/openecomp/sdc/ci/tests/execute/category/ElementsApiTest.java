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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.testng.annotations.Test;

public class ElementsApiTest extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public ElementsApiTest() {
		super(name, ElementsApiTest.class.getName());
	}

	// Expected 200 Keep
	@Test
	public void getAllPropertyScopesSuccess() throws Exception {
		RestResponse response = ResourceRestUtils.getAllPropertyScopesTowardsCatalogBe();
		String action = "Get All Property Scopes";
		int expectedCode = 200;
		verifyErrorCode(response, action, expectedCode);
	}

	// Expected 200 Keep
	@Test
	public void getAllArtifactTypes() throws Exception {
		RestResponse response = ResourceRestUtils.getAllArtifactTypesTowardsCatalogBe();
		String action = "Get All Artifact Types";
		int expectedCode = 200;
		verifyErrorCode(response, action, expectedCode);
	}

	// Expected 200 Keep
	@Test
	public void getConfiguration() throws Exception {
		RestResponse response = ResourceRestUtils.getConfigurationTowardsCatalogBe();
		String action = "Get All Artifact Types";
		int expectedCode = 200;

		String json = response.getResponse();
		JSONObject jsonResp = (JSONObject) JSONValue.parse(json);

		HashMap<String, Object> artifacts = (HashMap<String, Object>) jsonResp.get("artifacts");
		Long defaultHeatTimeout = (Long) jsonResp.get("defaultHeatTimeout");

		if (defaultHeatTimeout == null) {
			response.setErrorCode(500);
			verifyErrorCode(response, action, expectedCode);
			return;
		}

		if (artifacts == null) {
			response.setErrorCode(500);
			verifyErrorCode(response, action, expectedCode);
			return;
		}

		JSONObject deploymentResources = (JSONObject) artifacts.get("deployment");
		JSONArray otherResources = (JSONArray) artifacts.get("other");
		if (deploymentResources == null || otherResources == null) {
			response.setErrorCode(500);
			verifyErrorCode(response, action, expectedCode);
			return;
		}

		JSONArray roles = (JSONArray) jsonResp.get("roles");
		if (roles == null) {
			response.setErrorCode(500);
			verifyErrorCode(response, action, expectedCode);
			return;
		}

	}

	public void verifyErrorCode(RestResponse response, String action, int expectedCode) {
		assertNotNull("check response object is not null after " + action, response);
		assertNotNull("check error code exists in response after " + action, response.getErrorCode());
		assertEquals("Check response code after  + action" + action, expectedCode, response.getErrorCode().intValue());
	}

	@Test(enabled = false)
	public void getAllCategoriesSuccess() throws Exception {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		RestResponse response = CatalogRestUtils.getAllCategoriesTowardsCatalogBe();
		String action = "Get All Categories";
		int expectedCode = 200;
		verifyErrorCode(response, action, expectedCode);
	}

	@Test(enabled = false)
	public void getAllTagSuccess() throws Exception {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		RestResponse response = ResourceRestUtils.getAllTagsTowardsCatalogBe();
		String action = "Get All Categories";
		int expectedCode = 200;
		verifyErrorCode(response, action, expectedCode);
	}

}
