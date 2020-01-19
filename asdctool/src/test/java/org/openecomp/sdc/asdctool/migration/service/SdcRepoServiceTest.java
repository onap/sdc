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

package org.openecomp.sdc.asdctool.migration.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SdcRepoServiceTest {

    @InjectMocks
    private SdcRepoService testInstance;

    @Mock
    private MigrationTasksDao migrationTasksDaoMock;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetLatestVersion_noMinorVersionForCurrentVersion() {
        when(migrationTasksDaoMock.getLatestMajorVersion()).thenReturn(DBVersion.DEFAULT_VERSION.getMajor());
        when(migrationTasksDaoMock.getLatestMinorVersion(migrationTasksDaoMock.getLatestMajorVersion())).thenReturn(BigInteger.valueOf(0));
        DBVersion latestDBVersion = testInstance.getLatestDBVersion();
        assertEquals(latestDBVersion.getMajor(), DBVersion.DEFAULT_VERSION.getMajor());
        assertEquals(latestDBVersion.getMinor(), BigInteger.valueOf(0));
    }

    @Test
    public void testCreateMigrationTask() {
        MigrationTaskEntry taskEntry =  new MigrationTaskEntry();
        testInstance.createMigrationTask(taskEntry);
        verify(migrationTasksDaoMock, new Times(1)).createMigrationTask(taskEntry);
    }

}
