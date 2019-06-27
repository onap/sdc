package org.openecomp.sdc.asdctool.migration.resolver;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SpringBeansMigrationResolverTest {

	@InjectMocks
	private SpringBeansMigrationResolver testInstance;

	@Mock
	private SdcRepoService sdcRepoServiceMock;

	private List<Migration> migrations = Arrays.asList(createMigration("1710.1"), createMigration("1710.22"),
			createMigration("1707.12"), createMigration("1710.3"));

	@BeforeMethod
    public void setUp() {
		MockitoAnnotations.initMocks(this);
		testInstance.setMigrations(migrations);
	}

	@Test
    public void testResolveMigrations_getMigrationsWithVersionGreaterThanLatest() {
		when(sdcRepoServiceMock.getLatestDBVersion()).thenReturn(DBVersion.fromString("1710.2"));
		testInstance.setPostMigrations(Collections.emptyList());
		List<IMigrationStage> resolvedMigrations = testInstance.resolveMigrations();
		assertEquals(resolvedMigrations.size(), 2);
		assertEquals(resolvedMigrations.get(0).getVersion(), DBVersion.fromString("1710.3"));
		assertEquals(resolvedMigrations.get(1).getVersion(), DBVersion.fromString("1710.22"));
	}

	@Test
    public void testResolveMigration_noLatestVersionForCurrentMajorVersion() {
		when(sdcRepoServiceMock.getLatestDBVersion()).thenReturn(DBVersion.fromString("1710.-1"));
		testInstance.setPostMigrations(Collections.emptyList());
		List<IMigrationStage> resolvedMigrations = testInstance.resolveMigrations();
		assertEquals(resolvedMigrations.size(), 3);
		assertEquals(resolvedMigrations.get(0).getVersion(), DBVersion.fromString("1710.1"));
		assertEquals(resolvedMigrations.get(1).getVersion(), DBVersion.fromString("1710.3"));
		assertEquals(resolvedMigrations.get(2).getVersion(), DBVersion.fromString("1710.22"));
	}

	@Test
    public void testResolveMigrations_emptyMigrationsList() {
		testInstance.setMigrations(Collections.emptyList());
		testInstance.setPostMigrations(Collections.emptyList());
		when(sdcRepoServiceMock.getLatestDBVersion()).thenReturn(DBVersion.fromString("1710.-1"));
		List<IMigrationStage> resolvedMigrations = testInstance.resolveMigrations();
		assertTrue(resolvedMigrations.isEmpty());
	}

	private Migration createMigration(String version) {
		return new Migration() {
			@Override
			public String description() {
				return null;
			}

			@Override
			public DBVersion getVersion() {
				return DBVersion.fromString(version);
			}

			@Override
			public MigrationResult migrate() {
				return null;
			}
		};
	}

	private SpringBeansMigrationResolver createTestSubject() {
		return new SpringBeansMigrationResolver(null, null, new SdcRepoService(new MigrationTasksDao(mock(CassandraClient.class))));
	}
}
