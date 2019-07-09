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

package org.openecomp.sdc.be.model.category;

import java.util.List;

import org.junit.Test;


public class SubCategoryDefinitionTest {

	private SubCategoryDefinition createTestSubject() {
		return new SubCategoryDefinition();
	}

	
	@Test
	public void testGetGroupings() throws Exception {
		SubCategoryDefinition testSubject;
		List<GroupingDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupings();
	}

	
	@Test
	public void testSetGroupings() throws Exception {
		SubCategoryDefinition testSubject;
		List<GroupingDefinition> groupingDefinitions = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupings(groupingDefinitions);
	}

	
	@Test
	public void testAddGrouping() throws Exception {
		SubCategoryDefinition testSubject;
		GroupingDefinition groupingDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addGrouping(groupingDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		SubCategoryDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
