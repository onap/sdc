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

package org.openecomp.sdc.ci.tests.execute.product;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedProductAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ProductValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProductCrudTest extends ProductBaseTest {
	private static Logger log = LoggerFactory.getLogger(ProductCrudTest.class.getName());
	@Rule
	public static TestName name = new TestName();

	public static String INITIAL_PRODUCT_VERSION = "0.1";
	public static String CREATE_AUDIT_ACTION = "Create";
	public static String UPDATE_AUDIT_ACTION = "Update";
	public static String COMPONENT_TYPE = "Product";

	private ProductReqDetails productReqDetails;
	private RestResponse createProduct;
	private Product product;

	public ProductCrudTest() {
		super(name, ProductCrudTest.class.getName());
	}

	@Test // (enabled=false)
	public void createAndGetAll() throws Exception {
		createProductAndGet(UserRoleEnum.DESIGNER);
	}

	private void createProductAndGet(UserRoleEnum user) throws Exception, IOException {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());

		RestResponse catalog = CatalogRestUtils.getCatalog(user.getUserId());
		assertEquals("Check response code after get catalog", BaseRestUtils.STATUS_CODE_SUCCESS,
				catalog.getErrorCode().intValue());

		try {
			JsonElement jElement = new JsonParser().parse(catalog.getResponse());
			JsonObject jObject = jElement.getAsJsonObject();
			JsonArray products = (JsonArray) jObject.get("products");
			assertEquals("Check product array size", 1, products.size());
			Iterator<JsonElement> iter = products.iterator();
			while (iter.hasNext()) {
				JsonElement next = iter.next();
				Product product = ResponseParser.parseToObjectUsingMapper(next.toString(), Product.class);
				assertNotNull(product);
				assertEquals("Check product name", productReqDetails.getName(), product.getName());
				// Map<String, String> allVersions = product.getAllVersions();
				// assertEquals("Check product name", 1, allVersions.size());
			}

		} catch (Exception e) {
			log.debug("exception", e);
		}
	}

	@Test
	public void getAllNoProcduts() throws Exception {

		RestResponse catalog = CatalogRestUtils.getCatalog();
		assertEquals("Check response code after get catalog", BaseRestUtils.STATUS_CODE_SUCCESS,
				catalog.getErrorCode().intValue());

		try {
			JsonElement jElement = new JsonParser().parse(catalog.getResponse());
			JsonObject jObject = jElement.getAsJsonObject();
			JsonArray products = (JsonArray) jObject.get("products");
			assertEquals("Check product array size", 0, products.size());
		} catch (Exception e) {
			log.debug("exception", e);
		}

	}

	@Test
	public void getAllNoAttHeader() throws Exception {
		String url = String.format(Urls.GET_CATALOG_DATA, config.getCatalogBeHost(), config.getCatalogBePort());

		List<String> headersToRemove = new ArrayList<String>();
		headersToRemove.add(HttpHeaderEnum.USER_ID.getValue());

		RestResponse catalog = CatalogRestUtils.sendGetAndRemoveHeaders(url, null, headersToRemove);
		assertEquals("Check response code after get catalog", BaseRestUtils.STATUS_CODE_MISSING_INFORMATION,
				catalog.getErrorCode().intValue());

		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), new ArrayList<String>(),
				catalog.getResponse());
	}

	@Test
	public void getAllWrongUser() throws Exception {
		RestResponse catalog = CatalogRestUtils.getCatalog("kj8976");
		assertEquals("Check response code after get catalog", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				catalog.getErrorCode().intValue());

		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				catalog.getResponse());
	}

	@Test // (enabled=false)
	public void getAllWithProductStrategist_User() throws Exception {
		createProductAndGet(UserRoleEnum.PRODUCT_STRATEGIST1);
	}

	@Test // (enabled=false)
	public void getAllWithProductManager_User() throws Exception {
		createProductAndGet(UserRoleEnum.PRODUCT_MANAGER1);
	}

	@Test // (enabled=false)
	public void createProductNoCategories() throws Exception {
		createProductWithCategories(null);
	}

	@Test // (enabled=false)
	public void createProductOneGrouping() throws Exception {
		// Category1->[Subcategory1->[Grouping1]]
		createProductWithCategories(defaultCategories);
	}

	@Test // (enabled=false)
	public void createProductTwoGroupingsSameSubCategory() throws Exception {
		// Category1->Subcategory1->[Grouping1, Grouping11]
		List<CategoryDefinition> addSecondGroupingToDefaultCategory = addSecondGroupingToDefaultCategory();
		createProductWithCategories(addSecondGroupingToDefaultCategory);
	}

	@Test // (enabled=false)
	public void createProductTwoSubsDifferentGroupings() throws Exception {
		// Category1->[Subcategory1->[Grouping1,
		// Grouping11],Subcategory2->[Grouping12]]
		List<CategoryDefinition> addSubcategoryAndGroupingToDefaultCategory = addSubcategoryAndGroupingToDefaultCategory();
		createProductWithCategories(addSubcategoryAndGroupingToDefaultCategory);
	}

	@Test // (enabled=false)
	public void createManyGroupingsDiffCategories() throws Exception {
		// [Category1->[Subcategory1->[Grouping1,
		// Grouping11],Subcategory2->[Grouping12]],
		// Category2->[Subcategory1->[Grouping1],Subcategory2->[Grouping1]],
		// Category3->[Subcategory1->[Grouping11],Subcategory2->[Grouping11,
		// Grouping22]]]
		List<CategoryDefinition> addSubcategoryAndGroupingToDefaultCategory = addManyGroupingsDiffCategories();
		createProductWithCategories(addSubcategoryAndGroupingToDefaultCategory);
	}

	@Test // (enabled=false)
	public void createProductEmptyUserId() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		User emptyUser = new User();
		emptyUser.setUserId("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, emptyUser);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_MISSING_INFORMATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				emptyUser);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, emptyUser, ActionStatus.MISSING_INFORMATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled=false)
	public void createProductNonExistingUserId() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		User notExistingUser = new User();
		notExistingUser.setUserId("jj6444");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, notExistingUser);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				notExistingUser);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, notExistingUser, ActionStatus.RESTRICTED_OPERATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled=false)
	public void createProductInvalidJson() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct_Invalid_Json(productManager1.getUserId());
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_CONTENT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setRESOURCE_NAME("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled=false)
	public void createProductAdminRoleNotAllowed() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		User wrongRole = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, wrongRole);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				wrongRole);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, wrongRole, ActionStatus.RESTRICTED_OPERATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled=false)
	public void createProductProductStrategistRoleNotAllowed() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		User wrongRole = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST3);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, wrongRole);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				wrongRole);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, wrongRole, ActionStatus.RESTRICTED_OPERATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled=false)
	public void getProductSuccessFlow() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());

		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		assertEquals("Check response code after getting created Product", BaseRestUtils.STATUS_CODE_SUCCESS,
				getProductRes.getErrorCode().intValue());

		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
	}

	@Test // (enabled=false)
	public void getNonExistedProduct() throws Exception {

		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());

		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		assertEquals("Check response code after getting created Product", BaseRestUtils.STATUS_CODE_SUCCESS,
				getProductRes.getErrorCode().intValue());

		Product product = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		assertEquals("Assert on product icon", productReqDetails.getName(), product.getName());

		RestResponse deleteProductRes = ProductRestUtils.deleteProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		assertEquals("Check response code for deletign Product", BaseRestUtils.STATUS_CODE_SUCCESS,
				deleteProductRes.getErrorCode().intValue());

		RestResponse getProductAfterDeleteRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		assertEquals("Check response code after getting deleted Product", BaseRestUtils.STATUS_CODE_NOT_FOUND,
				getProductAfterDeleteRes.getErrorCode().intValue());
	}

	@Test // (enabled=false)
	public void getProductMissingHeader() throws Exception {

		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());

		productManager1.setUserId(null);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		assertEquals("Check response code after getting created Producuct with userId extracted from header",
				BaseRestUtils.STATUS_CODE_MISSING_INFORMATION, getProductRes.getErrorCode().intValue());

	}

	@Test // (enabled=false)
	public void getProductNonExistingUser() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());

		productManager1.setUserId("bt1111");
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		assertEquals("Check response code after getting created Producuct with non exsisting user",
				BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION, getProductRes.getErrorCode().intValue());
	}

	@Test // (enabled=false)
	public void createProductAndGetProductWithDifferentUser() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());
		User sdncProductStrategistUserAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				sdncProductStrategistUserAdminDetails.getUserId());
		assertEquals("Check response code after getting created Product different user role",
				BaseRestUtils.STATUS_CODE_SUCCESS, getProductRes.getErrorCode().intValue());
	}

	// US594753 - Update Product metadata

	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductAllFieldsByPM() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		// Update product
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("NewProductName");
		List<CategoryDefinition> addSecondGroupingToDefaultCategory = addSecondGroupingToDefaultCategory();
		productReqDetails.setFullName("New Full name");
		productReqDetails.setActive("false");
		productReqDetails.setContacts(
				Arrays.asList(productManager2.getUserId().toLowerCase(), productManager1.getUserId().toLowerCase()));
		productReqDetails.setDescription("New Product Description");
		productReqDetails.setIcon("asdfghjklqwertyuiozxcvbfv");
		productReqDetails.setProjectCode("98765");
		productReqDetails.setCategories(addSecondGroupingToDefaultCategory);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// productReqDetails.setTags(Arrays.asList(productReqDetails.getName(),
		// productOldName));
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(productReqDetails.getName().toLowerCase());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductByPS() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("New discription");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productStrategistUser1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
	}

	@Test // (enabled=false)
	public void updateProductByAdmin() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("New discription");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, designerUser);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
	}

	@Test // (enabled=false)
	public void updateProductByNonPmUser() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		// Update product name
		productReqDetails.setDescription("New discription");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, designerUser);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
	}

	@Test // (enabled=false)
	public void updateProductByNonAsdcUser() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		// Update product name
		productReqDetails.setDescription("New discription");
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		nonAsdcUser.setUserId("bt789k");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, nonAsdcUser);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
	}

	@Test // (enabled=false)
	public void updateProductUserIdIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		// Update product name
		productReqDetails.setDescription("New discription");
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		nonAsdcUser.setUserId("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, nonAsdcUser);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_MISSING_INFORMATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
	}

	@Test // (enabled=false)
	public void updateProductByNonProductOwner() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct(defaultCategories);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		Product product = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("New discription");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager2);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNotInCheckoutState() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct(defaultCategories);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		Product product = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		// Update product name
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		String valueFromJsonResponse = ResponseParser.getValueFromJsonResponse(changeProductLifeCycle.getResponse(),
				"lastUpdateDate");
		expectedProduct.setLastUpdateDate(Long.parseLong(valueFromJsonResponse));
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNameIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("abbreviated");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNameIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager2,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		// List<String> tags = productReqDetails.getTags();
		// tags.removeAll(tags);
		productReqDetails.setTags(new ArrayList<>());
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName(null); // no update will be performed
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager2);
		ProductRestUtils.checkSuccess(updateProduct);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager2);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(product.getName().toLowerCase());
		expectedProduct.setName(product.getName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNameLessThanMinLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("ABC"); // no update will be performed
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("abbreviated");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH.name(),
				varibales, updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductNameHasMinLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager2,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("NewP");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager2);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		productReqDetails.setLastUpdaterUserId(productManager2.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager2.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager2);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(productReqDetails.getName().toLowerCase());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	// DE193857 - Normalized Name is not removing special characters
	@Test(enabled = false)
	public void updateProductNameMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		// Update product name
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("Ac_2B3U4k mSKnob-u.j-uGgP");
		String newNormalizedName = "ac2b3u4kmsknobujuggp";
		String newName = "Ac_2B3U4k MSKnob-u.j-uGgP";
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setTags(Arrays.asList(newName));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(newNormalizedName);
		expectedProduct.setName(newName);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNameExceedMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("Ac_2B3U4k mSKnob-u.j-uGgPx");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("abbreviated");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH.name(),
				varibales, updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNameAlreadyExist() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct(defaultCategories);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager2);
		ProductRestUtils.checkCreateResponse(createProduct);
		Product product1 = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product1, productManager2,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setName("Product2000");
		// productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		createProduct = ProductRestUtils.createProduct(productReqDetails, productManager2);
		ProductRestUtils.checkCreateResponse(createProduct);
		Product product2 = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product2, productManager2,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product2, productManager2,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product2.getUniqueId());
		productReqDetails.setUUID(product2.getUUID());
		productReqDetails.setName(product1.getName());
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager2);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_ALREADY_EXISTS,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add(product1.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager2.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product2.getUniqueId());
		expectedProduct.setVersion(product2.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// DE193857 - Normalized Name is not removing special characters
	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductNameAllowedCharacters() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		// Update product name
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("A_BU4k m&K=o#b-u.j-uG'g+P"); // Allowed
																// characters
																// [a-z,A-Z,0-9]
																// , ‘ ‘
																// (space),
																// ampersand
																// "&", dash
																// “-“, plus
																// "+", period
																// ".",
																// apostrophe
																// "'", hashtag
																// "#", equal
																// "=", period
																// ":", at "@",
																// and
																// underscore
																// "_"
		String newNormalizedName = "abu4km&kobujuggp";
		String newName = "A_BU4k M&K=o#b-u.j-uG'g+P";
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setTags(Arrays.asList(newName));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(newNormalizedName);
		expectedProduct.setName(newName);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductNameRemoveSpaceFromBeginning() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("      asdfg");
		String newNormalizedName = "asdfg";
		String newName = "Asdfg";
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// productReqDetails.setTags(Arrays.asList(newName, productOldName));
		productReqDetails.setTags(Arrays.asList(newName));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(newNormalizedName);
		expectedProduct.setName(newName);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductNameRemoveSpaceFromEnd() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("asdfg fc        ");
		String newNormalizedName = "asdfgfc";
		String newName = "Asdfg Fc";
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// productReqDetails.setTags(Arrays.asList(newName, productOldName));
		productReqDetails.setTags(Arrays.asList(newName));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(newNormalizedName);
		expectedProduct.setName(newName);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	//// DE193857 - Normalized Name is not removing special characters
	// If user update "product name" we need to remove the old product name from
	//// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductNameRemoveExtraNonAlphanumericChars() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("A__k &&==##---u..hG'''+++");
		String newNormalizedName = "akhg";
		String newName = "A_k &=#-u.hG'+";
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// productReqDetails.setTags(Arrays.asList(newName, productOldName));
		productReqDetails.setTags(Arrays.asList(newName));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(newNormalizedName);
		expectedProduct.setName(newName);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// If user update "product name" we need to remove the old product name from
	// "Tags" and add the new product name instead - will handled in mew US
	@Test(enabled = false)
	public void updateProductNameValidationStartWithNumber() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("1000Ab");
		String newNormalizedName = productReqDetails.getName().toLowerCase();
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		// productReqDetails.setTags(Arrays.asList(productReqDetails.getName(),
		// productOldName));
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setNormalizedName(newNormalizedName);
		expectedProduct.setName(productReqDetails.getName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductNameValidationStartWithNonAlphaNumeric() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setName("_1000Ab");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("abbreviated");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT.name(),
				varibales, updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNameIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("full");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNameIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setNormalizedName(product.getNormalizedName());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setFullName(product.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNameHasMinLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName("asdc");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setNormalizedName(product.getNormalizedName());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setFullName(productReqDetails.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNameHasMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName(
				"1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setNormalizedName(product.getNormalizedName());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setFullName(productReqDetails.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNamelessThanMinLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName("123");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("full");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH.name(),
				varibales, updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNameExceedMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName(
				"1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjkx");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("full");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH.name(),
				varibales, updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	// DE193947
	@Test
	public void updateProductFullNameWithSpecialCharacters() throws Exception {
		char invalidChars[] = { '~', '!', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/', '|',
				'\\', ',', '$', '#', '@', '+' };
		String fullName = "avbng";
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		for (int i = 0; i < invalidChars.length; i++) {
			productReqDetails.setFullName(fullName + invalidChars[i]);
			RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
			assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS,
					updateProduct.getErrorCode().intValue());
		}
	}

	@Test // (enabled=false)
	public void updateProductFullNameValidCharactersCharacters01() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName("qwertyuiopasdfghjklzxcvbnm1234567890<b>Bold<</b>");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setNormalizedName(product.getNormalizedName());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setFullName("qwertyuiopasdfghjklzxcvbnm1234567890Bold&lt;");
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductFullNameRemoveExtraSpaces() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setFullName("Abbaaa  a1");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setNormalizedName(product.getNormalizedName());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setFullName("Abbaaa a1");
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test(enabled = false)
	public void updateProductDescriptionValidCharacters01() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("~!@#$%^&*()_+<>?qwertyuiopasdfghjklzxcvbnm1234567890#");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setDescription("~!@#$%^&amp;*()_+&lt;&gt;?qwertyuiopasdfghjklzxcvbnm1234567890#");
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionValidCharacters02() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("qwertyuiopasdfghjklzxcvbnm1234567890<b>Bold<</b>");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setDescription("qwertyuiopasdfghjklzxcvbnm1234567890Bold<");
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionInValidCharacters() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("מה");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_DESCRIPTION.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionRemoveSpacesFromBeginning() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("   abcd12345 g");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setDescription(" abcd12345 g");
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionRemoveSpacesFromTheEnd() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setDescription("abcd12345  gdf     ");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setDescription("abcd12345 gdf ");
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		String description = "1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfg";
		productReqDetails.setDescription(description);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setDescription(description);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductDescriptionExceedMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		String description = "01234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjk aa1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfg";
		productReqDetails.setDescription(description);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("1024");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT.name(),
				varibales, updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductTagIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(Arrays.asList(""));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		varibales.add("tag");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FIELD_FORMAT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductTagIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setTags(product.getTags());
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductTagsNameValidationProductNameIsNotInTag() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(Arrays.asList("Abc"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME.name(),
				new ArrayList<String>(), updateProduct.getResponse());
	}

	@Test // (enabled=false)
	public void createProductSingleTagMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(
				Arrays.asList(productReqDetails.getName(), "Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345678"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setTags(productReqDetails.getTags());
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductSingleTagExceedMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName(),
				"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("1024");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductAllTagsMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(
				Arrays.asList(productReqDetails.getName(), "Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345601",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345602",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345603",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345604",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345605",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345606",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345607"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setTags(productReqDetails.getTags());
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductAllTagsExceedMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(
				Arrays.asList(productReqDetails.getName(), "Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345601",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345602",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345603",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345604",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345605",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345606",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345607",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345608",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh1234569",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345610",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345611",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345612",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345613",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345614",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345615",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345616",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345617",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345618",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345619",
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh123456"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add("1024");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductTagsDuplicateTagRemoved() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName(), "KoKo", "KoKo"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata updated
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setTags(Arrays.asList(productReqDetails.getName(), "KoKo"));
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductContactsIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setContacts(Arrays.asList(""));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductContactsIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setContacts(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductContactsInvalidFormat() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setContacts(Arrays.asList("bt750345"));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(COMPONENT_TYPE);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_CONTACT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductConvertContactsToLowerCase() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setContacts(Arrays.asList(productManager2.getUserId().toUpperCase()));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct
				.setContacts(Arrays.asList(productManager2.getUserId().toLowerCase(), productManager1.getUserId()));
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductContactsNotAllowedAsdcUsers() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setContacts(Arrays.asList(productStrategistUser1.getUserId()));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(productStrategistUser1.getUserId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PRODUCT_CONTACT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductContactsNotAsdcUser() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		String nonAsdcUserUserId = "bt567h";
		productReqDetails.setContacts(Arrays.asList(nonAsdcUserUserId));
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(nonAsdcUserUserId);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PRODUCT_CONTACT.name(), varibales,
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_PROJECT_CODE.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setProjectCode(product.getProjectCode());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeLessThanMinCharacters() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode("9870");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROJECT_CODE.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeHasnMinCharacters() throws Exception { // min
																				// =5
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode("98700");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setProjectCode(productReqDetails.getProjectCode());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeHasnMaxCharacters() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode("1234567890");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setProjectCode(productReqDetails.getProjectCode());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeExceedMaxCharacters() throws Exception {// Max
		// =10
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode("12345678901");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROJECT_CODE.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setProjectCode(product.getProjectCode());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductProjectCodeIsNotNumeric() throws Exception {
		// Max =10
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setProjectCode("1234a");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROJECT_CODE.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setProjectCode(product.getProjectCode());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIconIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setIcon("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> variables = new ArrayList<String>();
		variables.add(COMPONENT_TYPE);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_ICON.name(), variables,
				updateProduct.getResponse());
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIcon(product.getIcon());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIconIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setIcon(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		// Get Product and verify that metadata didn't change
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIcon(product.getIcon());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIconMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setIcon("A_a-1-2--b__BB1234567890A"); // Max length =
																// 25
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIcon(productReqDetails.getIcon());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIconExceedMaxLength() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setIcon("A_a-1-2--b__BB1234567890A_");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ArrayList<String> variables = new ArrayList<String>();
		variables.add(COMPONENT_TYPE);
		variables.add("25");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT.name(), variables,
				updateProduct.getResponse());
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIcon(product.getIcon());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIconInValidCharacters() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		String icon = "asdfg"; // Allowed characters [a-zA-Z0-9], dash (‘-‘),
								// underscore (‘_’).
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/',
				'|', '\\', ',' };
		RestResponse updateProduct;
		for (int i = 0; i < invalidChars.length; i++) {
			productReqDetails.setIcon(icon + invalidChars[i]);
			updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
			assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
					updateProduct.getErrorCode().intValue());
			ArrayList<String> variables = new ArrayList<String>();
			variables.add(COMPONENT_TYPE);
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_INVALID_ICON.name(), variables,
					updateProduct.getResponse());
		}
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIcon(product.getIcon());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIsActiveIsEmpty() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setActive("");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIsActive(false);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIsActiveIsTrue() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setActive("true");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIsActive(true);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIsActiveIsNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct(defaultCategories);
		productReqDetails.setActive("true");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		Product product = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setActive(null);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIsActive(true);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIsActiveIsFalse() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setActive("false");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		expectedProduct.setIsActive(false);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductIsActiveHasInvalidValue() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setActive("eeeee");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				updateProduct.getResponse());
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				updateProduct.getErrorCode().intValue());
		RestResponse getProduct = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setUniqueId(product.getUniqueId());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProduct.getResponse(), Product.class);
		expectedProduct.setVersion(product.getVersion());
		expectedProduct.setLastUpdaterUserId(productManager1.getUserId());
		expectedProduct.setLastUpdaterFullName(productManager1.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductAssociations() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		List<CategoryDefinition> addSecondGroupingToDefaultCategory = addSecondGroupingToDefaultCategory();
		productReqDetails.setCategories(addSecondGroupingToDefaultCategory);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setCategories(productReqDetails.getCategories());
		expectedProduct.setNormalizedName(productReqDetails.getName().toLowerCase());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductAssociations02() throws Exception {
		List<CategoryDefinition> addSecondGroupingToDefaultCategory = addSecondGroupingToDefaultCategory(); // Category1->Subcategory1->[Grouping1,
																											// Grouping11]
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct(addSecondGroupingToDefaultCategory);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		Product product = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		List<CategoryDefinition> defaultCategory = addSubcategoryAndGroupingToDefaultCategory(); // Category1->[Subcategory1->[Grouping1,
																									// Grouping11],Subcategory2->[Grouping12]]
		productReqDetails.setCategories(defaultCategory);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setCategories(productReqDetails.getCategories());
		expectedProduct.setNormalizedName(productReqDetails.getName().toLowerCase());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductRemoveAllAssociations() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		List<CategoryDefinition> defaultCategory = new ArrayList<CategoryDefinition>();
		productReqDetails.setCategories(defaultCategory);
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setCategories(productReqDetails.getCategories());
		expectedProduct.setNormalizedName(productReqDetails.getName().toLowerCase());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	@Test // (enabled=false)
	public void updateProductAssociationsCategotyIsNull() throws Exception {
		createProducrByPSAndCheckIn();
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
		productReqDetails.setUniqueId(product.getUniqueId());
		productReqDetails.setUUID(product.getUUID());
		productReqDetails.setCategories(null);// product categories will not be
												// updated
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		ProductRestUtils.checkSuccess(updateProduct);
		productReqDetails.setLastUpdaterUserId(productManager1.getUserId());
		productReqDetails.setLastUpdaterFullName(productManager1.getFullName());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(updateProduct.getResponse(), Product.class);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "0.2", productManager1);
		expectedProduct.setUUID(product.getUUID());
		expectedProduct.setInvariantUUID(product.getInvariantUUID());
		expectedProduct.setCategories(product.getCategories());
		expectedProduct.setNormalizedName(productReqDetails.getName().toLowerCase());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.UPDATE_COMPONENT);
	}

	////////////////////////////////////////////////////////////////////////

	private void createProductWithCategories(List<CategoryDefinition> categoryDefinitions) throws Exception {
		ProductReqDetails productReqDetails = (categoryDefinitions != null
				? ElementFactory.getDefaultProduct(categoryDefinitions) : ElementFactory.getDefaultProduct());
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		String actualUuid = ResponseParser.getUuidFromResponse(createProduct);
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				productManager1);
		String normalizedNameFomJsonResponse = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(),
				"normalizedName");
		expectedProduct.setNormalizedName(normalizedNameFomJsonResponse);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.CREATE_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, actualUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	private void createProducrByPSAndCheckIn() throws Exception {
		productReqDetails = ElementFactory.getDefaultProduct(defaultCategories);
		createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		product = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse changeProductLifeCycle = ProductRestUtils.changeProductLifeCycle(product, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.checkSuccess(changeProductLifeCycle);
	}

	@Test
	public void checkInvariantUuidIsImmutable() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		String invariantUuidDefinedByUser = "!!!!!!!!!!!!!!!!!!!!!!!!";
		productReqDetails.setInvariantUUID(invariantUuidDefinedByUser);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		BaseRestUtils.checkStatusCode(createProduct, "create request failed", false, 201);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());
		Product ProductCreation = ResponseParser.convertProductResponseToJavaObject(createProduct.getResponse());
		String invariantUUIDcreation = ProductCreation.getInvariantUUID();

		// validate get response
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		BaseRestUtils.checkSuccess(getProductRes);
		Product productGetting = ResponseParser.convertProductResponseToJavaObject(getProductRes.getResponse());
		String invariantUUIDgetting = productGetting.getInvariantUUID();
		assertEquals(invariantUUIDcreation, invariantUUIDgetting);

		// Update Product with new invariant UUID
		RestResponse restResponseUpdate = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		BaseRestUtils.checkSuccess(restResponseUpdate);
		Product updatedProduct = ResponseParser.convertProductResponseToJavaObject(restResponseUpdate.getResponse());
		String invariantUUIDupdating = updatedProduct.getInvariantUUID();
		assertEquals(invariantUUIDcreation, invariantUUIDupdating);

		// Do checkin
		RestResponse restResponseCheckin = LifecycleRestUtils.changeProductState(productReqDetails, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(restResponseCheckin);
		Product checkinProduct = ResponseParser.convertProductResponseToJavaObject(restResponseCheckin.getResponse());
		String invariantUUIDcheckin = checkinProduct.getInvariantUUID();
		String version = checkinProduct.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcheckin);
		assertEquals(version, "0.1");

		// Do checkout
		RestResponse restResponseCheckout = LifecycleRestUtils.changeProductState(productReqDetails, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		BaseRestUtils.checkSuccess(restResponseCheckout);
		Product checkoutProduct = ResponseParser.convertProductResponseToJavaObject(restResponseCheckout.getResponse());
		String invariantUUIDcheckout = checkoutProduct.getInvariantUUID();
		version = checkoutProduct.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcheckout);
		assertEquals(version, "0.2");

	}

	// US672129 Benny
	private void getProductValidateInvariantUuid(String productUniqueId, String invariantUUIDcreation)
			throws Exception {
		RestResponse getProduct = ProductRestUtils.getProduct(productUniqueId,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, getProduct.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(getProduct));
	}

	@Test // invariantUUID generated when the component is created and never
			// changed
	public void productInvariantUuid() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		String invariantUuidDefinedByUser = "12345";
		productReqDetails.setInvariantUUID(invariantUuidDefinedByUser);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create resource", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());
		// invariantUUID generated when the component is created and never
		// changed
		String invariantUUIDcreation = ResponseParser.getInvariantUuid(createProduct);
		getProductValidateInvariantUuid(productReqDetails.getUniqueId(), invariantUUIDcreation);
		// Update Product with new invariant UUID
		RestResponse restResponse = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getProductValidateInvariantUuid(productReqDetails.getUniqueId(), invariantUUIDcreation);
		// Checkin
		restResponse = LifecycleRestUtils.changeProductState(productReqDetails, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getProductValidateInvariantUuid(productReqDetails.getUniqueId(), invariantUUIDcreation);
		// Checkout
		restResponse = LifecycleRestUtils.changeProductState(productReqDetails, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getProductValidateInvariantUuid(productReqDetails.getUniqueId(), invariantUUIDcreation);

		// UnDo-CheckOut
		restResponse = LifecycleRestUtils.changeProductState(productReqDetails, productManager1,
				LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getProductValidateInvariantUuid(productReqDetails.getUniqueId(), invariantUUIDcreation);

	}

}
