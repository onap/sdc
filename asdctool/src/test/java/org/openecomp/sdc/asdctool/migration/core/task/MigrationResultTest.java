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

package org.openecomp.sdc.asdctool.migration.core.task;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult.MigrationStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MigrationResultTest {

	private MigrationResult createTestSubject() {
		return new MigrationResult();
	}

	
	@Test
	public void testMsg() {
		MigrationResult testSubject  = createTestSubject();
		assertNull(testSubject.getMsg());
		testSubject.setMsg("msg");
		assertEquals("msg", testSubject.getMsg());
	}

	@Test
	public void testGetMigrationStatus() {
		MigrationResult testSubject;
		MigrationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMigrationStatus();
	}

	@Test
	public void testSetMigrationStatus() {
		MigrationResult testSubject;
		MigrationStatus migrationStatus = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMigrationStatus(migrationStatus);
	}

	@Test
	public void testSuccess() {
		MigrationResult testSubject = createTestSubject();
		assertEquals(MigrationResult.MigrationStatus.COMPLETED, testSubject.success().getMigrationStatus());
	}

	@Test
	public void testError() {
		MigrationResult testSubject = createTestSubject();
		MigrationResult result = testSubject.error("testErr");
		assertEquals(MigrationStatus.FAILED, result.getMigrationStatus());
		assertEquals("testErr", result.getMsg());
	}
}
