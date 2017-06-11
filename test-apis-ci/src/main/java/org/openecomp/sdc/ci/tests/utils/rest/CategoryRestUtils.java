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

package org.openecomp.sdc.ci.tests.utils.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CategoryRestUtils extends BaseRestUtils {

	private static final int STATUS_CODE_CREATED = 201;

	private static Gson gson = new Gson();

	public static RestResponse createCategory(CategoryDefinition categoryDefinition, User sdncModifierDetails,
			String categoryType) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_CATEGORY, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType);
		String bodyJson = gson.toJson(categoryDefinition);
		RestResponse addCategoryResponse = BaseRestUtils.sendPost(url, bodyJson, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		if (addCategoryResponse.getErrorCode().intValue() == STATUS_CODE_CREATED)
			categoryDefinition.setUniqueId(
					ResponseParser.getValueFromJsonResponse(addCategoryResponse.getResponse(), "uniqueId"));
		return addCategoryResponse;
	}

	// GET categories
	public static RestResponse getAllCategories(User sdncModifierDetails, String categoryType) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_CATEGORIES, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType);
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		RestResponse getCategotyResponse = http.httpSendGet(url, headersMap);
		return getCategotyResponse;
	}

	public static RestResponse getAllCategoriesTowardsFe(User sdncModifierDetails, String categoryType)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_CATEGORIES_FE, config.getCatalogFeHost(), config.getCatalogFePort(),
				categoryType);
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		RestResponse getCategotyResponse = http.httpSendGet(url, headersMap);
		return getCategotyResponse;
	}

	// Delete Category
	public static RestResponse deleteCategory(String categoryId, String psUserId, String categoryType)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_CATEGORY, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType, categoryId);
		url = url.replace("#", "%23"); // HEX
		url = url.replace(" ", "%20"); // HEX
		RestResponse deleteCategoryResponse = sendDelete(url, psUserId);
		return deleteCategoryResponse;
	}

	public static RestResponse createSubCategory(SubCategoryDefinition subCategory, CategoryDefinition parentCategory,
			User sdncModifierDetails, String categoryType) throws Exception {
		// categoryType = service/resource/product
		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_SUB_CATEGORY, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType, parentCategory.getUniqueId());
		String bodyJson = gson.toJson(subCategory);
		RestResponse createSubCategoryPost = BaseRestUtils.sendPost(url, bodyJson, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		if (createSubCategoryPost.getErrorCode().intValue() == STATUS_CODE_CREATED)
			subCategory.setUniqueId(
					ResponseParser.getValueFromJsonResponse(createSubCategoryPost.getResponse(), "uniqueId"));

		return createSubCategoryPost;
	}

	public static RestResponse deleteSubCategory(String subCategoryId, String categoryId, String psUserId,
			String categoryType) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_SUB_CATEGORY, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType, categoryId, subCategoryId);
		url = url.replace("#", "%23"); // HEX
		url = url.replace(" ", "%20"); // HEX
		RestResponse deleteSubCategoryResponse = sendDelete(url, psUserId);
		return deleteSubCategoryResponse;
	}

	public static RestResponse deleteGrouping(String groupId, String subCategoryId, String categoryId, String psUserId,
			String categoryType) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_GROUPING, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType, categoryId, subCategoryId, groupId);
		url = url.replace("#", "%23"); // HEX
		url = url.replace(" ", "%20"); // HEX
		RestResponse deleteGroupResponse = sendDelete(url, psUserId);
		return deleteGroupResponse;
	}

	public static RestResponse createServiceCategoryHttpCspAtuUidIsMissing(CategoryDefinition categoryDataDefinition,
			User sdncModifierDetails) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_CATEGORY, config.getCatalogBeHost(), config.getCatalogBePort(),
				SERVICE_COMPONENT_TYPE);

		Map<String, String> headersMap = prepareHeadersMap(sdncModifierDetails.getUserId());
		headersMap.remove("USER_ID");
		Gson gson = new Gson();
		String userBodyJson = gson.toJson(categoryDataDefinition);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse createCatergoryResponse = http.httpSendPost(url, userBodyJson, headersMap);
		return createCatergoryResponse;
	}

	public static RestResponse createSubCategoryHttpCspAtuUidIsMissing(SubCategoryDefinition subCategory,
			CategoryDefinition parentCategory, User sdncModifierDetails, String categoryType) throws Exception {
		// categoryType = service/resource/product
		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_SUB_CATEGORY, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType, parentCategory.getUniqueId());
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		headersMap.remove("USER_ID");
		Gson gson = new Gson();
		String subCatJson = gson.toJson(subCategory);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(subCatJson);
		RestResponse addCategoryResponse = http.httpSendPost(url, subCatJson, headersMap);
		return addCategoryResponse;
	}

	public static RestResponse deleteCatergoryHttpCspAtuUidIsMissing(CategoryDefinition categoryDataDefinition,
			User sdncModifierDetails) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_CONSUMER, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryDataDefinition.getName());

		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		headersMap.remove("USER_ID");
		Gson gson = new Gson();
		String userBodyJson = gson.toJson(categoryDataDefinition);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse deleteCategotyResponse = http.httpSendDelete(url, headersMap);
		return deleteCategotyResponse;
	}

	public static RestResponse createGrouping(GroupingDefinition grouping, SubCategoryDefinition subCategory,
			CategoryDefinition parentCategory, User sdncModifierDetails, String categoryType) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_GROUPING, config.getCatalogBeHost(), config.getCatalogBePort(),
				categoryType, parentCategory.getUniqueId(), subCategory.getUniqueId());
		String bodyJson = gson.toJson(grouping);
		RestResponse addGroupingResponse = BaseRestUtils.sendPost(url, bodyJson, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		return addGroupingResponse;
	}

	public static RestResponse importCategories(MultipartEntityBuilder mpBuilder, String userId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.IMPORT_CATEGORIES, config.getCatalogBeHost(), config.getCatalogBePort());

		RestResponse importResponse = BaseRestUtils.sendPost(url, mpBuilder.build(), userId, acceptHeaderData);
		return importResponse;
	}

	public static int getMatchingCategoriesNum(RestResponse getAllCategoryRest, CategoryDefinition categoryDefinition) {
		String response = getAllCategoryRest.getResponse();
		Gson gson = new Gson();
		List<CategoryDefinition> categoryDefinitions = gson.fromJson(response,
				new TypeToken<List<CategoryDefinition>>() {
				}.getType());
		int categoriesNum = 0;
		String catName = categoryDefinition.getName();
		for (CategoryDefinition elem : categoryDefinitions) {
			if (elem.getName().equals(catName)) {
				categoryDefinition.setUniqueId(elem.getUniqueId());
				categoriesNum++;
			}
		}

		return categoriesNum;
	}

	public static int getMatchingSubCategoriesNum(RestResponse getAllCategoryRest, String parentCategoryId,
			SubCategoryDefinition expectedSubCategoryDefinition) {

		String response = getAllCategoryRest.getResponse();
		Gson gson = new Gson();
		List<CategoryDefinition> categoryDefinitions = gson.fromJson(response,
				new TypeToken<List<CategoryDefinition>>() {
				}.getType());
		int subCatNum = 0;
		String subCatName = expectedSubCategoryDefinition.getName();
		for (CategoryDefinition elem : categoryDefinitions) {
			if (elem.getUniqueId().equals(parentCategoryId)) {
				List<SubCategoryDefinition> subCategories = elem.getSubcategories();
				if (subCategories != null) {
					for (SubCategoryDefinition subCategoryDataDefinition : subCategories) {
						if (subCatName.equals(subCategoryDataDefinition.getName())) {
							expectedSubCategoryDefinition.setUniqueId(subCategoryDataDefinition.getUniqueId());
							subCatNum++;
						}
					}
				}

			}
		}
		return subCatNum;
	}

	public static int getMatchingGroupingNum(RestResponse getAllCategoryRest, String parentCategoryId,
			String subCategoryId, GroupingDefinition expectedGroupingDefinition) {

		String response = getAllCategoryRest.getResponse();
		Gson gson = new Gson();
		List<CategoryDefinition> categoryDefinitions = gson.fromJson(response,
				new TypeToken<List<CategoryDefinition>>() {
				}.getType());
		int groupingNum = 0;
		String groupingName = expectedGroupingDefinition.getName();
		for (CategoryDefinition elem : categoryDefinitions) {
			if (elem.getUniqueId().equals(parentCategoryId)) {
				List<SubCategoryDefinition> subCategories = elem.getSubcategories();
				if (subCategories != null) {
					for (SubCategoryDefinition subCategoryDataDefinition : subCategories) {
						// if
						// (subCategoryId.equals(subCategoryDataDefinition.getUniqueId()))
						// {
						if (subCategoryId.equals(subCategoryDataDefinition.getUniqueId())
								&& subCategoryDataDefinition.getGroupings() != null) {
							List<GroupingDefinition> grouping = subCategoryDataDefinition.getGroupings();
							for (GroupingDefinition groupingDataDefinition : grouping) {
								if (groupingName.equals(groupingDataDefinition.getName())) {
									expectedGroupingDefinition.setUniqueId(groupingDataDefinition.getUniqueId());
									groupingNum++;
								}
							}

						}
					}
				}

			}
		}
		return groupingNum;
	}

	public enum CategoryAuditJsonKeysEnum {
		ACTION("ACTION"), MODIFIER("MODIFIER"), CATEGORY_NAME("CATEGORY_NAME"), SUB_CATEGORY_NAME("SUB_CATEGORY_NAME"), GROUPING_NAME("GROUPING_NAME"), RESOURCE_TYPE("RESOURCE_TYPE"), ECOMP_USER("ECOMP_USER"), STATUS("STATUS"), DESCRIPTION("DESCRIPTION"), DETAILS("DETAILS");
		
		private String auditJsonKeyName;

		private CategoryAuditJsonKeysEnum(String auditJsonKeyName) {
			this.auditJsonKeyName = auditJsonKeyName;
		}

		public String getAuditJsonKeyName() {
			return auditJsonKeyName.toLowerCase();
		}
	}
	

}
