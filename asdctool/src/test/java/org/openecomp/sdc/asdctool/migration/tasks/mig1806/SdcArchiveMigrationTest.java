package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import static org.junit.Assert.assertThat;
import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;

@RunWith(MockitoJUnitRunner.class)
public class SdcArchiveMigrationTest {
    @Mock
    private JanusGraphDao janusGraphDao;

    SdcArchiveMigration sdcArchiveMigration = null;

    @Before
    public void setUp() throws Exception {
        sdcArchiveMigration = new SdcArchiveMigration(janusGraphDao);
    }

    @Test
    public void testDescription() {
        assertThat(sdcArchiveMigration,IsNull.notNullValue());
        assertThat("add archive node for archiving/restoring components ", Is.is(sdcArchiveMigration.description()));
    }

    @Test
    public void testGetVersion() {
        DBVersion dbVersion = DBVersion.from(BigInteger.valueOf(Version.MAJOR.getValue()), BigInteger.valueOf(Version.MINOR.getValue()));
        assertThat(dbVersion, Is.is(sdcArchiveMigration.getVersion()));
    }

    @Test(expected = NullPointerException.class)
    public void testMigrate() {
        assertThat(sdcArchiveMigration,IsNull.notNullValue());
        sdcArchiveMigration.migrate();
    }
}
