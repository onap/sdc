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
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class ArtifactCassandraDaoTest {

	@InjectMocks
	private ArtifactCassandraDao testSubject;
	
	@Mock
	private CassandraClient client;
	
	@Mock
	private ArtifactAccessor artifactAccessor;

	@Mock
	private MappingManager mappingManager;
	
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
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		result = testSubject.isTableEmpty(tableName);
	}

	@Test
	public void testGetCountOfArtifactById() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		Mockito.when(client.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE)).thenReturn(Either.left(ImmutablePair.of(null,mappingManager)));
		Mockito.when(mappingManager.createAccessor(ArtifactAccessor.class)).thenReturn(artifactAccessor);
		String uniqeId = "mock";
		Either<Long, CassandraOperationStatus> result;
		ResultSet value = Mockito.mock(ResultSet.class);
		Row value2 = Mockito.mock(Row.class);
		Mockito.when(value2.getLong(0)).thenReturn(0L);
		Mockito.when(value.one()).thenReturn(value2);
		Mockito.when(artifactAccessor.getNumOfArtifactsById(uniqeId)).thenReturn(value);
		
		// default test
		testSubject.init();
		result = testSubject.getCountOfArtifactById(uniqeId);
	}
	
	@Test
	public void testGetCountOfArtifactById1() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		Mockito.when(client.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE)).thenReturn(Either.left(ImmutablePair.of(null,mappingManager)));
		Mockito.when(mappingManager.createAccessor(ArtifactAccessor.class)).thenReturn(artifactAccessor);
		String uniqeId = "mock";
		Either<Long, CassandraOperationStatus> result;
		ResultSet value = Mockito.mock(ResultSet.class);
		Mockito.when(artifactAccessor.getNumOfArtifactsById(uniqeId)).thenReturn(null);
		testSubject.init();
		// default test
		result = testSubject.getCountOfArtifactById(uniqeId);
	}
}
