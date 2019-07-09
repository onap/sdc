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

package org.openecomp.sdc.be.externalapi.servlet.representation;

import org.junit.Test;


public class ProductCategoryGroupMetadataTest {

	private ProductCategoryGroupMetadata createTestSubject() {
		return new ProductCategoryGroupMetadata("", "", "");
	}

	
	@Test
	public void testGetCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory();
	}

	
	@Test
	public void testSetCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String category = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategory(category);
	}

	
	@Test
	public void testGetSubCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategory();
	}

	
	@Test
	public void testSetSubCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String subCategory = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubCategory(subCategory);
	}

	
	@Test
	public void testGetGroup() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroup();
	}

	
	@Test
	public void testSetGroup() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String group = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroup(group);
	}
}
