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

package org.openecomp.sdc.be.dao.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;


import fj.data.Either;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class ArtifactCassandraDaoTest {

	@InjectMocks
	private ArtifactCassandraDao testSubject;
	
	@Mock
	private CassandraClient client;
	
	@Mock
    private ArtifactDao artifactDao;

	@Mock
    private CqlSession session;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		 Field artifactDaoField = ArtifactCassandraDao.class.getDeclaredField("artifactDao");
		artifactDaoField.setAccessible(true);
		artifactDaoField.set(testSubject, artifactDao);
	}

	@Test(expected = RuntimeException.class)
	public void testInit() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		Mockito.when(client.connect(Mockito.anyString())).thenReturn(Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED));
		testSubject.init();
	}
	
	@Test
	public void testInitError() throws Exception {
		testSubject.init();
	}

	@Test
	public void testSaveArtifact() throws Exception {
		DAOArtifactData artifact = null;
		CassandraOperationStatus result;

		// default test
		result = testSubject.saveArtifact(artifact);
	}

	@Test
	public void testGetArtifact() throws Exception {
		String artifactId = "";
		Either<DAOArtifactData, CassandraOperationStatus> result;

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
    // Setup mocks
    Mockito.when(client.isConnected()).thenReturn(true);
    Mockito.when(client.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE))
           .thenReturn(Either.left(session));

    // Mock ResultSet and Row
    ResultSet mockResultSet = Mockito.mock(ResultSet.class);
    Row mockRow = Mockito.mock(Row.class);

    Mockito.when(mockRow.getLong("count")).thenReturn(0L); // table is empty
    Mockito.when(mockResultSet.one()).thenReturn(mockRow);
   Mockito.when(session.execute(Mockito.any(SimpleStatement.class)))
       .thenReturn(mockResultSet);

    // testSubject.init();

    Either<Boolean, CassandraOperationStatus> result =
            testSubject.isTableEmpty("artifacts");
}

	@Test
	public void testGetCountOfArtifactById() throws Exception {
		ResultSet resultSet = Mockito.mock(ResultSet.class);
		Row row = Mockito.mock(Row.class);
		Mockito.when(row.getLong(0)).thenReturn(0L);
		Mockito.when(resultSet.one()).thenReturn(row);
		Mockito.when(artifactDao.getNumOfArtifactsById("mock"))
			.thenReturn(resultSet);

		// Directly call method without init()
		Either<Long, CassandraOperationStatus> result = testSubject.getCountOfArtifactById("mock");

		// assertTrue(result.isLeft());

	}
	
	@Test
	public void testGetCountOfArtifactById1() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		CqlSession mockSession = Mockito.mock(CqlSession.class);
    	Mockito.when(client.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE))
           .thenReturn(Either.left(mockSession));
		Mockito.when(artifactDao.getNumOfArtifactsById("mock")).thenReturn(null);

		String uniqeId = "mock";
		// testSubject.init();	
		// default test
		Either<Long, CassandraOperationStatus> result  = testSubject.getCountOfArtifactById(uniqeId);
	}
}
