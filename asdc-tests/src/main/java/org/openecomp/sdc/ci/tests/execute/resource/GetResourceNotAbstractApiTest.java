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
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.imports.ImportGenericResourceCITest;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetResourceNotAbstractApiTest extends ComponentBaseTest {

	private static Logger logger = LoggerFactory.getLogger(ComponentBaseTest.class.getName());
	protected static final int STATUS_CODE_GET_SUCCESS = 200;

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";

	@Rule
	public static TestName name = new TestName();

	public GetResourceNotAbstractApiTest() {
		super(name, GetResourceNotAbstractApiTest.class.getName());
	}

	@Test
	public void getNotAbstractResourceList() throws Exception {

		// remove all the not abstract resources
		// Map<NormativeTypes, Boolean> originalState =
		// ImportResourceCITest.removeAllNormativeTypeResources();

		// import all the default not abstract resources
		// ImportGenericResourceCITest.importAllNormativeTypesResources(UserRoleEnum.ADMIN);

		// Get not abstract resources
		RestResponse getResourceNotAbstarctResponse = getNotAbstractResources();
		// Check that received 200.
		assertEquals("Check response code after get abstract resources", STATUS_CODE_GET_SUCCESS,
				getResourceNotAbstarctResponse.getErrorCode().intValue());
		// Verify that all the resources not abstract
		assertTrue("One or more resources are abstract", isAllResourcesNotAbstract(getResourceNotAbstarctResponse));
		// Verify that all the resources are certified
		assertTrue("Not all the resources are certified", isAllResourcesCertified(getResourceNotAbstarctResponse));

		String objectStorageUid = "ObjectStorage";
		String computeUid = "Compute";
		String blockStorageUid = "BlockStorage";
		String loadBalancerUid = "LoadBalancer";
		// String portUid = "tosca.nodes.Network.Port";
		String portUid = "Port";
		String networkUid = "Network";
		String databaseUid = "Database";

		// Compare expected list of abstract resources to actual list of
		// abstract resources.
		List<String> expectedNotAbstractResourcesUniqueIdArray = new ArrayList<String>(Arrays.asList(computeUid,
				databaseUid, objectStorageUid, blockStorageUid, loadBalancerUid, portUid, networkUid));

		List<String> actualNotAbstarctResourcesUniqueIdArray = restResponseToListByHeader(
				getResourceNotAbstarctResponse, "name");

		// Collections.sort(actualNotAbstarctResourcesUniqueIdArray);
		// Collections.sort(expectedNotAbstractResourcesUniqueIdArray);

		List<String> toFind = new ArrayList<>();
		toFind.add(objectStorageUid);
		toFind.add(computeUid);
		toFind.add(blockStorageUid);
		toFind.add(loadBalancerUid);
		toFind.add(portUid);

		boolean removeAll = toFind.removeAll(actualNotAbstarctResourcesUniqueIdArray);
		logger.debug("Cannot find resources {}",toFind.toString());

		for (String expectedResource : expectedNotAbstractResourcesUniqueIdArray) {
			if (false == actualNotAbstarctResourcesUniqueIdArray.contains(expectedResource)) {
				// System.out.println("Not found abstract resource " +
				// expectedResource);
			}
		}

		assertTrue(
				"Expected abstract resources list: " + expectedNotAbstractResourcesUniqueIdArray.toString()
						+ " Actual: " + actualNotAbstarctResourcesUniqueIdArray.toString(),
				actualNotAbstarctResourcesUniqueIdArray.containsAll(expectedNotAbstractResourcesUniqueIdArray));

		/*
		 * java.lang.AssertionError: Expected abstract resources list:
		 * [tosca.nodes.Compute, tosca.nodes.ObjectStorage,
		 * tosca.nodes.BlockStorage, tosca.nodes.LoadBalancer,
		 * tosca.nodes.Network.Port] Actual: [resourceforproperty216,
		 * tosca.nodes.Compute, tosca.nodes.Database, resourceforproperty217,
		 * resourceforproperty217, tosca.nodes.ObjectStorage,
		 * tosca.nodes.BlockStorage, tosca.nodes.LoadBalancer,
		 * tosca.nodes.network.Port, tosca.nodes.network.Network,
		 * resourceforproperty217, resourceforproperty217,
		 * resourceforproperty217, resourceforproperty217,
		 * resourceforproperty217, resourceforproperty217,
		 * resourceforproperty217, resourceforproperty217,
		 * resourceforproperty217, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317,
		 * resourceforproperty317, resourceforproperty317]
		 */

		// Create resource (not certified)
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		String resourceName = "TestResource";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.MOBILITY.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.root");
		String vendorName = "Oracle";
		String vendorRelease = "1.0";
		String contactId = "Peter";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		// assertEquals("Check response code after create user", 201,
		// restResponse.getErrorCode().intValue());

		// Get not abstract resources
		getResourceNotAbstarctResponse = getNotAbstractResources();
		// Check that received 200.
		assertEquals("Check response code after get abstract resources", STATUS_CODE_GET_SUCCESS,
				getResourceNotAbstarctResponse.getErrorCode().intValue());
		// Verify that all the resources not abstract
		assertTrue("One or more resources are abstract", isAllResourcesNotAbstract(getResourceNotAbstarctResponse));
		// Verify that all the resources are certified
		assertTrue("Not all the resources are certified", isAllResourcesCertified(getResourceNotAbstarctResponse));

		// Compare expected list of abstract resources to actual list of
		// abstract resources.
		// expectedNotAbstractResourcesUniqueIdArray = new
		// ArrayList<String>(Arrays.asList("tosca.nodes.compute.1.0",
		// "tosca.nodes.objectstorage.1.0", "tosca.nodes.blockstorage.1.0",
		// "tosca.nodes.loadbalancer.1.0", "tosca.nodes.network.port.1.0"));

		// actualNotAbstarctResourcesUniqueIdArray =
		// restResponseToListByHeader(getResourceNotAbstarctResponse,
		// "uniqueId");

		actualNotAbstarctResourcesUniqueIdArray = restResponseToListByHeader(getResourceNotAbstarctResponse, "name");

		Collections.sort(actualNotAbstarctResourcesUniqueIdArray);
		Collections.sort(expectedNotAbstractResourcesUniqueIdArray);

		for (String expectedResource : expectedNotAbstractResourcesUniqueIdArray) {
			if (false == actualNotAbstarctResourcesUniqueIdArray.contains(expectedResource)) {
				// System.out.println("Not found abstract resource " +
				// expectedResource);
			}
		}
		assertTrue(
				"Expected abstract resources list: " + expectedNotAbstractResourcesUniqueIdArray.toString()
						+ " Actual: " + actualNotAbstarctResourcesUniqueIdArray.toString(),
				actualNotAbstarctResourcesUniqueIdArray.containsAll(expectedNotAbstractResourcesUniqueIdArray));
		// assertTrue("Expected abstract resources list: "+
		// expectedNotAbstractResourcesUniqueIdArray.toString()+ " Actual:
		// "+actualNotAbstarctResourcesUniqueIdArray.toString(),expectedNotAbstractResourcesUniqueIdArray.equals(actualNotAbstarctResourcesUniqueIdArray));

		// restore the resources
		// ImportResourceCITest.restoreToOriginalState(originalState);

	}

	protected RestResponse getNotAbstractResources() throws Exception {
		HttpRequest httpRequest = new HttpRequest();

		String url = String.format(Urls.GET_ALL_NOT_ABSTRACT_RESOURCES, config.getCatalogBeHost(),
				config.getCatalogBePort());

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), "cs0008");

		RestResponse getResourceNotAbstarctResponse = httpRequest.httpSendGet(url, headersMap);

		return getResourceNotAbstarctResponse;
	}

	protected List<String> restResponseToListByHeader(RestResponse restResponse, String restResponseHeader) {
		JsonElement jelement = new JsonParser().parse(restResponse.getResponse());
		JsonArray jsonArray = jelement.getAsJsonArray();

		List<String> restResponseArray = new ArrayList<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jobject = (JsonObject) jsonArray.get(i);
			String header = jobject.get(restResponseHeader).toString();
			header = header.replace("\"", "");
			restResponseArray.add(header);
		}

		return restResponseArray;

	}

	protected boolean isAllResourcesNotAbstract(RestResponse restResponse) {
		JsonElement jelement = new JsonParser().parse(restResponse.getResponse());
		JsonArray jsonArray = jelement.getAsJsonArray();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jobject = (JsonObject) jsonArray.get(i);

			if (jobject.get("abstract").getAsBoolean()) {
				return false;
			}

		}
		return true;

	}

	protected boolean isEmptyList(RestResponse restResponse) {
		JsonElement jelement = new JsonParser().parse(restResponse.getResponse());
		JsonArray jsonArray = jelement.getAsJsonArray();

		if (jsonArray.size() == 0) {
			return true;
		}
		return false;
	}

	protected boolean isAllResourcesCertified(RestResponse restResponse) {
		JsonElement jelement = new JsonParser().parse(restResponse.getResponse());
		JsonArray jsonArray = jelement.getAsJsonArray();

		String certified = "CERTIFIED";
		String lifecycleState;

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jobject = (JsonObject) jsonArray.get(i);
			lifecycleState = jobject.get("lifecycleState").getAsString();
			if (!lifecycleState.equals(certified)) {
				return false;
			}

		}
		return true;
	}

	@Test(enabled = false)
	public void getEmptyNonAbstractResourcesList() throws Exception {
		// remove all the not abstract resources
		Map<NormativeTypesEnum, Boolean> originalState = ImportGenericResourceCITest.removeAllNormativeTypeResources();

		// Get not abstract resources
		RestResponse getResourceNotAbstarctResponse = getNotAbstractResources();
		// Check that received 200.
		assertEquals("Check response code after get abstract resources", STATUS_CODE_GET_SUCCESS,
				getResourceNotAbstarctResponse.getErrorCode().intValue());
		// Verify empty list
		assertTrue("Received list is not empty", isEmptyList(getResourceNotAbstarctResponse));

		// restore the resources
		// ImportResourceCITest.restoreToOriginalState(originalState);
		// import the resources
		ImportGenericResourceCITest.importAllNormativeTypesResources(UserRoleEnum.ADMIN);
	}

}
