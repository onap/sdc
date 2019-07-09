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

package org.openecomp.sdc.be.model.tosca;

import org.junit.Assert;
import org.junit.Test;


public class ToscaTypeTest {

	private ToscaType createTestSubject() {
		return  ToscaType.BOOLEAN;
	}


	
	@Test
	public void testIsValidValue() throws Exception {
		ToscaType testSubject;
		String value = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isValidValue(value);
	}


	
	@Test
	public void testConvert() throws Exception {
		ToscaType testSubject;
		String value = "";
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convert(value);
	}

	
	@Test
	public void testToString() throws Exception {
		ToscaType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
