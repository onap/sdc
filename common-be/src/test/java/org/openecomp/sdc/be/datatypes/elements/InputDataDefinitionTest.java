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

import java.util.HashMap;


public class InputDataDefinitionTest {

	private InputDataDefinition createTestSubject() {
		return new InputDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		InputDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		new InputDataDefinition(testSubject);
		new InputDataDefinition(new HashMap<>());
		new InputDataDefinition(new PropertyDataDefinition());
	}
	
	@Test
	public void testIsHidden() throws Exception {
		InputDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isHidden();
	}

	
	@Test
	public void testSetHidden() throws Exception {
		InputDataDefinition testSubject;
		Boolean hidden = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHidden(hidden);
	}

	
	@Test
	public void testIsImmutable() throws Exception {
		InputDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isImmutable();
	}

	
	@Test
	public void testSetImmutable() throws Exception {
		InputDataDefinition testSubject;
		Boolean immutable = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImmutable(immutable);
	}

	
	@Test
	public void testGetLabel() throws Exception {
		InputDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}

	
	@Test
	public void testSetLabel() throws Exception {
		InputDataDefinition testSubject;
		String label = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(label);
	}
}
