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

import java.io.FileNotFoundException;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedProductAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ProductValidationUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProductCheckoutTest extends ProductLifecycleTest {

	@Rule
	public static TestName name = new TestName();

	@BeforeClass
	public static void staticInit() {
		auditAction = CHECKOUT_ACTION;
		operation = ComponentOperationEnum.CHANGE_STATE_CHECKOUT;
	}

	public ProductCheckoutTest() {
		super(name, ProductCheckoutTest.class.getName());
	}

	@Test
	public void checkOutProductByPmNotInContacts() throws Exception {
		checkOutProductSuccess(productManager2);
	}

	@Test
	public void checkOutProductByPmInContacts() throws Exception {
		checkOutProductSuccess(productManager1);
	}

	@Test
	public void checkOutProductByAdmin() throws Exception {
		checkOutProductSuccess(adminUser);
	}

	@Test
	public void checkOutProductByPs() throws Exception {
		// Changed in 1604 patch - now it's restricted
		checkOutProductRestricted(productStrategistUser3);
		// checkOutProductSuccess(productStrategistUser3);
	}

	@Test
	public void checkOutProductByDesignerRoleNotAllowed() throws Exception {
		checkOutProductRestricted(designerUser);
	}

	@Test
	public void checkOutProductAlreadyCheckedOut() throws Exception {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 200, lcsResponse.getErrorCode().intValue());

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkin resource", 200, lcsResponse.getErrorCode().intValue());
		Product checkedOutProduct = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);

		DbUtils.cleanAllAudits();

		lcsResponse = LifecycleRestUtils.changeProductState(checkedOutProduct, productManager2, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkin resource", 403, lcsResponse.getErrorCode().intValue());
		String[] auditParameters = new String[] { checkedOutProduct.getName(), "product", productManager1.getFirstName(), productManager1.getLastName(), productManager1.getUserId() };

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(checkedOutProduct, auditAction, productManager2, ActionStatus.COMPONENT_IN_CHECKOUT_STATE, "0.2", "0.2", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, checkedOutProduct.getUUID(), auditParameters);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	private void checkOutProductSuccess(User checkoutUser) throws Exception, FileNotFoundException {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 200, lcsResponse.getErrorCode().intValue());

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, checkoutUser, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkin resource", 200, lcsResponse.getErrorCode().intValue());

		// 0.1 is not highest now
		RestResponse prevVersionProductResp = ProductRestUtils.getProduct(expectedProduct.getUniqueId(), productStrategistUser1.getUserId());
		Product prevVersionProduct = ResponseParser.parseToObjectUsingMapper(prevVersionProductResp.getResponse(), Product.class);
		Boolean falseParam = false;
		assertEquals(falseParam, prevVersionProduct.isHighestVersion());

		Product checkedOutProduct = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);

		expectedProduct.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		expectedProduct.setVersion("0.2");
		expectedProduct.setLastUpdaterUserId(checkoutUser.getUserId());
		expectedProduct.setLastUpdaterFullName(checkoutUser.getFullName());
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, checkedOutProduct, operation);

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(checkedOutProduct, auditAction, checkoutUser, ActionStatus.OK, "0.1", "0.2", LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, checkedOutProduct.getUUID());
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	private void checkOutProductRestricted(User checkoutUser) throws Exception, FileNotFoundException {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin resource", 200, lcsResponse.getErrorCode().intValue());

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, checkoutUser, LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkin resource", 409, lcsResponse.getErrorCode().intValue());

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct, auditAction, checkoutUser, ActionStatus.RESTRICTED_OPERATION, "0.1", "0.1", LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, expectedProduct.getUUID());
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

}
