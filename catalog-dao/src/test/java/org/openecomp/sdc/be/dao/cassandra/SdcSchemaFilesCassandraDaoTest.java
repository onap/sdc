package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class SdcSchemaFilesCassandraDaoTest {

	@InjectMocks
	SdcSchemaFilesCassandraDao testSubject;

	@Mock
	CassandraClient clientMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInit() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		testSubject.init();
	}

	@Test
	public void testInitException() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either
				.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		try {
			testSubject.init();
		} catch (Exception e) {
			assertTrue(e.getClass() == RuntimeException.class);
		}
	}

	@Test
	public void testSaveSchemaFile() throws Exception {
		SdcSchemaFilesData schemaFileData = null;
		CassandraOperationStatus result;

		// default test
		result = testSubject.saveSchemaFile(schemaFileData);
	}
	
	@Test
	public void testGetSchemaFile() throws Exception {
		String schemaFileId = null;
		Either<SdcSchemaFilesData, CassandraOperationStatus> result;

		// default test
		result = testSubject.getSchemaFile(schemaFileId);
	}
	
	@Test
	public void testDeleteSchemaFile() throws Exception {
		String schemaFileId = null;
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteSchemaFile(schemaFileId);
	}
	
	@Test
	public void testGetSpecificSchemaFiles() throws Exception {
		String sdcreleasenum = "";
		String conformancelevel = "";
		Either<List<SdcSchemaFilesData>, CassandraOperationStatus> result;

		// default test
		result = testSubject.getSpecificSchemaFiles(sdcreleasenum, conformancelevel);
	}

	@Test
	public void testDeleteAll() throws Exception {
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteAllArtifacts();
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		testSubject.init();
		
		result = testSubject.deleteAllArtifacts();
	}
	
	@Test
	public void testIsTableEmpty() throws Exception {
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		result = testSubject.isTableEmpty(tableName);
	}
}