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

package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

public class CategoryTest {

	private Category createTestSubject() {
		return new Category();
	}

	@Test
	public void testGetName() throws Exception {
		Category testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		Category testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testHashCode() throws Exception {
		Category testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		Category testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(new Object());
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
		Category createTestSubject = createTestSubject();
		createTestSubject.setName("mock");
		testSubject.setName("mock2");
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		Category testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
