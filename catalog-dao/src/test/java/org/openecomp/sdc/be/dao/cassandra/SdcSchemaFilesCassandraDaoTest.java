package org.openecomp.sdc.be.dao.cassandra;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;

import fj.data.Either;


public class SdcSchemaFilesCassandraDaoTest {

	private SdcSchemaFilesCassandraDao createTestSubject() {
		return new SdcSchemaFilesCassandraDao();
	}


	
	@Test
	public void testGetSpecificSchemaFiles() throws Exception {
		SdcSchemaFilesCassandraDao testSubject;
		String sdcreleasenum = "";
		String conformancelevel = "";
		Either<List<SdcSchemaFilesData>, CassandraOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSpecificSchemaFiles(sdcreleasenum, conformancelevel);
	}

	
	@Test
	public void testDeleteAllArtifacts() throws Exception {
		SdcSchemaFilesCassandraDao testSubject;
		CassandraOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteAllArtifacts();
	}

	
	@Test
	public void testIsTableEmpty() throws Exception {
		SdcSchemaFilesCassandraDao testSubject;
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isTableEmpty(tableName);
	}
}