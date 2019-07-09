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

package org.openecomp.sdc.be.datatypes.category;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class SubCategoryDataDefinitionTest {

	private SubCategoryDataDefinition createTestSubject() {
		return new SubCategoryDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		SubCategoryDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		SubCategoryDataDefinition subCategoryDataDefinition = new SubCategoryDataDefinition(testSubject);
	}
	
	@Test
	public void testGetName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetNormalizedName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}

	
	@Test
	public void testSetNormalizedName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String normalizedName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNormalizedName(normalizedName);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		SubCategoryDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetIcons() throws Exception {
		SubCategoryDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIcons();
	}

	
	@Test
	public void testSetIcons() throws Exception {
		SubCategoryDataDefinition testSubject;
		List<String> icons = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIcons(icons);
	}

	
	@Test
	public void testHashCode() throws Exception {
		SubCategoryDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		SubCategoryDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(new SubCategoryDataDefinition());
		Assert.assertEquals(true, result);
	}

	
	@Test
	public void testToString() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
