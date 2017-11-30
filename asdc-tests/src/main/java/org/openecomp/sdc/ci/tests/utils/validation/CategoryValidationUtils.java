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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.json.JSONObject;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;

public class CategoryValidationUtils {

	public static void verifyCategoryExistInGetResponse(RestResponse getAllCategoryRest,
			CategoryDefinition categoryDefinition) {

		int categoriesNum = CategoryRestUtils.getMatchingCategoriesNum(getAllCategoryRest, categoryDefinition);
		assertEquals("category " + categoryDefinition.getName() + " not found during get or found more than once", 1,
				categoriesNum);
	}

	public static void verifyCategoryNotExistsInGetResponse(RestResponse getAllCategoryRest,
			CategoryDefinition categoryDefinition) {

		int categoriesNum = CategoryRestUtils.getMatchingCategoriesNum(getAllCategoryRest, categoryDefinition);
		assertEquals("category " + categoryDefinition.getName() + " should't be found during get", 0, categoriesNum);
	}

	public static void verifySubCategoryExistInGetResponse(RestResponse getAllCategoryRest, String parentCategoryId,
			SubCategoryDefinition expectedSubCategoryDefinition) {

		int subCategoriesNum = CategoryRestUtils.getMatchingSubCategoriesNum(getAllCategoryRest, parentCategoryId,
				expectedSubCategoryDefinition);
		assertEquals(
				"sub-category " + expectedSubCategoryDefinition.getName()
						+ " not found during get or found more than once for parentId " + parentCategoryId,
				1, subCategoriesNum);
	}

	public static void verifyGroupingExistInGetResponse(RestResponse getAllCategoryRest, String parentCategoryId,
			String subCategoryId, GroupingDefinition expectedGroupingDefinition) {

		int groupingNum = CategoryRestUtils.getMatchingGroupingNum(getAllCategoryRest, parentCategoryId, subCategoryId,
				expectedGroupingDefinition);
		assertEquals(
				"sub-category " + expectedGroupingDefinition.getName()
						+ " not found during get or found more than once for parentId " + parentCategoryId,
				1, groupingNum);
	}

	public static void verifyGroupingNotExistInGetResponse(RestResponse getAllCategoryRest, String parentCategoryId,
			String subCategoryId, GroupingDefinition expectedGroupingDefinition) {

		int groupingNum = CategoryRestUtils.getMatchingGroupingNum(getAllCategoryRest, parentCategoryId, subCategoryId,
				expectedGroupingDefinition);
		assertEquals(
				"sub-category " + expectedGroupingDefinition.getName()
						+ " not found during get or found more than once for parentId " + parentCategoryId,
				0, groupingNum);
	}

	public static void verifySubCategoryNotExistsInGetResponse(RestResponse getAllCategoryRest, String parentCategoryId,
			SubCategoryDefinition expectedSubCategoryDefinition) {

		int subCategoriesNum = CategoryRestUtils.getMatchingSubCategoriesNum(getAllCategoryRest, parentCategoryId,
				expectedSubCategoryDefinition);
		assertEquals("sub-category " + expectedSubCategoryDefinition.getName()
				+ " should't be found during get for parentId " + parentCategoryId, 0, subCategoriesNum);
	}

	/// NEE Benny
	public static void validateCreateGroupResponse(RestResponse createSubCategoryRest,
			GroupingDefinition expectedGroupDefinition) throws Exception {

		String response = createSubCategoryRest.getResponse();
		JSONObject jobject = new JSONObject(response);
		assertTrue(jobject.get("name").equals(expectedGroupDefinition.getName()));
		assertTrue(jobject.get("normalizedName").equals(expectedGroupDefinition.getNormalizedName()));
		// assertNotNull(jobject.get("normalizedName"));
		assertNotNull(jobject.get("uniqueId"));
		expectedGroupDefinition.setUniqueId(jobject.get("uniqueId").toString());

	}

	public static void validateCreateSubCategoryResponse(RestResponse createSubCategoryRest,
			SubCategoryDefinition expectedSubCategoryDefinition) throws Exception {

		String response = createSubCategoryRest.getResponse();
		JSONObject jobject = new JSONObject(response);
		assertTrue(jobject.get("name").equals(expectedSubCategoryDefinition.getName()));
		assertNotNull(jobject.get("normalizedName"));
		assertNotNull(jobject.get("uniqueId"));
	}

	public static void validateCreateCategoryResponse(RestResponse createCategoryRest,
			CategoryDefinition expectedCategoryDefinition) throws Exception {
		String response = createCategoryRest.getResponse();
		JSONObject jobject = new JSONObject(response);
		assertTrue(jobject.get("name").equals(expectedCategoryDefinition.getName()));
		assertTrue(jobject.get("normalizedName").equals(expectedCategoryDefinition.getNormalizedName()));
		assertNotNull(jobject.get("uniqueId"));
	}

}
