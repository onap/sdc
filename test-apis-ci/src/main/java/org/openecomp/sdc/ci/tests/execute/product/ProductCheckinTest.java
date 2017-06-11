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

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.enums.AuditJsonKeysEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedProductAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ProductValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProductCheckinTest extends ProductLifecycleTest {

	@Rule
	public static TestName name = new TestName();

	public ProductCheckinTest() {
		super(name, ProductCheckinTest.class.getName());
	}

	@BeforeClass
	public static void staticInit() {
		auditAction = CHECKIN_ACTION;
		operation = ComponentOperationEnum.CHANGE_STATE_CHECKIN;
	}

	@Test
	public void checkInProductByCreator() throws Exception {

		String checkinComment = "good checkin";
		RestResponse checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN, checkinComment);
		assertEquals("Check response code after checkin resource", 200, checkInResponse.getErrorCode().intValue());
		Product checkedInProduct = ResponseParser.parseToObjectUsingMapper(checkInResponse.getResponse(),
				Product.class);

		expectedProduct.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, checkedInProduct, operation);

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(checkedInProduct,
				auditAction, productManager1, ActionStatus.OK, "0.1", "0.1", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, checkedInProduct.getUUID());
		expectedProductAudit.setCOMMENT(checkinComment);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction, AuditJsonKeysEnum.COMMENT);
	}

	@Test
	public void checkInProductByPM() throws Exception {

		String checkinComment = "good checkin";
		RestResponse response = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN, checkinComment);
		assertEquals("Check response code after checkin resource", 200, response.getErrorCode().intValue());

		User checkoutUser = productManager2;
		response = LifecycleRestUtils.changeProductState(expectedProduct, checkoutUser, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkin resource", 200, response.getErrorCode().intValue());
		expectedProduct = ResponseParser.parseToObjectUsingMapper(response.getResponse(), Product.class);

		DbUtils.cleanAllAudits();
		checkinComment = "good checkin no 2";
		response = LifecycleRestUtils.changeProductState(expectedProduct, checkoutUser, LifeCycleStatesEnum.CHECKIN,
				checkinComment);
		assertEquals("Check response code after checkin resource", 200, response.getErrorCode().intValue());

		Product checkedInProduct = ResponseParser.parseToObjectUsingMapper(response.getResponse(), Product.class);

		expectedProduct.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		expectedProduct.setVersion("0.2");
		expectedProduct.setLastUpdaterUserId(checkoutUser.getUserId());
		expectedProduct.setLastUpdaterFullName(checkoutUser.getFullName());

		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, checkedInProduct, operation);

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(checkedInProduct,
				auditAction, checkoutUser, ActionStatus.OK, "0.2", "0.2", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, checkedInProduct.getUUID());
		expectedProductAudit.setCOMMENT(checkinComment);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction, AuditJsonKeysEnum.COMMENT);
	}

	@Test
	public void checkInProductByAdmin() throws Exception {

		String checkinComment = "good checkin";
		RestResponse checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, adminUser,
				LifeCycleStatesEnum.CHECKIN, checkinComment);
		assertEquals("Check response code after checkin resource", 200, checkInResponse.getErrorCode().intValue());
		Product checkedInProduct = ResponseParser.parseToObjectUsingMapper(checkInResponse.getResponse(),
				Product.class);

		expectedProduct.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		expectedProduct.setLastUpdaterUserId(adminUser.getUserId());
		expectedProduct.setLastUpdaterFullName(adminUser.getFullName());

		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, checkedInProduct, operation);

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(checkedInProduct,
				auditAction, adminUser, ActionStatus.OK, "0.1", "0.1", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, checkedInProduct.getUUID());
		expectedProductAudit.setCOMMENT(checkinComment);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction, AuditJsonKeysEnum.COMMENT);
	}

	@Test
	public void checkInProductByPMNotOwner() throws Exception {

		RestResponse checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager2,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 403, checkInResponse.getErrorCode().intValue());
		String[] auditParameters = new String[] { expectedProduct.getName(), "product", productManager1.getFirstName(),
				productManager1.getLastName(), productManager1.getUserId() };
		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, productManager2, ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, "0.1", "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				expectedProduct.getUUID(), auditParameters);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	@Test
	public void checkInProductByPsRoleNotAllowed() throws Exception {

		RestResponse checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, productStrategistUser1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 409, checkInResponse.getErrorCode().intValue());
		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, productStrategistUser1, ActionStatus.RESTRICTED_OPERATION, "0.1", "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				expectedProduct.getUUID());
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	@Test
	public void checkInProductNotExist() throws Exception {
		String notExisitingUuid = "1234";
		expectedProduct.setUniqueId(notExisitingUuid);
		RestResponse checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 404, checkInResponse.getErrorCode().intValue());
		String[] auditParameters = new String[] { "", "product" };
		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, productManager1, ActionStatus.PRODUCT_NOT_FOUND, Constants.EMPTY_STRING,
				Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, auditParameters);
		expectedProductAudit.setCURR_STATE(Constants.EMPTY_STRING);
		expectedProductAudit.setRESOURCE_NAME(notExisitingUuid);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	@Test
	public void checkInProductAlreadyCheckedIn() throws Exception {
		RestResponse checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 200, checkInResponse.getErrorCode().intValue());
		DbUtils.cleanAllAudits();
		checkInResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager2,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 409, checkInResponse.getErrorCode().intValue());
		String[] auditParameters = new String[] { expectedProduct.getName(), "product", productManager1.getFirstName(),
				productManager1.getLastName(), productManager1.getUserId() };
		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, productManager2, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, "0.1", "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				expectedProduct.getUUID(), auditParameters);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}
}
