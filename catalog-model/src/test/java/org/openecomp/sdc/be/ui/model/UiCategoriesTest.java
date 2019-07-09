/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.model.category.CategoryDefinition;


public class UiCategoriesTest {

	private UiCategories createTestSubject() {
		return new UiCategories();
	}

	
	@Test
	public void testGetResourceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceCategories();
	}

	
	@Test
	public void testSetResourceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> resourceCategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceCategories(resourceCategories);
	}

	
	@Test
	public void testGetServiceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceCategories();
	}

	
	@Test
	public void testSetServiceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> serviceCategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceCategories(serviceCategories);
	}

	
	@Test
	public void testGetProductCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProductCategories();
	}

	
	@Test
	public void testSetProductCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> productCategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProductCategories(productCategories);
	}
}
