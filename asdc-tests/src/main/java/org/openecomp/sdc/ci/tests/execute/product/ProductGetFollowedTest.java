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

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ProductValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductGetFollowedTest extends ProductBaseTest {

	protected Product product200;
	protected Product product400;

	private ProductReqDetails productDetails200;
	private ProductReqDetails productDetails400;

	@Rule
	public static TestName name = new TestName();

	public ProductGetFollowedTest() {
		super(name, ProductGetFollowedTest.class.getName());
	}

	@BeforeMethod
	public void init() throws Exception {
		createProducts();
	}

	@Test
	public void followedPageTest() throws Exception { // Actions
		RestResponse changeLifeCycleResponse;
		changeLifeCycleResponse = ProductRestUtils.changeProductLifeCycle(product200, productManager1, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeLifeCycleResponse);
		changeLifeCycleResponse = ProductRestUtils.changeProductLifeCycle(product400, productManager2, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeLifeCycleResponse);
		// Expected users Followed page
		ProductValidationUtils.checkUserFollowedPage(productManager1, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager1, product400);
		ProductValidationUtils.checkUserFollowedPage(productManager2, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager2, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(adminUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(designerUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productStrategistUser1, product200, product400);
	}

	@Test
	public void followedPagePmCheckedOutProductWasCheckInByOtherPm() throws Exception {
		ProductRestUtils.changeProductLifeCycle(product200, productManager1, LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.changeProductLifeCycle(product400, productManager2, LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.changeProductLifeCycle(product400, productManager1, LifeCycleStatesEnum.CHECKOUT);
		RestResponse changeLifeCycleResponse = ProductRestUtils.changeProductLifeCycle(product200, productManager2, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeLifeCycleResponse);

		ProductValidationUtils.checkUserFollowedPage(productManager1, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager2, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(adminUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(designerUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productStrategistUser1, product200, product400);
	}

	@Test
	public void followedPagePmCheckInProduct02() throws Exception {
		ProductRestUtils.changeProductLifeCycle(product200, productManager1, LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.changeProductLifeCycle(product400, productManager2, LifeCycleStatesEnum.CHECKIN);

		ProductValidationUtils.checkUserFollowedPage(productManager1, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager1, product400);
		ProductValidationUtils.checkUserFollowedPage(productManager2, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager2, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(adminUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(designerUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productStrategistUser1, product200, product400);
	}

	@Test
	public void followedPageAdminCheckoutProductWasCheckedinByPm() throws Exception {
		ProductRestUtils.changeProductLifeCycle(product400, productManager2, LifeCycleStatesEnum.CHECKIN);
		RestResponse changeLifeCycleResponse = ProductRestUtils.changeProductLifeCycle(product400, adminUser, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeLifeCycleResponse);

		ProductValidationUtils.checkUserFollowedPage(productManager1, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager1, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager2, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(adminUser, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(designerUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productStrategistUser1, product200, product400);
	}

	@Test
	public void followedPageAdminCheckInProduct() throws Exception {
		ProductRestUtils.changeProductLifeCycle(product400, productManager2, LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.changeProductLifeCycle(product400, adminUser, LifeCycleStatesEnum.CHECKOUT);
		RestResponse changeLifeCycleResponse = ProductRestUtils.changeProductLifeCycle(product400, adminUser, LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(changeLifeCycleResponse);

		ProductValidationUtils.checkUserFollowedPage(productManager1, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager1, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager2, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(adminUser, product200);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(designerUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productStrategistUser1, product200, product400);
	}

	@Test
	public void followedPagePmCheckoutProductWasCheckedinByAdmin() throws Exception {
		ProductRestUtils.changeProductLifeCycle(product200, productManager1, LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.changeProductLifeCycle(product400, productManager2, LifeCycleStatesEnum.CHECKIN);
		ProductRestUtils.changeProductLifeCycle(product200, adminUser, LifeCycleStatesEnum.CHECKOUT);
		ProductRestUtils.changeProductLifeCycle(product200, adminUser, LifeCycleStatesEnum.CHECKIN);
		RestResponse changeLifeCycleResponse = ProductRestUtils.changeProductLifeCycle(product200, productManager2, LifeCycleStatesEnum.CHECKOUT);
		ResourceRestUtils.checkSuccess(changeLifeCycleResponse);

		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productManager1, product200);
		ProductValidationUtils.checkUserFollowedPage(productManager2, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(adminUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(designerUser, product200, product400);
		ProductValidationUtils.verifyProductsNotExistInUserFollowedPage(productStrategistUser1, product200, product400);
	}

	private void createProducts() throws Exception {
		// PM1 (Product manager) create :PR200 (check In State)
		// PM2 (Product manager) create :PR400 (check In State)

		productDetails200 = ElementFactory.getDefaultProduct("CiProd200", defaultCategories);
		productDetails400 = ElementFactory.getDefaultProduct("CiProd400", defaultCategories);

		RestResponse createProduct = ProductRestUtils.createProduct(productDetails200, productManager1);
		ResourceRestUtils.checkCreateResponse(createProduct);
		product200 = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);

		createProduct = ProductRestUtils.createProduct(productDetails400, productManager2);
		ResourceRestUtils.checkCreateResponse(createProduct);
		product400 = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);
	}

}
