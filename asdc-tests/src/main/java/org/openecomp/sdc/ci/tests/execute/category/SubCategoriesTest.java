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

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SubCategoriesTest extends ComponentBaseTest {

	protected static final String ADD_SUB_CATEGORY = "AddSubCategory";
	protected static final String CATEGORY = "category";
	protected static final String SUB_CATEGORY = "sub-category";

	protected static final String AUDIT_SERVICE_TYPE = "Service";
	protected static final String AUDIT_RESOURCE_TYPE = "Resource";
	protected static final String AUDIT_PRODUCT_TYPE = "Product";
	protected static final String GET_CATEGORY_HIERARCHY = "GetCategoryHierarchy";
	protected static User sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	protected static User sdncAdminUserDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	protected static User sdncDesignerUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	protected static User sdncTesterUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
	protected static User sdncGovernorUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
	protected static User sdncOpsUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
	protected static User sdncProductManagerUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
	protected static User sdncProductStrategistUserDetails = ElementFactory
			.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);

	public SubCategoriesTest() {
		super(name, SubCategoriesTest.class.getName());
	}

	@Rule
	public static TestName name = new TestName();

	private CategoryDefinition resourceCategoryDefinition;
	private CategoryDefinition resourceCategoryDefinition1;
	private CategoryDefinition serviceCategoryDefinition;
	private CategoryDefinition productCategoryDefinition;
	private CategoryDefinition productCategoryDefinition1;
	private CategoryDefinition resourceCategoryDefinition100;
	private CategoryDefinition productCategoryDefinition200;

	private SubCategoryDefinition resourceSubCategoryDefinition;
	private SubCategoryDefinition resourceSubCategoryDefinition1;
	private SubCategoryDefinition serviceSubCategoryDefinition;
	private SubCategoryDefinition productSubCategoryDefinition;
	private SubCategoryDefinition productSubCategoryDefinition1;

	@BeforeMethod
	public void init() throws Exception {

		// Category setup
		resourceCategoryDefinition = new CategoryDefinition();
		resourceCategoryDefinition1 = new CategoryDefinition();
		serviceCategoryDefinition = new CategoryDefinition();
		productCategoryDefinition = new CategoryDefinition();
		productCategoryDefinition1 = new CategoryDefinition();
		resourceCategoryDefinition100 = new CategoryDefinition(); // for
																	// negative
																	// tests
		productCategoryDefinition200 = new CategoryDefinition(); // for negative
																	// tests

		resourceCategoryDefinition.setName("Category1");
		resourceCategoryDefinition1.setName("Category2");
		serviceCategoryDefinition.setName("Category1");
		productCategoryDefinition.setName("Category2");
		productCategoryDefinition1.setName("Category3");
		resourceCategoryDefinition100.setName("Category100");
		productCategoryDefinition200.setName("Category100");

		// Subcategory setup
		resourceSubCategoryDefinition = new SubCategoryDefinition();
		resourceSubCategoryDefinition1 = new SubCategoryDefinition();
		serviceSubCategoryDefinition = new SubCategoryDefinition();
		productSubCategoryDefinition = new SubCategoryDefinition();
		productSubCategoryDefinition1 = new SubCategoryDefinition();

		resourceSubCategoryDefinition.setName("Resource-subcat");
		// Service sub - for negative testing since it's not allowed
		serviceSubCategoryDefinition.setName("Service-subcat");
		productSubCategoryDefinition.setName("Product-subcat");

		// Init resource category
		RestResponse createCategory = CategoryRestUtils.createCategory(resourceCategoryDefinition, sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		CategoryDefinition category = ResponseParser.parseToObject(createCategory.getResponse(),
				CategoryDefinition.class);
		assertEquals("Check category name after creating category ", resourceCategoryDefinition.getName(),
				category.getName());
		resourceCategoryDefinition = category;

		// Init resource category1
		createCategory = CategoryRestUtils.createCategory(resourceCategoryDefinition1, sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		category = ResponseParser.parseToObject(createCategory.getResponse(), CategoryDefinition.class);
		assertEquals("Check category name after creating category ", resourceCategoryDefinition1.getName(),
				category.getName());
		resourceCategoryDefinition1 = category;

		// Init service category
		createCategory = CategoryRestUtils.createCategory(serviceCategoryDefinition, sdncAdminUserDetails,
				SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		category = ResponseParser.parseToObject(createCategory.getResponse(), CategoryDefinition.class);
		assertEquals("Check category name after creating category ", serviceCategoryDefinition.getName(),
				category.getName());
		serviceCategoryDefinition = category;

		// Init product category
		createCategory = CategoryRestUtils.createCategory(productCategoryDefinition, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		category = ResponseParser.parseToObject(createCategory.getResponse(), CategoryDefinition.class);
		assertEquals("Check category name after creating category ", productCategoryDefinition.getName(),
				category.getName());
		productCategoryDefinition = category;

		// Init product category1
		createCategory = CategoryRestUtils.createCategory(productCategoryDefinition1, sdncProductStrategistUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create category", STATUS_CODE_CREATED,
				createCategory.getErrorCode().intValue());
		category = ResponseParser.parseToObject(createCategory.getResponse(), CategoryDefinition.class);
		assertEquals("Check category name after creating category ", productCategoryDefinition1.getName(),
				category.getName());
		productCategoryDefinition1 = category;

	}

	@Test
	public void createResourceSubCategorySuccess() throws Exception {
		createSubCategorySuccess(resourceCategoryDefinition, resourceSubCategoryDefinition, sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void createProductSubCategorySuccess() throws Exception {
		createSubCategorySuccess(productCategoryDefinition, productSubCategoryDefinition,
				sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void createProductSubCategoryTwoCategoriesCaseInsensitive() throws Exception {
		String componentType = PRODUCT_COMPONENT_TYPE;
		String auditType = AUDIT_PRODUCT_TYPE;
		User user = sdncProductStrategistUserDetails;
		// Create product sub Category2-->Product-subcat
		createSubCategorySuccess(productCategoryDefinition, productSubCategoryDefinition, user, componentType,
				auditType);
		DbUtils.deleteFromEsDbByPattern("_all");

		// Create product sub Category3-->PRoDUCT-SUBcat
		// Should be created Category3-->Product-subcat
		productSubCategoryDefinition1.setName("PRoDUCT-SUBcat");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition1,
				productCategoryDefinition1, user, componentType);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(user, componentType);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		productSubCategoryDefinition1.setName(productSubCategoryDefinition.getName());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition1.getUniqueId(), productSubCategoryDefinition1);
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, productCategoryDefinition1,
				productSubCategoryDefinition1, user, STATUS_CODE_CREATED, auditType);
	}

	// Benny
	@Test
	public void createResourceSubCategoryAlreadyExistInDifferentResourceCategory() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
		DbUtils.deleteFromEsDbByPattern("_all");
		resourceSubCategoryDefinition1.setName("ResourcE-subCat");
		createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition1,
				resourceCategoryDefinition1, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		resourceSubCategoryDefinition1.setName(resourceSubCategoryDefinition.getName());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition1.getUniqueId(), resourceSubCategoryDefinition1); // also
																							// set
																							// catalog
																							// uniqeId
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition1,
				resourceSubCategoryDefinition1, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void createProductSubCategoryAlreadyExistInDifferentProductCategory() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED,
				AUDIT_PRODUCT_TYPE);
		DbUtils.deleteFromEsDbByPattern("_all");
		productSubCategoryDefinition1.setName("PRoDUCT-SUBcat");
		createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition1,
				productCategoryDefinition1, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		productSubCategoryDefinition1.setName(productSubCategoryDefinition.getName());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition1.getUniqueId(), productSubCategoryDefinition1); // also
																							// set
																							// catalog
																							// uniqeId
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, productCategoryDefinition1,
				productSubCategoryDefinition1, sdncProductStrategistUserDetails, STATUS_CODE_CREATED,
				AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void createResourceSubCategoryAlreadyExistInCategory() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
		DbUtils.deleteFromEsDbByPattern("_all");
		resourceSubCategoryDefinition1.setName("ResourcE-subCat");
		createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition1,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_ALREADY_EXISTS,
				createSubCategoryRest.getErrorCode().intValue());
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition1, sdncAdminUserDetails,
				ActionStatus.COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY, STATUS_CODE_ALREADY_EXISTS,
				AUDIT_RESOURCE_TYPE, AUDIT_RESOURCE_TYPE, resourceSubCategoryDefinition1.getName(),
				resourceCategoryDefinition.getName());
	}

	@Test
	public void createProductSubCategoryAlreadyExistInCategory() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED,
				AUDIT_PRODUCT_TYPE);
		DbUtils.deleteFromEsDbByPattern("_all");
		productSubCategoryDefinition1.setName("ProducT-subCat");
		createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition1,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_ALREADY_EXISTS,
				createSubCategoryRest.getErrorCode().intValue());
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition1, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY, STATUS_CODE_ALREADY_EXISTS, AUDIT_PRODUCT_TYPE,
				AUDIT_PRODUCT_TYPE, productSubCategoryDefinition1.getName(), productCategoryDefinition.getName());
	}

	@Test
	public void addSameNormalizedSubCategoryNameForRecourceAndProductCategory() throws Exception {
		// add sub-categoty name "SubCaT" to resource category
		// add sub-categoty name "SUbcAt" to product category
		resourceSubCategoryDefinition.setName("SubCaT"); // normalized 'subcat'
		productSubCategoryDefinition.setName("SUbcAt"); // normalized 'subcat'
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);

		DbUtils.deleteFromEsDbByPattern("_all");
		createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncProductStrategistUserDetails, STATUS_CODE_CREATED,
				AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void createResourceSubCategoryByNonAdminUser() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncTesterUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_RESTRICTED_OPERATION,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		resourceCategoryDefinition.setName(resourceCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncTesterUserDetails, ActionStatus.RESTRICTED_OPERATION,
				STATUS_CODE_RESTRICTED_OPERATION, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void createResourceSubCategoryByProducStrategistUser() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncProductStrategistUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_RESTRICTED_OPERATION,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		resourceCategoryDefinition.setName(resourceCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncProductStrategistUserDetails, ActionStatus.RESTRICTED_OPERATION,
				STATUS_CODE_RESTRICTED_OPERATION, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void createProductSubCategoryByNonProducStrategistUser() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncDesignerUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_RESTRICTED_OPERATION,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncDesignerUserDetails, ActionStatus.RESTRICTED_OPERATION,
				STATUS_CODE_RESTRICTED_OPERATION, AUDIT_PRODUCT_TYPE);
	}

	@Test
	public void createProductSubCategoryByAdminUser() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncAdminUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_RESTRICTED_OPERATION,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.RESTRICTED_OPERATION,
				STATUS_CODE_RESTRICTED_OPERATION, AUDIT_PRODUCT_TYPE);
	}

	// @Ignore("DE176245")
	@Test
	public void createResourceSubCategoryForNonExistingComponentType() throws Exception {
		String nonSupportedComponentType = "NonExistingComponentType"; // instead
																		// resource/product
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, nonSupportedComponentType);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		resourceCategoryDefinition.setName(resourceCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, nonSupportedComponentType);
	}

	// @Ignore("DE176245")
	@Test
	public void createProductSubCategoryForNonExistingComponentType() throws Exception {
		String nonSupportedComponentType = "NonExistingComponentType"; // instead
																		// resource/product
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, nonSupportedComponentType);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncProductStrategistUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, nonSupportedComponentType);
	}

	@Test
	public void createServiceSubCategoryByAdmin() throws Exception {
		// Service doesn't have sub-category
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		resourceCategoryDefinition.setName(resourceCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void createServiceSubCategoryByProductStrategist() throws Exception {
		// Service doesn't have sub-category
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition, sdncProductStrategistUserDetails, SERVICE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				productCategoryDefinition.getUniqueId(), productSubCategoryDefinition);
		// Audit validation
		productCategoryDefinition.setName(productCategoryDefinition.getUniqueId());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, productCategoryDefinition,
				productSubCategoryDefinition, sdncProductStrategistUserDetails, ActionStatus.INVALID_CONTENT,
				STATUS_CODE_INVALID_CONTENT, AUDIT_SERVICE_TYPE);
	}

	@Test
	public void createResourceSubCategoryForNonExistingCategory() throws Exception {
		resourceCategoryDefinition100.setUniqueId(resourceCategoryDefinition100.getName());
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition100, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_NOT_FOUND,
				createSubCategoryRest.getErrorCode().intValue());
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition100,
				resourceSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.COMPONENT_CATEGORY_NOT_FOUND,
				STATUS_CODE_NOT_FOUND, AUDIT_RESOURCE_TYPE, RESOURCE_COMPONENT_TYPE, CATEGORY, "");

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition100.getUniqueId(), resourceSubCategoryDefinition);
	}

	@Test
	public void createProductSubCategoryForNonExistingCategory() throws Exception {
		productCategoryDefinition200.setUniqueId(productCategoryDefinition200.getName());
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(productSubCategoryDefinition,
				productCategoryDefinition200, sdncProductStrategistUserDetails, PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_NOT_FOUND,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				PRODUCT_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				productCategoryDefinition200.getUniqueId(), productSubCategoryDefinition);
		// Audit validation // need to change ActionStatus
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, productCategoryDefinition200,
				productSubCategoryDefinition, sdncProductStrategistUserDetails,
				ActionStatus.COMPONENT_CATEGORY_NOT_FOUND, STATUS_CODE_NOT_FOUND, AUDIT_PRODUCT_TYPE,
				PRODUCT_COMPONENT_TYPE, CATEGORY, "");
	}

	// pass
	@Test
	public void subCategoryAllowedcharacters_01() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE-");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition); // also
																							// set
																							// catalog
																							// uniqeId
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	// pass
	@Test
	public void subCategoryAllowedcharacters_02() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE+");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_03() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE&");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_04() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE.");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_05() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE'");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_06() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE=");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_07() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE:");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_08() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE@");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_09() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE_");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_10() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE#");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_11() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE d");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("1234AbcdE D");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryAllowedcharacters_12() throws Exception {
		resourceSubCategoryDefinition.setName("1234AbcdE   &_=+.-'#:@ d");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("1234AbcdE &_=+.-'#:@ D");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveSpaceFromBeginning() throws Exception {
		resourceSubCategoryDefinition.setName("  Category01");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category01");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveSpaceFromEnd() throws Exception {
		resourceSubCategoryDefinition.setName("Category01    ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category01");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraSpace() throws Exception {
		resourceSubCategoryDefinition.setName("Category    02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category 02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraAmpersand() throws Exception {
		resourceSubCategoryDefinition.setName("Category&& &02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category& &02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraDash() throws Exception {
		resourceSubCategoryDefinition.setName("CategorY-- --02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("CategorY- -02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraPlus() throws Exception {
		resourceSubCategoryDefinition.setName("CateGory++++ +02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("CateGory+ +02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraPeriod() throws Exception {
		resourceSubCategoryDefinition.setName("Category.... .02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category. .02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraApostrophe() throws Exception {
		resourceSubCategoryDefinition.setName("CaTegory''' '02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("CaTegory' '02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraHashtag() throws Exception {
		resourceSubCategoryDefinition.setName("Category### #02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category# #02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtrEequal() throws Exception {
		resourceSubCategoryDefinition.setName("Category=== =02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category= =02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtrColon() throws Exception {
		resourceSubCategoryDefinition.setName("Category::: :02");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category: :02");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtrAt() throws Exception {
		resourceSubCategoryDefinition.setName("Category@@@ @a2");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category@ @a2");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryRemoveExtraUnderscore() throws Exception {
		resourceSubCategoryDefinition.setName("Category___ _22");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Category_ _22");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryFirstWordStartWithNumber() throws Exception {
		resourceSubCategoryDefinition.setName("1Category one");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("1Category One");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	// Bug
	// Desc=<ACTION = "AddSubCategory" MODIFIER = "Jimmy Hendrix(jh0003)"
	// CATEGORY_NAME = "Category1" SUB_CATEGORY_NAME = "&AbcD123" GROUPING_NAME
	// = "" RESOURCE_TYPE = "Resource" STATUS = "400" DESC = "SVC4556: Error:
	// Invalid Resource sub-category name format.">
	// DESC=SVC4556: Error: InvalidResourcesub-categorynameformat.,
	// @Ignore
	@Test
	public void subCategoryFirstWordStartWithNonAlphaNumeric() throws Exception {
		// The first word must start with an alpha-numeric character [a-Z A..Z,
		// 0..9]
		char invalidChars[] = { '&', '-', '+', '.', '\'', '#', '=', ':', '@', '_' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			resourceSubCategoryDefinition.setName(invalidChars[i] + "AbcD123");
			RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
					resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
			assertEquals("Check response code after create Category", STATUS_CODE_INVALID_CONTENT,
					createSubCategoryRest.getErrorCode().intValue());
			RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
					RESOURCE_COMPONENT_TYPE);
			assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
					getAllCategoriesRest.getErrorCode().intValue());
			CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
					resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
			// Audit validation
			AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
					resourceSubCategoryDefinition, sdncAdminUserDetails,
					ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT,
					AUDIT_RESOURCE_TYPE, AUDIT_RESOURCE_TYPE, SUB_CATEGORY);

		}
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_01() throws Exception {
		resourceSubCategoryDefinition.setName("At and T");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("At & T");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_02() throws Exception {
		resourceSubCategoryDefinition.setName("At and t");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("At & T");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_03() throws Exception {
		resourceSubCategoryDefinition.setName("Atand T");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_04() throws Exception {
		resourceSubCategoryDefinition.setName("At andT");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("At AndT");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_05() throws Exception {
		resourceSubCategoryDefinition.setName(" and AttT");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("And AttT");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_06() throws Exception {
		resourceSubCategoryDefinition.setName("AttT and ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("AttT And");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryReplaceAndWithAmpersand_07() throws Exception {
		resourceSubCategoryDefinition.setName(" and a");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("And a");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationMaxLength() throws Exception {
		resourceSubCategoryDefinition.setName("AsdfghjQ234567890@#.&:+-_");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationMaxLengthAfterNormalization() throws Exception {
		resourceSubCategoryDefinition.setName("  A jQ234 @@@___ +++ At and T and and ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("A JQ234 @_ + At & T & And");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	// bug :
	// Desc=<ACTION = "AddSubCategory" MODIFIER = "Jimmy Hendrix(jh0003)"
	// CATEGORY_NAME = "Category1" SUB_CATEGORY_NAME = " AbdfghBCVa jQ234 @@___
	// +++ At and T " GROUPING_NAME = "" RESOURCE_TYPE = "Resource" STATUS =
	// "400" DESC = "SVC4555: Error: Invalid Resource sub-category name
	// length.">
	@Test
	public void subCategoryNameValidationExceedMaxLengthAfterNormalization() throws Exception {
		resourceSubCategoryDefinition.setName("  AbdfghBCVa jQ234 @@___ +++ At and T   ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH,
				STATUS_CODE_INVALID_CONTENT, AUDIT_RESOURCE_TYPE, AUDIT_RESOURCE_TYPE, SUB_CATEGORY);
	}

	@Test
	public void subCategoryNameValidationMinLengthAfterNormalization() throws Exception {
		resourceSubCategoryDefinition.setName("  AT&&&&&&&&&T   ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("AT&T");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	// bug
	// Desc=<ACTION = "AddSubCategory" MODIFIER = "Jimmy Hendrix(jh0003)"
	// CATEGORY_NAME = "Category1" SUB_CATEGORY_NAME = " A and T " GROUPING_NAME
	// = "" RESOURCE_TYPE = "Resource" STATUS = "400" DESC = "SVC4555: Error:
	// Invalid Resource sub-category name length.">
	@Test
	public void subCategoryNameValidationLessThanMinLengthAfterNormalization() throws Exception {
		resourceSubCategoryDefinition.setName("  A&&&T   ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH,
				STATUS_CODE_INVALID_CONTENT, AUDIT_RESOURCE_TYPE, AUDIT_RESOURCE_TYPE, SUB_CATEGORY);
	}

	@Test
	public void subCategoryNameIsEmpty() throws Exception {
		resourceSubCategoryDefinition.setName("");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT,
				STATUS_CODE_INVALID_CONTENT, AUDIT_RESOURCE_TYPE, AUDIT_RESOURCE_TYPE, SUB_CATEGORY);
	}

	// bug
	// Desc=<ACTION = "AddSubCategory" MODIFIER = "Jimmy Hendrix(jh0003)"
	// CATEGORY_NAME = "Category1" SUB_CATEGORY_NAME = "AbcD123~" GROUPING_NAME
	// = "" RESOURCE_TYPE = "Resource" STATUS = "400" DESC = "SVC4556: Error:
	// Invalid Resource sub-category name format.">
	@Test
	public void subCategoryNameValidationInvalidCharacters() throws Exception {
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/',
				'|', '\\', ',' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			resourceSubCategoryDefinition.setName("AbcD123" + invalidChars[i]);
			RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
					resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
			assertEquals("Check response code after create Sub category", STATUS_CODE_INVALID_CONTENT,
					createSubCategoryRest.getErrorCode().intValue());
			RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
					RESOURCE_COMPONENT_TYPE);
			assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
					getAllCategoriesRest.getErrorCode().intValue());
			CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
					resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
			// Audit validation
			AuditValidationUtils.subCategoryAuditFailure(ADD_SUB_CATEGORY, resourceCategoryDefinition,
					resourceSubCategoryDefinition, sdncAdminUserDetails,
					ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, STATUS_CODE_INVALID_CONTENT,
					AUDIT_RESOURCE_TYPE, AUDIT_RESOURCE_TYPE, SUB_CATEGORY);
		}
	}

	@Test
	public void subCategoryNameValidationFirstLetterOfKeyWordsCapitalized() throws Exception {
		resourceSubCategoryDefinition.setName("beNNy shaY michEl");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("BeNNy ShaY MichEl");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_01() throws Exception {
		resourceSubCategoryDefinition.setName(" bank OF america  ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Bank of America");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_02() throws Exception {
		resourceSubCategoryDefinition.setName("THE america bank   ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("THE America Bank");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_03() throws Exception {
		resourceSubCategoryDefinition.setName("   A bank OF america  ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("A Bank of America");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_04() throws Exception {
		resourceSubCategoryDefinition.setName("  bank  america is A big ban  ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Bank America Is a Big Ban");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_05() throws Exception {
		resourceSubCategoryDefinition.setName(" aN apple comPany inC ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("AN Apple ComPany InC");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_06() throws Exception {
		resourceSubCategoryDefinition.setName(" eat AN apple ANAN");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Eat an Apple ANAN");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_07() throws Exception {
		resourceSubCategoryDefinition.setName(" united states OF americA ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("United States of AmericA");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_08() throws Exception {
		resourceSubCategoryDefinition.setName(" oF united states OF amer ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("OF United States of Amer");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_09() throws Exception {
		resourceSubCategoryDefinition.setName(" to Apple TO at&T TOO ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("To Apple to At&T TOO");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_10() throws Exception {
		resourceSubCategoryDefinition.setName(" eat apple AS you liiikeas ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Eat Apple as You Liiikeas");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_11() throws Exception {
		resourceSubCategoryDefinition.setName(" as you may want ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("As You May Want");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_12() throws Exception {
		resourceSubCategoryDefinition.setName(" the bank OF america ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("The Bank of America");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_13() throws Exception {
		resourceSubCategoryDefinition.setName("  To tel-toto ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("To Tel-toto");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void subCategoryNameValidationConjunctions_14() throws Exception {
		resourceSubCategoryDefinition.setName("   tel-aviv To   la ");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		resourceSubCategoryDefinition.setName("Tel-aviv to La");
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, resourceSubCategoryDefinition);

		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, resourceCategoryDefinition,
				resourceSubCategoryDefinition, sdncAdminUserDetails, STATUS_CODE_CREATED, AUDIT_RESOURCE_TYPE);
	}

	@Test
	public void createSubCategoryHttpCspUserIdHeaderIsMissing() throws Exception {
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategoryHttpCspAtuUidIsMissing(
				resourceSubCategoryDefinition, resourceCategoryDefinition, sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_MISSING_INFORMATION,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_SUB_CATEGORY);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(resourceCategoryDefinition.getUniqueId());
		// String subCategoryName = (resourceSubCategoryDefinition != null ?
		// resourceSubCategoryDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setSubCategoryName(resourceSubCategoryDefinition.getName());
		// String groupingName = (groupingDefinition != null ?
		// groupingDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setGroupingName("");
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_RESOURCE_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_SUB_CATEGORY);
	}

	@Test
	public void createSubCategoryHttpCspUserIdIsEmpty() throws Exception {
		User sdncAdminUserDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncAdminUserDetails1.setUserId("");
		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
				resourceCategoryDefinition, sdncAdminUserDetails1, RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after create Sub category", STATUS_CODE_MISSING_INFORMATION,
				createSubCategoryRest.getErrorCode().intValue());
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryNotExistsInGetResponse(getAllCategoriesRest,
				resourceCategoryDefinition.getUniqueId(), resourceSubCategoryDefinition);
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(ADD_SUB_CATEGORY);
		expectedCatrgoryAuditJavaObject.setModifier("");
		expectedCatrgoryAuditJavaObject.setCategoryName(resourceCategoryDefinition.getUniqueId());
		// String subCategoryName = (resourceSubCategoryDefinition != null ?
		// resourceSubCategoryDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setSubCategoryName(resourceSubCategoryDefinition.getName());
		// String groupingName = (groupingDefinition != null ?
		// groupingDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setGroupingName("");
		expectedCatrgoryAuditJavaObject.setResourceType(AUDIT_RESOURCE_TYPE);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(STATUS_CODE_MISSING_INFORMATION));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, ADD_SUB_CATEGORY);
	}

	////////////////////////////////////////////////////////////
	private void createSubCategorySuccess(CategoryDefinition categoryDefinition,
			SubCategoryDefinition subCategoryDefinition, User sdncAdminUserDetails, String componentType,
			String auditType) throws Exception {

		RestResponse createSubCategoryRest = CategoryRestUtils.createSubCategory(subCategoryDefinition,
				categoryDefinition, sdncAdminUserDetails, componentType);
		assertEquals("Check response code after create Sub category", STATUS_CODE_CREATED,
				createSubCategoryRest.getErrorCode().intValue());
		CategoryValidationUtils.validateCreateSubCategoryResponse(createSubCategoryRest, subCategoryDefinition);
		// Audit validation
		AuditValidationUtils.subCategoryAuditSuccess(ADD_SUB_CATEGORY, categoryDefinition, subCategoryDefinition,
				sdncAdminUserDetails, STATUS_CODE_CREATED, auditType);
		// get service category and validate that category added as defined
		// (also set catalog uniqeId)
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails, componentType);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
				categoryDefinition.getUniqueId(), subCategoryDefinition); // also
																			// set
																			// catalog
																			// uniqeId
	}

	@Test
	public void getResourceCategoryHierarchySuccessFlow() throws Exception {
		int numOfSubCategories = 3;
		List<SubCategoryDefinition> subCategories = new ArrayList();
		RestResponse restResponse;
		SubCategoryDefinition subCategory;
		String subName = resourceSubCategoryDefinition.getName();
		for (int i = 0; i < numOfSubCategories; i++) {
			resourceSubCategoryDefinition.setName(subName + i);
			restResponse = CategoryRestUtils.createSubCategory(resourceSubCategoryDefinition,
					resourceCategoryDefinition, sdncAdminUserDetails, RESOURCE_COMPONENT_TYPE);
			subCategory = ResponseParser.parseToObject(restResponse.getResponse(), SubCategoryDefinition.class);
			subCategories.add(subCategory);
		}
		RestResponse getAllCategoriesRest = CategoryRestUtils.getAllCategories(sdncAdminUserDetails,
				RESOURCE_COMPONENT_TYPE);
		assertEquals("Check response code after get all categories ", STATUS_CODE_SUCCESS,
				getAllCategoriesRest.getErrorCode().intValue());
		AuditValidationUtils.GetCategoryHierarchyAuditSuccess(GET_CATEGORY_HIERARCHY, AUDIT_RESOURCE_TYPE,
				sdncAdminUserDetails, STATUS_CODE_SUCCESS);
		for (SubCategoryDefinition sub : subCategories) {
			CategoryValidationUtils.verifySubCategoryExistInGetResponse(getAllCategoriesRest,
					resourceCategoryDefinition.getUniqueId(), sub);
		}
	}

}
