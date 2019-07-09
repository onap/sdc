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

package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;


public class ValidationTaskResultTest {

	private ValidationTaskResult createTestSubject() {
		return new ValidationTaskResult(new GraphVertex(), "", "", false);
	}

	
	@Test
	public void testGetName() {
		ValidationTaskResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() {
		ValidationTaskResult testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetResultMessage() {
		ValidationTaskResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResultMessage();
	}

	
	@Test
	public void testSetResultMessage() {
		ValidationTaskResult testSubject;
		String resultMessage = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResultMessage(resultMessage);
	}

	
	@Test
	public void testIsSuccessful() {
		ValidationTaskResult testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isSuccessful();
	}

	
	@Test
	public void testSetSuccessful() {
		ValidationTaskResult testSubject;
		boolean successful = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setSuccessful(successful);
	}
}
