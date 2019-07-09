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

public class EntryDataTest {

	private EntryData createTestSubject() {
		return new EntryData(new Object(), new Object());
	}

	@Test
	public void testGetKey() throws Exception {
		EntryData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKey();
	}

	@Test
	public void testGetValue() throws Exception {
		EntryData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testSetValue() throws Exception {
		EntryData testSubject;
		Object value = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setValue(value);
	}
}
