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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest.ComponentOperationEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

public class ProductValidationUtils {

	static Logger logger = Logger.getLogger(ProductValidationUtils.class.getName());

	public static void compareExpectedAndActualProducts(Product expectedProduct, Product actualProduct) {
		compareExpectedAndActualProducts(expectedProduct, actualProduct, null);
	}

	public static void compareExpectedAndActualProducts(Product expectedProduct, Product actualProduct,
			ComponentOperationEnum operation) {

		assertEquals(expectedProduct.getName(), actualProduct.getName());
		assertEquals(expectedProduct.getFullName(), actualProduct.getFullName());
		assertEquals(expectedProduct.getDescription(), actualProduct.getDescription());

		List<String> expectedContacts = expectedProduct.getContacts();
		List<String> actualContacts = actualProduct.getContacts();
		assertTrue(
				"Expected contacts:" + Arrays.toString(expectedContacts.toArray()) + ", actual contacts:"
						+ Arrays.toString(actualContacts.toArray()),
				expectedContacts.size() == actualContacts.size() && expectedContacts.containsAll(actualContacts)
						&& actualContacts.containsAll(expectedContacts));

		List<String> expectedTags = expectedProduct.getTags();
		List<String> actualTags = actualProduct.getTags();
		assertTrue(
				"Expected tags:" + Arrays.toString(expectedTags.toArray()) + ", actual tags:"
						+ Arrays.toString(actualTags.toArray()),
				expectedTags.size() == actualTags.size() && expectedTags.containsAll(actualTags)
						&& actualTags.containsAll(expectedTags));

		assertEquals(expectedProduct.getLifecycleState(), actualProduct.getLifecycleState());
		assertEquals(expectedProduct.getVersion(), actualProduct.getVersion());
		assertEquals(expectedProduct.isHighestVersion(), actualProduct.isHighestVersion());
		assertEquals(expectedProduct.getNormalizedName(), actualProduct.getNormalizedName());

		compareCategories(expectedProduct, actualProduct);
		assertEquals(expectedProduct.getLastUpdaterUserId(), actualProduct.getLastUpdaterUserId());
		if (operation != null) {
			assertEquals(expectedProduct.getCreatorUserId(), actualProduct.getCreatorUserId());
		}

		Long lastUpdateDate = actualProduct.getLastUpdateDate();
		Long creationDate = actualProduct.getCreationDate();
		Map<String, String> allVersions = actualProduct.getAllVersions();

		if (operation != null) {
			if (operation == ComponentOperationEnum.UPDATE_COMPONENT
					|| operation == ComponentOperationEnum.CHANGE_STATE_CHECKOUT
					|| operation == ComponentOperationEnum.CHANGE_STATE_CHECKIN
					|| operation == ComponentOperationEnum.CHANGE_STATE_UNDO_CHECKOUT) {
				assertTrue("Last update date:" + lastUpdateDate + ", creation date: " + creationDate,
						lastUpdateDate > 0 && creationDate > 0 && lastUpdateDate > creationDate);
			} else {
				assertTrue("Last update date:" + lastUpdateDate + ", creation date: " + creationDate,
						lastUpdateDate > 0 && lastUpdateDate.equals(creationDate));
			}
		}

		// Check UUIDs
		// If just created, no way to test the UUIDs themselves
		// If updated, we expect the UUIDs of actual to match the expected
		String uniqueId = actualProduct.getUniqueId();
		if (operation == ComponentOperationEnum.CREATE_COMPONENT) {
			UUID.fromString(uniqueId);
			UUID.fromString(actualProduct.getUUID());
			UUID.fromString(actualProduct.getInvariantUUID());
			assertTrue(allVersions.size() == 1);
			assertTrue(allVersions.get("0.1").equals(uniqueId));
		} else {
			if (operation == ComponentOperationEnum.CHANGE_STATE_CHECKOUT) {
				assertFalse(expectedProduct.getUniqueId().equals(uniqueId));
				// Assigning the updated uniqueId to expected so that it can be
				// passed to further logic
				expectedProduct.setUniqueId(uniqueId);
			} else if (operation != null) {
				assertTrue(expectedProduct.getUniqueId().equals(uniqueId));
			}
			assertEquals(expectedProduct.getUUID(), actualProduct.getUUID());
			assertEquals(expectedProduct.getInvariantUUID(), actualProduct.getInvariantUUID());
		}
	}

	private static void compareCategories(Product expectedProduct, Product actualProduct) {
		List<CategoryDefinition> expectedCategories = expectedProduct.getCategories();
		List<CategoryDefinition> actualCategories = actualProduct.getCategories();
		if (expectedCategories != null && actualCategories != null) {
			int expSize = expectedCategories.size();
			int actSize = actualCategories.size();

			assertTrue("Expected size:" + expSize + ", actual size:" + actSize, expSize == actSize);

			for (CategoryDefinition actualDefinition : actualCategories) {
				int lastIndexOfCat = expectedCategories.lastIndexOf(actualDefinition);
				assertTrue("Actual category " + actualDefinition + " not found in expected.", lastIndexOfCat != -1);
				CategoryDefinition expectedDefinition = expectedCategories.get(lastIndexOfCat);
				List<SubCategoryDefinition> actualSubcategories = actualDefinition.getSubcategories();
				List<SubCategoryDefinition> expectedSubcategories = expectedDefinition.getSubcategories();
				for (SubCategoryDefinition actualSub : actualSubcategories) {
					lastIndexOfCat = expectedSubcategories.lastIndexOf(actualSub);
					assertTrue("Actual subcategory " + actualSub + " not found in expected.", lastIndexOfCat != -1);
					SubCategoryDefinition expectedSub = expectedSubcategories.get(lastIndexOfCat);
					List<GroupingDefinition> actualGroupings = actualSub.getGroupings();
					List<GroupingDefinition> expectedGroupings = expectedSub.getGroupings();
					for (GroupingDefinition actualGrouping : actualGroupings) {
						lastIndexOfCat = expectedGroupings.lastIndexOf(actualGrouping);
						assertTrue("Actual grouping " + actualSub + " not found in expected.", lastIndexOfCat != -1);
					}
				}
			}

			for (CategoryDefinition expectedDefinition : expectedCategories) {
				int lastIndexOfCat = actualCategories.lastIndexOf(expectedDefinition);
				assertTrue("Expected category " + expectedDefinition + " not found in actual.", lastIndexOfCat != -1);
				CategoryDefinition actualDefinition = actualCategories.get(lastIndexOfCat);
				List<SubCategoryDefinition> actualSubcategories = actualDefinition.getSubcategories();
				List<SubCategoryDefinition> expectedSubcategories = expectedDefinition.getSubcategories();
				for (SubCategoryDefinition expectedSub : expectedSubcategories) {
					lastIndexOfCat = actualSubcategories.lastIndexOf(expectedSub);
					assertTrue("Expected subcategory " + expectedSub + " not found in actual.", lastIndexOfCat != -1);
					SubCategoryDefinition actualSub = actualSubcategories.get(lastIndexOfCat);
					List<GroupingDefinition> actualGroupings = actualSub.getGroupings();
					List<GroupingDefinition> expectedGroupings = expectedSub.getGroupings();
					for (GroupingDefinition expectedGrouping : expectedGroupings) {
						lastIndexOfCat = actualGroupings.lastIndexOf(expectedGrouping);
						assertTrue("Expected grouping " + expectedGrouping + " not found in actual.",
								lastIndexOfCat != -1);
					}
				}
			}
		}
	}

	public static void verifyProductsNotExistInUserFollowedPage(User user, Product... nonExpectedProducts)
			throws Exception {
		String component = "products";
		Boolean isExist;
		Product nonExpectedProduct;
		RestResponse getFollowedPage = ProductRestUtils.getFollowed(user.getUserId());
		JSONArray followedProductes = getListArrayFromRestResponse(getFollowedPage, component);
		if (followedProductes != null) { // if any product exist in followed
											// page
			for (int i = 0; i < nonExpectedProducts.length; i++) {
				nonExpectedProduct = nonExpectedProducts[i];
				isExist = false;
				for (int k = 0; k < followedProductes.size(); k++) {
					JSONObject jobject = (JSONObject) followedProductes.get(k);
					if (jobject.get("uuid").toString().equals(nonExpectedProduct.getUUID())) {
						isExist = true;
						k = followedProductes.size();
					}
				}
				assertFalse(isExist);
			}
		}

	}

	public static void checkUserFollowedPage(User user, Product... expectedProducts) throws Exception {
		String component = "products";
		Boolean isExist;
		Product expectedProduct;
		RestResponse getFollowedPage = ProductRestUtils.getFollowed(user.getUserId());
		JSONArray followedProductes = getListArrayFromRestResponse(getFollowedPage, component);
		assertTrue("check if any followedProductes received ", followedProductes != null);
		assertTrue("check if any expectedProducts and followedProductes are the same size",
				expectedProducts.length == followedProductes.size());
		for (int i = 0; i < expectedProducts.length; i++) {
			expectedProduct = expectedProducts[i];
			isExist = false;
			for (int k = 0; k < followedProductes.size(); k++) {
				JSONObject jobject = (JSONObject) followedProductes.get(k);
				// if(jobject.get("uuid").toString().equals(expectedProduct.getUUID()))
				if (jobject.get("uniqueId").toString().equals(expectedProduct.getUniqueId())) {

					String productString = jobject.toJSONString();
					Product actualProduct = ResponseParser.parseToObjectUsingMapper(productString, Product.class);
					ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct, null);
					isExist = true;
					k = followedProductes.size();
				}
			}
			assertTrue(isExist);
		}
	}

	private static JSONArray getListArrayFromRestResponse(RestResponse restResponse, String lst) {
		String json = restResponse.getResponse();
		JSONObject jsonResp = (JSONObject) JSONValue.parse(json);
		JSONArray resources = (JSONArray) jsonResp.get(lst);
		return resources;
	}

}
