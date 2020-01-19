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

package org.openecomp.sdc.asdctool.migration.tasks.mig1802;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;

public class SdcCatalogMigrationTest {

	private SdcCatalogMigration createTestSubject() {
		return new SdcCatalogMigration(new TopologyTemplateOperation(), new JanusGraphDao(new JanusGraphClient()));
	}

	@Test
	public void testDescription() throws Exception {
		SdcCatalogMigration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.description();
	}

	@Test
	public void testGetVersion() throws Exception {
		SdcCatalogMigration testSubject;
		DBVersion result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testMigrate() throws Exception {
		SdcCatalogMigration testSubject;
		MigrationResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.migrate();
	}
}
