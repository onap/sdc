package org.openecomp.sdc.asdctool.impl;

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

		// default test
		testSubject = createTestSubject();
		result = testSubject.dataMigration();
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
