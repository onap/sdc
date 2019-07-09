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

package org.openecomp.sdc.asdctool.migration.core.execution;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult.MigrationStatus;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

public class MigrationExecutionResultTest {

	private MigrationExecutionResult createTestSubject() {
		return new MigrationExecutionResult();
	}

	@Test(expected=NullPointerException.class)
	public void testToMigrationTaskEntry() throws Exception {
		MigrationExecutionResult testSubject;
		MigrationTaskEntry result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toMigrationTaskEntry();
	}

	@Test
	public void testGetMigrationStatus() throws Exception {
		MigrationExecutionResult testSubject;
		MigrationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMigrationStatus();
	}

	@Test
	public void testGetMsg() throws Exception {
		MigrationExecutionResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMsg();
	}

	@Test
	public void testGetVersion() throws Exception {
		MigrationExecutionResult testSubject;
		DBVersion result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testSetVersion() throws Exception {
		MigrationExecutionResult testSubject;
		DBVersion version = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	@Test
	public void testGetDescription() throws Exception {
		MigrationExecutionResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		MigrationExecutionResult testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}
