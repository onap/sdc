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

package org.openecomp.sdc.be.model.tosca.validators;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

import java.util.Map;

public class ToscaBooleanValidatorTest {

	private ToscaBooleanValidator createTestSubject() {
		return ToscaBooleanValidator.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaBooleanValidator result;

		// default test
		result = ToscaBooleanValidator.getInstance();
	}

	
	@Test
	public void testIsValid() throws Exception {
		ToscaBooleanValidator testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		value = null;
		result = testSubject.isValid(value, innerType, dataTypes);

		// test 2
		testSubject = createTestSubject();
		value = "";
		result = testSubject.isValid(value, innerType, dataTypes);
	}

	
	@Test
	public void testIsValid_1() throws Exception {
		ToscaBooleanValidator testSubject;
		String value = "";
		String innerType = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isValid(value, innerType);
	}
}
