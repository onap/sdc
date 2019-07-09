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

package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class JsonPresentationFieldsTest {

	private JsonPresentationFields createTestSubject() {
		return JsonPresentationFields.API_URL;
	}

	@Test
	public void testGetPresentation() throws Exception {
		JsonPresentationFields testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPresentation();
	}

	@Test
	public void testSetPresentation() throws Exception {
		JsonPresentationFields testSubject;
		String presentation = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPresentation(presentation);
	}

	@Test
	public void testGetStoredAs() throws Exception {
		JsonPresentationFields testSubject;
		GraphPropertyEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStoredAs();
	}

	@Test
	public void testSetStoredAs() throws Exception {
		JsonPresentationFields testSubject;
		GraphPropertyEnum storedAs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setStoredAs(storedAs);
	}

	@Test
	public void testGetPresentationByGraphProperty() throws Exception {
		GraphPropertyEnum property = null;
		String result;

		// default test
		result = JsonPresentationFields.getPresentationByGraphProperty(null);
		result = JsonPresentationFields.getPresentationByGraphProperty(GraphPropertyEnum.INVARIANT_UUID);
	}

	@Test
	public void testToString() throws Exception {
		JsonPresentationFields testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testGetByPresentation() throws Exception {
		String presentation = "";
		JsonPresentationFields result;

		// default test
		result = JsonPresentationFields.getByPresentation(presentation);
	}
}
