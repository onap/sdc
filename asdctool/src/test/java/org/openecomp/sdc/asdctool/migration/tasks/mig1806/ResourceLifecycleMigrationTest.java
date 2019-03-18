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
        assertThat(resourceLifecycleMigration,IsNull.notNullValue());
        assertThat("change resource lifecycle state from testing to certified", Is.is(resourceLifecycleMigration.description()));
    }

    @Test
    public void testGetVersion() {
        DBVersion dbVersion = DBVersion.from(BigInteger.valueOf(Version.MAJOR.getValue()), BigInteger.valueOf(Version.MINOR.getValue()));
        assertThat(dbVersion, Is.is(resourceLifecycleMigration.getVersion()));
    }

    @Test(expected = NullPointerException.class)
    public void testMigrate() {
        assertThat(resourceLifecycleMigration,IsNull.notNullValue());
        resourceLifecycleMigration.migrate();
    }
}
