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

package org.openecomp.sdc.asdctool.migration.resolver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;

@ExtendWith(MockitoExtension.class)
class SpringBeansMigrationResolverTest {

    @InjectMocks
    private SpringBeansMigrationResolver testInstance;

    @Mock
    private SdcRepoService sdcRepoServiceMock;

    private List<Migration> migrations = Arrays.asList(createMigration("1710.1"), createMigration("1710.22"),
        createMigration("1707.12"), createMigration("1710.3"));

    @BeforeEach
    public void setUp() {
        testInstance.setMigrations(migrations);
    }

    @Test
    void testResolveMigrations_getMigrationsWithVersionGreaterThanLatest() {
        when(sdcRepoServiceMock.getLatestDBVersion()).thenReturn(DBVersion.fromString("1710.2"));
        testInstance.setPostMigrations(Collections.emptyList());
        List<IMigrationStage> resolvedMigrations = testInstance.resolveMigrations();
        assertEquals(resolvedMigrations.size(), 2);
        assertEquals(resolvedMigrations.get(0).getVersion(), DBVersion.fromString("1710.3"));
        assertEquals(resolvedMigrations.get(1).getVersion(), DBVersion.fromString("1710.22"));
    }

    @Test
    void testResolveMigration_noLatestVersionForCurrentMajorVersion() {
        when(sdcRepoServiceMock.getLatestDBVersion()).thenReturn(DBVersion.fromString("1710.-1"));
        testInstance.setPostMigrations(Collections.emptyList());
        List<IMigrationStage> resolvedMigrations = testInstance.resolveMigrations();
        assertEquals(resolvedMigrations.size(), 3);
        assertEquals(resolvedMigrations.get(0).getVersion(), DBVersion.fromString("1710.1"));
        assertEquals(resolvedMigrations.get(1).getVersion(), DBVersion.fromString("1710.3"));
        assertEquals(resolvedMigrations.get(2).getVersion(), DBVersion.fromString("1710.22"));
    }

    @Test
    void testResolveMigrations_emptyMigrationsList() {
        testInstance.setMigrations(Collections.emptyList());
        testInstance.setPostMigrations(Collections.emptyList());
        when(sdcRepoServiceMock.getLatestDBVersion()).thenReturn(DBVersion.fromString("1710.-1"));
        List<IMigrationStage> resolvedMigrations = testInstance.resolveMigrations();
        assertTrue(resolvedMigrations.isEmpty());
    }

    private Migration createMigration(String version) {
        return new Migration() {
            @Override
            public String description() {
                return null;
            }

            @Override
            public DBVersion getVersion() {
                return DBVersion.fromString(version);
            }

            @Override
            public MigrationResult migrate() {
                return null;
            }
        };
    }

    private SpringBeansMigrationResolver createTestSubject() {
        return new SpringBeansMigrationResolver(null, null, new SdcRepoService(new MigrationTasksDao(mock(CassandraClient.class))));
    }
}
