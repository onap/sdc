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

package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

public class OperationInputDefinitionTest {

	private OperationInputDefinition createTestSubject() {
		return new OperationInputDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		OperationInputDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new OperationInputDefinition("", "", true, "");
		new OperationInputDefinition("stam", testSubject, null, null);
	}

	@Test
	public void testGetLabel() throws Exception {
		OperationInputDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}

	@Test
	public void testSetLabel() throws Exception {
		OperationInputDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(name);
	}
}
