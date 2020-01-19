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

package org.openecomp.sdc.asdctool.migration.core;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SdcMigrationToolTest {

	@InjectMocks
	private SdcMigrationTool testInstance = spy(SdcMigrationTool.class);

	@Mock
	private MigrationResolver migrationResolverMock;

	@Mock
	private SdcRepoService sdcRepoServiceMock;

	@BeforeMethod
    public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
    public void testMigrate_noMigrations() {
		when(migrationResolverMock.resolveMigrations()).thenReturn(Collections.emptyList());
		testInstance.migrate(false);
		verify(sdcRepoServiceMock, new Times(0)).clearTasksForCurrentMajor();
		verify(sdcRepoServiceMock, new Times(0)).createMigrationTask(Mockito.any());
	}

	@Test
    public void testMigrate_enforceFlag_removeAllMigrationTasksForCurrentVersion() {
		when(migrationResolverMock.resolveMigrations()).thenReturn(Collections.emptyList());
		testInstance.migrate(true);
		verify(sdcRepoServiceMock, new Times(1)).clearTasksForCurrentMajor();
	}

	@Test
    public void testMigrate_stopAfterFirstFailure() {
		when(migrationResolverMock.resolveMigrations())
				.thenReturn(Arrays.asList(new SuccessfulMigration(), new FailedMigration(), new SuccessfulMigration()));
		testInstance.migrate(false);
		verify(sdcRepoServiceMock, new Times(0)).clearTasksForCurrentMajor();
		verify(sdcRepoServiceMock, new Times(1)).createMigrationTask(Mockito.any());

	}

	private class FailedMigration implements Migration {

		@Override
		public String description() {
			return null;
		}

		@Override
		public DBVersion getVersion() {
			return DBVersion.fromString("1710.22");
		}

		@Override
		public MigrationResult migrate() {
			MigrationResult migrationResult = new MigrationResult();
			migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.FAILED);
			return migrationResult;
		}
	}

	private class SuccessfulMigration implements Migration {

		@Override
		public String description() {
			return null;
		}

		@Override
		public DBVersion getVersion() {
			return DBVersion.fromString("1710.22");
		}

		@Override
		public MigrationResult migrate() {
			MigrationResult migrationResult = new MigrationResult();
			migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
			return migrationResult;
		}
	}
}
