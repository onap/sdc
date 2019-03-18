package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigInteger;
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
        assertNotNull(forwardPathMigration);
        assertEquals("remove corrupted forwarding paths ",
                forwardPathMigration.description());
    }

    @Test
    public void testGetVersion() {
        DBVersion dbVersion = DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
        assertEquals(dbVersion, forwardPathMigration.getVersion());
    }

    @Test(expected = NullPointerException.class)
    public void testMigrate() {
        assertNotNull(forwardPathMigration);
        forwardPathMigration.migrate();
    }
}
