package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
import org.openecomp.sdc.be.resources.data.ESArtifactData;

public class ArtifactCassandraDaoTest {

	@InjectMocks
	ArtifactCassandraDao testSubject;
	
	@Mock
	CassandraClient client;
	
	@Mock
	ArtifactAccessor artifactAccessor;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = RuntimeException.class)
	public void testInit() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
		Mockito.when(client.connect(Mockito.anyString())).thenReturn(value);
		testSubject.init();
	}
	
	@Test
	public void testInitError() throws Exception {
		testSubject.init();
	}

	@Test
	public void testSaveArtifact() throws Exception {
		ESArtifactData artifact = null;
		CassandraOperationStatus result;

		// default test
		result = testSubject.saveArtifact(artifact);
	}

	@Test
	public void testGetArtifact() throws Exception {
		String artifactId = "";
		Either<ESArtifactData, CassandraOperationStatus> result;

		// default test
		result = testSubject.getArtifact(artifactId);
	}

	@Test
	public void testDeleteArtifact() throws Exception {
		String artifactId = "";
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteArtifact(artifactId);
	}

	@Test
	public void testDeleteAllArtifacts() throws Exception {
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteAllArtifacts();
	}

	@Test
	public void testIsTableEmpty() throws Exception {
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		result = testSubject.isTableEmpty(tableName);
	}

	@Test
	public void testGetCountOfArtifactById() throws Exception {
		String uniqeId = "mock";
		Either<Long, CassandraOperationStatus> result;
		ResultSet value = Mockito.mock(ResultSet.class);
		Row value2 = Mockito.mock(Row.class);
		Mockito.when(value2.getLong(0)).thenReturn(0L);
		Mockito.when(value.one()).thenReturn(value2);
		Mockito.when(artifactAccessor.getNumOfArtifactsById(uniqeId)).thenReturn(value);
		
		// default test
		result = testSubject.getCountOfArtifactById(uniqeId);
	}
	
	@Test
	public void testGetCountOfArtifactById1() throws Exception {
		String uniqeId = "mock";
		Either<Long, CassandraOperationStatus> result;
		ResultSet value = Mockito.mock(ResultSet.class);
		Mockito.when(artifactAccessor.getNumOfArtifactsById(uniqeId)).thenReturn(null);
		
		// default test
		result = testSubject.getCountOfArtifactById(uniqeId);
	}
}