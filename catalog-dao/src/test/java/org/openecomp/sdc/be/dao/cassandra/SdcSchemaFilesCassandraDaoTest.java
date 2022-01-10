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
		MockitoAnnotations.openMocks(this);
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
