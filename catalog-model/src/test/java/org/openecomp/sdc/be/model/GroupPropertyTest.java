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

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public class GroupPropertyTest {

	private GroupProperty createTestSubject() {
		return new GroupProperty();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupProperty(new GroupProperty());
		new GroupProperty(new PropertyDataDefinition());
		new GroupProperty(new PropertyDefinition(), "mock", "mock");
	}
	
	@Test
	public void testGetValueUniqueUid() throws Exception {
		GroupProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueUniqueUid();
	}

	
	@Test
	public void testSetValueUniqueUid() throws Exception {
		GroupProperty testSubject;
		String valueUniqueUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValueUniqueUid(valueUniqueUid);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
