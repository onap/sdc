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


public class GroupingDataDefinitionTest {

	private GroupingDataDefinition createTestSubject() {
		return new GroupingDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		GroupingDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		GroupingDataDefinition groupingDataDefinition = new GroupingDataDefinition(testSubject);
	}
	
	@Test
	public void testGetName() throws Exception {
		GroupingDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		GroupingDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetNormalizedName() throws Exception {
		GroupingDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}

	
	@Test
	public void testSetNormalizedName() throws Exception {
		GroupingDataDefinition testSubject;
		String normalizedName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNormalizedName(normalizedName);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupingDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		GroupingDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testHashCode() throws Exception {
		GroupingDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		GroupingDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(new GroupingDataDefinition(testSubject));
		Assert.assertEquals(true, result);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupingDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
