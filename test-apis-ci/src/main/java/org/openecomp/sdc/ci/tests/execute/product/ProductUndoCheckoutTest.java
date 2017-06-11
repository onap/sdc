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
import static org.testng.AssertJUnit.assertNull;

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
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ProductValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProductUndoCheckoutTest extends ProductLifecycleTest {

	@Rule
	public static TestName name = new TestName();

	@BeforeClass
	public static void staticInit() {
		auditAction = UNDO_CHECKOUT_ACTION;
		operation = ComponentOperationEnum.CHANGE_STATE_UNDO_CHECKOUT;
	}

	public ProductUndoCheckoutTest() {
		super(name, ProductUndoCheckoutTest.class.getName());
	}

	@Test
	public void undoCheckOutProductByPm() throws Exception {
		undoCheckOutProductSuccess(productManager1, false);
	}

	@Test
	public void undoCheckOutProductByAdmin() throws Exception {
		undoCheckOutProductSuccess(adminUser, true);
	}

	@Test
	public void undoCheckOutAfterCreate() throws Exception {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals("Check response code after undo checkout product", 200, lcsResponse.getErrorCode().intValue());

		// Verify version was removed
		lcsResponse = ProductRestUtils.getProduct(expectedProduct.getUniqueId(), productManager1.getUserId());
		assertEquals("Check response code after get undone product", 404, lcsResponse.getErrorCode().intValue());

		Product emptyProduct = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);
		assertNull(emptyProduct);
		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(emptyProduct,
				auditAction, productManager1, ActionStatus.OK, "0.1", Constants.EMPTY_STRING,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null, Constants.EMPTY_STRING);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);

	}

	@Test
	public void undoCheckOutNotExist() throws Exception {
		String notExistId = "1234";
		expectedProduct.setUniqueId(notExistId);
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals("Check response code after undo checkout product", 404, lcsResponse.getErrorCode().intValue());
		expectedProduct.setName(notExistId);
		String[] auditParameters = new String[] { Constants.EMPTY_STRING };
		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, productManager1, ActionStatus.PRODUCT_NOT_FOUND, Constants.EMPTY_STRING,
				Constants.EMPTY_STRING, null, null, Constants.EMPTY_STRING, auditParameters);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);

	}

	@Test
	public void undoCheckOutNotInCheckout() throws Exception {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin product", 200, lcsResponse.getErrorCode().intValue());

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals("Check response code after undo checkout product", 409, lcsResponse.getErrorCode().intValue());

		String[] auditParameters = new String[] { expectedProduct.getName(), "product", productManager1.getFirstName(),
				productManager1.getLastName(), productManager1.getUserId() };

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, productManager1, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, "0.1", "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				expectedProduct.getUUID(), auditParameters);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);

	}

	@Test
	public void undoCheckOutProductByPsRoleNotAllowed() throws Exception {
		undoCheckOutProductRestricted(productStrategistUser1);
	}

	@Test
	public void undoCheckOutProductByPmNotStateOwner() throws Exception {
		undoCheckOutProductForbidden(productManager2);
	}

	private void undoCheckOutProductSuccess(User user, boolean isAdmin) throws Exception, FileNotFoundException {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin product", 200, lcsResponse.getErrorCode().intValue());

		// Checking undo checkout of admin even if not state owner
		User checkoutUser = isAdmin ? productManager1 : user;

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, checkoutUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout product", 200, lcsResponse.getErrorCode().intValue());
		Product productToBeUndone = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);

		lcsResponse = LifecycleRestUtils.changeProductState(productToBeUndone, user, LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals("Check response code after undo checkout product", 200, lcsResponse.getErrorCode().intValue());

		// Verify version was removed
		lcsResponse = ProductRestUtils.getProduct(productToBeUndone.getUniqueId(), user.getUserId());
		assertEquals("Check response code after get undone product", 404, lcsResponse.getErrorCode().intValue());

		lcsResponse = ProductRestUtils.getProduct(expectedProduct.getUniqueId(), user.getUserId());
		assertEquals("Check response code after get latest version", 200, lcsResponse.getErrorCode().intValue());

		Product latestProduct = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);

		expectedProduct.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		ProductValidationUtils.compareExpectedAndActualProducts(expectedProduct, latestProduct, operation);

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(latestProduct,
				auditAction, user, ActionStatus.OK, "0.2", "0.1", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, latestProduct.getUUID());
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	private void undoCheckOutProductRestricted(User undoCheckoutUser) throws Exception, FileNotFoundException {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin product", 200, lcsResponse.getErrorCode().intValue());

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout product", 200, lcsResponse.getErrorCode().intValue());
		Product productToBeUndone = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);

		lcsResponse = LifecycleRestUtils.changeProductState(productToBeUndone, undoCheckoutUser,
				LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals("Check response code after undocheckout product", 409, lcsResponse.getErrorCode().intValue());

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(productToBeUndone,
				auditAction, undoCheckoutUser, ActionStatus.RESTRICTED_OPERATION, "0.2", "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				productToBeUndone.getUUID());
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

	private void undoCheckOutProductForbidden(User undoCheckoutUser) throws Exception, FileNotFoundException {
		RestResponse lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkin product", 200, lcsResponse.getErrorCode().intValue());

		lcsResponse = LifecycleRestUtils.changeProductState(expectedProduct, productManager1,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after checkout product", 200, lcsResponse.getErrorCode().intValue());
		Product productToBeUndone = ResponseParser.parseToObjectUsingMapper(lcsResponse.getResponse(), Product.class);

		lcsResponse = LifecycleRestUtils.changeProductState(productToBeUndone, undoCheckoutUser,
				LifeCycleStatesEnum.UNDOCHECKOUT);
		assertEquals("Check response code after undocheckout product", 403, lcsResponse.getErrorCode().intValue());
		String[] auditParameters = new String[] { productToBeUndone.getName(), "product",
				productManager1.getFirstName(), productManager1.getLastName(), productManager1.getUserId() };

		ExpectedProductAudit expectedProductAudit = Convertor.constructFieldsForAuditValidation(expectedProduct,
				auditAction, undoCheckoutUser, ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, "0.2", "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				productToBeUndone.getUUID(), auditParameters);
		AuditValidationUtils.validateAuditProduct(expectedProductAudit, auditAction);
	}

}
