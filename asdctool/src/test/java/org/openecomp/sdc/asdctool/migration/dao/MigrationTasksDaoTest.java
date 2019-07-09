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

package org.openecomp.sdc.asdctool.migration.dao;

import org.junit.Test;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

import java.math.BigInteger;

import static org.mockito.Mockito.mock;

public class MigrationTasksDaoTest {

	private MigrationTasksDao createTestSubject() {
		return new MigrationTasksDao(mock(CassandraClient.class));
	}

	@Test
	public void testInit() throws Exception {
		MigrationTasksDao testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}

	@Test(expected=NullPointerException.class)
	public void testGetLatestMinorVersion() throws Exception {
		MigrationTasksDao testSubject;
		BigInteger majorVersion = null;
		BigInteger result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLatestMinorVersion(majorVersion);
	}

	@Test(expected=NullPointerException.class)
	public void testDeleteAllTasksForVersion() throws Exception {
		MigrationTasksDao testSubject;
		BigInteger majorVersion = null;

		// default test
		testSubject = createTestSubject();
		testSubject.deleteAllTasksForVersion(majorVersion);
	}

	@Test(expected=NullPointerException.class)
	public void testCreateMigrationTask() throws Exception {
		MigrationTasksDao testSubject;
		MigrationTaskEntry migrationTask = null;

		// default test
		testSubject = createTestSubject();
		testSubject.createMigrationTask(migrationTask);
	}
}
