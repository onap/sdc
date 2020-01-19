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

import java.util.List;


public class ProductAssetMetadataTest {

	private ProductAssetMetadata createTestSubject() {
		return new ProductAssetMetadata();
	}

	
	@Test
	public void testGetLifecycleState() throws Exception {
		ProductAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLifecycleState();
	}

	
	@Test
	public void testSetLifecycleState() throws Exception {
		ProductAssetMetadata testSubject;
		String lifecycleState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLifecycleState(lifecycleState);
	}

	
	@Test
	public void testGetLastUpdaterUserId() throws Exception {
		ProductAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterUserId();
	}

	
	@Test
	public void testSetLastUpdaterUserId() throws Exception {
		ProductAssetMetadata testSubject;
		String lastUpdaterUserId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterUserId(lastUpdaterUserId);
	}

	
	@Test
	public void testIsActive() throws Exception {
		ProductAssetMetadata testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isActive();
	}

	
	@Test
	public void testSetActive() throws Exception {
		ProductAssetMetadata testSubject;
		boolean isActive = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setActive(isActive);
	}

	
	@Test
	public void testGetContacts() throws Exception {
		ProductAssetMetadata testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContacts();
	}

	
	@Test
	public void testSetContacts() throws Exception {
		ProductAssetMetadata testSubject;
		List<String> contacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setContacts(contacts);
	}

	
	@Test
	public void testGetProductGroupings() throws Exception {
		ProductAssetMetadata testSubject;
		List<ProductCategoryGroupMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProductGroupings();
	}

	
	@Test
	public void testSetProductGroupings() throws Exception {
		ProductAssetMetadata testSubject;
		List<ProductCategoryGroupMetadata> productGroupings = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProductGroupings(productGroupings);
	}
}
