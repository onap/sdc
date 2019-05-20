package org.openecomp.sdc.asdctool.migration.tasks.mig1802;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
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