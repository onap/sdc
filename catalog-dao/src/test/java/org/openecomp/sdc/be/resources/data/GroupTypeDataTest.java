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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class GroupTypeDataTest {

	private GroupTypeData createTestSubject() {
		return new GroupTypeData(new GroupTypeDataDefinition());
	}

	@Test
	public void testCtor() throws Exception {
		new GroupTypeData(new HashMap<>());
		new GroupTypeData(new GroupTypeDataDefinition());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		GroupTypeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetGroupTypeDataDefinition() throws Exception {
		GroupTypeData testSubject;
		GroupTypeDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupTypeDataDefinition();
	}

	
	@Test
	public void testSetGroupTypeDataDefinition() throws Exception {
		GroupTypeData testSubject;
		GroupTypeDataDefinition groupTypeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupTypeDataDefinition(groupTypeDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}
