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
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class OperationalEnvironmentDaoTest {

	@InjectMocks
	OperationalEnvironmentDao testSubject;

	@Mock
	CassandraClient clientMock;

	@Mock
    CqlSession sessionMock;

	@Mock
    OperationalEnvironmentsAccessor accessorMock;

	@Mock
	CassandraClient client;

	@Mock
    private CqlSession session;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		session = Mockito.mock(CqlSession.class);
    	ReflectionTestUtils.setField(testSubject, "session", session);
		Field accessorField = OperationalEnvironmentDao.class.getDeclaredField("operationalEnvironmentsAccessor");
		accessorField.setAccessible(true);
		accessorField.set(testSubject, accessorMock);
	}

	@Test
	public void testInit() throws Exception {

		// default test
		// testSubject.init();
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<CqlSession, CassandraOperationStatus> value = Either.left(sessionMock);
		Mockito.when(clientMock.connect(Mockito.anyString())).thenReturn(value);
		OperationalEnvironmentDaoMapper mapper = Mockito.mock(OperationalEnvironmentDaoMapper.class);
		Mockito.when(mapper.operationalEnvironmentsAccessor(Mockito.anyString())).thenReturn(accessorMock);

		 new OperationalEnvironmentDaoMapperBuilder(sessionMock) {
            @Override
            public OperationalEnvironmentDaoMapper build() {
                return mapper;
            }
        };
		// testSubject.init();
	}
	
	@Test
	public void testInitException() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<CqlSession, CassandraOperationStatus> value = Either
				.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		try {
			testSubject.init();
		} catch (Exception e) {
			assertTrue(e.getClass() == RuntimeException.class);
		}
	}
	
	@Test
	public void testSave() throws Exception {
		OperationalEnvironmentEntry operationalEnvironmentEntry = null;
		CassandraOperationStatus result;

		// default test
		result = testSubject.save(operationalEnvironmentEntry);
	}

	@Test
	public void testGet() throws Exception {
		String envId = "";
		Either<OperationalEnvironmentEntry, CassandraOperationStatus> result;

		// default test
		result = testSubject.get(envId);
	}

	@Test
	public void testDelete() throws Exception {
		String envId = "";
		CassandraOperationStatus result;

		// default test
		result = testSubject.delete(envId);
	}

	@Test
	public void testDeleteAll() throws Exception {
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteAll();
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		
 		Either<CqlSession, CassandraOperationStatus> value = Either.left(sessionMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		// testSubject.init();
		
		result = testSubject.deleteAll();
	}

	@Test
	public void testIsTableEmpty() throws Exception {
	Mockito.when(clientMock.isConnected()).thenReturn(true);
	Mockito.when(clientMock.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE))
       .thenReturn(Either.left(sessionMock));	

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
	public void testGetByEnvironmentsStatus() throws Exception {
		Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> result;
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<CqlSession, CassandraOperationStatus> value = Either.left(sessionMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		OperationalEnvironmentDaoMapper mapper = Mockito.mock(OperationalEnvironmentDaoMapper.class);
		new OperationalEnvironmentDaoMapperBuilder(sessionMock) {
            @Override
            public OperationalEnvironmentDaoMapper build() {
                return mapper;
            }
        };
		PagingIterable<OperationalEnvironmentEntry> iterable = Mockito.mock(PagingIterable.class);

		Mockito.when(iterable.all()).thenReturn(Arrays.asList(new OperationalEnvironmentEntry()));
        Mockito.when(accessorMock.getByEnvironmentsStatus(Mockito.anyString())).thenReturn(iterable);
		// testSubject.init();
		
		// default test
		result = testSubject.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);
	}
	
	@Test
	public void testGetByEnvironmentsStatusNull() throws Exception {
		Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> result;
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<CqlSession, CassandraOperationStatus> value = Either.left(sessionMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
        OperationalEnvironmentDaoMapper mapper = Mockito.mock(OperationalEnvironmentDaoMapper.class);
        Mockito.when(mapper.operationalEnvironmentsAccessor(Mockito.anyString())).thenReturn(accessorMock);
		new OperationalEnvironmentDaoMapperBuilder(sessionMock) {
            @Override
            public OperationalEnvironmentDaoMapper build() {
                return mapper;
            }
        };
		Mockito.when(accessorMock.getByEnvironmentsStatus(Mockito.anyString())).thenReturn(null);
		// testSubject.init();
		// default test
		result = testSubject.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);
	}
}
