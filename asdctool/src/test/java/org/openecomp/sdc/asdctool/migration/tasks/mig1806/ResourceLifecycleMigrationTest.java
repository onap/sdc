/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;

import java.math.BigInteger;

import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLifecycleMigrationTest {

    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Mock
    private UserAdminOperation userAdminOperation;

    ResourceLifecycleMigration resourceLifecycleMigration = null;

    @Before
    public void setUp() throws Exception {
        resourceLifecycleMigration =
                new ResourceLifecycleMigration(janusGraphDao, lifecycleBusinessLogic, userAdminOperation);
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
