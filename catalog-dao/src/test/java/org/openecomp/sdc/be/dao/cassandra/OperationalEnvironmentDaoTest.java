package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
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

import java.util.List;

import static org.junit.Assert.assertTrue;

public class OperationalEnvironmentDaoTest {

	@InjectMocks
	OperationalEnvironmentDao testSubject;

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
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		testSubject.init();
		
		result = testSubject.deleteAll();
	}

	@Test
	public void testIsTableEmpty() throws Exception {
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		result = testSubject.isTableEmpty(tableName);
	}

	@Test
	public void testGetByEnvironmentsStatus() throws Exception {
		Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> result;
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		OperationalEnvironmentsAccessor value2 = Mockito.mock(OperationalEnvironmentsAccessor.class);
		Mockito.when(mappMock.createAccessor(OperationalEnvironmentsAccessor.class)).thenReturn(value2);
		Result<OperationalEnvironmentEntry> value3 = Mockito.mock(Result.class);
		Mockito.when(value2.getByEnvironmentsStatus(Mockito.any(String.class))).thenReturn(value3);
		testSubject.init();
		
		// default test
		result = testSubject.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);
	}
	
	@Test
	public void testGetByEnvironmentsStatusNull() throws Exception {
		Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> result;
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		OperationalEnvironmentsAccessor value2 = Mockito.mock(OperationalEnvironmentsAccessor.class);
		Mockito.when(mappMock.createAccessor(OperationalEnvironmentsAccessor.class)).thenReturn(value2);
		Result<OperationalEnvironmentEntry> value3 = null;
		Mockito.when(value2.getByEnvironmentsStatus(Mockito.any(String.class))).thenReturn(value3);
		testSubject.init();
		
		// default test
		result = testSubject.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);
	}
}