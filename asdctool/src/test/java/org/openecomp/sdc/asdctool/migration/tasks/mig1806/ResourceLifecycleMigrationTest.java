package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLifecycleMigrationTest {

    @Mock
    private TitanDao titanDao;
    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Mock
    private UserAdminOperation userAdminOperation;

    ResourceLifecycleMigration resourceLifecycleMigration = null;

    @Before
    public void setUp() throws Exception {
        resourceLifecycleMigration =
                new ResourceLifecycleMigration(titanDao, lifecycleBusinessLogic, userAdminOperation);
    }

    @Test
    public void testDescription() {
        assertNotNull(resourceLifecycleMigration);
        assertEquals("change resource lifecycle state from testing to certified",
                resourceLifecycleMigration.description());
    }

    @Test
    public void testGetVersion() {
        DBVersion dbVersion = DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
        assertEquals(dbVersion, resourceLifecycleMigration.getVersion());
    }

    @Test(expected = NullPointerException.class)
    public void testMigrate() {
        assertNotNull(resourceLifecycleMigration);
        resourceLifecycleMigration.migrate();
    }
}
