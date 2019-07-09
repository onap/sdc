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

package org.openecomp.sdc.be.resources.data.category;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class GroupingDataTest {

	private GroupingData createTestSubject() {
		return new GroupingData(NodeTypeEnum.AdditionalInfoParameters);
	}

	@Test
	public void testCtor() throws Exception {
		new GroupingData(new HashMap<>());
		new GroupingData(NodeTypeEnum.AdditionalInfoParameters, new GroupingDataDefinition());
	}
	
	@Test
	public void testGetGroupingDataDefinition() throws Exception {
		GroupingData testSubject;
		GroupingDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupingDataDefinition();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupingData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		GroupingData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}
