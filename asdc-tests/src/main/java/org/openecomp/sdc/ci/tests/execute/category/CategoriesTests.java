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
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedCategoryAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.CategoryValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CategoriesTests extends CategoriesBaseTest {

	private static final String GET_CATEGORY_HIERARCHY = "GetCategoryHierarchy";
	protected static final String ADD_CATEGORY = "AddCategory";
	protected static final String DELETE_CATEGORY = "DeleteCategory";

	public CategoriesTests() {
		super(name, CategoriesTests.class.getName());
	}

	@Rule
	public static TestName name = new TestName();
	private CategoryDefinition categoryDefinition;
	private List<CategoryDefinition> categoryList;
	private List<SubCategoryDefinition> subCategoryList;
	private Map<String, List<String>> subCategoriesToDeleteMap;

	@BeforeMethod
	public void init() throws Exception {
		subCategoriesToDeleteMap = new HashMap<String, List<String>>();
		DbUtils.deleteFromEsDbByPattern("_all");

		categoryDefinition = new CategoryDefinition();
		categoryDefinition.setName("Abcd");
		categoryList = defineCategories();
		subCategoryList = defineSubCategories(categoryList.size());
	}

	// pass
	@Test
	public void createServiceCategorySuccessFlow() throws Exception {
		// Add New category
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
		// get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition); // also
																											// set
																											// catalog
																											// uniqeId

	}

	// pass
	@Test
	public void createResourceCategorySuccessFlow() throws Exception {
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get Category
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	// pass
	@Test
	public void createProductCategorySuccessFlow() throws Exception {
		// Add Category by Product-strategist
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition,
				sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);

		// Get Category
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void CategoryNameValidation_FirstWordStartWithAlphaNumeric_01() throws Exception { // category
																								// for
																								// service
		categoryDefinition.setName("Category14AadE  &&&---+++.'''###=:@@@____");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category14AadE &-+.'#=:@_");
		categoryDefinition.setNormalizedName("category14aade &-+.'#=:@_");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_FirstWordStartWithAlphaNumeric_02() throws Exception { // category
																								// for
																								// resource
		categoryDefinition.setName("Category14AadE  &&&---+++.'''###=:@@@____");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category14AadE &-+.'#=:@_");
		categoryDefinition.setNormalizedName("category14aade &-+.'#=:@_");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void categoryNameValidation_FirstWordStartWithAlphaNumeric_03() throws Exception { // category
																								// for
																								// resource
		categoryDefinition.setName("Category14AadE  &&&---+++.'''###=:@@@____");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition,
				sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category14AadE &-+.'#=:@_");
		categoryDefinition.setNormalizedName("category14aade &-+.'#=:@_");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	// pass
	@Test
	public void createServiceCategoryByNonAdminUser() throws Exception {
		// Add New category
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition,
				sdncProductStrategistUserDetails, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_RESTRICTED_OPERATION,
				createCategotyRest.getErrorCode().intValue());
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncProductStrategistUserDetails,
				ActionStatus.RESTRICTED_OPERATION, STATUS_CODE_RESTRICTED_OPERATION, AUDIT_SERVICE_TYPE);
	}

	// pass
	@Test
	public void createResourceCategoryByNonAdminUser() throws Exception {
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition,
				sdncProductStrategistUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_RESTRICTED_OPERATION,
				createCategotyRest.getErrorCode().intValue());
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncProductStrategistUserDetails,
				ActionStatus.RESTRICTED_OPERATION, STATUS_CODE_RESTRICTED_OPERATION, AUDIT_RESOURCE_TYPE);
	}

	// pass
	@Test
	public void createProductCategoryByNonProductStrategistUser() throws Exception {
		// Add New product category not by Product-Strategist
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_RESTRICTED_OPERATION,
				createCategotyRest.getErrorCode().intValue());
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				ActionStatus.RESTRICTED_OPERATION, STATUS_CODE_RESTRICTED_OPERATION, AUDIT_PRODUCT_TYPE);

	}

	// pass
	@Test
	public void addCategoryByNonExistingUser() throws Exception {
		User sdncAdminUserDetailsNonExisting = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncAdminUserDetailsNonExisting.setUserId("bt555h");
		// Add New category
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition,
				sdncAdminUserDetailsNonExisting, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_RESTRICTED_OPERATION,
				createCategotyRest.getErrorCode().intValue());
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_CATEGORY);
		expectedCatrgoryAuditJavaObject.setModifier("(" + sdncAdminUserDetailsNonExisting.getUserId() + ")");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDefinition.getName());
		expectedCatrgoryAuditJavaObject.setSubCategoryName("");
		expectedCatrgoryAuditJavaObject.setGroupingName("");
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_SERVICE_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_RESTRICTED_OPERATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_CATEGORY);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_01() throws Exception {
		categoryDefinition.setName("1234AbcdE&");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde&"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_02() throws Exception {
		categoryDefinition.setName("1234AbcdE-");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde-"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_03() throws Exception {
		categoryDefinition.setName("1234AbcdE+");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde+"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_04() throws Exception {
		categoryDefinition.setName("1234AbcdE.");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde."); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_05() throws Exception {
		categoryDefinition.setName("1234AbcdE'");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde'");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_06() throws Exception {
		categoryDefinition.setName("1234AbcdE=");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde="); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_07() throws Exception {
		categoryDefinition.setName("1234AbcdE:");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde:"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_08() throws Exception {
		categoryDefinition.setName("1234AbcdE@");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde@"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_09() throws Exception {
		categoryDefinition.setName("1234AbcdE_");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde_"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_10() throws Exception {
		categoryDefinition.setName("1234AbcdE#");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde#"); // normalization
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_11() throws Exception {
		categoryDefinition.setName("1234AbcdE d");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde d"); // normalization
		categoryDefinition.setName("1234AbcdE D");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void addServiceCategoryAllowedcharacters_12() throws Exception {
		categoryDefinition.setName("1234AbcdE   &_=+.-'#:@ d");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("1234abcde &_=+.-'#:@ d"); // normalization
		categoryDefinition.setName("1234AbcdE &_=+.-'#:@ D");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveSpaceFromBeginning() throws Exception {
		categoryDefinition.setName("  Category01");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("category01"); // normalization
		categoryDefinition.setName("Category01");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveSpaceFromEnd() throws Exception {
		categoryDefinition.setName("Category01    ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("category01"); // normalization
		categoryDefinition.setName("Category01");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraSpace() throws Exception {
		categoryDefinition.setName("Category    02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("category 02"); // normalization
		categoryDefinition.setName("Category 02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraAmpersand() throws Exception {
		categoryDefinition.setName("Category&& &02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("category& &02"); // normalization
		categoryDefinition.setName("Category& &02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraDash() throws Exception {
		categoryDefinition.setName("CategorY-- --02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("CategorY- -02");
		categoryDefinition.setNormalizedName("category- -02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraPlus() throws Exception {
		categoryDefinition.setName("CateGory++++ +02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("CateGory+ +02");
		categoryDefinition.setNormalizedName("category+ +02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraPeriod() throws Exception {
		categoryDefinition.setName("Category.... .02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category. .02");
		categoryDefinition.setNormalizedName("category. .02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraApostrophe() throws Exception {
		categoryDefinition.setName("CaTegory''' '02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("CaTegory' '02");
		categoryDefinition.setNormalizedName("category' '02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraHashtag() throws Exception {
		categoryDefinition.setName("Category### #02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category# #02");
		categoryDefinition.setNormalizedName("category# #02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtrEequal() throws Exception {
		categoryDefinition.setName("Category=== =02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category= =02");
		categoryDefinition.setNormalizedName("category= =02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtrColon() throws Exception {
		categoryDefinition.setName("Category::: :02");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category: :02");
		categoryDefinition.setNormalizedName("category: :02");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtrAt() throws Exception {
		categoryDefinition.setName("Category@@@ @a2");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category@ @a2");
		categoryDefinition.setNormalizedName("category@ @a2");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_RemoveExtraUnderscore() throws Exception {
		categoryDefinition.setName("Category___ _22");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Category_ _22");
		categoryDefinition.setNormalizedName("category_ _22");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_FirstWordStartWithNumber() throws Exception {
		categoryDefinition.setName("1Category one");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("1Category One");
		categoryDefinition.setNormalizedName("1category one");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_FirstWordStartWithNonAlphaNumeric() throws Exception { // The
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
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			categoryDefinition.setName(invalidChars[i] + "AbcD123");
			categoryDefinition.setNormalizedName((invalidChars[i] + "AbcD123").toLowerCase());
			RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition,
					sdncAdminUserDetails1, SERVICE_COMPONENT_TYPE);
			assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
					createCategotyRest.getErrorCode().intValue());

			// get service category and validate that category was not added
			RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
					SERVICE_COMPONENT_TYPE);
			assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
					getAllCategoriesRest.getErrorCode().intValue());
			CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
			// Audit validation
			AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails1,
					ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE,
					"Service", "category");

		}
	}

	@Test
	public void addServiceCategoryAlreadyExist_uniqueness() throws Exception { // Verify
																				// category
																				// name
																				// duplication
																				// ("uniqueness")
																				// as
																				// non-case-sensitive,
																				// so
																				// we
																				// don’t
																				// create
																				// duplicate
																				// names
																				// with
																				// upper/lower
																				// case
																				// inconsistency.
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition); // also
																											// set
																											// catalog
																											// uniqeId
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
		// Create same category name again
		DbUtils.deleteFromEsDbByPattern("_all");
		CategoryDefinition categoryDataDefinition2 = new CategoryDefinition();
		categoryDataDefinition2.setName(categoryDefinition.getName());
		RestResponse addDuplicateCategoryRest = CategoryRestUtils.createCategory(categoryDataDefinition2,
				sdncAdminUserDetails, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_ALREADY_EXISTS,
				addDuplicateCategoryRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDataDefinition2, sdncAdminUserDetails,
				ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS, STATUS_CODE_ALREADY_EXISTS, AUDIT_SERVICE_TYPE,
				"Service", categoryDefinition.getName());
		// Get Category and verify that category was created is not deleted
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);

	}

	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_01() throws Exception {
		categoryDefinition.setName("At and T");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("At & T");
		categoryDefinition.setNormalizedName("at & t");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_02() throws Exception {
		categoryDefinition.setName("At and t");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("At & T");
		categoryDefinition.setNormalizedName("at & t");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_03() throws Exception {
		categoryDefinition.setName("Atand T");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("atand t");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_04() throws Exception {
		categoryDefinition.setName("At andT");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("at andt");
		categoryDefinition.setName("At AndT");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_05() throws Exception {
		categoryDefinition.setName(" and AttT");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("and attt");
		categoryDefinition.setName("And AttT");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_06() throws Exception {
		categoryDefinition.setName("AttT and ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("attt and");
		categoryDefinition.setName("AttT And");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	// Bug
	@Test
	public void categoryNameValidation_ReplaceAndWithAmpersand_07() throws Exception {
		categoryDefinition.setName(" and a");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("and a");
		categoryDefinition.setName("And a");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationMaxLength() throws Exception {
		categoryDefinition.setName("AsdfghjQ234567890@#.&:+-_");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("asdfghjq234567890@#.&:+-_");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);

	}

	@Test
	public void categoryNameValidationMaxLengthAfterNormalization() throws Exception {
		categoryDefinition.setName("  A jQ234 @@@___ +++ At and T and and ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("A JQ234 @_ + At & T & And");
		categoryDefinition.setNormalizedName("a jq234 @_ + at & t & and");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);

	}

	@Test
	public void categoryNameValidationExceedMaxLengthAfterNormalization() throws Exception {
		categoryDefinition.setName("  AbdfghBCVa jQ234 @@___ +++ At and T   ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("abdfghbcva jq234 @_ + at&t");
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE,
				"Service", "category");
	}

	@Test
	public void categoryNameValidationMinLengthAfterNormalization() throws Exception { // MinLengthAfterNormalization
																						// =
																						// 4
																						// characters
		categoryDefinition.setName("  At and  T   ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("At & T");
		categoryDefinition.setNormalizedName("at & t");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationLessThanMinLengthAfterNormalization() throws Exception {
		categoryDefinition.setName("  A&&&&&&&&&&&&&&&&&T   ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("a&t");
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE,
				"Service", "category");
	}

	@Test
	public void categoryNameValidationIsNull() throws Exception {
		categoryDefinition.setName(null);
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
				createCategotyRest.getErrorCode().intValue());
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE,
				"Service", "category");
	}

	@Test
	public void categoryNameValidationIsEmpty() throws Exception {
		categoryDefinition.setName("");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setNormalizedName("");
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE,
				"Service", "category");
	}

	@Test
	public void categoryNameValidationInvalidCharacters() throws Exception {
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/',
				'|', '\\', ',' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			// DbUtils.cleanAllAudits();
			categoryDefinition.setName("AbcD123" + invalidChars[i]);
			RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
					SERVICE_COMPONENT_TYPE);
			assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
					createCategotyRest.getErrorCode().intValue());
			categoryDefinition.setNormalizedName("");
			// get service category and validate that category was not added
			RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
					SERVICE_COMPONENT_TYPE);
			assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
					getAllCategoriesRest.getErrorCode().intValue());
			CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
			// Audit validation
			AuditValidationUtils.categoryAuditFailure(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
					ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE,
					"Service", "category");
		}
	}

	@Test
	public void categoryNameValidationSameNameDifferentResourceType() throws Exception { // same
																							// Catalog
																							// Name
																							// for
																							// service/resource/product
																							// is
																							// allowed
		String name = ("Abcd");
		CategoryDefinition categoryDataDefinition1 = new CategoryDefinition();
		CategoryDefinition categoryDataDefinition2 = new CategoryDefinition();
		CategoryDefinition categoryDataDefinition3 = new CategoryDefinition();
		categoryDataDefinition1.setName(name);
		categoryDataDefinition2.setName(name);
		categoryDataDefinition3.setName(name);
		// CREATE CATEGORY FOR SERVICE
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDataDefinition1,
				sdncAdminUserDetails, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDataDefinition1.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDataDefinition1);
		// get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDataDefinition1); // also
																													// set
																													// catalog
																													// uniqeId
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDataDefinition1, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
		// CREATE CATEGORY FOR RESOURCE_COMPONENT_TYPE
		DbUtils.deleteFromEsDbByPattern("_all");
		createCategotyRest = CategoryRestUtils.createCategory(categoryDataDefinition2, sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDataDefinition2.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDataDefinition2);
		// Get Category
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDataDefinition2);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDataDefinition2, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
		// CREATE CATEGORY FOR PRODUCT
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse addCategotyRest = CategoryRestUtils.createCategory(categoryDataDefinition3,
				sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				addCategotyRest.getErrorCode().intValue());
		categoryDataDefinition3.setNormalizedName("abcd");
		CategoryValidationUtils.validateCreateCategoryResponse(addCategotyRest, categoryDataDefinition3);

		// Get Category
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDataDefinition3);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncProductStrategistUserDetails,
				STATUS_CODE_CREATED, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void categoryNameValidationFirstLetterOfKeyWordsCapitalized() throws Exception { // First
																							// letter
																							// of
																							// key
																							// words
																							// are
																							// capitalized
		categoryDefinition.setName("beNNy shaY michEl");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("BeNNy ShaY MichEl");
		categoryDefinition.setNormalizedName("benny shay michel");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_01() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" bank OF america  ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Bank of America");
		categoryDefinition.setNormalizedName("bank of america");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_02() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName("THE america bank   ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("THE America Bank");
		categoryDefinition.setNormalizedName("the america bank");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_03() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName("   A bank OF america  ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("A Bank of America");
		categoryDefinition.setNormalizedName("a bank of america");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_04() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName("  bank  america is A big ban  ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Bank America Is a Big Ban");
		categoryDefinition.setNormalizedName("bank america is a big ban");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_05() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" aN apple comPany inC ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("AN Apple ComPany InC");
		categoryDefinition.setNormalizedName("an apple company inc");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_06() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" eat AN apple ANAN");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Eat an Apple ANAN");
		categoryDefinition.setNormalizedName("eat an apple anan");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_07() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" united states OF americA ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("United States of AmericA");
		categoryDefinition.setNormalizedName("united states of america");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	// need to re-check
	@Test
	public void categoryNameValidationConjunctions_08() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" oF united states OF amer ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("OF United States of Amer");
		categoryDefinition.setNormalizedName("of united states of amer");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_09() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" to Apple TO at&T TOO ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("To Apple to At&T TOO");
		categoryDefinition.setNormalizedName("to apple to at&t too");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_10() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" eat apple AS you liiikeas ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Eat Apple as You Liiikeas");
		categoryDefinition.setNormalizedName("eat apple as you liiikeas");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_11() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" as you may want ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("As You May Want");
		categoryDefinition.setNormalizedName("as you may want");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void categoryNameValidationConjunctions_12() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName(" the bank OF america ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("The Bank of America");
		categoryDefinition.setNormalizedName("the bank of america");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	// need to recheck
	@Test
	public void categoryNameValidationConjunctions_13() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName("  To tel-toto ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("To Tel-toto");
		categoryDefinition.setNormalizedName("to tel-toto");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	// recheck
	@Test
	public void categoryNameValidationConjunctions_14() throws Exception { // Normalize
																			// the
																			// category
																			// name
																			// conjunctions
																			// ('of',
																			// 'to',
																			// 'for',
																			// 'as',
																			// 'a',
																			// 'an'
																			// ,
																			// 'the')
																			// are
																			// lower
																			// case.
		categoryDefinition.setName("   tel-aviv To   la ");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Category", STATUS_CODE_CREATED,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Tel-aviv to La");
		categoryDefinition.setNormalizedName("tel-aviv to la");
		CategoryValidationUtils.validateCreateCategoryResponse(createCategotyRest, categoryDefinition);
		// Get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		AuditValidationUtils.categoryAuditSuccess(ADD_CATEGORY, categoryDefinition, sdncAdminUserDetails,
				STATUS_CODE_CREATED, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void createServiceCategoryHttpCspUserIdIsEmpty() throws Exception {
		User sdncAdminUserDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncAdminUserDetails1.setUserId("");
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Abcd");
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_CATEGORY);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDefinition.getName());
		expectedCatrgoryAuditJavaObject.setSubCategoryName("");
		expectedCatrgoryAuditJavaObject.setGroupingName("");
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_SERVICE_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_CATEGORY);
	}

	@Test
	public void createServiceCategorHttpCspUserIdIsNull() throws Exception {
		User sdncAdminUserDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncAdminUserDetails1.setUserId(null);
		RestResponse createCategotyRest = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails1,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createCategotyRest.getErrorCode().intValue());
		categoryDefinition.setName("Abcd");
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_CATEGORY);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDefinition.getName());
		expectedCatrgoryAuditJavaObject.setSubCategoryName("");
		expectedCatrgoryAuditJavaObject.setGroupingName("");
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_SERVICE_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_CATEGORY);
	}

	@Test
	public void createSrvcCategoryHttpCspUserIdHeaderIsMissing() throws Exception {
		RestResponse createConsumerRest = CategoryRestUtils
				.createServiceCategoryHttpCspAtuUidIsMissing(categoryDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createConsumerRest.getErrorCode().intValue());
		categoryDefinition.setName("Abcd");
		// get service category and validate that category was not added
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifyCategoryNotExistsInGetResponse(getAllCategoriesRest, categoryDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_CATEGORY);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDefinition.getName());
		expectedCatrgoryAuditJavaObject.setSubCategoryName("");
		expectedCatrgoryAuditJavaObject.setGroupingName("");
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_SERVICE_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_CATEGORY);
	}

	@Test
	public void getServiceCategoryHierarchySuccessFlow() throws Exception {

		int numOfCategories = 3;
		List<CategoryDefinition> categories = new ArrayList<CategoryDefinition>();
		RestResponse restResponse;
		CategoryDefinition category;
		String categoryName = categoryDefinition.getName();
		for (int i = 0; i < numOfCategories; i++) {
			categoryDefinition.setName(categoryName + i);
			restResponse = CategoryRestUtils.createCategory(categoryDefinition, sdncAdminUserDetails,
					SERVICE_COMPONENT_TYPE);
			category = ResponseParser.parseToObject(restResponse.getResponse(), CategoryDefinition.class);
			categories.add(category);
		}
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());

		AuditValidationUtils.GetCategoryHierarchyAuditSuccess(GET_CATEGORY_HIERARCHY, AUDIT_SERVICE_TYPE,
				sdncAdminUserDetails, STATUS_CODE_SUCCESS);
		for (CategoryDefinition categoryCurr : categories) {
			CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryCurr);
		}
	}

	///////////////////////////////// US570520 /////////////////////////////////
	private List<CategoryDefinition> defineCategories() throws Exception {
		String firstCategory = "FirstCategory";
		String secondCategory = "secondCategory";
		String thirdCategory = "ThirdCategory";
		String forthCategory = "forthCategory";
		CategoryDefinition category1 = new CategoryDefinition(categoryDefinition);
		category1.setName(firstCategory);
		CategoryDefinition category2 = new CategoryDefinition(categoryDefinition);
		category2.setName(secondCategory);
		CategoryDefinition category3 = new CategoryDefinition(categoryDefinition);
		category3.setName(thirdCategory);
		CategoryDefinition category4 = new CategoryDefinition(categoryDefinition);
		category4.setName(forthCategory);
		ArrayList<CategoryDefinition> categoryList = new ArrayList<CategoryDefinition>();
		categoryList.add(category1);
		categoryList.add(category2);
		categoryList.add(category3);
		categoryList.add(category4);
		return categoryList;
	}

	@Test
	public void getAllResourceCategoriesHirarchy() throws Exception {
		createAndValidateCategoriesExist(RESOURCE_COMPONENT_TYPE, categoryList);

		for (int i = 0; i < categoryList.size(); i++) {
			List<String> subCategorieUniqueIdList = new ArrayList<String>();
			for (int j = 0; j < subCategoryList.size(); j++) {
				RestResponse createSubCategory = CategoryRestUtils.createSubCategory(subCategoryList.get(j),
						categoryList.get(i), sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
				if (createSubCategory.getErrorCode().intValue() == STATUS_CODE_CREATED) {
					String subCategoryUniqeId = ResponseParser.getUniqueIdFromResponse(createSubCategory);
					subCategorieUniqueIdList.add(subCategoryUniqeId);
					subCategoriesToDeleteMap.put(categoryList.get(i).getUniqueId(), subCategorieUniqueIdList);
				}
			}
		}

		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());

		for (int i = 0; i < categoryList.size(); i++) {
			for (int j = 0; j < subCategoryList.size(); j++) {
				CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
						categoryList.get(i).getUniqueId(), subCategoryList.get(j));
			}
		}

		checkAuditSuccess(RESOURCE_COMPONENT_TYPE);
	}

	private List<SubCategoryDefinition> defineSubCategories(int catListSize) {
		List<SubCategoryDefinition> subCatList = new ArrayList<SubCategoryDefinition>();
		for (int j = 1; j <= catListSize; j++) {
			SubCategoryDefinition subCategory = new SubCategoryDefinition();
			subCategory.setName("SubCategory" + String.valueOf(j));
			subCatList.add(subCategory);
		}
		return subCatList;
	}

	private void createAndValidateCategoriesExist(String comp, List<CategoryDefinition> categoryList) throws Exception {
		createCategories(comp, categoryList);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, comp);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		verifyCategoriesExist(categoryList, getAllCategoriesRest);
	}

	private void verifyCategoriesExist(List<CategoryDefinition> categoryList, RestResponse getAllCategoriesRest) {
		for (int i = 0; i < categoryList.size(); i++) {
			categoryList.get(i).setName(WordUtils.capitalize(categoryList.get(i).getName()));
			CategoryValidationUtils.verifyCategoryExistInGetResponse(getAllCategoriesRest, categoryList.get(i));
		}
	}

	private void createCategories(String comp, List<CategoryDefinition> categoryList) throws Exception {
		for (int i = 0; i < categoryList.size(); i++) {
			CategoryRestUtils.createCategory(categoryList.get(i), sdncAdminUserDetails, comp);
		}
	}

	@Test
	public void getAllServiceCategoriesHirarchy() throws Exception {
		// deleteCategories(categoryList, SERVICE_COMPONENT_TYPE);
		createAndValidateCategoriesExist(SERVICE_COMPONENT_TYPE, categoryList);
		checkAuditSuccess(SERVICE_COMPONENT_TYPE);
		// deleteCategories(categoryList, SERVICE_COMPONENT_TYPE);
	}

	@Test
	public void getAllResourceCategories_noAttUserHeader() throws Exception {
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(new User(), RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", 403, getAllCategoriesRest.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), new ArrayList<String>(),
				getAllCategoriesRest.getResponse());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(GET_CATEGORY_HIERARCHY);
		expectedCatrgoryAuditJavaObject.setModifierName("");
		expectedCatrgoryAuditJavaObject.setModifierUid("");
		expectedCatrgoryAuditJavaObject.setDetails(RESOURCE_COMPONENT_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateGetCategoryHirarchy(expectedCatrgoryAuditJavaObject, GET_CATEGORY_HIERARCHY);
	}

	@Test
	public void getAllResourceCategories_userNotProvisioned() throws Exception {
		User notProvisionedUser = new User();
		notProvisionedUser.setUserId("aa0001");
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(notProvisionedUser,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", 409, getAllCategoriesRest.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				getAllCategoriesRest.getResponse());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(GET_CATEGORY_HIERARCHY);
		expectedCatrgoryAuditJavaObject.setModifierName("");
		expectedCatrgoryAuditJavaObject.setModifierUid(notProvisionedUser.getUserId());
		expectedCatrgoryAuditJavaObject.setDetails(RESOURCE_COMPONENT_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_RESTRICTED_OPERATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateGetCategoryHirarchy(expectedCatrgoryAuditJavaObject, GET_CATEGORY_HIERARCHY);
	}

	@Test
	public void getAllResourceCategories_unsupportedComponent() throws Exception {
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, "comp");
		assertEquals("Check response code after get all categories hirarchy", 400,
				getAllCategoriesRest.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.UNSUPPORTED_ERROR.name(),
				new ArrayList<String>(Arrays.asList("component type")), getAllCategoriesRest.getResponse());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.UNSUPPORTED_ERROR.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(GET_CATEGORY_HIERARCHY);
		expectedCatrgoryAuditJavaObject.setModifierUid(sdncAdminUserDetails.getUserId());
		expectedCatrgoryAuditJavaObject.setModifierName(sdncAdminUserDetails.getFullName());
		expectedCatrgoryAuditJavaObject.setDetails("comp");
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_INVALID_CONTENT));
		expectedCatrgoryAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo,
				new ArrayList<String>(Arrays.asList("component type"))));
		AuditValidationUtils.validateGetCategoryHirarchy(expectedCatrgoryAuditJavaObject, GET_CATEGORY_HIERARCHY);
	}

	@Test(enabled = false)
	public void getAllResourceCategories_emptyList() throws Exception {
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		JSONArray jArr = new JSONArray(getAllCategoriesRest.getResponse());
		assertTrue(jArr.length() == 0);

		checkAuditSuccess(RESOURCE_COMPONENT_TYPE);
	}

	private void checkAuditSuccess(String componentType) throws Exception {
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(GET_CATEGORY_HIERARCHY);
		expectedCatrgoryAuditJavaObject.setModifierName(sdncAdminUserDetails.getFullName());
		expectedCatrgoryAuditJavaObject.setModifierUid(sdncAdminUserDetails.getUserId());
		expectedCatrgoryAuditJavaObject.setDetails(componentType);
		expectedCatrgoryAuditJavaObject.setStatus("200");
		expectedCatrgoryAuditJavaObject.setDesc("OK");
		AuditValidationUtils.validateGetCategoryHirarchy(expectedCatrgoryAuditJavaObject, GET_CATEGORY_HIERARCHY);
	}

	@Test(enabled = false)
	public void getAllServiceCategories_emptyList() throws Exception {
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		JSONArray jArr = new JSONArray(getAllCategoriesRest.getResponse());
		assertTrue(jArr.length() == 0);

		checkAuditSuccess(SERVICE_COMPONENT_TYPE);
	}

	@Test(enabled = false)
	public void getAllProductCategories_emptyList() throws Exception {
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get Category", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		JSONArray jArr = new JSONArray(getAllCategoriesRest.getResponse());
		assertTrue(jArr.length() == 0);

		checkAuditSuccess(PRODUCT_COMPONENT_TYPE);
	}

	// @Test
	// public void getAllResourceCategories_generalError() throws Exception
	// {
	// User user = new User();
	// RestResponse getAllCategoriesRest =
	// CategoryRestUtils.getAllCategories(user, SERVICE_COMPONENT_TYPE);
	// assertEquals("Check response code after get Category", 500,
	// getAllCategoriesRest.getErrorCode().intValue());
	// Utils.checkBodyResponseOnError(ActionStatus.GENERAL_ERROR.name(), new
	// ArrayList<String>(), getAllCategoriesRest.getResponse());
	// }

	//////////////////////////////////////////////////////////////////////////////

	@Test
	public void importCategories() throws Exception {

		String importResourceDir = config.getImportTypesConfigDir() + File.separator + "categoryTypesTest.zip";

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();
		mpBuilder.addPart("categoriesZip", new FileBody(new File(importResourceDir)));

		RestResponse importResult = CategoryRestUtils.importCategories(mpBuilder, sdncAdminUserDetails.getUserId());
		assertEquals("Check response code after Import", BaseRestUtils.STATUS_CODE_CREATED,
				importResult.getErrorCode().intValue());

		Map<String, Object> map = ResponseParser.parseToObjectUsingMapper(importResult.getResponse(), Map.class);
		assertEquals("Check  entries count", 2, map.size());

		List<Map<String, Object>> resources = (List<Map<String, Object>>) map.get("resources");
		assertEquals("Check resource category  entries count", 1, resources.size());

		List<Map<String, Object>> services = (List<Map<String, Object>>) map.get("services");
		assertEquals("Check resource category  entries count", 2, services.size());

		RestResponse allCategories = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, "resources");
		List<CategoryDefinition> resourceCategories = ResponseParser.parseCategories(allCategories);
		for (Map<String, Object> resource : resources) {
			boolean exist = false;

			for (CategoryDefinition categ : resourceCategories) {
				if (categ.getName().equals(resource.get("name"))) {
					exist = true;
					break;
				}
			}
			assertTrue("Check existance resource category  " + resource.get("name"), exist);
		}

		allCategories = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, "services");
		List<CategoryDefinition> servicesCategories = ResponseParser.parseCategories(allCategories);
		for (Map<String, Object> service : services) {
			boolean exist = false;

			for (CategoryDefinition categ : servicesCategories) {
				if (categ.getName().equals(service.get("name"))) {
					exist = true;
					break;
				}
			}
			assertTrue("Check existance service category  " + service.get("name"), exist);
		}
	}
}
