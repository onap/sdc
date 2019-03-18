package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;

@RunWith(MockitoJUnitRunner.class)
public class SdcArchiveMigrationTest {
    @Mock
    private TitanDao titanDao;

    SdcArchiveMigration sdcArchiveMigration = null;

    @Before
    public void setUp() throws Exception {
        sdcArchiveMigration = new SdcArchiveMigration(titanDao);
    }

    @Test
    public void testDescription() {
        assertNotNull(sdcArchiveMigration);
        assertEquals("add archive node for archiving/restoring components ", sdcArchiveMigration.description());
    }

    @Test
    public void testGetVersion() {
        DBVersion dbVersion = DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
        assertEquals(dbVersion, sdcArchiveMigration.getVersion());
    }

    @Test(expected = NullPointerException.class)
    public void testMigrate() {
        assertNotNull(sdcArchiveMigration);
        sdcArchiveMigration.migrate();
    }
}
