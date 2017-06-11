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

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.BeforeMethod;

public abstract class ProductLifecycleTest extends ProductBaseTest {

	protected static final String CHECKIN_ACTION = "Checkin";
	protected static final String CHECKOUT_ACTION = "Checkout";
	protected static final String UNDO_CHECKOUT_ACTION = "UndoCheckout";

	protected Product expectedProduct;

	public ProductLifecycleTest(TestName testName, String className) {
		super(testName, className);
	}

	@BeforeMethod
	public void init() throws Exception {
		ProductReqDetails defaultProduct = ElementFactory.getDefaultProduct(defaultCategories);
		RestResponse createProduct = ProductRestUtils.createProduct(defaultProduct, productManager1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());
		expectedProduct = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
	}
}
