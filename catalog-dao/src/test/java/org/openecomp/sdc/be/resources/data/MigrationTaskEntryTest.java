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

import java.util.Date;

import org.junit.Test;


public class MigrationTaskEntryTest {

	private MigrationTaskEntry createTestSubject() {
		return new MigrationTaskEntry();
	}

	
	@Test
	public void testSetMajorVersion() throws Exception {
		MigrationTaskEntry testSubject;
		Long majorVersion = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMajorVersion(majorVersion);
	}

	
	@Test
	public void testSetMinorVersion() throws Exception {
		MigrationTaskEntry testSubject;
		Long minorVersion = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMinorVersion(minorVersion);
	}

	
	@Test
	public void testSetTimestamp() throws Exception {
		MigrationTaskEntry testSubject;
		Date timestamp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}

	
	@Test
	public void testSetTaskName() throws Exception {
		MigrationTaskEntry testSubject;
		String taskName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTaskName(taskName);
	}

	
	@Test
	public void testSetTaskStatus() throws Exception {
		MigrationTaskEntry testSubject;
		String taskStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTaskStatus(taskStatus);
	}

	
	@Test
	public void testSetMessage() throws Exception {
		MigrationTaskEntry testSubject;
		String message = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMessage(message);
	}

	
	@Test
	public void testSetExecutionTime() throws Exception {
		MigrationTaskEntry testSubject;
		double executionTime = 0.0;

		// default test
		testSubject = createTestSubject();
		testSubject.setExecutionTime(executionTime);
	}

	
	@Test
	public void testGetMajorVersion() throws Exception {
		MigrationTaskEntry testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMajorVersion();
	}

	
	@Test
	public void testGetMinorVersion() throws Exception {
		MigrationTaskEntry testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinorVersion();
	}

	
	@Test
	public void testGetTimestamp() throws Exception {
		MigrationTaskEntry testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}

	
	@Test
	public void testGetTaskName() throws Exception {
		MigrationTaskEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTaskName();
	}

	
	@Test
	public void testGetTaskStatus() throws Exception {
		MigrationTaskEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTaskStatus();
	}

	
	@Test
	public void testGetMessage() throws Exception {
		MigrationTaskEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMessage();
	}

	
	@Test
	public void testGetExecutionTime() throws Exception {
		MigrationTaskEntry testSubject;
		double result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getExecutionTime();
	}

	
	@Test
	public void testGetDescription() throws Exception {
		MigrationTaskEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		MigrationTaskEntry testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}
