package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import static org.junit.Assert.assertThat;
import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;

public class ForwardPathMigrationTest {

    ForwardPathMigration forwardPathMigration = null;

    @Mock
    TitanDao titanDao;

    @Mock
    UserAdminOperation userAdminOperation;

    @Mock
    ToscaOperationFacade toscaOperationFacade;

    @Before
    public void setUp() throws Exception {
        forwardPathMigration = new ForwardPathMigration(titanDao, userAdminOperation, toscaOperationFacade);
    }

    @Test
    public void testDescription() {
        assertThat(forwardPathMigration,IsNull.notNullValue());
        assertThat("remove corrupted forwarding paths ", Is.is(forwardPathMigration.description()));
    }

    @Test
    public void testGetVersion() {
        DBVersion dbVersion = DBVersion.from(BigInteger.valueOf(Version.MAJOR.getValue()), BigInteger.valueOf(Version.MINOR.getValue()));
        assertThat(dbVersion,Is.is(forwardPathMigration.getVersion()));
    }

    @Test(expected = NullPointerException.class)
    public void testMigrate() {
        assertThat(forwardPathMigration,IsNull.notNullValue());
        forwardPathMigration.migrate();
    }
}
