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

package org.openecomp.sdc.asdctool.migration.config;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.resolver.SpringBeansMigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;

import static org.mockito.Mockito.mock;

public class MigrationSpringConfigTest {

	private MigrationSpringConfig createTestSubject() {
		return new MigrationSpringConfig();
	}

	@Test
	public void testSdcMigrationTool() throws Exception {
		MigrationSpringConfig testSubject;
		MigrationResolver migrationResolver = null;
		SdcRepoService sdcRepoService = null;
		SdcMigrationTool result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.sdcMigrationTool(migrationResolver, sdcRepoService);
	}

	@Test
	public void testMigrationResolver() throws Exception {
		MigrationSpringConfig testSubject;
		SdcRepoService sdcRepoService = null;
		SpringBeansMigrationResolver result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.migrationResolver(sdcRepoService);
	}

	@Test
	public void testSdcRepoService() throws Exception {
		MigrationSpringConfig testSubject;
		MigrationTasksDao migrationTasksDao = null;
		SdcRepoService result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.sdcRepoService(migrationTasksDao);
	}

	@Test
	public void testMigrationTasksDao() throws Exception {
		MigrationSpringConfig testSubject;
		MigrationTasksDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.migrationTasksDao(mock(CassandraClient.class));
	}


}
