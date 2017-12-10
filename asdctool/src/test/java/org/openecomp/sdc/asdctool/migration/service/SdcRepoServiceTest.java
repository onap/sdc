package org.openecomp.sdc.asdctool.migration.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.math.BigInteger;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SdcRepoServiceTest {

    @InjectMocks
    private SdcRepoService testInstance;

    @Mock
    private MigrationTasksDao migrationTasksDaoMock;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetLatestVersion_noMinorVersionForCurrentVersion() throws Exception {
        when(migrationTasksDaoMock.getLatestMinorVersion(DBVersion.CURRENT_VERSION.getMajor())).thenReturn(null);
        DBVersion latestDBVersion = testInstance.getLatestDBVersion();
        assertEquals(latestDBVersion.getMajor(), DBVersion.CURRENT_VERSION.getMajor());
        assertEquals(latestDBVersion.getMinor(), BigInteger.valueOf(Integer.MIN_VALUE));
    }

    @Test
    public void testGetLatestVersion() throws Exception {
        when(migrationTasksDaoMock.getLatestMinorVersion(DBVersion.CURRENT_VERSION.getMajor())).thenReturn(BigInteger.TEN);
        DBVersion latestDBVersion = testInstance.getLatestDBVersion();
        assertEquals(latestDBVersion.getMajor(), DBVersion.CURRENT_VERSION.getMajor());
        assertEquals(latestDBVersion.getMinor(), BigInteger.TEN);
    }

    @Test
    public void testCreateMigrationTask() throws Exception {
        MigrationTaskEntry taskEntry =  new MigrationTaskEntry();
        testInstance.createMigrationTask(taskEntry);
        verify(migrationTasksDaoMock, new Times(1)).createMigrationTask(taskEntry);
    }

}
