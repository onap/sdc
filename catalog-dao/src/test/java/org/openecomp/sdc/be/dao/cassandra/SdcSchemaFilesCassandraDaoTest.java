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


import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.springframework.test.util.ReflectionTestUtils;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class SdcSchemaFilesCassandraDaoTest {

	@InjectMocks
	SdcSchemaFilesCassandraDao testSubject;

	@Mock
	CassandraClient clientMock;

	@Mock
	CassandraClient client;

	@Mock
    private CqlSession session;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		session = Mockito.mock(CqlSession.class);
    	ReflectionTestUtils.setField(testSubject, "session", session);
		ReflectionTestUtils.setField(testSubject, "client", clientMock);
	}

	@Test
	public void testInit() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		CqlSession sessionMock  = Mockito.mock(CqlSession.class);
        Either<CqlSession, CassandraOperationStatus> value = Either.left(sessionMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		// testSubject.init();
	}

	@Test
	public void testInitException() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		 Either<CqlSession, CassandraOperationStatus> value =
                Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
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
		 Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<CqlSession, CassandraOperationStatus> value = Either.left(session);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);

		// testSubject.init();

		// Stub session.execute to avoid NPE
		Mockito.when(session.execute(Mockito.anyString())).thenReturn(Mockito.mock(ResultSet.class));

		// Act
		CassandraOperationStatus result = testSubject.deleteAllArtifacts();
			
		result = testSubject.deleteAllArtifacts();
	}
	
	@Test
	public void testIsTableEmpty() throws Exception {
	Mockito.when(clientMock.isConnected()).thenReturn(true);
    Mockito.when(clientMock.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE))
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
}
