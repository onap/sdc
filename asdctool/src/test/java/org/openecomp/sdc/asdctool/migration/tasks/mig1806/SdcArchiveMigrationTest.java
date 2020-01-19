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
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;

import java.math.BigInteger;

import static org.junit.Assert.assertThat;

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
