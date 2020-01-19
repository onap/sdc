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
import org.mockito.Mock;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;

import java.math.BigInteger;

import static org.junit.Assert.assertThat;

public class ForwardPathMigrationTest {

    ForwardPathMigration forwardPathMigration = null;

    @Mock
    JanusGraphDao janusGraphDao;

    @Mock
    UserAdminOperation userAdminOperation;

    @Mock
    ToscaOperationFacade toscaOperationFacade;

    @Before
    public void setUp() throws Exception {
        forwardPathMigration = new ForwardPathMigration(janusGraphDao, userAdminOperation, toscaOperationFacade);
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
