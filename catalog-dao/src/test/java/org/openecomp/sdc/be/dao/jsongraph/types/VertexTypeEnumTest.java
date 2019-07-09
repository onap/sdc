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

package org.openecomp.sdc.be.dao.jsongraph.types;

import org.junit.Test;


public class VertexTypeEnumTest {

	private VertexTypeEnum createTestSubject() {
		return VertexTypeEnum.ADDITIONAL_INFORMATION;
	}

	
	@Test
	public void testGetName() throws Exception {
		VertexTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testGetClassOfJson() throws Exception {
		VertexTypeEnum testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClassOfJson();
	}

	
	@Test
	public void testGetByName() throws Exception {
		String name = "";
		VertexTypeEnum result;

		// default test
		result = VertexTypeEnum.getByName(name);
	}
}
