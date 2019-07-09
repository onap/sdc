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

public class AdditionalInfoParameterInfoTest {

	private AdditionalInfoParameterInfo createTestSubject() {
		return new AdditionalInfoParameterInfo();
	}
	
	@Test
	public void testConstructors() throws Exception {

		// default test
		new AdditionalInfoParameterInfo("stam", "stam");
		new AdditionalInfoParameterInfo("stam", "stam", "stam");
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	@Test
	public void testGetKey() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKey();
	}

	@Test
	public void testSetKey() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String key = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setKey(key);
	}

	@Test
	public void testGetValue() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testSetValue() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String value = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}

	@Test
	public void testToString() throws Exception {
		AdditionalInfoParameterInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
