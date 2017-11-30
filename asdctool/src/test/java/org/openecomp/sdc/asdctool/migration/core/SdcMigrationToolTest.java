package org.openecomp.sdc.asdctool.migration.core;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SdcMigrationToolTest {

    @InjectMocks
    private SdcMigrationTool testInstance = spy(SdcMigrationTool.class);

    @Mock
    private MigrationResolver migrationResolverMock;

    @Mock
    private SdcRepoService sdcRepoServiceMock;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMigrate_noMigrations() throws Exception {
        when(migrationResolverMock.resolveMigrations()).thenReturn(Collections.emptyList());
        testInstance.migrate(false);
        verify(sdcRepoServiceMock, new Times(0)).clearTasksForCurrentMajor();
        verify(sdcRepoServiceMock, new Times(0)).createMigrationTask(Mockito.any());
    }

    @Test
    public void testMigrate_enforceFlag_removeAllMigrationTasksForCurrentVersion() throws Exception {
        when(migrationResolverMock.resolveMigrations()).thenReturn(Collections.emptyList());
        testInstance.migrate(true);
        verify(sdcRepoServiceMock, new Times(1)).clearTasksForCurrentMajor();
    }

    @Test
    public void testMigrate_stopAfterFirstFailure() throws Exception {
        when(migrationResolverMock.resolveMigrations()).thenReturn(Arrays.asList(new SuccessfulMigration(), new FailedMigration(), new SuccessfulMigration()));
        testInstance.migrate(false);
        verify(sdcRepoServiceMock, new Times(0)).clearTasksForCurrentMajor();
        verify(sdcRepoServiceMock, new Times(1)).createMigrationTask(Mockito.any());

    }

    private class FailedMigration implements Migration {

        @Override
        public String description() {
            return null;
        }

        @Override
        public DBVersion getVersion() {
            return DBVersion.fromString("1710.22");
        }

        @Override
        public MigrationResult migrate() {
            MigrationResult migrationResult = new MigrationResult();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.FAILED);
            return migrationResult;
        }
    }

    private class SuccessfulMigration implements Migration {

        @Override
        public String description() {
            return null;
        }

        @Override
        public DBVersion getVersion() {
            return DBVersion.fromString("1710.22");
        }

        @Override
        public MigrationResult migrate() {
            MigrationResult migrationResult = new MigrationResult();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
            return migrationResult;
        }
    }
}
