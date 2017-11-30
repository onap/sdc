package org.openecomp.sdc.asdctool.migration.config;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.resolver.SpringBeansMigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;


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
		result = testSubject.migrationTasksDao();
	}

	
	@Test
	public void testCassandraClient() throws Exception {
		MigrationSpringConfig testSubject;
		CassandraClient result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.cassandraClient();
	}
}