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

package org.openecomp.sdc.asdctool.impl;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;

import static org.mockito.Mockito.mock;

public class EsToCassandraDataMigrationConfigTest {

	private EsToCassandraDataMigrationConfig createTestSubject() {
		return new EsToCassandraDataMigrationConfig();
	}

	@Test
	public void testDataMigration() throws Exception {
		EsToCassandraDataMigrationConfig testSubject;
		DataMigration result;
		AuditCassandraDao auditCassandraDaoMock = mock(AuditCassandraDao.class);
		ArtifactCassandraDao artifactCassandraDaoMock = mock(ArtifactCassandraDao.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.dataMigration(auditCassandraDaoMock, artifactCassandraDaoMock);
	}

	@Test
	public void testArtifactCassandraDao() throws Exception {
		EsToCassandraDataMigrationConfig testSubject;
		ArtifactCassandraDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactCassandraDao(mock(CassandraClient.class));
	}

	@Test
	public void testAuditCassandraDao() throws Exception {
		EsToCassandraDataMigrationConfig testSubject;
		AuditCassandraDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.auditCassandraDao(mock(CassandraClient.class));
	}

	@Test
	public void testCassandraClient() throws Exception {
		EsToCassandraDataMigrationConfig testSubject;
		CassandraClient result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.cassandraClient();
	}

	@Test
	public void testSdcSchemaFilesCassandraDao() throws Exception {
		EsToCassandraDataMigrationConfig testSubject;
		SdcSchemaFilesCassandraDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.sdcSchemaFilesCassandraDao(mock(CassandraClient.class));
	}
}
