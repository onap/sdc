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

package org.openecomp.sdc.be.model.tosca.version;

import org.junit.Test;

public class VersionTest {

	private Version createTestSubject() {
		return new Version("");
	}

	
	@Test
	public void testHashCode() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		Version testSubject;
		Object other = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(other);
	}

	
	@Test
	public void testCompareTo() throws Exception {
		Version testSubject;
		Version otherVersion = null;
		int result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetMajorVersion() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMajorVersion();
	}

	
	@Test
	public void testGetMinorVersion() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinorVersion();
	}

	
	@Test
	public void testGetIncrementalVersion() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIncrementalVersion();
	}

	
	@Test
	public void testGetBuildNumber() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBuildNumber();
	}

	
	@Test
	public void testGetQualifier() throws Exception {
		Version testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getQualifier();
	}

	
	@Test
	public void testParseVersion() throws Exception {
		Version testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.parseVersion(version);
	}

	
	@Test
	public void testGetNextIntegerToken() throws Exception {
		Integer result;

		// default test
	}

	
	@Test
	public void testToString() throws Exception {
		Version testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
