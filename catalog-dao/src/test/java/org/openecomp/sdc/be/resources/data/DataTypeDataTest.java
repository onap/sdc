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
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class DataTypeDataTest {

	private DataTypeData createTestSubject() {
		return new DataTypeData();
	}

	@Test
	public void testCtor() throws Exception {
		new DataTypeData(new DataTypeDataDefinition());
		new DataTypeData(new HashMap<>());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		DataTypeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetDataTypeDataDefinition() throws Exception {
		DataTypeData testSubject;
		DataTypeDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeDataDefinition();
	}

	
	@Test
	public void testSetDataTypeDataDefinition() throws Exception {
		DataTypeData testSubject;
		DataTypeDataDefinition dataTypeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDataTypeDataDefinition(dataTypeDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		DataTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		DataTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}
