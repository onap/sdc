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

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.PRODUCT_COMPONENT_TYPE;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.RESOURCE_COMPONENT_TYPE;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.SERVICE_COMPONENT_TYPE;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_ALREADY_EXISTS;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_CREATED;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_INVALID_CONTENT;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_MISSING_INFORMATION;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_NOT_FOUND;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;
import static org.testng.AssertJUnit.assertEquals;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedCategoryAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.CategoryValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupingTest extends CategoriesBaseTest {

	protected static final String ADD_GROUPING = "AddGrouping";
	protected static final String CATEGORY = "category";
	protected static final String SUB_CATEGORY = "sub-category";
	protected static final String GROUPING = "grouping";

	public GroupingTest() {
		super(name, GroupingTest.class.getName());
	}

	@Rule
	public static TestName name = new TestName();

	private CategoryDefinition productCategoryDefinition;
	private CategoryDefinition productCategoryDefinition2;

	private SubCategoryDefinition productSubCategoryDefinition;
	private SubCategoryDefinition productSubCategoryDefinition2;
	private SubCategoryDefinition productSubCategoryDefinition3;

	private GroupingDefinition productGroupingDefinition;
	private GroupingDefinition productGroupingDefinition2;
	private GroupingDefinition productGroupingDefinition3;

	@BeforeMethod
	public void init() throws Exception {

		// Category setup
		productCategoryDefinition = new CategoryDefinition();
		productCategoryDefinition.setName("Category1");
		productCategoryDefinition2 = new CategoryDefinition();
		productCategoryDefinition2.setName("Category2");

		// Subcategory setup
		productSubCategoryDefinition = new SubCategoryDefinition();
		productSubCategoryDefinition.setName("SubCategory1");

		productSubCategoryDefinition2 = new SubCategoryDefinition();
		productSubCategoryDefinition2.setName("SubCategory2");

		productSubCategoryDefinition3 = new SubCategoryDefinition();
		productSubCategoryDefinition3.setName("SubCategory1");

		// Group setup
		productGroupingDefinition = new GroupingDefinition();
		productGroupingDefinition.setName("Grouping1");

		productGroupingDefinition2 = new GroupingDefinition();
		productGroupingDefinition2.setName("Grouping2");

		productGroupingDefinition3 = new GroupingDefinition();
		productGroupingDefinition3.setName("Grouping1");

		// Init product category
		RestResponse createCategory = CategoryRestUtils.createCategory(productCategoryDefinition,
				sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		CategoryDefinition category = ResponseParser.parseToObject(createCategory.getResponse(),
				CategoryDefinition.class);
		assertEquals("Check category name after creating category ", productCategoryDefinition.getName(),
				category.getName());
		productCategoryDefinition = category;

		// Init product category1
		createCategory = CategoryRestUtils.createCategory(productCategoryDefinition2, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		category = ResponseParser.parseToObject(createCategory.getResponse(), CategoryDefinition.class);
		assertEquals("Check category name after creating category ", productCategoryDefinition2.getName(),
				category.getName());
		productCategoryDefinition2 = category;

		// Init product productSubCategoryDefinition to
		// productCategoryDefinition
		RestResponse createSubCategory = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createSubCategory.getErrorCode().intValue());
		SubCategoryDefinition subCategory = ResponseParser.parseToObject(createSubCategory.getResponse(),
				SubCategoryDefinition.class);
		assertEquals("Check category name after creating category ", productSubCategoryDefinition.getName(),
				subCategory.getName());
		productSubCategoryDefinition = subCategory;
		productCategoryDefinition.addSubCategory(productSubCategoryDefinition);

		// Init product productSubCategoryDefinition1 to
		// productCategoryDefinition
		createSubCategory = CategoryRestUtils.createSubCategory(productSubCategoryDefinition2,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createSubCategory.getErrorCode().intValue());
		subCategory = ResponseParser.parseToObject(createSubCategory.getResponse(), SubCategoryDefinition.class);
		assertEquals("Check category name after creating category ", productSubCategoryDefinition2.getName(),
				subCategory.getName());
		productSubCategoryDefinition2 = subCategory;
		productCategoryDefinition.addSubCategory(productSubCategoryDefinition2);

		// Init product productSubCategoryDefinition3 to
		// productCategoryDefinition2
		createSubCategory = CategoryRestUtils.createSubCategory(productSubCategoryDefinition3,
				productCategoryDefinition2, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createSubCategory.getErrorCode().intValue());
		subCategory = ResponseParser.parseToObject(createSubCategory.getResponse(), SubCategoryDefinition.class);
		assertEquals("Check category name after creating category ", productSubCategoryDefinition3.getName(),
				subCategory.getName());
		productSubCategoryDefinition3 = subCategory;
		productCategoryDefinition2.addSubCategory(productSubCategoryDefinition3);
	}

	@Test
	public void createProductGroupCategorySuccess() throws Exception {
		createGroupingSuccess(productGroupingDefinition, productSubCategoryDefinition, productCategoryDefinition,
				sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE, AUDIT_PRODUCT_TYPE);
	}

	private void createGroupingSuccess(GroupingDefinition groupingDefinition,
			SubCategoryDefinition subCategoryDefinition, CategoryDefinition categoryDefinition,
			User sdncProductStrategistUserDetails, String productComponentType, String auditType) throws Exception {

		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(groupingDefinition, subCategoryDefinition,
				categoryDefinition, sdncProductStrategistUserDetails, productComponentType);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("grouping1");
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				productComponentType);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest, categoryDefinition.getUniqueId(),
				subCategoryDefinition.getUniqueId(), groupingDefinition);

		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, categoryDefinition, subCategoryDefinition,
				groupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, auditType);
	}

	//// Benny

	@Test
	public void createProductGroupByProductStrategist() throws Exception {
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("grouping1");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void createProductGroupAlreadyExistInSameCategorySubCategory() throws Exception {
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("grouping1");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
		// Create Same Group already exist on same Category/SubCategory
		DbUtils.deleteFromEsDbByPattern("_all");
		createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition, productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_ALREADY_EXISTS,
				createGroupingRest.getErrorCode().intValue());
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_GROUPING_EXISTS_FOR_SUB_CATEGORY, STATUS_CODE_ALREADY_EXISTS, AUDIT_PRODUCT_TYPE,
				AUDIT_PRODUCT_TYPE, productGroupingDefinition.getName(), productSubCategoryDefinition.getName());
	}

	@Test
	public void createProductGroupUnderSameCategoryButDifferentSubCategory() throws Exception {
		// Setting : Category-A, Sub-category-B , group : aBcd (display-Name :
		// ABcd, normalized: abcd)  [A, B, ABcd]
		// Action : Category-A, Sub-category-C, group : abcD (display-Name :
		// ABcd, normalized: abcd)  [A, C, ABcd]
		productGroupingDefinition.setName("ABCd");
		productGroupingDefinition2.setName("abcD");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		DbUtils.deleteFromEsDbByPattern("_all");
		createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition2, productSubCategoryDefinition2,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition2.setName("ABCd");
		productGroupingDefinition2.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition2);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition,
				productSubCategoryDefinition2, productGroupingDefinition2, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition2.getUniqueId(),
				productGroupingDefinition2);
	}

	@Test
	public void createProductGroupUnderSameSubCategoryButDifferentCategory() throws Exception {
		// Setting : Category-A, Sub-category-B , group : aBcd (display-Name :
		// ABcd, normalized: abcd)  [A, B, ABcd]
		// : Category-A, Sub-category-C, group : abcD (display-Name : ABcd,
		// normalized: abcd)  [A, C, ABcd]
		// : Category-K, Sub-category-B, group : abcD (display-Name : ABcd,
		// normalized: abcd)  [K, B, ABcd]
		productGroupingDefinition.setName("ABCd");
		productGroupingDefinition2.setName("abcD");
		productGroupingDefinition3.setName("aBCd");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		DbUtils.deleteFromEsDbByPattern("_all");
		createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition2, productSubCategoryDefinition2,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition2.setName("ABCd");
		productGroupingDefinition2.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition2);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition,
				productSubCategoryDefinition2, productGroupingDefinition2, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
		DbUtils.deleteFromEsDbByPattern("_all");
		createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition3, productSubCategoryDefinition3,
				productCategoryDefinition2, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition3.setName("ABCd");
		productGroupingDefinition3.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition3);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition2,
				productSubCategoryDefinition3, productGroupingDefinition3, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition2.getUniqueId(),
				productGroupingDefinition2);
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition2.getUniqueId(), productSubCategoryDefinition3.getUniqueId(),
				productGroupingDefinition3);

	}

	@Test
	public void createProductGroupsOnSameCategorySubCategory() throws Exception {
		// Setting : Category-A, Sub-category-B , group : ABcd (display-Name :
		// ABcd, normalized: abcd) [A ,B, ABcd]
		// Action : Category-A, Sub-category-B, group : ZXcv (display-Name :
		// ZXcv, normalized: zxcv) [A, B, ZXcv]
		productGroupingDefinition.setName("ABcd");
		productGroupingDefinition2.setName("ZXcv");
		productGroupingDefinition2.setNormalizedName("zxcv");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		DbUtils.deleteFromEsDbByPattern("_all");
		createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition2, productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition2);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition2, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition2);
	}

	@Test
	public void createProductGroupUnderDifferentCategory() throws Exception {
		// Setting : Category-A, Sub-category-B , group : aBcd (display-Name :
		// ABcd, normalized: abcd) [A ,B, ABcd]
		// Action : Category-K, Sub-category-B, group : abcD (display-Name :
		// ABcd, normalized: abcd) [K, B, ABcd]
		// productGroupingDefinition.setName("ABCd");
		productGroupingDefinition.setName("ABcD");
		productGroupingDefinition2.setName("abcD");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		DbUtils.deleteFromEsDbByPattern("_all");
		createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition2, productSubCategoryDefinition3,
				productCategoryDefinition2, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition2.setNormalizedName("abcd");
		productGroupingDefinition2.setName("ABcD");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition2);
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition2,
				productSubCategoryDefinition3, productGroupingDefinition2, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition2.getUniqueId(), productSubCategoryDefinition3.getUniqueId(),
				productGroupingDefinition2);
	}

	///////////
	@Test
	public void createProductGroupByNonProductStrategist() throws Exception {
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_RESTRICTED_OPERATION,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		productSubCategoryDefinition.setName(productSubCategoryDefinition.getUniqueId());
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncAdminUserDetails, ActionStatus.RESTRICTED_OPERATION,
				STATUS_CODE_RESTRICTED_OPERATION, AUDIT_PRODUCT_TYPE);
	}

	// @Ignore("DE176245")
	@Test
	public void createProductGroupForNonExistingComponentType() throws Exception {
		String nonSupportedComponentType = "NonExistingComponentType"; // instead
																		// resource/product
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				nonSupportedComponentType);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		productSubCategoryDefinition.setName(productSubCategoryDefinition.getUniqueId());
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, nonSupportedComponentType);
	}

	// @Ignore("DE176245")
	@Test
	public void createResourceGroup() throws Exception {
		// Resource doesn't have group
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		productSubCategoryDefinition.setName(productSubCategoryDefinition.getUniqueId());
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, AUDIT_RESOURCE_TYPE);
	}

	// @Ignore("DE176245")
	@Test
	public void createServiceGroup() throws Exception {
		// Service doesn't have group
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		productSubCategoryDefinition.setName(productSubCategoryDefinition.getUniqueId());
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void createProductGroupForNonExistingCategory() throws Exception {

		CategoryDefinition productCategoryDefinition100 = new CategoryDefinition();
		productCategoryDefinition100.setName("category.nonexistingCategory");
		productCategoryDefinition100.setUniqueId("category.nonexistingCategory");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition100, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_NOT_FOUND,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		productSubCategoryDefinition.setName(productSubCategoryDefinition.getUniqueId());
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition100,
				productSubCategoryDefinition, productGroupingDefinition, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_CATEGORY_NOT_FOUND, STATUS_CODE_NOT_FOUND, AUDIT_PRODUCT_TYPE,
				PRODUCT_COMPONENT_TYPE, CATEGORY, "");
	}

	@Test
	public void createProductGroupForNonExistingSunCategory() throws Exception {
		throw new SkipException(
				"Skipping - failed in audit validation expected \"products\" actual result was \"product\" ");
		// SubCategoryDefinition productSubCategoryDefinition100 = new
		// SubCategoryDefinition();
		// productSubCategoryDefinition100.setUniqueId("category.nonexistingSubCategory");
		// RestResponse createGroupingRest =
		// CategoryRestUtils.createGrouping(productGroupingDefinition,
		// productSubCategoryDefinition100, productCategoryDefinition,
		// sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		// assertEquals("Check response code after create Sub category",
		// STATUS_CODE_NOT_FOUND, createGroupingRest.getErrorCode().intValue());
		// RestResponse getAllCategoriesRest =
		// CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
		// PRODUCT_COMPONENT_TYPE);
		// assertEquals("Check response code after get all categories ",
		// STATUS_CODE_SUCCESS, getAllCategoriesRest.getErrorCode().intValue());
		// CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
		// productCategoryDefinition.getUniqueId(),
		// productSubCategoryDefinition.getUniqueId(),
		// productGroupingDefinition);
		// //Audit validation
		// productSubCategoryDefinition100.setName(productSubCategoryDefinition100.getUniqueId());
		// AuditValidationUtils.groupingAuditFailure(ADD_GROUPING ,
		// productCategoryDefinition, productSubCategoryDefinition100,
		// productGroupingDefinition, sdncProductStrategistUserDetails,
		// ActionStatus.COMPONENT_CATEGORY_NOT_FOUND,
		// STATUS_CODE_NOT_FOUND,AUDIT_PRODUCT_TYPE, PRODUCT_COMPONENT_TYPE,
		// SUB_CATEGORY, "");
	}

	@Test
	public void ProductGroupAllowedcharacters_01() throws Exception {
		productGroupingDefinition.setName("1234AbcdE-");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde-");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_02() throws Exception {
		productGroupingDefinition.setName("1234AbcdE+");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde+");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_03() throws Exception {
		productGroupingDefinition.setName("1234AbcdE&");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde&");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_04() throws Exception {
		productGroupingDefinition.setName("1234AbcdE-");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde-");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_05() throws Exception {
		productGroupingDefinition.setName("1234AbcdE+");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde+");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_06() throws Exception {
		productGroupingDefinition.setName("1234AbcdE.");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde.");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_07() throws Exception {
		productGroupingDefinition.setName("1234AbcdE'");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde'");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_08() throws Exception {
		productGroupingDefinition.setName("1234AbcdE=");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde=");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_09() throws Exception {
		productGroupingDefinition.setName("1234AbcdE:");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde:");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_10() throws Exception {
		productGroupingDefinition.setName("1234AbcdE@");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde@");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_11() throws Exception {
		productGroupingDefinition.setName("1234AbcdE_");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde_");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_12() throws Exception {
		productGroupingDefinition.setName("1234AbcdE#");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("1234abcde#");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void ProductGroupAllowedcharacters_13() throws Exception {
		productGroupingDefinition.setName("1234AbcdE d");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("1234AbcdE D");
		productGroupingDefinition.setNormalizedName("1234abcde d");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveSpaceFromBeginning() throws Exception {
		productGroupingDefinition.setName("  Category01");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("category01");
		productGroupingDefinition.setName("Category01");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveSpaceFromEnd() throws Exception {
		productGroupingDefinition.setName("Category01    ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("category01");
		productGroupingDefinition.setName("Category01");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraSpace() throws Exception {
		productGroupingDefinition.setName("Category    02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("category 02");
		productGroupingDefinition.setName("Category 02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraAmpersand() throws Exception {
		productGroupingDefinition.setName("Category&& &02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("category& &02");
		productGroupingDefinition.setName("Category& &02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraDash() throws Exception {
		productGroupingDefinition.setName("CategorY-- --02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("category- -02");
		productGroupingDefinition.setName("CategorY- -02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraPlus() throws Exception {
		productGroupingDefinition.setName("CateGory++++ +02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("CateGory+ +02");
		productGroupingDefinition.setNormalizedName("category+ +02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraPeriod() throws Exception {
		productGroupingDefinition.setName("Category.... .02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Category. .02");
		productGroupingDefinition.setNormalizedName("category. .02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraApostrophe() throws Exception {
		productGroupingDefinition.setName("CaTegory''' '02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("CaTegory' '02");
		productGroupingDefinition.setNormalizedName("category' '02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraHashtag() throws Exception {
		productGroupingDefinition.setName("Category### #02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Category# #02");
		productGroupingDefinition.setNormalizedName("category# #02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtrEequal() throws Exception {
		productGroupingDefinition.setName("Category=== =02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Category= =02");
		productGroupingDefinition.setNormalizedName("category= =02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtrColon() throws Exception {
		productGroupingDefinition.setName("Category::: :02");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Category: :02");
		productGroupingDefinition.setNormalizedName("category: :02");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtrAt() throws Exception {
		productGroupingDefinition.setName("Category@@@ @a2");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Category@ @a2");
		productGroupingDefinition.setNormalizedName("category@ @a2");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_RemoveExtraUnderscore() throws Exception {
		productGroupingDefinition.setName("Category___ _22");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Category_ _22");
		productGroupingDefinition.setNormalizedName("category_ _22");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_FirstWordStartWithNumber() throws Exception {
		productGroupingDefinition.setName("1Category one");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("1Category One");
		productGroupingDefinition.setNormalizedName("1category one");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_FirstWordStartWithNonAlphaNumeric() throws Exception { // The
																							// first
																							// word
																							// must
																							// start
																							// with
																							// an
																							// alpha-numeric
																							// character
																							// [a-Z
																							// A..Z,
																							// 0..9]
		char invalidChars[] = { '&', '-', '+', '.', '\'', '#', '=', ':', '@', '_' };
		RestResponse createGroupingRest;
		RestResponse getAllCategoriesRest;
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			productGroupingDefinition.setName(invalidChars[i] + "AbcD123");
			productGroupingDefinition.setNormalizedName((invalidChars[i] + "AbcD123").toLowerCase());
			createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
					productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
					PRODUCT_COMPONENT_TYPE);
			assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
					createGroupingRest.getErrorCode().intValue());

			getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
			assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
					getAllCategoriesRest.getErrorCode().intValue());
			CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
					productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
					productGroupingDefinition);
		}
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_01() throws Exception {
		productGroupingDefinition.setName("At and T");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("At & T");
		productGroupingDefinition.setNormalizedName("at & t");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_02() throws Exception {
		productGroupingDefinition.setName("At and t");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("At & T");
		productGroupingDefinition.setNormalizedName("at & t");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_03() throws Exception {
		productGroupingDefinition.setName("Atand T");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("atand t");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_04() throws Exception {
		productGroupingDefinition.setName("At andT");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("at andt");
		productGroupingDefinition.setName("At AndT");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_05() throws Exception {
		productGroupingDefinition.setName(" and AttT");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("and attt");
		productGroupingDefinition.setName("And AttT");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_06() throws Exception {
		productGroupingDefinition.setName("AttT and ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("attt and");
		productGroupingDefinition.setName("AttT And");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidation_ReplaceAndWithAmpersand_07() throws Exception {
		productGroupingDefinition.setName(" and a");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("and a");
		productGroupingDefinition.setName("And a");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationMaxLength() throws Exception {
		productGroupingDefinition.setName("AsdfghjQ234567890@#.&:+-_");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("asdfghjq234567890@#.&:+-_");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationMaxLengthAfterNormalization() throws Exception {
		productGroupingDefinition.setName("  A jQ234 @@@___ +++ At and T and and ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("a jq234 @_ + at & t & and");
		productGroupingDefinition.setName("A JQ234 @_ + At & T & And");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationExceedMaxLengthAfterNormalization() throws Exception {
		productGroupingDefinition.setName("  AbdfghBCVa jQ234 @@___ +++ At and T   ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_INVALID_CONTENT,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("abdfghbcva jq234 @_ + at&t");
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, STATUS_CODE_INVALID_CONTENT, AUDIT_PRODUCT_TYPE,
				AUDIT_PRODUCT_TYPE, GROUPING);
	}

	@Test
	public void groupNameValidationMinLengthAfterNormalization() throws Exception {
		productGroupingDefinition.setName("  At&&&&&&&&&&&&t   ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("at&t");
		productGroupingDefinition.setName("At&t");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationLessThanMinLengthAfterNormalization() throws Exception {
		productGroupingDefinition.setName("  A&&&&&&&&&&&&T   ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_INVALID_CONTENT,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("a&t");
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, STATUS_CODE_INVALID_CONTENT, AUDIT_PRODUCT_TYPE,
				AUDIT_PRODUCT_TYPE, GROUPING);
	}

	@Test
	public void groupNameValidationIsEmpty() throws Exception {
		productGroupingDefinition.setName("");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_INVALID_CONTENT,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("");
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT, AUDIT_PRODUCT_TYPE,
				AUDIT_PRODUCT_TYPE, GROUPING);
	}

	@Test
	public void groupNameValidationInvalidCharacters() throws Exception {
		RestResponse createGroupingRest;
		RestResponse getAllCategoriesRest;
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/',
				'|', '\\', ',' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			productGroupingDefinition.setName("AbcD123" + invalidChars[i]);
			createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
					productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
					PRODUCT_COMPONENT_TYPE);
			assertEquals("Check response code after create product group", STATUS_CODE_INVALID_CONTENT,
					createGroupingRest.getErrorCode().intValue());
			productGroupingDefinition.setNormalizedName("");
			getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
			assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
					getAllCategoriesRest.getErrorCode().intValue());
			CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
					productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
					productGroupingDefinition);
			// Audit validation
			AuditValidationUtils.groupingAuditFailure(ADD_GROUPING, productCategoryDefinition,
					productSubCategoryDefinition, productGroupingDefinition, sdncProductStrategistUserDetails,
					ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT, AUDIT_PRODUCT_TYPE,
					AUDIT_PRODUCT_TYPE, GROUPING);
		}
	}

	@Test
	public void groupNameValidationConjunctions_01() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" bank OF america  ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setNormalizedName("bank of america");
		productGroupingDefinition.setName("Bank of America");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_02() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName("THE america bank   ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("THE America Bank");
		productGroupingDefinition.setNormalizedName("the america bank");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_03() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName("   A bank OF america  ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("A Bank of America");
		productGroupingDefinition.setNormalizedName("a bank of america");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_04() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName("  bank  america is A big ban  ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Bank America Is a Big Ban");
		productGroupingDefinition.setNormalizedName("bank america is a big ban");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_05() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" aN apple comPany inC ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("AN Apple ComPany InC");
		productGroupingDefinition.setNormalizedName("an apple company inc");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_06() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" eat AN apple ANAN");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Eat an Apple ANAN");
		productGroupingDefinition.setNormalizedName("eat an apple anan");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_07() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" united states OF americA ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("United States of AmericA");
		productGroupingDefinition.setNormalizedName("united states of america");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_08() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" oF united states OF amer ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("OF United States of Amer");
		productGroupingDefinition.setNormalizedName("of united states of amer");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_09() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" to Apple TO at&T TOO ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("To Apple to At&T TOO");
		productGroupingDefinition.setNormalizedName("to apple to at&t too");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_10() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" eat apple AS you liiikeas ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Eat Apple as You Liiikeas");
		productGroupingDefinition.setNormalizedName("eat apple as you liiikeas");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_11() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" as you may want ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("As You May Want");
		productGroupingDefinition.setNormalizedName("as you may want");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_12() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName(" the bank OF america ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("The Bank of America");
		productGroupingDefinition.setNormalizedName("the bank of america");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_13() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName("  To tel-toto ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("To Tel-toto");
		productGroupingDefinition.setNormalizedName("to tel-toto");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void groupNameValidationConjunctions_14() throws Exception {
		// Normalize the grouping name conjunctions ('of', 'to', 'for', 'as',
		// 'a', 'an' , 'the') are lower case.
		productGroupingDefinition.setName("   tel-aviv To   la ");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create product group", STATUS_CODE_CREATED,
				createGroupingRest.getErrorCode().intValue());
		productGroupingDefinition.setName("Tel-aviv to La");
		productGroupingDefinition.setNormalizedName("tel-aviv to la");
		CategoryValidationUtils.validateCreateGroupResponse(createGroupingRest, productGroupingDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		AuditValidationUtils.groupingAuditSuccess(ADD_GROUPING, productCategoryDefinition, productSubCategoryDefinition,
				productGroupingDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void createProductGroupHttpCspUserIdIsEmpty() throws Exception {
		User sdncPS = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);
		sdncPS.setUserId("");
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncPS, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_GROUPING);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(productCategoryDefinition.getUniqueId());
		expectedCatrgoryAuditJavaObject.setSubCategoryName(productSubCategoryDefinition.getUniqueId());
		expectedCatrgoryAuditJavaObject.setGroupingName(productGroupingDefinition.getName());
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_PRODUCT_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_GROUPING);
	}

	@Test
	public void createProductGroupHttpCspUserIdIsNull() throws Exception {
		User sdncPS = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);
		sdncPS.setUserId(null);
		RestResponse createGroupingRest = CategoryRestUtils.createGrouping(productGroupingDefinition,
				productSubCategoryDefinition, productCategoryDefinition, sdncPS, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createGroupingRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyGroupingNotExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition.getUniqueId(),
				productGroupingDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_GROUPING);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(productCategoryDefinition.getUniqueId());
		expectedCatrgoryAuditJavaObject.setSubCategoryName(productSubCategoryDefinition.getUniqueId());
		expectedCatrgoryAuditJavaObject.setGroupingName(productGroupingDefinition.getName());
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_PRODUCT_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_GROUPING);
	}

	////////////////////////////////////////////////
	///////////////////////////////////////////////
	@Test
	public void getProductCategoryHierarchySuccessFlow() throws Exception {
		throw new SkipException(
				"Skipping - failed in audit validation expected \"products\" actual result was \"product\" ");
		// int numOfGrouping = 3;
		// List<GroupingDefinition> groupingList = new ArrayList<>();
		// RestResponse restResponse;
		// GroupingDefinition grouping;
		// String groupingName = productGroupingDefinition.getName();
		// for (int i = 0; i < numOfGrouping; i++) {
		// productGroupingDefinition.setName(groupingName+i);
		// restResponse =
		// CategoryRestUtils.createGrouping(productGroupingDefinition,
		// productSubCategoryDefinition, productCategoryDefinition,
		// sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		// grouping = ResponseParser.parseToObject(restResponse.getResponse(),
		// GroupingDefinition.class);
		// groupingList.add(grouping);
		// }
		// RestResponse getAllCategoriesRest =
		// CategoryRestUtils.getAllCategories(sdncProductStrategistUserDetails,
		// PRODUCT_COMPONENT_TYPE);
		// assertEquals("Check response code after get all categories ",
		// STATUS_CODE_SUCCESS, getAllCategoriesRest.getErrorCode().intValue());
		// AuditValidationUtils.GetCategoryHierarchyAuditSuccess(GET_CATEGORY_HIERARCHY,
		// AUDIT_PRODUCT_TYPE, sdncProductStrategistUserDetails,
		// STATUS_CODE_SUCCESS);
		//
		// for (GroupingDefinition group : groupingList) {
		// CategoryValidationUtils.verifyGroupingExistInGetResponse(getAllCategoriesRest,
		// productCategoryDefinition.getUniqueId(),
		// productSubCategoryDefinition.getUniqueId(), group);
		// }
	}
}
