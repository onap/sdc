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
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedProductAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ProductValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.Test;

public class ProductCreateWithValidationsTest extends ProductBaseTest {
	@Rule
	public static TestName name = new TestName();

	public static String INITIAL_PRODUCT_VERSION = "0.1";
	public static String CREATE_AUDIT_ACTION = "Create";
	public String normalizedName;

	public ProductCreateWithValidationsTest() {
		super(name, ProductCreateWithValidationsTest.class.getName());
	}

	@Test // (enabled = false)
	public void createProductSuccessValidation() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNotByPmUser() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productStrategistUser1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				productStrategistUser1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productStrategistUser1, ActionStatus.RESTRICTED_OPERATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNotByAsdcUser() throws Exception {
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		nonAsdcUser.setUserId("bt750k");
		nonAsdcUser.setFirstName(null);
		nonAsdcUser.setLastName(null);
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, nonAsdcUser);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				nonAsdcUser);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, nonAsdcUser, ActionStatus.RESTRICTED_OPERATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductUserIdIsEmpty() throws Exception {
		User nonAsdcUser = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		nonAsdcUser.setUserId("");
		nonAsdcUser.setFirstName(null);
		nonAsdcUser.setLastName(null);
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, nonAsdcUser);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_MISSING_INFORMATION,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, INITIAL_PRODUCT_VERSION,
				nonAsdcUser);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, nonAsdcUser, ActionStatus.MISSING_INFORMATION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNameValidationLessThanMinCharacters() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("Pro");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, Constants.EMPTY_STRING, Constants.EMPTY_STRING,
				null, null, Constants.EMPTY_STRING, "Product", "abbreviated");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductNameValidationMaxLength() throws Exception {
		// Max length = 25
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
//		productReqDetails.setName("Qwertyuiop1234567890asdfA");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNameValidationExceedMaxLength() throws Exception {
		// Max length = 25
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("Qwertyuiop1234567890asdfAa");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, Constants.EMPTY_STRING, Constants.EMPTY_STRING,
				null, null, Constants.EMPTY_STRING, "Product", "abbreviated");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNameValidationEmptyName() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product",
				"abbreviated");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductNameAlreadyExist() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		// // productReqDetails.setName("CIProduct1");
		DbUtils.deleteFromEsDbByPattern("_all");
		createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_ALREADY_EXISTS,
				createProduct.getErrorCode().intValue());
		productReqDetails.setVersion("0.1");
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_NAME_ALREADY_EXIST,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product",
				productReqDetails.getName());
		constructFieldsForAuditValidation.setCURR_VERSION("0.1");
		constructFieldsForAuditValidation.setCURR_STATE("");
		constructFieldsForAuditValidation.setCURR_STATE("NOT_CERTIFIED_CHECKOUT");
		constructFieldsForAuditValidation.setSERVICE_INSTANCE_ID(null);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNameValidationNameIsNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName(null);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product",
				"abbreviated");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	// DE193857
	@Test (enabled = false)
	public void createProductNameValidationAllowedCharacters() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("Ci_2@3:4& m=n+b-u.j-u'g#b"); // Bug @:&=+'#
		normalizedName = "ci234mnbujugb";
		String expectedProductName = "Ci_2@3:4& M=n+b-u.j-u'g#b";
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		productReqDetails.setName(expectedProductName);
		productReqDetails.setName("Ci_2@3:4& M=n+b-u.j-u'g#b");
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		expectedProduct.setNormalizedName(normalizedName);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	// DE193857
	@Test
	public void createProductNameValidationREmoveExtraNonAlphanumericChars() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("Ci____222----333......asd");
//		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setName("Ci_222-333.asd");
		normalizedName = "ci222333asd";
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNameValidationNotAllowedCharacters() throws Exception {
		ExpectedProductAudit constructFieldsForAuditValidation;
		char invalidChars[] = { '~', '!', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/', '|',
				'\\', ',', '$' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
			productReqDetails.setName("abc" + invalidChars[i]);
			RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
			assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
					createProduct.getErrorCode().intValue());
			Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "",
					productManager1);
			constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(expectedProduct,
					CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT,
					Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product",
					"abbreviated");
			constructFieldsForAuditValidation.setCURR_VERSION("");
			constructFieldsForAuditValidation.setCURR_STATE("");
			AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
		}
	}

	@Test
	public void createProductFullNameContainSpecialCharacters() throws Exception {
		char invalidChars[] = { '~', '!', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/', '|',
				'\\', ',', '$' };
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			productReqDetails.setFullName("abc" + invalidChars[i]);
			RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
			assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_CREATED,
					createProduct.getErrorCode().intValue());
			RestResponse deleteProduct = ProductRestUtils.deleteProduct(productReqDetails.getUniqueId(),
					productManager1.getUserId());
			assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS,
					deleteProduct.getErrorCode().intValue());
		}
	}

	// Already enabled = false
	@Test
	public void createProductNameValidationRemoveSpaceFromBeginning() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("         Ciertyuiop1234567890asdfA");
//		productReqDetails.setTags(Arrays.asList(productReqDetails.getName().trim()));
		normalizedName = productReqDetails.getName().trim().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		productReqDetails.setName("         Ciertyuiop1234567890asdfA".trim());
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	// Already enabled = false
	@Test
	public void createProductNameValidationRemoveSpaceFromTheEnd() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("Ciertyuiop1234567890asdfA        ");
//		productReqDetails.setTags(Arrays.asList(productReqDetails.getName().trim()));
		normalizedName = productReqDetails.getName().trim().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		productReqDetails.setName("Ciertyuiop1234567890asdfA        ".trim());
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductNameValidationStartWithNumber() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("1Qwert");
//		productReqDetails.setTags(Arrays.asList(productReqDetails.getName().trim()));
		normalizedName = productReqDetails.getName().trim().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
		productReqDetails.setName("Ci1Qwert");
		RestResponse updateProduct = ProductRestUtils.updateProduct(productReqDetails, productManager1);
		
	}

	@Test
	public void createProductNameValidationStartWithNonAlphaNumeric() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("_Qwert");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName().trim()));
		normalizedName = productReqDetails.getName().trim().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, Constants.EMPTY_STRING, Constants.EMPTY_STRING,
				null, null, Constants.EMPTY_STRING, "Product", "abbreviated");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductNameValidationFirstLetterOfKeyWordsCapitalized() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		// productReqDetails.setTags(Arrays.asList("abba"));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		// // productReqDetails.setName("Abba");
		/*
		 * String actualNormalizedNameFromResponse =
		 * ResponseParser.getValueFromJsonResponse(createProduct.getResponse(),
		 * "normalizedName");
		 * assertTrue(actualNormalizedNameFromResponse.equals(normalizedName));
		 */
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductFullNameValidationIsEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setFullName("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product", "full");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductFullNameValidationIsNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setFullName("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product", "full");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductFullNameLessThanMinLength() throws Exception {
		// Min is 4 characters
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setFullName("abc");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, Constants.EMPTY_STRING, Constants.EMPTY_STRING,
				null, null, Constants.EMPTY_STRING, "Product", "full");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductFullNameHasMinLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setFullName("abcd");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductFullNameHasMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setFullName(
				"Abba1234567890asdfghjk l123zxcvbnm432adfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductFullNameExceedMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setFullName(
				"Abba1234567890asdfghjk l123zxcvbnm432adfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.123");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1,
				ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, Constants.EMPTY_STRING, Constants.EMPTY_STRING,
				null, null, Constants.EMPTY_STRING, "Product", "full");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductFullNameRemoveExtraSpaces() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setFullName("Abbaaa  a1");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setFullName("Abbaaa a1");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductDescriptionValidationIsEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setDescription("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductDescriptionValidationIsNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setDescription(null);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductDescriptionValidCharacters01() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription("qwertyuiopasdfghjklzxcvbnm1234567890<b>Bold<</b>");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setDescription("qwertyuiopasdfghjklzxcvbnm1234567890Bold&lt;");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductDescriptionValidCharacters02() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription("~!@#$%^&*()_+<>?qwertyuiopasdfghjklzxcvbnm1234567890#");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setDescription("~!@#$%^&amp;*()_+&lt;&gt;?qwertyuiopasdfghjklzxcvbnm1234567890#");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductDescriptionInValidCharacters() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription("מה");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_INVALID_DESCRIPTION,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductDescriptionRemoveSpacesFromBeginning() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription("   abcd12345");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setDescription("abcd12345");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductDescriptionRemoveSpacesFromTheEnd() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription("abcd 12345 xcvb    ");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setDescription("abcd 12345 xcvb");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductDescriptionMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription(
				"Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12asdfghjklzxcvbnmqwertyui");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductDescriptionExceedMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("Abba");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setDescription(
				"Abxba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12Abba1234567890asdfghjkl123zxcvbnm432asdfghjkl_-.123Abba1234567890asdfghjkl23zxcvbnm432asdfghjkl_-.12asdfghjklzxcvbnmqwertyui");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product", "1024");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductTagIsEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setName("Product");
		productReqDetails.setTags(Arrays.asList(""));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_FIELD_FORMAT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product", "tag");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	// DE192351
	@Test // (enabled = false)
	public void createProductTagValidationAllowedCharacters() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName(), "Acde2@3:4& m=n+b-u.j-u'g#b"));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductTagsNameValidationProductNameIsNotInTag() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
//		productReqDetails.setName("Qwertyuiop1234567890asdfA");
		productReqDetails.setTags(Arrays.asList("Abc"));
		normalizedName = productReqDetails.getName().trim().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductSingleTagMaxLength() throws Exception {
		// SingleTagMaxLength = 50
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(
				Arrays.asList(productReqDetails.getName(), "Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345678"));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductSingleTagExceedMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(
				Arrays.asList(productReqDetails.getName(), "Axbba1234567890asdfghjkl123zxcvbnm432asdfgh12345678"));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "50");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductAllTagsMaxLength() throws Exception {
		// AllTagsMaxLength = 1024
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
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
						"Abba1234567890asdfghjkl123zxcvbnm432asdfgh12345"));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductAllTagsExceedMaxLength() throws Exception {
		// AllTagsMaxLength = 1024
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
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
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "1024");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductDuplicateTagRemoved() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName(), productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductContactsIsEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		productReqDetails.setContacts(Arrays.asList(""));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_INVALID_CONTACT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductContactsInvalidFormat() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		productReqDetails.setContacts(Arrays.asList("bt750345"));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_INVALID_CONTACT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductConvertContactsToLowerCase() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		productReqDetails.setContacts(Arrays.asList(productManager1.getUserId().toUpperCase()));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setContacts(Arrays.asList(productManager1.getUserId().toLowerCase()));
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductContactsDoexNotContainTheProductCreator() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		productReqDetails.setContacts(Arrays.asList(productManager2.getUserId()));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setContacts(Arrays.asList(productManager2.getUserId(), productManager1.getUserId()));
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductContactsNotAllowedAsdcUsers() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		productReqDetails.setContacts(Arrays.asList(designerUser.getUserId()));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_PRODUCT_CONTACT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING,
				designerUser.getUserId());
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductContactsNotAsdcUser() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1"); // Bug @:&=+'#
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase();
		String nonAsdcUser = "bh1234";
		productReqDetails.setContacts(Arrays.asList(nonAsdcUser));
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_PRODUCT_CONTACT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, nonAsdcUser);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeIsEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.MISSING_PROJECT_CODE,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeIsNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode(null);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.MISSING_PROJECT_CODE,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeIsNotNumeric() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode("asdfgh");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_PROJECT_CODE,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeHasnMinCharacters() throws Exception {
		// Min =5
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode("12345");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeHasnMaxCharacters() throws Exception {
		// Max =10
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode("1234567890");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeExceedMaxCharacters() throws Exception {
		// Max =10
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode("12345678901");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_PROJECT_CODE,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductProjectCodeLessThanMinCharacters() throws Exception {
		// Max =10
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setProjectCode("1234");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_PROJECT_CODE,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIconIsEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setIcon("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_MISSING_ICON,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIconIsNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setIcon(null);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_MISSING_ICON,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIconMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setIcon("asdfghjklqwertyuiozxcvbfv");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIconExceedMaxLength() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setIcon("asdfghjklqwertyuiozxcvbf12");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product", "25");
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIconAllowedCharacters() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setIcon("a--s-fghjk_q__r1234567890");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test (enabled = false)
	public void createProductIconInValidCharacters() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		String icon = "asdfg";
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/',
				'|', '\\', ',' };
		RestResponse createProduct;
		for (int i = 0; i < invalidChars.length; i++) {
			productReqDetails.setIcon(icon + invalidChars[i]);
			createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
			assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
					createProduct.getErrorCode().intValue());
			Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "",
					productManager1);
			ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
					expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.COMPONENT_INVALID_ICON,
					Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, "Product");
			constructFieldsForAuditValidation.setCURR_VERSION("");
			constructFieldsForAuditValidation.setCURR_STATE("");
			AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
		}
	}

	@Test // (enabled = false)
	public void createProductIsActiveisEmpty() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setActive("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setActive("false");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIsActiveisNull() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setActive("");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		productReqDetails.setActive("false");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIsActiveisFalse() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setActive("false");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test
	public void createProductIsActiveisHasInvalidValue() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setActive("xfalse");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_INVALID_CONTENT,
				createProduct.getErrorCode().intValue());
		Product expectedProduct = Convertor.constructFieldsForRespValidation(productReqDetails, "", productManager1);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.INVALID_CONTENT,
				Constants.EMPTY_STRING, Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING);
		constructFieldsForAuditValidation.setCURR_VERSION("");
		constructFieldsForAuditValidation.setCURR_STATE("");
		constructFieldsForAuditValidation.setRESOURCE_NAME("");
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	@Test // (enabled = false)
	public void createProductIsActiveisTrue() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		// productReqDetails.setName("CIProduct1");
		productReqDetails.setTags(Arrays.asList(productReqDetails.getName()));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		productReqDetails.setActive("true");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	//////////////////////////////////////////////
	// DE192424
	@Test // (enabled = false)
	public void createProductNameValidationNormalizationNameWithSpaces() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
//		productReqDetails.setName("Abba Emma");
		// productReqDetails.setName("abba emma");
		// productReqDetails.setTags(Arrays.asList("abba emma"));
		normalizedName = productReqDetails.getName().toLowerCase().replaceAll("\\s+", "");
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		ProductRestUtils.checkCreateResponse(createProduct);
		String actualNormalizedNameFromResponse = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(),
				"normalizedName");
		assertTrue(actualNormalizedNameFromResponse.equals(normalizedName));
		// productReqDetails.setName("Abba Emma");
		String productUuid = ResponseParser.getUuidFromResponse(createProduct);
		compareExpectedAndActualProducts(productReqDetails, createProduct);
		Product expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
		RestResponse getProductRes = ProductRestUtils.getProduct(productReqDetails.getUniqueId(),
				productManager1.getUserId());
		ProductRestUtils.checkSuccess(getProductRes);
		Product actualProduct = ResponseParser.parseToObjectUsingMapper(getProductRes.getResponse(), Product.class);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, actualProduct,
				ComponentOperationEnum.GET_COMPONENT);
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
		ExpectedProductAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(
				expectedProduct, CREATE_AUDIT_ACTION, productManager1, ActionStatus.CREATED, Constants.EMPTY_STRING,
				"0.1", null, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, productUuid);
		AuditValidationUtils.validateAuditProduct(constructFieldsForAuditValidation, CREATE_AUDIT_ACTION);
	}

	private void compareExpectedAndActualProducts(ProductReqDetails productReqDetails, RestResponse createProduct)
			throws JSONException {
		String productName = ResponseParser.getNameFromResponse(createProduct);
		assertTrue(productReqDetails.getName().equals(productName));
		String productIcon = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "icon");
		assertTrue(productReqDetails.getIcon().equals(productIcon));
		String productFullName = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "fullName");
		assertTrue(productReqDetails.getFullName().equals(productFullName));
		String productProjectCode = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "projectCode");
		assertTrue(productReqDetails.getProjectCode().equals(productProjectCode));
		String productIsActive = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "isActive");
		String expectedIsActive = (productReqDetails.getActive() != null ? productReqDetails.getActive() : "false");
		assertTrue(productIsActive.equals(expectedIsActive));
		String productdescription = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "description");
		assertTrue(productReqDetails.getDescription().equals(productdescription));
		String productNormalizedName = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(),
				"normalizedName");
		assertTrue(normalizedName.equals(productNormalizedName));
		String productContacts = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "contacts");
		JSONArray reciviedContacts = new JSONArray(productContacts);
		String actualContact = null;
		for (int i = 0; i < reciviedContacts.length(); i++) {
			actualContact = reciviedContacts.getString(i);
			assertEquals(productReqDetails.getContacts().get(i), actualContact);
		}
		String productTags = ResponseParser.getValueFromJsonResponse(createProduct.getResponse(), "tags");
		JSONArray reciviedTages = new JSONArray(productTags);
		String actualTag = null;
		for (int i = 0; i < reciviedTages.length(); i++) {
			actualTag = reciviedTages.getString(i);
			assertEquals(productReqDetails.getTags().get(i), actualTag);
		}
	}

	// END
	///////////////////////////////////////////////////////
	@Test
	public void createProductSuccessFlow() throws Exception {
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
		assertEquals(actualProduct.getIsActive(), new Boolean(false));
	}

	@Test
	public void createProductSetIsActive() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setActive("true");
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
		assertEquals(actualProduct.getIsActive(), new Boolean(true));
	}

	@Test
	public void createProductNoIcon() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setIcon(null);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_MISSING_DATA,
				createProduct.getErrorCode().intValue());

	}

	@Test
	public void createProductNoProjectCode() throws Exception {
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		productReqDetails.setProjectCode(null);
		RestResponse createProduct = ProductRestUtils.createProduct(productReqDetails, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_MISSING_DATA,
				createProduct.getErrorCode().intValue());

	}

}
